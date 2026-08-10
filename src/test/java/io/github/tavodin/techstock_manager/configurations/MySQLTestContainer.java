package io.github.tavodin.techstock_manager.configurations;

import org.testcontainers.containers.MySQLContainer;

public abstract class MySQLTestContainer {

    static final MySQLContainer<?> MYSQL =
            new MySQLContainer<>("mysql:8.0.28");

    static {
        MYSQL.start();
    }
}
