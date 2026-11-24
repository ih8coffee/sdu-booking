package kz.sdu.booking.repository;

import kz.sdu.booking.model.Booking;
import java.util.Optional;

/** Persistence boundary for the standalone example. */
public interface BookingRepository {
    Optional<Booking> findById(String bookingId);

    void save(Booking booking);
}
