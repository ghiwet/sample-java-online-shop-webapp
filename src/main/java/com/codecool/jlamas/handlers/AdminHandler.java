package com.codecool.jlamas.handlers;

import com.codecool.jlamas.controllers.*;
import com.codecool.jlamas.database.SessionDAO;
import com.codecool.jlamas.database.UserDAO;
import com.codecool.jlamas.exceptions.InvalidCityDataException;
import com.codecool.jlamas.exceptions.InvalidGroupDataException;
import com.codecool.jlamas.exceptions.InvalidUserDataException;
import com.codecool.jlamas.models.account.Admin;
import com.codecool.jlamas.models.level.Level;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class AdminHandler extends AbstractHandler implements HttpHandler {

    private static final String MAIN = "templates/main.twig";
    private static final String NAV_MENU = "classpath:/templates/admin/nav_menu.twig";
    private static final String PROFILE = "classpath:/templates/admin/admin.twig";
    private static final String LIST = "classpath:/templates/admin/admin_list.twig";
    private static final String MENTOR_FORM = "classpath:/templates/admin/admin_mentor_form.twig";
    private static final String CITY_FORM = "classpath:/templates/admin/admin_city_form.twig";
    private static final String GROUP_FORM = "classpath:/templates/admin/admin_group_form.twig";
    private static final String LEVEL_ADD = "classpath:/templates/admin/admin_level_add.twig";
    private static final String LEVEL_EDIT = "classpath:/templates/admin/admin_level_edit.twig";
    private static final String CHANGE_PASSWORD = "classpath:/templates/change_password.twig";
    private static final String LOGOUT = "/admin/logout";

    private static final Integer OBJ_INDEX = 5;

    private Map<String, Callable> getCommands = new HashMap<String, Callable>();
    private Map<String, Callable> postCommands = new HashMap<String, Callable>();
    private Admin admin;
    private SessionDAO session = new SessionDAO();
    private CookieController cookieController = new CookieController();
    private Response responseCode = new Response();
    private LevelController levelController;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String response = "";
        String method = httpExchange.getRequestMethod();
        HttpCookie cookie = cookieController.getCookie(httpExchange);

        if (cookie != null) {
            String userType = new UserDAO().getType(session.getUserByCookie(httpExchange).getLogin().getValue());
            if (userType.equals("admin")) {
                this.admin = (Admin) session.getUserByCookie(httpExchange);

                if (admin != null) {
                    if (method.equals("GET")) {
                        if (httpExchange.getRequestURI().getPath().toString().equals(LOGOUT)) {
                            this.logout(httpExchange);

                        } else {
                            response = this.findCommand(httpExchange, getCommands);
                            responseCode.sendOKResponse(response, httpExchange);
                        }
                    }

                    if (method.equals("POST")) {
                        response = this.findCommand(httpExchange, postCommands);
                        responseCode.sendOKResponse(response, httpExchange);
                    }
                }
            } else {
                responseCode.sendRedirectResponse(httpExchange, "/");
            }
        } else {
            responseCode.sendRedirectResponse(httpExchange, "/");
        }
    }

    protected void addGetCommands (HttpExchange httpExchange) {
        this.getCommands.put("/admin", () -> {return this.displayProfile();} );
        this.getCommands.put("/admin/mentors/list", () -> {return this.displayMentors();} );
        this.getCommands.put("/admin/levels/list", () -> {return this.displayLevels();} );
        this.getCommands.put("/admin/levels/add", () -> {return this.displayAddLevel();} );
        this.getCommands.put("/admin/levels/edit/.+", () -> {return this.displayEditLevel(httpExchange);} );
        this.getCommands.put("/admin/levels/remove/.+", () -> {return this.deleteLevel(httpExchange);} );
        this.getCommands.put("/admin/mentors/add", () -> {return this.displayMentorForm(null, null); } );
        this.getCommands.put("/admin/mentors/list/edit/.+", () -> {return this.displayMentorForm(httpExchange, null); } );
        this.getCommands.put("/admin/mentors/list/remove/.+", () -> { return this.removeMentor(httpExchange);} );
        this.getCommands.put("/admin/cities/list", () -> {return this.displayCities();} );
        this.getCommands.put("/admin/cities/add", () -> {return this.displayCityForm(null, null, null); } );
        this.getCommands.put("/admin/cities/list/remove/.+", () -> {return this.removeCity(httpExchange);} );
        this.getCommands.put("/admin/cities/list/edit/.+", () -> {return this.displayCityForm(httpExchange, null, null); } );
        this.getCommands.put("/admin/groups/add", () -> {return this.displayGroupForm(null, null); } );
        this.getCommands.put("/admin/groups/list", () -> {return this.displayGroups();} );
        this.getCommands.put("/admin/groups/list/remove/.+", () -> {return this.removeGroup(httpExchange);} );
        this.getCommands.put("/admin/groups/list/edit/.+", () -> {return this.displayGroupForm(null, null); } );
        this.getCommands.put("/admin/password/edit/.+", () -> {return this.displayEditPassword("");} );
    }

    protected void addPostCommands (HttpExchange httpExchange) {
        postCommands.put("/admin/mentors/add", () -> { return this.addMentor(httpExchange);} );
        postCommands.put("/admin/mentors/list/edit/.+", () -> { return this.editMentor(httpExchange);} );
        postCommands.put("/admin/cities/add", () -> { return this.addCity(httpExchange);} );
        postCommands.put("/admin/cities/list/edit/[0-9]+", () -> { return this.editCity(httpExchange);} );
        postCommands.put("/admin/levels/add", () -> { return this.addLevel(httpExchange);} );
        postCommands.put("/admin/levels/edit/.+", () -> {return this.editLevel(httpExchange);} );
        postCommands.put("/admin/groups/add", () -> { return this.addGroup(httpExchange);} );
        postCommands.put("/admin/groups/list/edit/[0-9]+", () -> { return this.editGroup(httpExchange);} );
        postCommands.put("/admin/password/edit/.+", () -> { return this.editPassword(httpExchange); });
    }

    protected JtwigModel getContent(String content_path) {
        JtwigModel model = JtwigModel.newModel();

        model.with("nav_path", NAV_MENU);
        model.with("logout_path", LOGOUT);
        model.with("content_path", content_path);
        model.with("login", admin.getLogin().getValue());

        return model;
    }
    protected String displayProfile() {
        JtwigTemplate template = JtwigTemplate.classpathTemplate(MAIN);

        JtwigModel model = getContent(PROFILE);
        model.with("admin", this.admin);

        return template.render(model);
    }

    private String displayMentors() {
        JtwigTemplate template = JtwigTemplate.classpathTemplate(MAIN);

        JtwigModel model = getContent(LIST);
        model.with("mentors", new MentorController().getAll());

        return template.render(model);
    }

    private String displayGroups() {
        JtwigTemplate template = JtwigTemplate.classpathTemplate(MAIN);

        JtwigModel model = getContent(LIST);
        model.with("groups", new GroupController().getAll());

        return template.render(model);
    }

    private String displayCities() {
        JtwigTemplate template = JtwigTemplate.classpathTemplate(MAIN);

        JtwigModel model = getContent(LIST);
        model.with("cities", new CityController().getAll());

        return template.render(model);
    }

    protected String displayEditPassword(String message) {
        JtwigTemplate template = JtwigTemplate.classpathTemplate(MAIN);

        JtwigModel model = getContent(CHANGE_PASSWORD);
        model.with("msg", message);

        return template.render(model);
    }

    private String displayMentorForm(HttpExchange httpExchange, Map<String, String> inputs) {
        // where inputs is a html parsed inputs passed in retake if there was an exception catch (while adding to db)
        JtwigTemplate template = JtwigTemplate.classpathTemplate(MAIN);
        JtwigModel model = getContent(MENTOR_FORM);

        if (inputs == null && httpExchange != null) {
            model.with("mentor", new MentorController().get(this.parseStringFromURL(httpExchange, OBJ_INDEX)));
        }
        else if (inputs != null) {
            model.with("name", inputs.get("name"));
            model.with("surname", inputs.get("surname"));
        }
        model.with("groups", new GroupController().getAll());

        return template.render(model);
    }

    private String displayCityForm(HttpExchange httpExchange, Map<String, String> inputs, String errmsg) {
        JtwigTemplate template = JtwigTemplate.classpathTemplate(MAIN);
        JtwigModel model = getContent(CITY_FORM);

        if (inputs == null && httpExchange != null) {
            model.with("city", new CityController().get(this.parseStringFromURL(httpExchange, OBJ_INDEX)));
        }
        else if (inputs != null) {
            model.with("name", inputs.get("name"));
            model.with("shortname", inputs.get("shortname"));
        }
        model.with("msg", errmsg);

        return template.render(model);
    }

    private String displayGroupForm(HttpExchange httpExchange, String errmsg) {
        JtwigTemplate template = JtwigTemplate.classpathTemplate(MAIN);
        JtwigModel model = getContent(GROUP_FORM);

        CityController cityController = new CityController();
        GroupController groupController = new GroupController();

        if (httpExchange != null) {
            model.with("group", new GroupController().get(this.parseStringFromURL(httpExchange, OBJ_INDEX)));
        }
        model.with("errmsg", errmsg);
        model.with("cities", cityController.getAll());
        model.with("years", groupController.getYears());
        model.with("numbers", groupController.getAvailableGroupNumbers());

        return template.render(model);
    }

    private String addMentor(HttpExchange httpExchange) throws IOException {
        Map<String, String> inputs = this.parseUserInputsFromHttp(httpExchange);

        MentorController ctrl = new MentorController();
        try {
            ctrl.createFromMap(inputs);
        } catch (InvalidUserDataException e) {
            return this.displayMentorForm(null, inputs);
        }

        return this.displayMentors();
    }

    private String editMentor(HttpExchange httpExchange) throws IOException {
        Map<String, String> inputs = this.parseUserInputsFromHttp(httpExchange);

        MentorController ctrl = new MentorController();
        try {
            ctrl.editFromMap(inputs, this.parseStringFromURL(httpExchange, OBJ_INDEX));
        } catch (InvalidUserDataException e) {
            return this.displayMentorForm(httpExchange, inputs);
        }

        return this.displayMentors();
    }

    private String removeMentor(HttpExchange httpExchange) throws IOException {
        MentorController mentorController = new MentorController();
        mentorController.remove(this.parseStringFromURL(httpExchange, OBJ_INDEX));

        return this.displayMentors();
    }

    private String addCity(HttpExchange httpExchange) throws IOException {
        Map<String, String> inputs = this.parseUserInputsFromHttp(httpExchange);

        CityController ctrl = new CityController();
        try {
            ctrl.createFromMap(inputs);
        } catch (InvalidCityDataException e) {
            return this.displayCityForm(null, inputs, e.getMessage());
        }

        return this.displayCities();
    }

    private String editCity(HttpExchange httpExchange) throws IOException {
        Map<String, String> inputs = this.parseUserInputsFromHttp(httpExchange);

        CityController ctrl = new CityController();
        try {
            ctrl.editFromMap(inputs, this.parseStringFromURL(httpExchange, OBJ_INDEX));
        } catch (InvalidCityDataException e) {
            return this.displayCityForm(null, inputs, e.getMessage());
        }

        return this.displayCities();
    }

    private String removeCity(HttpExchange httpExchange) throws IOException {
        CityController cityController = new CityController();
        cityController.remove(this.parseStringFromURL(httpExchange, OBJ_INDEX));

        return this.displayCities();
    }

    private String addGroup(HttpExchange httpExchange) throws IOException {
        Map<String, String> inputs = this.parseUserInputsFromHttp(httpExchange);

        GroupController ctrl = new GroupController();
        try {
            ctrl.createFromMap(inputs);
        } catch(InvalidGroupDataException e) {
            return this.displayGroupForm(null, e.getMessage());
        }
        return this.displayGroups();
    }

    private String editGroup(HttpExchange httpExchange) throws IOException {
        Map<String, String> inputs = this.parseUserInputsFromHttp(httpExchange);

        GroupController ctrl = new GroupController();
        try {
            ctrl.editFromMap(inputs, this.parseStringFromURL(httpExchange, OBJ_INDEX));
        } catch(InvalidGroupDataException e) {
            return this.displayGroupForm(httpExchange, e.getMessage());
        }
        return this.displayGroups();
    }

    private String removeGroup(HttpExchange httpExchange) {
        GroupController ctrl = new GroupController();
        ctrl.remove(this.parseStringFromURL(httpExchange, OBJ_INDEX));

        return this.displayGroups();
    }

    private String displayLevels() {
        JtwigTemplate template = JtwigTemplate.classpathTemplate(MAIN);
        JtwigModel model = getContent(LIST);
        model.with("levels", new LevelController().showAllLevels());
        return template.render(model);
    }

    private String displayAddLevel() {
        JtwigTemplate template = JtwigTemplate.classpathTemplate(MAIN);
        JtwigModel model = getContent(LEVEL_ADD);

        return template.render(model);
    }

    private String displayEditLevel(HttpExchange httpExchange) {
        String levelName = this.parseStringFromURL(httpExchange, 4);
        levelController = new LevelController();

        JtwigTemplate template = JtwigTemplate.classpathTemplate(MAIN);
        JtwigModel model = getContent(LEVEL_EDIT);
        Level level = levelController.chooseLevel(levelName);
        model.with("level", level);

        return template.render(model);
    }


    private String addLevel(HttpExchange httpExchange) throws IOException {
        Map<String, String> inputs = this.parseUserInputsFromHttp(httpExchange);
        levelController = new LevelController();
        levelController.createLevel(inputs);

        return displayLevels();
    }

    private String editLevel(HttpExchange httpExchange) throws IOException {
        Map<String, String> inputs = this.parseUserInputsFromHttp(httpExchange);
        levelController = new LevelController();
        levelController.editLevel(inputs, this.parseStringFromURL(httpExchange, 4));
        
        return displayLevels();
    }

    private String deleteLevel(HttpExchange httpExchange) {
        levelController = new LevelController();
        levelController.deleteLevel(this.parseStringFromURL(httpExchange, 4));

        return displayLevels();
    }
}
