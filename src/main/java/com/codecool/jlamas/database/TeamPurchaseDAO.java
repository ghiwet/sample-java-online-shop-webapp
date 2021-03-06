package com.codecool.jlamas.database;

import com.codecool.jlamas.models.account.Student;
import com.codecool.jlamas.models.artifact.TeamPurchase;

import java.sql.*;
import java.util.ArrayList;

public class TeamPurchaseDAO {

    ArtifactDAO artifactDAO = new ArtifactDAO();
    StudentDAO studentDAO = new StudentDAO();

    public TeamPurchaseDAO() {}

    public void insert(TeamPurchase purchase) {
        String sql = "INSERT INTO team_purchase VALUES(?, ?, ?, ?, ?)";

        try (Connection c = ConnectDB.connect();
             PreparedStatement pstmt = c.prepareStatement(sql);) {

            pstmt.setInt(1, purchase.getId());
            pstmt.setString(2, purchase.getArtifact().getName());
            pstmt.setString(3, purchase.getStudent().getLogin().getValue());
            pstmt.setInt(4, purchase.getPrice());
            pstmt.setInt(5, purchase.isMarkedAsInteger());
            pstmt.executeUpdate();

        } catch (ClassNotFoundException|SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public ArrayList<TeamPurchase> requestAllBy(Student student) {

        ArrayList<Integer> ids = getIdsBy(student);
        ArrayList<TeamPurchase> pendingPurchases = new ArrayList<>();

        for (Integer id : ids) {
            pendingPurchases.addAll(requestAllBy(id));
        }
        return pendingPurchases;
    }

    public ArrayList<TeamPurchase> requestAllBy(Integer id) {

        ArrayList<TeamPurchase> pendingPurchases = new ArrayList<>();

        String sql = "SELECT artifact_name, student_login, price, is_marked FROM artifact "
                + "WHERE id = ?";

        try (Connection c = ConnectDB.connect();
             PreparedStatement pstmt = c.prepareStatement(sql);) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                TeamPurchase purchase = new TeamPurchase(id, artifactDAO.selectArtifact(rs.getString("artifact_name")),
                        studentDAO.getStudent(rs.getString("student_login")), rs.getInt("price"),
                        intToBoolean(rs.getInt("is_marked")));
                pendingPurchases.add(purchase);
            }

        } catch (ClassNotFoundException|SQLException e) {
            System.out.println(e.getMessage());
        }
        return pendingPurchases;
    }

    private ArrayList<Integer> getIdsBy(Student student) {

        String sql = "SELECT id FROM `team_purchase` WHERE student_login = ?";
        ArrayList<Integer> ids = new ArrayList<>();

        try (Connection c = ConnectDB.connect();
             PreparedStatement pstmt = c.prepareStatement(sql);) {

            pstmt.setString(1, student.getLogin().getValue());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Integer id = rs.getInt("id");
                ids.add(id);
            }

        } catch (ClassNotFoundException|SQLException e) {
            System.out.println(e.getMessage());
        }

        return ids;
    }

    public void update(TeamPurchase purchase) {
        String sql = "UPDATE team_purchase SET is_marked = ? WHERE id = ? AND student_login = ?";

        try (Connection c = ConnectDB.connect();
             PreparedStatement pstmt = c.prepareStatement(sql);) {

            pstmt.setInt(1, purchase.isMarkedAsInteger());
            pstmt.setInt(2, purchase.getId());
            pstmt.setString(3, purchase.getStudent().getLogin().getValue());
            pstmt.executeUpdate();

        } catch (ClassNotFoundException|SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean delete(TeamPurchase purchase) {
        String sql = "DELETE FROM team_purchase WHERE id = ?";

        try (Connection c = ConnectDB.connect();
             PreparedStatement pstmt = c.prepareStatement(sql);) {

            pstmt.setInt(1, purchase.getId());

        } catch (ClassNotFoundException|SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    private boolean intToBoolean(Integer status) {
        if (status.equals(0)) {
            return false;
        } else return true;
    }
}
