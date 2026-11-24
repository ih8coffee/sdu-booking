# BookingCancelled — backend proposal (Python/Django)

## Purpose

This is an optional, backend-only proposal for the SDU Booking project. It
shows how a completed booking cancellation can publish a `BookingCancelled`
event and how a consumer can record the released room interval exactly once.

## What the project currently does

- The Django `Booking` model already defines the `cancelled` status.
- The Django `/api/bookings` endpoint currently supports `GET` only.
- The current React booking context changes local reservations in browser
  `localStorage`; there is not yet a server-side cancellation endpoint.

For that reason, this proposal is deliberately isolated. It does **not** modify
`views.py`, `urls.py`, `models.py`, migrations, frontend files, or runtime API
behavior. It is a reviewable service-layer proposal, not a claim that the
existing UI is already connected to this backend event flow.

## Event-driven flow

1. `cancel_booking()` loads the existing Django `Booking` row and checks its
   current status.
2. It changes the booking status to `Booking.CANCELLED` and saves it inside a
   database transaction.
3. It creates `BookingCancelled` with a UUID event ID, booking ID, room ID,
   slot start/end, and occurrence timestamp.
4. It registers publication using `transaction.on_commit()`. Therefore the
   event is not published if the database transaction rolls back.
5. `InProcessEventPublisher` delivers the event to `RoomAvailabilityConsumer`.
6. The consumer records a `ReleasedRoomSlot`. Repeated delivery with the same
   `event_id` is ignored.

## Files

- `booking_cancelled.py` — event, in-process publisher, consumer, and proposed
  cancellation service.
- `test_booking_cancelled.py` — the two required automated tests.

## Run tests

From the `sdu-booking-backend` directory:

```powershell
python manage.py test booking_backend.event_proposal
```

Or run all backend tests:

```powershell
python manage.py test
```

## Scope and production note

The publisher and consumer use in-process delivery and in-memory idempotency
state for this practical exercise. The consumer's `ReleasedRoomSlot` is an
observable audit-style record; it does not currently change the authoritative
availability of a room in the application. In a production implementation,
processed event IDs and audit records should be persisted with a database
unique constraint.

When the team approves a server-side cancellation endpoint, that endpoint can
call `cancel_booking()` after validating the request and permissions. That
future wiring is intentionally outside this proposal-only change.
