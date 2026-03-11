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
* **Collision prevention logic:** Prevents overlapping bookings for the same room.
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

### Example Request: Create a `Booking`
**POST** `/bookings`

```json
{
  "customerId": 1,
  "employeeId": 2,
  "stays": [
    {
      "roomId": 1,
      "from": "2027-03-11",
      "to": "2027-03-15",
      "customPricePerNight": 0
    }
  ]
}
```

### Example Request: Update a `Booking` (Add another `RoomStay`)
**PUT** `/bookings/{id}`

```json
{
  "bookingId": 1,
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
      "id": 0,
      "roomId": 2,
      "from": "2027-03-15",
      "to": "2027-03-20",
      "pricePerNight": 0
    }
  ]
}
```

   

