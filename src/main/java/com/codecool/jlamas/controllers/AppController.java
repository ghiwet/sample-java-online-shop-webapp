package com.codecool.jlamas.controllers;

import com.codecool.jlamas.database.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.*;
import java.net.HttpCookie;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AppController implements HttpHandler {

    LoginDAO loginData;
    UserDAO userData;

    public AppController() {
        this.loginData = new LoginDAO();
        this.userData = new UserDAO();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        HttpCookie cookie = getCookie(httpExchange);

        if (cookie != null) {
            if (method.equals("GET")) {
                sendRedirectResponse(httpExchange);
            } else {
//                logout(cookie, httpExchange);
            }

        } else {
            if (method.equals("GET")) {
                displayLoginFormula(httpExchange);
            } else {
                login(httpExchange);
            }
        }
    }

    public void sendOKResponse(String response, HttpExchange httpExchange) throws IOException{
        final byte[] finalResponseBytes = response.getBytes("UTF-8");
        httpExchange.sendResponseHeaders(200, finalResponseBytes.length);
        OutputStream os = httpExchange.getResponseBody();
        os.write(finalResponseBytes);
        os.close();
    }

    public void sendRedirectResponse(HttpExchange httpExchange) throws IOException {
        httpExchange.getResponseHeaders().set("Location", "/admin");
        httpExchange.sendResponseHeaders(302,-1);
    }

    public void displayLoginFormula(HttpExchange httpExchange) throws IOException {
        JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/login.twig");
        JtwigModel model = JtwigModel.newModel();

        String response = template.render(model);
        sendOKResponse(response, httpExchange);
    }

    public void login(HttpExchange httpExchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        String formData = br.readLine();

        System.out.println(formData);
        Map inputs = parseFormData(formData);
        String login = inputs.get("login").toString();
        String password = inputs.get("password").toString();

        if (loginData.matchLogin(login, password)) {
            launchUserController(login, httpExchange);

        } else {
            displayLoginFormula(httpExchange);
        }
    }

    private static Map<String, String> parseFormData(String formData) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();
        String[] pairs = formData.split("&");
        for(String pair : pairs){
            String[] keyValue = pair.split("=");
            String value = new URLDecoder().decode(keyValue[1], "UTF-8");
            map.put(keyValue[0], value);
        }
        return map;
    }

    public void launchUserController(String login, HttpExchange httpExchange) throws IOException {

        String userType = this.userData.getType(login);

        if (userType.equals("admin")) {
//            Admin admin = this.userData.getAdmin(login);
            createCookie(httpExchange, login);
            sendRedirectResponse(httpExchange);

        } else if (userType.equals("mentor")) {
//            MentorDAO mentorData = new MentorDAO();
//            Mentor mentor = mentorData.getMentor(login);
//            MentorMenuController mentorMenu = new MentorMenuController(mentor);
//            mentorMenu.start();
        } else if (userType.equals("student")) {
//            StudentDAO studentData = new StudentDAO();
//            Student student = studentData.getStudent(login);
//            StudentMenuController studentMenu = new StudentMenuController(student);
//            studentMenu.start();
        } else {
            displayLoginFormula(httpExchange);
        }
    }

    public HttpCookie getCookie(HttpExchange httpExchange) {
        String cookieStr = httpExchange.getRequestHeaders().getFirst("Cookie");
        HttpCookie cookie = null;
        if (cookieStr != null) {  // Cookie already exists
            cookie = HttpCookie.parse(cookieStr).get(0);
        }

        return cookie;
    }

    public HttpCookie createCookie(HttpExchange httpExchange, String login) {
        // Create a new cookie
        String sessionId = UUID.randomUUID().toString();
        HttpCookie cookie = new HttpCookie("sessionId", sessionId);
        httpExchange.getResponseHeaders().add("Set-Cookie", cookie.toString());
        this.addCookieToDb(cookie, login);

        return cookie;
    }

    public void addCookieToDb(HttpCookie cookie, String login) {
        String query = "INSERT INTO `cookies` VALUES (?, ?);";

        try (Connection c = ConnectDB.connect();
             PreparedStatement pstmt = c.prepareStatement(query)) {

            pstmt.setString(1, cookie.getValue());
            pstmt.setString(2, login);
            pstmt.executeUpdate();

        } catch (ClassNotFoundException|SQLException e) {
            System.out.println(e.getMessage());
        }

    }



}
