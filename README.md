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

## Key Business Features
* **Multi-room booking system:** Supports bookings for multiple rooms across various stay periods within a single transaction.
* **Collision prevention logic:** Prevents overlapping bookings for the same room by validating each request.
* **Automatic price calculation:** Calculates the total cost based on the room's base price and customer's loyalty status, with support for manual price overrides.
* **Full Error Aggregation:** System collects all violations (Integrity, Domain invariants, States, Conflicts) and
  returns them in a single, comprehensive report.

## Technical highlights
* **Aggregate Root:** The `Booking` acts as an Aggregate Root of related `RoomStay` entities.
* **Encapsulated Domain:** Business logic (pricing, date validation, state management) is contained within the domain entities,
protected by package-private access modifiers.
* **Concurrency management**: Handles concurrent requests using pessimistic locking on the `Room` entity, ensuring only one
    transaction at a time can create or update a booking for a specific room.
* **Role management:** Uses the Shared Primary Key pattern to consolidate roles and eliminate data redundancy.
* **Booking lifecycle management:** Manages booking state transitions and validates update requests.

## Performance Tests
The following benchmarks evaluate the impact of Redis-based caching
and indexing on the `room_stay` table performance.

### Database Indexes

Following indexes were applied and tested:

* **`idx_room_stay_room_id`**:
* **`idx_room_stay_dates`**: A multi-column index on `(active_from, active_to)`.

### Redis Cache
To reduce latency and database load, a Redis cache was implemented on room occupancy search.

### Test Methodology
Before the tests, the database was populated with dataset with following size:

| Entity                    | Count     |
|:--------------------------|:----------|
| **Total Rooms**           | 1,000     |
| **Planned Room Stays**    | 300,000   |
| **Historical Room Stays** | 3,000,000 |

The performance was tested using Gatling to simulate real-world traffic and measure how the database
and cache handle load.

* **Load:** Linear ramp up of users per second, starting from 1 up to the target (50, 200, 1,000).
* **Duration:** 300 seconds of continuous load generation.
* **Scenario:** Each user picks a random room from the database, checks its availability
  for a full month (the full year ahead is cached regardless of the requested range).

### Runtime Environment
#### Hardware Specs
* **CPU:** Intel Core i5 7400F @ 2.90GHz
* **RAM:** 16GB DDR4
* **Storage:** NVMe M.2 SSD PCIe 3.0

#### Infrastructure Configuration
* **HikariCP Connection Pool:** maximum-pool-size: 10 (default)
* **Embedded Tomcat:** 200 max threads (default)
* **Gatling Client:** `shareConnections` enabled

### Results

| ID | Configuration | Target RPS | Total Requests |           Success Rate            | Min (ms) | P50 (ms) | P95 (ms) | P99 (ms) | Max (ms) |
|:---|:---|:---:|:---:|:---------------------------------:|:---:|:---:|:---:|:--------:|:--------:|
| **1** | No Cache + No Indexes | 50 | 7,650 |   44.56% (3,409 OK / 4,241 KO)    | 119 | 60,002 | 60,014 |  60,021  |  60,072  |
| **2** | No Cache + Indexes | 50 | 7,650 |      100% (7,650 OK / 0 KO)       | 2 | 11 | 14 |    18    |   509    |
| **3** | Redis Cache + No Indexes | 50 | 7,650 |      100% (7,650 OK / 0 KO)       | 2 | 3 | 148 |   174    |  1,040   |
| **4** | Redis Cache + Indexes | 50 | 7,650 |      100% (7,650 OK / 0 KO)       | 1 | 4 | 16 |    19    |  1,096   |
| **5** | No Cache + No Indexes | 200 | 30,150 |   6.52% (1,965 OK / 28,185 KO)    | 3 | 60,004 | 60,015 |  60,029  |  60,173  |
| **6** | No Cache + Indexes | 200 | 30,150 |     100% (30,150 OK / 0 KO)     | 2 | 12 | 18 |   321    |  5,882   |
| **7** | Redis Cache + No Indexes | 200 | 30,150 |     100% (30,150 OK / 0 KO)     | 1 | 3 | 8 | 1,495  |  4,175   |
| **8** | Redis Cache + Indexes | 200 | 30,150 |    100% (30,150 OK / 0 KO)    | 1 | 3 | 5 |    16    |   917    |
| **9** | Redis Cache + Indexes | 1,000 | 150,150 |    100% (150,150 OK / 0 KO)     | 1 | 4 | 7 |    12    |  1,805   |
| **10** | No Cache + Indexes | 1,000 | 150,150 | 25.40% (38,138 OK / 112,012 KO) | 0 | 1,183 | 60,375 |  62,274  |  63,360  |

#### Key Takeaways:
* **No Cache + No Indexes (Tests 1 & 5):** System fails. Success rates drop to 44.56% at 50 RPS and 6.52% at 200 RPS.
* **No Redis + Indexes (Tests 2 & 6):** 100% success up to 200 RPS, but at 200 RPS max latency spikes to 5.8 seconds.
* **Redis + No Indexes (Test 7):** 100% success at 200 RPS, but initial cache misses spike max latency to 4.1 seconds.
* **Redis + Indexes (Tests 8 & 9):** 100% success. At 200 RPS, P99 latency is 16 ms. At 1,000 RPS, the system processes
150k requests with a 12 ms P99.
* **No Redis + Indexes with 1,000 RPS (Test 10):** System fails. Success rate drops to 25.40% due to connection refused,
premature close, and socket errors.

