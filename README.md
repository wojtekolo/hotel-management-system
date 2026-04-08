# Hotel Management System

A system for managing hotel bookings, rooms and employees.

## Tech Stack
* Java 21 & Spring Boot 4
* PostgreSQL
* Docker
* Maven
* Hibernate/JPA
* AssertJ
* JUnit 6

## Key Features
* **Multi-room booking system:** Supports bookings for multiple rooms across various stay periods within a single transaction.
* **Collision prevention logic:** Prevents overlapping bookings for the same room by validating each request.
* **Concurrency management**: Handles concurrent requests using pessimistic locking on the `Room` entity, ensuring only one
    transaction at a time can create or update a booking for a specific room.
* **Automatic price calculation:** Calculates the total cost based on the room's base price and customer's loyalty status, with support for manual price overrides.
* **Role management:** Uses the Shared Primary Key pattern to consolidate roles and eliminate data redundancy.
* **Booking lifecycle management:** Manages booking state transitions and validates update requests.


## Getting Started

### Prerequisites
* Docker & Docker Compose installed

### Running the Application
1. Clone the repository: `git clone https://github.com/wojtekolo/hotel-management-system`
2. Navigate to the project directory.
3. Run the following command:
   ```bash
   docker-compose up --build
   ```


### API Documentation & Usage
Interactive API documentation is available via Swagger UI. Once the container is running, access it here:
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)


> **Note:** The database is populated with default data (`Room`, `RoomType`, `Customer`, `LoyaltyStatus`, `Employee`) on startup to speed up testing

### Example Request: Create a new `Booking`
Request to create a new booking for a single room. A null value in customPricePerNight defaults to the room's current price.

**POST** `/api/v1/bookings`

```json
{
  "customerId": 1,
  "employeeId": 2,
  "stays": [
    {
      "roomId": 1,
      "from": "2027-03-11",
      "to": "2027-03-15",
      "customPricePerNight": null
    }
  ]
}
```
**Received response:**
```json
{
  "id": 1,
  "customerFullName": "Customer_name Customer_surname",
  "customerPhone": "123123123",
  "loyaltyDiscount": "0.00",
  "createTime": "2026-04-08T13:24:44.873158038",
  "totalCost": 400,
  "createBy": "Employee_name Employee_surname",
  "paymentStatus": "UNPAID",
  "status": "PLANNED",
  "stays": [
    {
      "id": 1,
      "roomId": 1,
      "roomName": "1",
      "roomType": "Default room type",
      "actualCheckIn": null,
      "actualCheckOut": null,
      "activeFrom": "2027-03-11",
      "activeTo": "2027-03-15",
      "pricePerNight": 100,
      "totalCost": 400,
      "status": "PLANNED",
      "createBy": "Employee_name Employee_surname",
      "checkInBy": null,
      "checkOutBy": null
    }
  ]
}
```

### Example Request: Update existing `Booking` (Add new `RoomStay`)
Using non-existent `RoomStay` ID (e.g. 0) results in creating a new one.
This example also demonstrates a manual price override.
In case of updating, `null` value doesn't change the current cost of specified `RoomStay`.

**PUT** `/api/v1/bookings/1`

```json
{
  "employeeId": 2,
  "stays": [
    {
      "id": 1,
      "roomId": 1,
      "from": "2027-03-11",
      "to": "2027-03-15",
      "pricePerNight": null
    },
    {
      "id": 0,
      "roomId": 2,
      "from": "2027-03-15",
      "to": "2027-03-25",
      "pricePerNight": 10
    }
  ]
}
```

**Received response:**
```json
{
  "id": 1,
  "customerFullName": "Customer_name Customer_surname",
  "customerPhone": "123123123",
  "loyaltyDiscount": "0.00",
  "createTime": "2026-04-08T13:27:13.514355",
  "totalCost": 500,
  "createBy": "Employee_name Employee_surname",
  "paymentStatus": "UNPAID",
  "status": "PLANNED",
  "stays": [
    {
      "id": 1,
      "roomId": 1,
      "roomName": "1",
      "roomType": "Default room type",
      "actualCheckIn": null,
      "actualCheckOut": null,
      "activeFrom": "2027-03-11",
      "activeTo": "2027-03-15",
      "pricePerNight": 100,
      "totalCost": 400,
      "status": "PLANNED",
      "createBy": "Employee_name Employee_surname",
      "checkInBy": null,
      "checkOutBy": null
    },
    {
      "id": 2,
      "roomId": 2,
      "roomName": "2",
      "roomType": "Default room type",
      "actualCheckIn": null,
      "actualCheckOut": null,
      "activeFrom": "2027-03-15",
      "activeTo": "2027-03-25",
      "pricePerNight": 10,
      "totalCost": 100,
      "status": "PLANNED",
      "createBy": "Employee_name Employee_surname",
      "checkInBy": null,
      "checkOutBy": null
    }
  ]
}
```

### Example Bad Request: Update a `Booking` with collisions
**Database state:** planned `RoomStay` with id = 3 belonging to `Booking` with id = 2, planned for the `Room` with id = 1 for period:  2027-03-16 : 2027-03-20 \
Request contains internal conflict within itself and external conflict with the existing RoomStay with id = 3. \
\
The response consists of 3 sections:
* External conflicts: List of existing `Roomstays` which conflict with requested `RoomStay`.
* Internal conflicts: List of all conflicts in the request.
* Bad status details: `RoomStays` that cannot be modified due to their current status (e.g. canceled). \
\
**PUT** `/api/v1/bookings/1`
```json
{
  "employeeId": 2,
  "stays": [
    {
      "id": 1,
      "roomId": 1,
      "from": "2027-03-11",
      "to": "2027-03-15",
      "pricePerNight": 0
    },
    {
      "id": 2,
      "roomId": 1,
      "from": "2027-03-14",
      "to": "2027-03-20",
      "pricePerNight": 0
    }
  ]
}
```
**Received response:**\
Error code: 409 - conflict
```json
{
  "message": "Error updating booking",
  "externalConflicts": [
    {
      "roomId": 1,
      "roomName": "1",
      "roomStayId": 2,
      "from": "2027-03-14",
      "to": "2027-03-20",
      "roomConflictsDetails": [
        {
          "bookingId": 2,
          "roomStayId": 3,
          "from": "2027-03-16",
          "to": "2027-03-20"
        }
      ]
    }
  ],
  "internalConflicts": [
    {
      "roomId": 1,
      "id1": 1,
      "from1": "2027-03-11",
      "to1": "2027-03-15",
      "id2": 2,
      "from2": "2027-03-14",
      "to2": "2027-03-20"
    }
  ],
  "badStatusDetails": []
}
```