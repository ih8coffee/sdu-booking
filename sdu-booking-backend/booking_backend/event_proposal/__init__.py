"""Optional event-driven proposal for booking cancellation."""

from .booking_cancelled import (
    BookingAlreadyCancelledError,
    BookingCancelled,
    InProcessEventPublisher,
    ReleasedRoomSlot,
    RoomAvailabilityConsumer,
    cancel_booking,
)

__all__ = [
    "BookingAlreadyCancelledError",
    "BookingCancelled",
    "InProcessEventPublisher",
    "ReleasedRoomSlot",
    "RoomAvailabilityConsumer",
    "cancel_booking",
]
