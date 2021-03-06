package com.codecool.jlamas.database;

import java.sql.*;
import java.util.ArrayList;

import com.codecool.jlamas.models.account.Mentor;
import com.codecool.jlamas.models.account.Student;
import com.codecool.jlamas.models.accountdata.*;

public class StudentDAO {

    public StudentDAO() {

    }

    public ArrayList<Student> requestAll() {
        Student student = null;
        String query = String.format("%s %s %s %s %s %s %s %s"
                , "SELECT user.login, user.email, user.name, user.surname, login.password, student.group_id,"
                ,         "student.team, student.balance"
                , "FROM user"
                ,     "INNER JOIN login"
                ,             "ON login.login = user.login"
                ,     "INNER JOIN student"
                ,             "ON student.login = user.login"
                , "WHERE user.type = 'student';");


        ArrayList<Student> students = new ArrayList<Student>();
        try (Connection c = ConnectDB.connect();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(query);) {

            while (rs.next()) {
                student = getStudentFromResultSet(rs);
                students.add(student);
            }

        } catch (ClassNotFoundException|SQLException e) {
            System.out.println(e.getMessage());
        }

        return students;
    }

    public boolean delete(Student student) {
            // true if was successful
            String query;

            try (Connection c = ConnectDB.connect();
                 Statement stmt = c.createStatement();) {

                query = String.format("DELETE FROM `user` WHERE login = '%s'; ",
                        student.getLogin().getValue());

                query += String.format("DELETE FROM `login` WHERE login = '%s'; ",
                        student.getLogin().getValue());

                query += String.format("DELETE FROM `mentor` WHERE login = '%s'; ",
                        student.getLogin().getValue());

                stmt.executeUpdate(query);

            } catch (ClassNotFoundException|SQLException e) {
                System.out.println(e.getMessage());

                return false;
            }
            return true;
    }

    public boolean insert(Student student) {

        String query;

        try (Connection c = ConnectDB.connect();
             Statement stmt = c.createStatement();) {

            query = String.format("INSERT INTO `user` VALUES('%s', '%s', '%s', '%s', 'student'); ",
                    student.getLogin().getValue(),
                    student.getEmail().getValue(),
                    student.getName(),
                    student.getSurname());

            query += String.format("INSERT INTO `login` VALUES('%s', '%s'); ",
                    student.getLogin().getValue(),
                    student.getPassword().getValue());

            query += String.format("INSERT INTO `student` VALUES('%s', %d, '%d', '%d'); ",
                    student.getLogin().getValue(),
                    student.getGroup().getID(),
                    student.getTeam().getId(),
                    student.getWallet().getBalance());


            stmt.executeUpdate(query);

        } catch (ClassNotFoundException|SQLException e) {
            System.out.println(e.getMessage());

            return false;
        }
        return true;

    }

    public boolean update(Student student) {
        String query;

        try (Connection c = ConnectDB.connect();
             Statement stmt = c.createStatement();) {
            query = String.format("UPDATE `user` SET login = '%s', email = '%s', name = '%s', surname = '%s', " +
                                  "type = 'student' WHERE login = '%s'; ",
                    student.getLogin().getValue(),
                    student.getEmail().getValue(),
                    student.getName(),
                    student.getSurname(),
                    student.getLogin().getValue());

            query += String.format("UPDATE `login` SET login = '%s', password = '%s' WHERE login = '%s'; ",
                    student.getLogin().getValue(),
                    student.getPassword().getValue(),
                    student.getLogin().getValue());

            query += String.format("UPDATE `student` SET login = '%s', group_id = %d, team = '%s', " +
                                   "balance = '%s' WHERE login = '%s'; ",
                    student.getLogin().getValue(),
                    student.getGroup().getID(),
                    student.getTeam().getName(),
                    student.getWallet().getBalance(),
                    student.getLogin().getValue());
            stmt.executeUpdate(query);

        } catch (ClassNotFoundException|SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    public Student getStudent(String userLogin) {
        Student student = null;

        String query = String.format("%s %s %s %s %s %s %s WHERE user.type = 'student' AND login.login = '%s';"
                , "SELECT user.login, user.email, user.name, user.surname, login.password, student.group_id,"
                , "student.team, student.balance"
                , "FROM user"
                , "INNER JOIN login"
                , "ON login.login = user.login"
                , "INNER JOIN student"
                , "ON student.login = user.login"
                , userLogin);

        try (Connection c = ConnectDB.connect();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(query);) {
             student = getStudentFromResultSet(rs);

        } catch (ClassNotFoundException | SQLException e) {
            System.out.println(e.getMessage());
        }
        return student;
    }

    public ArrayList<Student> getStudentsFromTeam(Team team) {

        ArrayList<Student> students = new ArrayList<>();
        String query = String.format("SELECT * FROM student WHERE team = %s", team.getId());

        try (Connection c = ConnectDB.connect();
            Statement stmt = c.createStatement()) {

            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                students.add(getStudentFromResultSet(rs));
            }

        } catch (ClassNotFoundException|SQLException e) {
            System.out.println(e.getMessage());
        }
        return students;
    }

    public Student getStudentFromResultSet(ResultSet rs) throws SQLException{

        Student student = new Student();
        DoneQuestDAO doneQuests = new DoneQuestDAO();
        OwnedArtifactDAO ownedArtifacts = new OwnedArtifactDAO();
        TeamPurchaseDAO teamPurchases = new TeamPurchaseDAO();
        GroupDAO groupDAO = new GroupDAO();
        TeamDAO teamDAO = new TeamDAO();

        student.setName(rs.getString("name"));
        student.setSurname(rs.getString("surname"));
        student.setLogin(new Login(rs.getString("login")));
        student.setPassword(new Password(rs.getString("password")));
        student.setEmail(new Mail(rs.getString("email")));
        student.setGroup(groupDAO.getGroup(rs.getInt("group_id")));
        student.setTeam(teamDAO.get(rs.getInt("team")));
        student.setWallet(new Wallet(rs.getInt("balance")));
        student.getWallet().setDoneQuests(doneQuests.requestAllBy(student));
        student.getWallet().setOwnedArtifacts(ownedArtifacts.requestAllBy(student));
        student.getWallet().setPendingPurchases(teamPurchases.requestAllBy(student));

        return student;
    }
}
