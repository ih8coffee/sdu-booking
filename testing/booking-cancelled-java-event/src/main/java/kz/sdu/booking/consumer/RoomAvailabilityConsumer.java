package kz.sdu.booking.consumer;

import kz.sdu.booking.events.BookingCancelled;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Records released room slots and makes processing idempotent by eventId. */
public final class RoomAvailabilityConsumer {
    private final Set<String> processedEventIds = new HashSet<>();
    private final Map<String, ReleasedRoomSlot> releasedSlotsByEventId = new HashMap<>();

    /**
     * Synchronized check-and-record prevents two concurrent deliveries of the
     * same event from creating two effects in this in-memory example.
     */
    public synchronized void handle(BookingCancelled event) {
        Objects.requireNonNull(event, "event is required");
        if (processedEventIds.contains(event.eventId())) {
            return;
        }

        ReleasedRoomSlot slot = new ReleasedRoomSlot(
            event.eventId(),
            event.bookingId(),
            event.roomId(),
            event.start(),
            event.end(),
            event.occurredAt()
        );
        releasedSlotsByEventId.put(event.eventId(), slot);
        processedEventIds.add(event.eventId());
    }

    public synchronized int releasedSlotCount() {
        return releasedSlotsByEventId.size();
    }

    public synchronized boolean wasProcessed(String eventId) {
        return processedEventIds.contains(eventId);
    }

    public synchronized ReleasedRoomSlot findByEventId(String eventId) {
        return releasedSlotsByEventId.get(eventId);
    }
}
