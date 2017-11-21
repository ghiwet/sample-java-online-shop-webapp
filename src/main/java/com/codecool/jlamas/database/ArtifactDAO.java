package com.codecool.jlamas.database;


import com.codecool.jlamas.models.artifact.Artifact;

import java.sql.*;
import java.util.ArrayList;

public class ArtifactDAO {

    public ArtifactDAO() {

    }

    public ArrayList<Artifact> requestAll(){
        ArrayList<Artifact> artifactList = new ArrayList<>();
        String sql = "SELECT id, name, price, description FROM artifact";

        try (Connection c = ConnectDB.connect();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)){

            while (rs.next()) {
                Artifact artifact = new Artifact(rs.getInt("id"), rs.getString("name"),
                                    rs.getInt("price"), rs.getString("description"));
                artifactList.add(artifact);
            }
        } catch (ClassNotFoundException|SQLException e) {
            System.out.println(e.getMessage());
        }

        return artifactList;
    }

    public void insert(Artifact artifact) {
        String sql = "INSERT INTO artifact(name, price, description) VALUES (?, ?, ?);";

        try (Connection c = ConnectDB.connect();
             PreparedStatement pstmt = c.prepareStatement(sql);) {

            pstmt.setString(1, artifact.getName());
            pstmt.setInt(2, artifact.getPrice());
            pstmt.setString(3, artifact.getDescription());
            pstmt.executeUpdate();

        } catch (ClassNotFoundException|SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void update(Artifact artifact, String preUpdateName) {
        String sql = "UPDATE artifact(name, price, description) SET name = ? , "
                + "price = ? , "
                + "description = ? "
                + "WHERE name = ?";

        try (Connection c = ConnectDB.connect();
             PreparedStatement pstmt = c.prepareStatement(sql);) {

            pstmt.setString(1, artifact.getName());
            pstmt.setInt(2, artifact.getPrice());
            pstmt.setString(3, artifact.getDescription());
            pstmt.setString(4, preUpdateName);
            pstmt.executeUpdate();

        } catch (ClassNotFoundException|SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
