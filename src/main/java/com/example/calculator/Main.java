package com.example.calculator;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

        CalculatorServer server = new CalculatorServer(port);
        server.start();
        // Lets "docker stop" shut the server down cleanly
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        System.out.println("Calculator API listening on port " + port);
    }
}
