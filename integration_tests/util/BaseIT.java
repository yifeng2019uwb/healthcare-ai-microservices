package util;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TestResultLogger.class)
public abstract class BaseIT {

    private static final int WARMUP_ATTEMPTS = 20;
    private static final int WARMUP_DELAY_MS = 3000;

    @BeforeAll
    static void configureRestAssured() {
        RestAssured.baseURI = System.getProperty("gateway.url",
                "https://gateway-dev-824144893232.us-west1.run.app");
    }

    /**
     * Retries an authenticated GET to the given endpoint until the service responds
     * with a non-503 status, or throws AssertionError after 60s.
     */
    protected static void waitUntilReady(String token, String endpoint) {
        for (int i = 0; i < WARMUP_ATTEMPTS; i++) {
            int status = LoginHelper.withToken(token)
                .when().get(endpoint)
                .then().extract().statusCode();
            if (status != 503) return;
            try {
                Thread.sleep(WARMUP_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        throw new AssertionError("Service not ready after 60s: " + endpoint);
    }
}
