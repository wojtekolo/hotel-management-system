package io.github.wojtekolo.hotelsystem.room.api;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import static io.gatling.javaapi.jdbc.JdbcDsl.jdbcFeeder;

public class RoomOccupancySimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json");

    FeederBuilder<Object> roomFeeder = jdbcFeeder(
            "jdbc:postgresql://localhost:5432/hotel_system",
            "admin",
            "secret",
            "SELECT id AS \"randomRoomId\" FROM room"
    ).random();

    ScenarioBuilder scn = scenario("Room Occupancy Cache Test")
            .feed(roomFeeder)
            .exec(http("Get Occupancy Request")
                    .get("/api/v1/rooms/#{randomRoomId}/occupancy")
                    .queryParam("from", "2026-05-01")
                    .queryParam("to", "2026-05-31")
                    .check(status().is(200))).pause(1);

    {
        setUp(scn.injectClosed(constantConcurrentUsers(100).during(60))).protocols(httpProtocol);
    }
}