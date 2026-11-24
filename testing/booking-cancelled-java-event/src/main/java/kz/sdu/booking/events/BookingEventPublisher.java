package kz.sdu.booking.events;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** A small synchronous in-process event publisher; no broker is required. */
public final class BookingEventPublisher {
    private final CopyOnWriteArrayList<Consumer<BookingCancelled>> subscribers =
        new CopyOnWriteArrayList<>();

    public void subscribe(Consumer<BookingCancelled> subscriber) {
        subscribers.add(Objects.requireNonNull(subscriber, "subscriber is required"));
    }

    public void publish(BookingCancelled event) {
        Objects.requireNonNull(event, "event is required");
        for (Consumer<BookingCancelled> subscriber : subscribers) {
            subscriber.accept(event);
        }
    }
}
