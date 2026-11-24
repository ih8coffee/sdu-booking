package kz.sdu.booking;

import kz.sdu.booking.consumer.ReleasedRoomSlot;
import kz.sdu.booking.consumer.RoomAvailabilityConsumer;
import kz.sdu.booking.events.BookingCancelled;
import kz.sdu.booking.events.BookingEventPublisher;
import kz.sdu.booking.model.Booking;
import kz.sdu.booking.model.BookingStatus;
import kz.sdu.booking.repository.InMemoryBookingRepository;
import kz.sdu.booking.service.BookingService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class BookingCancelledEventTest {
    @Test
    void successfulCancellationPublishesEventAndRecordsReleasedRoomSlot() {
        InMemoryBookingRepository repository = new InMemoryBookingRepository();
        Booking confirmedBooking = new Booking(
            "booking-42",
            "room-7",
            LocalDateTime.parse("2026-11-24T10:00:00"),
            LocalDateTime.parse("2026-11-24T11:00:00"),
            BookingStatus.CONFIRMED
        );
        repository.save(confirmedBooking);

        BookingEventPublisher publisher = new BookingEventPublisher();
        RoomAvailabilityConsumer consumer = new RoomAvailabilityConsumer();
        AtomicReference<BookingCancelled> publishedEvent = new AtomicReference<>();
        publisher.subscribe(consumer::handle);
        publisher.subscribe(publishedEvent::set);
        BookingService bookingService = new BookingService(repository, publisher);

        Booking cancelledBooking = bookingService.cancelBooking("booking-42");

        assertEquals(BookingStatus.CANCELLED, cancelledBooking.status());
        assertEquals(BookingStatus.CANCELLED,
            repository.findById("booking-42").orElseThrow().status());

        BookingCancelled event = publishedEvent.get();
        assertNotNull(event, "the publisher must deliver BookingCancelled after cancellation");
        assertFalse(event.eventId().isBlank());
        assertEquals("booking-42", event.bookingId());
        assertNotNull(event.occurredAt());

        assertEquals(1, consumer.releasedSlotCount());
        assertTrue(consumer.wasProcessed(event.eventId()));
        ReleasedRoomSlot releasedSlot = consumer.findByEventId(event.eventId());
        assertNotNull(releasedSlot);
        assertEquals("room-7", releasedSlot.roomId());
        assertEquals(event.start(), releasedSlot.start());
        assertEquals(event.end(), releasedSlot.end());
    }

    @Test
    void duplicateDeliveryThroughPublisherCreatesOnlyOneReleasedRoomSlot() {
        RoomAvailabilityConsumer consumer = new RoomAvailabilityConsumer();
        BookingEventPublisher publisher = new BookingEventPublisher();
        publisher.subscribe(consumer::handle);

        BookingCancelled sameEvent = new BookingCancelled(
            "evt-duplicate",
            "booking-42",
            "room-7",
            LocalDateTime.parse("2026-11-24T10:00:00"),
            LocalDateTime.parse("2026-11-24T11:00:00"),
            java.time.Instant.parse("2026-11-24T09:00:00Z")
        );

        publisher.publish(sameEvent);
        publisher.publish(sameEvent);

        assertEquals(1, consumer.releasedSlotCount());
        assertTrue(consumer.wasProcessed("evt-duplicate"));
        assertNotNull(consumer.findByEventId("evt-duplicate"));
    }
}
