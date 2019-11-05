package com.wire.bots.don.db;

import com.wire.bots.sdk.Configuration;

import java.sql.*;
import java.util.UUID;

public class Database {
    private final Configuration.DB conf;

    public Database(Configuration.DB postgres) {
        this.conf = postgres;
    }

    public int insertUser(UUID userId, String name) throws Exception {
        try (Connection connection = getConnection()) {
            String cmd = "INSERT INTO DON_USER (UserId, Name) VALUES(?, ?)";
            PreparedStatement stm = connection.prepareStatement(cmd);
            stm.setString(1, userId.toString());
            stm.setString(2, name);
            return stm.executeUpdate();
        }
    }

    public User getUser(UUID userId) throws Exception {
        try (Connection connection = getConnection()) {
            String cmd = "SELECT * FROM DON_USER WHERE UserId = ?";
            PreparedStatement stm = connection.prepareStatement(cmd);
            stm.setString(1, userId.toString());
            ResultSet rs = stm.executeQuery();
            User user = new User();
            if (rs.next()) {
                user.id = rs.getString("UserId");
                user.name = rs.getString("name");
                user.email = rs.getString("email");
                user.provider = rs.getString("provider");
                user.cookie = rs.getString("cookie");
                return user;
            }
        }
        return null;
    }

    public int updateUser(UUID userId, String email, String provider) throws Exception {
        try (Connection connection = getConnection()) {
            String cmd = "UPDATE DON_USER SET " +
                    "Email = ?, " +
                    "Provider = ? " +
                    "WHERE UserId = ?";

            PreparedStatement stm = connection.prepareStatement(cmd);
            stm.setString(1, email);
            stm.setString(2, provider);
            stm.setString(3, userId.toString());
            return stm.executeUpdate();
        }
    }

    public int updateCookie(UUID userId, String token) throws Exception {
        try (Connection connection = getConnection()) {
            String cmd = "UPDATE DON_USER SET cookie = ? WHERE UserId = ?";
            PreparedStatement stm = connection.prepareStatement(cmd);
            stm.setString(1, token);
            stm.setString(2, userId.toString());
            return stm.executeUpdate();
        }
    }

    public int deleteCookie(UUID userId) throws SQLException {
        try (Connection connection = getConnection()) {
            String cmd = "UPDATE DON_USER SET cookie = ? WHERE UserId = ?";
            PreparedStatement stm = connection.prepareStatement(cmd);
            stm.setString(1, null);
            stm.setString(2, userId.toString());
            return stm.executeUpdate();
        }
    }

    public int insertService(String serviceName) throws Exception {
        try (Connection connection = getConnection()) {
            String cmd = "INSERT INTO DON_SERVICE (Name) VALUES(?)";
            PreparedStatement stm = connection.prepareStatement(cmd, Statement.RETURN_GENERATED_KEYS);
            stm.setString(1, serviceName);
            stm.executeUpdate();
            ResultSet generatedKeys = stm.getGeneratedKeys();
            if (generatedKeys.next())
                return generatedKeys.getInt(1);

            return -1;
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
