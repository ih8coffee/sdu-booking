package kz.sdu.booking.repository;

import kz.sdu.booking.model.Booking;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory repository so the event workflow can be tested without a database. */
public final class InMemoryBookingRepository implements BookingRepository {
    private final Map<String, Booking> bookings = new ConcurrentHashMap<>();

    @Override
    public Optional<Booking> findById(String bookingId) {
        return Optional.ofNullable(bookings.get(bookingId));
    }

    @Override
    public void save(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("booking is required");
        }
        bookings.put(booking.id(), booking);
    }
}
