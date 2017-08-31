package database;

import models.account.Mentor;
import models.accountdata.*;

import java.util.ArrayList;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;


public class MentorDAO implements DAO {

    private final String FILEPATH = "database/mentors/";

    public MentorDAO() {
    }

    public Mentor load(String login) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILEPATH + login + ".txt"))) {
            Mentor mentor = new Mentor();
            String rLogin = br.readline();
            String rPassword = br.readline();
            String rEmail = br.readline();
            String rName = br.readline();
            String rSurname = br.readline();
            String rClassId = br.readline();

            if (rLogin.startswith("LOGIN: ")) {
                mentor.setLogin(new Login(rLogin.substring("LOGIN: ".length())));
            }
            else {
                return null;
            }
            if (rPassword.startswith("PASSWORD: ")) {
                mentor.setPassword(new Password(rPassword.substring("PASSWORD: ".length())));
            }
            else {
                return null;
            }
            if (rEmail.startswith("EMAIL: ")) {
                mentor.setEmail(new Mail(rEmail.substring("EMAIL: ".length())));
            }
            else {
                return null;
            }
            if (rName.startswith("NAME: ")) {
                mentor.setName(rName.substring("NAME: ".length()));
            }
            else {
                return null;
            }
            if (rSurname.startswith("SURNAME: ")) {
                mentor.setSurname(rSurname.substring("SURNAME: ".length()));
            }
            else {
                return null;
            }
            if (rClassId.startswith("CLASSID: ")) {
                mentor.setClassId(rClassId.substring("CLASSID: ".length()));
            }
            else {
                return null;
            }
            return mentor;
        } catch (IOException e) {
            System.out.println("File not found.");
        }
    }

    public ArrayList<Mentor> loadAll() {
        ArrayList<Mentor> mentorList = new ArrayList<>();

        File[] files = new File(FILEPATH).listFiles();

        for (File file : files) {
            int dotPosition = file.getName().lastIndexOf(".");
            mentorList.add(load(file.getName().substring(0, dotPosition)));
        }
        return mentorList;
    }

    public void save(Mentor mentor) {
        try (FileWriter fw = new FileWriter(FILEPATH + mentor.getLogin() + ".txt")) {
            fw.write("LOGIN: " + mentor.getLogin().getValue());
            fw.write("PASSWORD: " + mentor.getPassword().getValue());
            fw.write("EMAIL: " + mentor.getEmail().getValue());
            fw.write("NAME: " + mentor.getName());
            fw.write("SURNAME: " + mentor.getSurname());
            fw.write("CLASSID: " + mentor.getClassId());
        } catch (IOException e) {
            System.out.println("Filepath not found.");
        }
    }
}