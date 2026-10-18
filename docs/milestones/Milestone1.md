# PROBLEM AND SYSTEM CONTEXT

## 1. Problem and Scope
Project Title: SDU Booking.  
Description: Users have an option to book rooms at university that are acceptable (decided by staff) and available.  
Intended Users: Students and Teachers.

What our system will cover and not: Our system will cover booking various acceptable rooms within SDU. It won’t cover letting users book any room you want at any time, some can be busy by teachers, maintenance etc.

## 2. Stakeholders
- Students: they want to book rooms.
- Teachers: they too have an option to book rooms, but also they don’t want their classrooms booked when they need to teach.
- Staff: to approve booking requests, they serve as a last confirmation step.
- IT Support: to find and fix problems.

## 3. Requirements
### Functional Requirements:
1. Users can request to book any available room.
2. Users can see available rooms provided by our system.
3. Students can cancel their booking.
4. Staff can reject the booking.
5. Users will receive notifications upon successful booking request (after staff
approval).
### Constraints:
1. Options are available only for free rooms at times when they are not held by
other booking or classes.
2. Users must login to their existing SDU login.
3. Sent requests must not be confirmed until staff approval.
## 4. Quality Attributes.
1. Performance
Upon a sent request from the user, the system should delegate it to staff within 1
second.
2. Reliability
The service is available at least 95% of time.
3. Space complexity
The size of data behind the systems should be sufficiently small enough to scale
well. For example, 1 million requests should not exceed 50Mb in size.
### 5. Context Diagram
reference image later
### 6. Team Contribution

| Name | Student ID | Role / Main contribution |
|------|-----------|--------------------------|
| Myrzatayev Nuraly | 240107075 | Wrote Requirements for the system |
| Abdrakhpan Dinmukhamed  | 240107044 | Did problems & scope; context diagram |
| Eralp Aliyar Sengoz | 220107150 | Stakeholders |
| Orynbassar Nurpeiis | 240110009 | Quality Attributes |