#### Conclusion
Both optimizations are mandatory for high-scale traffic. Database indexes prevent system collapse during cold starts
and cache misses, while Redis cache absorbs high-volume traffic to protect the database connection limits.

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


> **Note:** The database is populated with default data (`Room` ids: 1, 2; `RoomType` id: 1;
> `Customer` id:1; `LoyaltyStatus`, `Employee` id =2) on startup to speed up testing

### Example Request: Create a new `Booking`
Request to create a new booking for a single room. A null value in customPricePerNight defaults to the room's current price.

**The request:**
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
**The response:**
```json
{
  "id": 1,
  "customerFullName": "Customer_name Customer_surname",
  "customerPhone": "123123123",
  "loyaltyDiscount": "0.00",
  "createTime": "2026-04-11T16:53:54.299076544",
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
In order to add a new `RoomStay` to an existing `Booking`, the `id` field must be `null`. Any non-null value is treated as an update
and must be an existing `RoomStay` associated with specific `Booking`. \
This example also demonstrates a manual price override.
In case of updating, `null` value doesn't change the current cost of specified `RoomStay`.

**Prerequisites:**
Booking from previous example.

**The request:**
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
      "customPricePerNight": null
    },
    {
      "id": null,
      "roomId": 2,
      "from": "2027-03-15",
      "to": "2027-03-25",
      "customPricePerNight": 10
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
  "createTime": "2026-04-11T16:53:54.299077",
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
**Preconditions:**
* Booking (ID: 1) contains: 
    * Stay (ID: 1) with status `PLANNED`
    * Stay (ID: 2) with status `PLANNED`
* Booking (ID: 2) contains:
    * Stay (ID: 3) with status `PLANNED`, activeFrom `2027-03-16`, activeTo `2027-03-20`, Room (ID: 1)

The request contains an internal conflict within itself and an external conflict with the existing `RoomStay` with `id` = 3.

**The Request:**
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
      "customPricePerNight": null
    },
    {
      "id": 2,
      "roomId": 1,
      "from": "2027-03-14",
      "to": "2027-03-20",
      "customPricePerNight": null
    }
  ]
}
```

The response consists of 4 sections:
* External Conflicts: List of existing `Roomstays` which conflict with requested `RoomStay`.
* Internal Conflicts: List of all conflicts between the requested `RoomStays`.
* Violations Details: Business violations (e.g. invalid dates, invalid `RoomStay` status).
* Integrity Violations Details: non-existent resources (e.g. `Employee`).

**The response:**
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
      "conflictingStays": [
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
  "violationsDetails": [],
  "integrityViolationsDetails": []
}
```




### Example Bad Request: Update a `Booking` with invalid input
**Preconditions:**
* Booking (ID: 1) contains:
  * Stay (ID: 1) with status `COMPLETED`
  * Stay (ID: 2) with status `PLANNED`


**The Request** consists of many mistakes such as dates being in the past and in invalid order,
negative `customPricePerNight`, invalid `roomId`, invalid `employeeId`. The request also tries to update the `RoomStay`
with completed status, which is not allowed to be edited.

**The Request:**
**PUT** `/api/v1/bookings/1`
```json
{
  "employeeId": 20,
  "stays": [
    {
      "id": 1,
      "roomId": 1,
      "from": "2025-03-11",
      "to": "2025-03-05",
      "customPricePerNight": -10
    },
    {
      "id": 2,
      "roomId": 123,
      "from": "2028-03-14",
      "to": "2028-03-10",
      "customPricePerNight": null
    }
  ]
}
```
**The Response** consists of a comprehensive list of all violations:
```json
{
  "message": "Error updating booking",
  "externalConflicts": [],
  "internalConflicts": [],
  "violationsDetails": [
    {
      "stayId": 1,
      "currentStatus": "COMPLETED",
      "code": "END_DATE_NOT_AFTER_START_DATE",
      "context": {
        "from": "2025-03-11",
        "to": "2025-03-05",
        "roomId": 1
      }
    },
    {
      "stayId": 1,
      "currentStatus": "COMPLETED",
      "code": "START_DATE_IN_THE_PAST",
      "context": {
        "from": "2025-03-11",
        "roomId": 1
      }
    },
    {
      "stayId": 1,
      "currentStatus": "COMPLETED",
      "code": "END_DATE_IN_THE_PAST",
      "context": {
        "to": "2025-03-05",
        "roomId": 1
      }
    },
    {
      "stayId": 1,
      "currentStatus": "COMPLETED",
      "code": "PRICE_NEGATIVE",
      "context": {
        "roomId": 1,
        "pricePerNight": -10
      }
    },
    {
      "stayId": 1,
      "currentStatus": "COMPLETED",
      "code": "START_DATE_EDIT_INVALID_STATUS",
      "context": null
    },
    {
      "stayId": 1,
      "currentStatus": "COMPLETED",
      "code": "END_DATE_EDIT_INVALID_STATUS",
      "context": null
    },
    {
      "stayId": 1,
      "currentStatus": "COMPLETED",
      "code": "PRICE_EDIT_INVALID_STATUS",
      "context": null
    },
    {
      "stayId": 2,
      "currentStatus": "PLANNED",
      "code": "END_DATE_NOT_AFTER_START_DATE",
      "context": {
        "from": "2028-03-14",
        "to": "2028-03-10",
        "roomId": 123
      }
    }
  ],
  "integrityViolationsDetails": [
    {
      "code": "ROOM_NOT_FOUND",
      "context": {
        "roomId": 123
      }
    },
    {
      "code": "EMPLOYEE_NOT_FOUND",
      "context": {
        "employeeId": 20
      }
    }
  ]
}
```