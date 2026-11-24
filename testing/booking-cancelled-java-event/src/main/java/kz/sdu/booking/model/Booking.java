package kz.sdu.booking.model;

import java.time.LocalDateTime;
import java.util.Objects;

/** A minimal booking aggregate for demonstrating a completed cancellation. */
public record Booking(
    String id,
    String roomId,
    LocalDateTime start,
    LocalDateTime end,
    BookingStatus status
) {
    public Booking {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("booking id is required");
        }
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("room id is required");
        }
        Objects.requireNonNull(start, "start is required");
        Objects.requireNonNull(end, "end is required");
        Objects.requireNonNull(status, "status is required");
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("booking start must be before booking end");
        }
    }

    public Booking cancel() {
        if (status != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("only a confirmed booking can be cancelled");
        }
        return new Booking(id, roomId, start, end, BookingStatus.CANCELLED);
    }
}
