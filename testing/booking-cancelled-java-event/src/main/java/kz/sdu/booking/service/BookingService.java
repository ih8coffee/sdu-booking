package kz.sdu.booking.service;

import kz.sdu.booking.events.BookingCancelled;
import kz.sdu.booking.events.BookingEventPublisher;
import kz.sdu.booking.model.Booking;
import kz.sdu.booking.repository.BookingRepository;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Executes the cancellation first and publishes the completed fact afterwards. */
public final class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingEventPublisher eventPublisher;

    public BookingService(
        BookingRepository bookingRepository,
        BookingEventPublisher eventPublisher
    ) {
        this.bookingRepository = Objects.requireNonNull(bookingRepository, "bookingRepository is required");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher is required");
    }

    public Booking cancelBooking(String bookingId) {
        Booking existingBooking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("booking not found: " + bookingId));

        Booking cancelledBooking = existingBooking.cancel();
        bookingRepository.save(cancelledBooking);

        // Publish only after the booking state has been successfully saved.
        BookingCancelled event = new BookingCancelled(
            UUID.randomUUID().toString(),
            cancelledBooking.id(),
            cancelledBooking.roomId(),
            cancelledBooking.start(),
            cancelledBooking.end(),
            Instant.now()
        );
        eventPublisher.publish(event);
        return cancelledBooking;
    }
}
