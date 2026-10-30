package com.example.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Integration test: starts the real HTTP server on a random free port
 * and sends real HTTP requests to it.
 */
class CalculatorServerIntegrationTest {

    private static CalculatorServer server;
    private static HttpClient client;
    private static String baseUrl;

    @BeforeAll
    static void startServer() throws IOException {
        server = new CalculatorServer(0);
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getPort();
        client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    private static HttpResponse<String> get(String pathAndQuery) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + pathAndQuery)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void addReturnsExpectedJson() throws Exception {
        HttpResponse<String> response = get("/api/v1/add?a=2&b=3");
        assertEquals(200, response.statusCode());
        assertEquals("{\"operation\":\"add\",\"a\":2,\"b\":3,\"result\":5}", response.body());
    }

    @Test
    void subtractReturnsExpectedJson() throws Exception {
        HttpResponse<String> response = get("/api/v1/subtract?a=10&b=4");
        assertEquals(200, response.statusCode());
        assertEquals("{\"operation\":\"subtract\",\"a\":10,\"b\":4,\"result\":6}", response.body());
    }

    @Test
    void multiplyReturnsExpectedJson() throws Exception {
        HttpResponse<String> response = get("/api/v1/multiply?a=6&b=7");
        assertEquals(200, response.statusCode());
        assertEquals("{\"operation\":\"multiply\",\"a\":6,\"b\":7,\"result\":42}", response.body());
    }

    @Test
    void divideReturnsExpectedJson() throws Exception {
        HttpResponse<String> response = get("/api/v1/divide?a=10&b=4");
        assertEquals(200, response.statusCode());
        assertEquals("{\"operation\":\"divide\",\"a\":10,\"b\":4,\"result\":2.5}", response.body());
    }

    @Test
    void divideByZeroReturns400WithErrorJson() throws Exception {
        HttpResponse<String> response = get("/api/v1/divide?a=5&b=0");
        assertEquals(400, response.statusCode());
        assertEquals("{\"status\":400,\"error\":\"Bad Request\","
                + "\"message\":\"Division by zero is not allowed\"}", response.body());
    }

    @Test
    void missingParameterReturns400() throws Exception {
        HttpResponse<String> response = get("/api/v1/add?a=2");
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Missing required parameter: b"));
    }

    @Test
    void nonNumericParameterReturns400() throws Exception {
        HttpResponse<String> response = get("/api/v1/add?a=abc&b=3");
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("not a valid number"));
    }

    @Test
    void unknownEndpointReturns404() throws Exception {
        assertEquals(404, get("/api/v1/power?a=2&b=3").statusCode());
    }

    @Test
    void healthEndpointIsUp() throws Exception {
        HttpResponse<String> response = get("/actuator/health");
        assertEquals(200, response.statusCode());
        assertEquals("{\"status\":\"UP\"}", response.body());
    }
}
