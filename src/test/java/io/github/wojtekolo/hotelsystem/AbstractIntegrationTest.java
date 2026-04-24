package io.github.wojtekolo.hotelsystem;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static PostgreSQLContainer database =
            new PostgreSQLContainer("postgres:16");

    static {
        database.start();
    }
}
