package com.codecool.jlamas;

import org.flywaydb.core.Flyway;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

import com.codecool.jlamas.controllers.AdminController;
import com.codecool.jlamas.controllers.TemplateController;

public class Main {
    public static void main(String[] args) throws Exception {

        // set database migration
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:sqlite:./target/database", "jlamas", null);
        flyway.migrate();

        // create local server
        HttpServer server = HttpServer.create(new InetSocketAddress(8801), 0);

        // set routes
        // server.createContext("/url", new ControllerName());
        server.createContext("/template", new TemplateController());
        server.createContext("/admin", new AdminController());
        server.createContext("/static", new Static());
        server.setExecutor(null);

        server.start();

    }
}
