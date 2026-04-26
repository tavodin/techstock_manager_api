package io.github.tavodin.techstock_manager.integrationtests.utils;

import static io.restassured.RestAssured.given;

public class AuthTestUtil {

    private static String token;

    public static String getToken(int port) {
        if (token == null) {
            token = given()
                    .port(port)
                    .contentType("application/json")
                    .body("""
                        {
                            "username": "test123",
                            "password": "test"
                        }
                    """)
                    .when()
                    .post("/auth/login")
                    .then()
                    .log().all()
                    .statusCode(200)
                    .extract()
                    .path("token");
        }
        return token;
    }
}
