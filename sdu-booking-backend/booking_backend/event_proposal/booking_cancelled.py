"""Backend-only proposal for publishing BookingCancelled after a DB commit.

This module is deliberately not connected to the existing HTTP views or URLs.
It can be called by a future server-side cancellation endpoint after the team
approves moving cancellation from the frontend localStorage to the backend.
"""

from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime
from threading import RLock
from typing import Callable, Type
from uuid import uuid4

from django.db import transaction
from django.utils import timezone

from booking_backend.models import Booking


@dataclass(frozen=True)
class BookingCancelled:
    """Fact that a booking has been successfully cancelled."""

    event_id: str
    booking_id: str
    room_id: str
    slot_start: datetime
    slot_end: datetime
    occurred_at: datetime


@dataclass(frozen=True)
class ReleasedRoomSlot:
    """Observable consumer record for the room interval released by a booking."""

    event_id: str
    booking_id: str
    room_id: str
    slot_start: datetime
    slot_end: datetime
    released_at: datetime


EventHandler = Callable[[BookingCancelled], None]


class InProcessEventPublisher:
    """Small synchronous event publisher; no external message broker required."""

    def __init__(self) -> None:
        self._handlers: dict[Type[BookingCancelled], list[EventHandler]] = {}
        self._lock = RLock()
        self.published_events: list[BookingCancelled] = []

    def subscribe(
        self,
        event_type: Type[BookingCancelled],
        handler: EventHandler,
    ) -> None:
        with self._lock:
            self._handlers.setdefault(event_type, []).append(handler)

    def publish(self, event: BookingCancelled) -> None:
        with self._lock:
            self.published_events.append(event)
            handlers = tuple(self._handlers.get(type(event), ()))

        for handler in handlers:
            handler(event)


class RoomAvailabilityConsumer:
    """Records a released room interval once per event ID.

    The in-memory collections satisfy this small practical exercise. They are
    intentionally not presented as durable production storage.
    """

    def __init__(self) -> None:
        self._processed_event_ids: set[str] = set()
        self._released_slots: dict[str, ReleasedRoomSlot] = {}
        self._lock = RLock()

    def handle(self, event: BookingCancelled) -> bool:
        """Return True when processed; return False for a duplicate delivery."""
        with self._lock:
            if event.event_id in self._processed_event_ids:
                return False

            record = ReleasedRoomSlot(
                event_id=event.event_id,
                booking_id=event.booking_id,
                room_id=event.room_id,
                slot_start=event.slot_start,
                slot_end=event.slot_end,
                released_at=event.occurred_at,
            )
            self._released_slots[event.event_id] = record
            self._processed_event_ids.add(event.event_id)
            return True

    @property
    def released_slots(self) -> tuple[ReleasedRoomSlot, ...]:
        with self._lock:
            return tuple(self._released_slots.values())

    @property
    def processed_event_ids(self) -> frozenset[str]:
        with self._lock:
            return frozenset(self._processed_event_ids)


def cancel_booking(
    booking_id: int,
    *,
    publisher: InProcessEventPublisher,
) -> BookingCancelled:
    """Cancel a Django Booking and publish only after its DB transaction commits.

    This is an optional service proposal. The current project does not expose a
    server-side cancellation endpoint, so existing views/URLs are not changed.
    """
    with transaction.atomic():
        booking = (
            Booking.objects.select_for_update()
            .select_related("room", "slot")
            .get(pk=booking_id)
        )

        if booking.status == Booking.CANCELLED:
            raise BookingAlreadyCancelledError(
                f"Booking {booking.pk} is already cancelled."
            )

        if booking.status not in (Booking.PENDING, Booking.CONFIRMED):
            raise ValueError(
                f"Booking {booking.pk} with status '{booking.status}' cannot be cancelled."
            )

        booking.status = Booking.CANCELLED
        booking.save(update_fields=["status"])

        event = BookingCancelled(
            event_id=str(uuid4()),
            booking_id=str(booking.pk),
            room_id=str(booking.room_id),
            slot_start=booking.slot.start,
            slot_end=booking.slot.end,
            occurred_at=timezone.now(),
        )

        # Do not publish if the transaction rolls back. The callback runs only
        # after the database transaction successfully commits.
        transaction.on_commit(lambda event=event: publisher.publish(event))

    return event


class BookingAlreadyCancelledError(ValueError):
    """Raised when a caller tries to cancel an already-cancelled booking."""
