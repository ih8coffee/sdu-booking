# BookingCancelled — standalone Java event-driven testing module

This is an individual Java module stored under the SDU Booking repository's `testing/` directory. It is **not wired into the Django/Python runtime application yet**; the existing React and Django application code is intentionally unchanged.

## Link to existing project functionality

The SDU Booking project already supports cancelling a booking:
- `sdu-booking-webapp/src/pages/RoomDetail.jsx` changes a booking status to `cancelled` from its cancellation handler.
- `sdu-booking-webapp/src/pages/Manage.jsx` and `MyBookings.jsx` also expose cancellation actions.
- `sdu-booking-backend/booking_backend/models.py` defines the `CANCELLED` booking status.

This standalone Java module models the completed business fact represented by that existing status change. Its in-memory repository demonstrates the event flow independently; it does not call or modify the Django backend.

## Event

`BookingCancelled` describes the completed business fact that an existing booking has been cancelled. It is an event, not a command such as `CancelBooking`.

The event contains:
- `eventId` — generated using `UUID.randomUUID()`;
- `bookingId` — the relevant booking identifier;
- `roomId` — room identifier needed by the consumer;
- `start` and `end` — the released booking interval;
- `occurredAt` — the event timestamp (`Instant`).

## Flow

1. `BookingService.cancelBooking(bookingId)` loads a confirmed booking.
2. The service changes the booking status to `CANCELLED` and saves it using `BookingRepository`.
3. Only after the save returns successfully, the service creates and publishes `BookingCancelled`.
4. `BookingEventPublisher` delivers the event to its in-process subscribers.
5. `RoomAvailabilityConsumer` records a `ReleasedRoomSlot`, which is an observable reaction.
6. The consumer tracks processed event IDs and ignores a repeated delivery with the same `eventId`.

No Kafka, RabbitMQ, or external message broker is used.

## Automated tests

Exactly two JUnit 5 tests are provided:

1. `successfulCancellationPublishesEventAndRecordsReleasedRoomSlot` — verifies that the booking operation succeeds, the event is published, and the consumer records the released room slot.
2. `duplicateDeliveryThroughPublisherCreatesOnlyOneReleasedRoomSlot` — publishes the same event twice through the publisher and verifies that the consumer effect occurs only once.

## Requirements

- JDK 17 or newer
- Apache Maven 3.8 or newer

## Run tests on Windows PowerShell

From the `booking-cancelled-java-event` directory:

```powershell
java -version
mvn -version
mvn test
```

Expected final Maven summary: `Tests run: 2, Failures: 0, Errors: 0, Skipped: 0` and `BUILD SUCCESS`.

## Scope and limitation

The booking repository and released-slot consumer use in-memory collections so this small module stays easy to test. The records do not persist across application restarts. The consumer records a released slot as an observable reaction; it does not yet update authoritative room availability in the Django application. Runtime integration is intentionally deferred until the team reviews this individual Java contribution.
