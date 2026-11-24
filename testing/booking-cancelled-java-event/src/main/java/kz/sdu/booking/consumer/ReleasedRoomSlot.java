package kz.sdu.booking.consumer;

import java.time.Instant;
import java.time.LocalDateTime;

/** Observable record of a room time slot released by a cancellation event. */
public record ReleasedRoomSlot(
    String eventId,
    String bookingId,
    String roomId,
    LocalDateTime start,
    LocalDateTime end,
    Instant releasedAt
) {}
