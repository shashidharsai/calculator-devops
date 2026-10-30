package com.example.calculator;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * A tiny HTTP server built on the JDK's own com.sun.net.httpserver.
 * No framework and no extra libraries are needed.
 */
public class CalculatorServer {

    private static final String API_PREFIX = "/api/v1/";
    private static final Set<String> OPERATIONS = Set.of("add", "subtract", "multiply", "divide");

    private final Calculator calculator = new Calculator();
    private final HttpServer server;

    /** Use port 0 to let the OS pick a free port (handy for tests). */
    public CalculatorServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handle);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public int getPort() {
        return server.getAddress().getPort();
    }

    // ---------- request handling ----------

    private void handle(HttpExchange exchange) throws IOException {
        try {
            route(exchange);
        } catch (Exception e) {
            send(exchange, 500, error(500, "Internal Server Error", "Unexpected error"));
        } finally {
            exchange.close();
        }
    }

    private void route(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            send(exchange, 405, error(405, "Method Not Allowed", "Only GET is supported"));
            return;
        }

        String path = exchange.getRequestURI().getPath();

        if ("/actuator/health".equals(path) || "/health".equals(path)) {
            send(exchange, 200, "{\"status\":\"UP\"}");
        } else if (path.startsWith(API_PREFIX) && OPERATIONS.contains(path.substring(API_PREFIX.length()))) {
            calculate(exchange, path.substring(API_PREFIX.length()));
        } else {
            send(exchange, 404, error(404, "Not Found", "No such endpoint: " + path));
        }
    }

    private void calculate(HttpExchange exchange, String operation) throws IOException {
        try {
            Map<String, String> params = parseQuery(exchange.getRequestURI().getRawQuery());
            BigDecimal a = number(params, "a");
            BigDecimal b = number(params, "b");

            BigDecimal result = switch (operation) {
                case "add" -> calculator.add(a, b);
                case "subtract" -> calculator.subtract(a, b);
                case "multiply" -> calculator.multiply(a, b);
                case "divide" -> calculator.divide(a, b);
                default -> throw new IllegalStateException("Unknown operation " + operation);
            };

            send(exchange, 200, "{\"operation\":\"" + operation + "\",\"a\":" + a
                    + ",\"b\":" + b + ",\"result\":" + result + "}");
        } catch (IllegalArgumentException | ArithmeticException e) {
            send(exchange, 400, error(400, "Bad Request", e.getMessage()));
        }
    }

    // ---------- helpers ----------

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> params = new HashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return params;
        }
        for (String pair : rawQuery.split("&")) {
            int eq = pair.indexOf('=');
            String key = eq >= 0 ? pair.substring(0, eq) : pair;
            String value = eq >= 0 ? pair.substring(eq + 1) : "";
            params.put(URLDecoder.decode(key, StandardCharsets.UTF_8),
                    URLDecoder.decode(value, StandardCharsets.UTF_8));
        }
        return params;
    }

    private static BigDecimal number(Map<String, String> params, String name) {
        String value = params.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required parameter: " + name);
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid value for parameter '" + name + "': '" + value + "' is not a valid number");
        }
    }

    private static String error(int status, String error, String message) {
        return "{\"status\":" + status + ",\"error\":\"" + escape(error)
                + "\",\"message\":\"" + escape(message) + "\"}";
    }

    /** Escapes text so it is safe inside a JSON string. */
    private static String escape(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
