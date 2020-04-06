package com.wire.bots.don.DAO;


import com.wire.bots.don.DAO.model.User;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public interface UserDAO {
    @SqlUpdate("INSERT INTO Developer (UserId, Name) VALUES(:userId, :name)")
    int insertUser(@Bind("userId") UUID userId, @Bind("name") String name);

    @SqlQuery("SELECT * FROM Developer WHERE UserId = :userId")
    @RegisterMapper(_Mapper.class)
    User getUser(@Bind("userId") UUID userId);

    @SqlUpdate("UPDATE Developer SET Email = :email, Provider = :provider WHERE UserId = :userId")
    int updateUser(@Bind("userId") UUID userId, @Bind("email") String email, @Bind("provider") String provider);

    @SqlUpdate("UPDATE Developer SET cookie = :cookie WHERE UserId = :userId")
    int updateCookie(@Bind("userId") UUID userId, @Bind("cookie") String cookie);

    @SqlUpdate("UPDATE Developer SET cookie = null WHERE UserId = :userId")
    int deleteCookie(@Bind("userId") UUID userId);

    class _Mapper implements ResultSetMapper<User> {
        @Override
        public User map(int i, ResultSet rs, StatementContext statementContext) throws SQLException {
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