package com.wire.bots.don.db;

import com.wire.bots.sdk.Configuration;

import java.sql.*;

public class Manager {
    private final Configuration.DB conf;

    public Manager(Configuration.DB postgres) {
        this.conf = postgres;
    }

    public int insertUser(String userId, String name) throws Exception {
        try (Connection connection = getConnection()) {
            String cmd = "INSERT INTO DON_USER (UserId, Name) VALUES(?, ?)";
            PreparedStatement stm = connection.prepareStatement(cmd);
            stm.setString(1, userId);
            stm.setString(2, name);
            return stm.executeUpdate();
        }
    }

    public User getUser(String userId) throws Exception {
        try (Connection connection = getConnection()) {
            String cmd = "SELECT * FROM DON_USER WHERE UserId = ?";
            PreparedStatement stm = connection.prepareStatement(cmd);
            stm.setString(1, userId);
            ResultSet rs = stm.executeQuery();
            User user = new User();
            if (rs.next()) {
                user.id = rs.getString("UserId");
                user.name = rs.getString("name");
                user.email = rs.getString("email");
                user.password = rs.getString("password");
                user.provider = rs.getString("provider");
                user.cookie = rs.getString("cookie");
                return user;
            }
        }
        return null;
    }

    public int updateUser(String userId, String email, String password, String provider) throws Exception {
        try (Connection connection = getConnection()) {
            String cmd = "UPDATE DON_USER SET " +
                    "Email = ?, " +
                    "Password = ?, " +
                    "Provider = ? " +
                    "WHERE UserId = ?";

            PreparedStatement stm = connection.prepareStatement(cmd);
            stm.setString(1, email);
            stm.setString(2, password);
            stm.setString(3, provider);
            stm.setString(4, userId);
            return stm.executeUpdate();
        }
    }

    public int updateCookie(String userId, String token) throws Exception {
        try (Connection connection = getConnection()) {
            String cmd = "UPDATE DON_USER SET cookie = ? WHERE UserId = ?";
            PreparedStatement stm = connection.prepareStatement(cmd);
            stm.setString(1, token);
            stm.setString(2, userId);
            return stm.executeUpdate();
        }
    }

    public int deleteCookie(String userId) throws SQLException {
        try (Connection connection = getConnection()) {
            String cmd = "UPDATE DON_USER SET cookie = ? WHERE UserId = ?";
            PreparedStatement stm = connection.prepareStatement(cmd);
            stm.setString(1, null);
            stm.setString(2, userId);
            return stm.executeUpdate();
        }
    }

    public int insertService() throws Exception {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO DON_SERVICE (Name) VALUES(null)");
            ResultSet generatedKeys = statement.getGeneratedKeys();
            generatedKeys.next();
            return generatedKeys.getInt("last_insert_rowid()");
        }
    }

    public Service getService(int serviceId) throws SQLException {
        try (Connection connection = getConnection()) {
            String cmd = "SELECT * FROM DON_SERVICE WHERE Id = ?";
            PreparedStatement stm = connection.prepareStatement(cmd);
            stm.setInt(1, serviceId);
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
                Service service = new Service();
                service.id = rs.getInt("id");
                service.name = rs.getString("name");
                service.description = rs.getString("description");
                service.url = rs.getString("url");
                service.profile = rs.getString("profile");
                service.serviceId = rs.getString("serviceId");
                service.field = rs.getString("field");
                return service;
            }
        }
        return null;
    }

    public int updateService(int id, String name, String value) throws Exception {
        try (Connection connection = getConnection()) {
            String cmd = String.format("UPDATE DON_SERVICE SET %s = ? WHERE id = ?", name);
            PreparedStatement stm = connection.prepareStatement(cmd);
            stm.setString(1, value);
            stm.setInt(2, id);
            return stm.executeUpdate();
        }
    }

    private Connection getConnection() throws SQLException {
        String driver = conf.driver != null ? conf.driver : "postgresql";
        String url = conf.url != null
                ? conf.url
                : String.format("jdbc:%s://%s:%d/%s", driver, conf.host, conf.port, conf.database);
        return DriverManager.getConnection(url, conf.user, conf.password);
    }
}
