package io.github.wojtekolo.hotelsystem.room.api;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.LocalDate;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import static io.gatling.javaapi.jdbc.JdbcDsl.jdbcFeeder;

public class RoomOccupancySimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .shareConnections();

    FeederBuilder<Object> roomFeeder = jdbcFeeder(
            "jdbc:postgresql://localhost:5432/hotel_system",
            "admin",
            "secret",
            "SELECT id AS \"randomRoomId\" FROM room"
    ).random();
    LocalDate now = LocalDate.now();
    ScenarioBuilder scn = scenario("Room Occupancy Cache Test")
            .feed(roomFeeder)
            .exec(http("Get Occupancy Request")
                    .get("/api/v1/rooms/#{randomRoomId}/occupancy")
                    .queryParam("from", now.plusDays(2))
                    .queryParam("to", now.plusDays(20))
                    .check(status().is(200))).pause(1);

    {
        setUp(scn.injectOpen(rampUsersPerSec(1).to(200).during(300))).protocols(httpProtocol);
    }
}