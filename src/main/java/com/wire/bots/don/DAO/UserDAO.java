package com.wire.bots.don.DAO;


import com.wire.bots.don.DAO.model.User;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public interface UserDAO {
    @SqlUpdate("INSERT INTO Developer (UserId, Name) VALUES(:userId, :name)")
    int insertUser(@Bind("userId") UUID userId, @Bind("name") String name);

    @SqlQuery("SELECT * FROM Developer WHERE UserId = :userId")
    @RegisterColumnMapper(_Mapper.class)
    User getUser(@Bind("userId") UUID userId);

    @SqlUpdate("UPDATE Developer SET Email = :email, Provider = :provider WHERE UserId = :userId")
    int updateUser(@Bind("userId") UUID userId, @Bind("email") String email, @Bind("provider") String provider);

    @SqlUpdate("UPDATE Developer SET cookie = :cookie WHERE UserId = :userId")
    int updateCookie(@Bind("userId") UUID userId, @Bind("cookie") String cookie);

    @SqlUpdate("UPDATE Developer SET cookie = null WHERE UserId = :userId")
    int deleteCookie(@Bind("userId") UUID userId);

    class _Mapper implements ColumnMapper<User> {
        @Override
        public User map(ResultSet rs, int columnNumber, StatementContext ctx) throws SQLException {
            User user = new User();
            user.id = (UUID) rs.getObject("UserId");
            user.name = rs.getString("name");
            user.email = rs.getString("email");
            user.provider = rs.getString("provider");
            user.cookie = rs.getString("cookie");

            return user;
        }

    }
}