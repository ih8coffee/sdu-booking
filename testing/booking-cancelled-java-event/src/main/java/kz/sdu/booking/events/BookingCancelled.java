package kz.sdu.booking.events;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

/** Immutable fact that a booking has already been cancelled successfully. */
public record BookingCancelled(
    String eventId,
    String bookingId,
    String roomId,
    LocalDateTime start,
    LocalDateTime end,
    Instant occurredAt
) {
    public BookingCancelled {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("eventId is required");
        }
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("bookingId is required");
        }
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("roomId is required");
        }
        Objects.requireNonNull(start, "start is required");
        Objects.requireNonNull(end, "end is required");
        Objects.requireNonNull(occurredAt, "occurredAt is required");
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("event start must be before event end");
        }
    }
}
