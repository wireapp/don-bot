package com.wire.bots.don.db;

import com.wire.bots.sdk.Logger;

import java.sql.*;

public class Manager {
    private final String path;

    public Manager(String path) {
        this.path = path;

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            int update = statement.executeUpdate("CREATE TABLE IF NOT EXISTS User " +
                    "(UserId STRING PRIMARY KEY," +
                    " Name STRING," +
                    " Email STRING," +
                    " Password STRING," +
                    " WebSite STRING," +
                    " Description STRING," +
                    " Cookie STRING," +
                    " Provider STRING)");
            if (update > 0)
                Logger.info("CREATED TABLE User");

            update = statement.executeUpdate("CREATE TABLE IF NOT EXISTS Service " +
                    "(Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ServiceId STRING," +
                    " Name STRING," +
                    " Field STRING," +
                    " Url STRING," +
                    " Description STRING," +
                    " Profile STRING)");
            if (update > 0)
                Logger.info("CREATED TABLE Service");
        }catch (Exception e){
            e.printStackTrace();
            Logger.error(e.getLocalizedMessage());
        }
    }

    public int insertUser(String userId, String name) throws Exception {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();

            String cmd = String.format("INSERT INTO User (UserId, Name) VALUES('%s', '%s')", userId, name);

            return statement.executeUpdate(cmd);
        }
    }

    public User getUser(String userId) throws Exception {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();

            ResultSet rs = statement.executeQuery("SELECT * FROM User WHERE UserId = '" + userId + "'");
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
            Statement statement = connection.createStatement();

            String cmd = String.format("UPDATE User SET " +
                            "Email = '%s', " +
                            "Password = '%s', " +
                            "Provider = '%s' " +
                            "WHERE UserId = '%s'",
                    email,
                    password,
                    provider,
                    userId);

            return statement.executeUpdate(cmd);
        }
    }

    public int updateCookie(String userId, String cookie) throws Exception {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();

            String cmd = String.format("UPDATE User SET " +
                            "cookie = '%s' " +
                            "WHERE UserId = '%s'",
                    cookie,
                    userId);

            return statement.executeUpdate(cmd);
        }
    }

    public int deleteCookie(String userId) throws SQLException {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();

            String cmd = String.format("UPDATE User SET " +
                            "cookie = null " +
                            "WHERE UserId = '%s'",
                    userId);

            return statement.executeUpdate(cmd);
        }
    }

    public int insertService() throws Exception {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();

            if (statement.executeUpdate("INSERT INTO Service(Name) VALUES(null)") != 1)
                throw new Exception("Unable to insert new promo");

            ResultSet generatedKeys = statement.getGeneratedKeys();
            generatedKeys.next();
            return generatedKeys.getInt("last_insert_rowid()");
        }
    }

    public Service getService(int serviceId) throws SQLException {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();

            ResultSet rs = statement.executeQuery("SELECT * FROM Service WHERE Id = " + serviceId);
            Service service = new Service();
            if (rs.next()) {
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
            Statement statement = connection.createStatement();

            String cmd = String.format("UPDATE Service SET " +
                            "%s = '%s' WHERE id = %d",
                    name,
                    value,
                    id);

            return statement.executeUpdate(cmd);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(String.format("jdbc:sqlite:%s", path));
    }
}
