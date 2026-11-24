from datetime import timedelta

from django.test import TestCase
from django.utils import timezone

from booking_backend.models import Booking, Room, Slot
from booking_backend.event_proposal import (
    BookingCancelled,
    InProcessEventPublisher,
    RoomAvailabilityConsumer,
    cancel_booking,
)


class BookingCancelledProposalTests(TestCase):
    def setUp(self):
        self.room = Room.objects.create(
            name="J102",
            building="Main Building",
            floor=1,
            capacity=143,
            equipment=["Projector"],
            description="Standard classroom.",
            bookable=True,
        )
        start = timezone.now() + timedelta(days=1)
        self.slot = Slot.objects.create(
            id="J102-PROPOSAL-1",
            room=self.room,
            start=start,
            end=start + timedelta(hours=1),
        )
        self.booking = Booking.objects.create(
            room=self.room,
            slot=self.slot,
            status=Booking.CONFIRMED,
            userLogin="240110009",
        )

        self.consumer = RoomAvailabilityConsumer()
        self.publisher = InProcessEventPublisher()
        self.publisher.subscribe(BookingCancelled, self.consumer.handle)

    def test_successful_cancellation_publishes_event_and_records_released_slot(self):
        # Django TestCase wraps each test in a transaction. Execute captured
        # on_commit callbacks to model a successful commit in this test.
        with self.captureOnCommitCallbacks(execute=True) as callbacks:
            event = cancel_booking(self.booking.pk, publisher=self.publisher)

        self.booking.refresh_from_db()

        self.assertEqual(self.booking.status, Booking.CANCELLED)
        self.assertEqual(len(callbacks), 1)
        self.assertEqual(self.publisher.published_events, [event])
        self.assertEqual(event.booking_id, str(self.booking.pk))
        self.assertEqual(event.room_id, str(self.room.pk))
        self.assertEqual(len(self.consumer.released_slots), 1)

        released_slot = self.consumer.released_slots[0]
        self.assertEqual(released_slot.event_id, event.event_id)
        self.assertEqual(released_slot.booking_id, str(self.booking.pk))
        self.assertEqual(released_slot.room_id, str(self.room.pk))
        self.assertEqual(released_slot.slot_start, self.slot.start)
        self.assertEqual(released_slot.slot_end, self.slot.end)

    def test_duplicate_delivery_creates_only_one_released_slot(self):
        event = BookingCancelled(
            event_id="same-event-id",
            booking_id=str(self.booking.pk),
            room_id=str(self.room.pk),
            slot_start=self.slot.start,
            slot_end=self.slot.end,
            occurred_at=timezone.now(),
        )

        self.publisher.publish(event)
        self.publisher.publish(event)

        self.assertEqual(self.publisher.published_events, [event, event])
        self.assertEqual(len(self.consumer.released_slots), 1)
        self.assertEqual(
            self.consumer.processed_event_ids,
            frozenset({"same-event-id"}),
        )
