package com.wire.bots.don.DAO;

import com.wire.bots.don.DAO.model.Service;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@UseStringTemplate3StatementLocator
public interface ServiceDAO {
    @SqlUpdate("INSERT INTO Service (Name) VALUES(:name)")
    @GetGeneratedKeys
    int insertService(@Bind("name") String name);

    @SqlQuery("SELECT * FROM Service WHERE Id = :serviceId")
    @RegisterMapper(_Mapper.class)
    Service getService(@Bind("serviceId") int serviceId);

    @SqlUpdate("UPDATE Service SET <column> = :value WHERE id = :serviceId")
    int updateService(@Bind("serviceId") int serviceId, @Define("column") String column, @Bind("value") String value);

    class _Mapper implements ResultSetMapper<Service> {
        @Override
        public Service map(int i, ResultSet rs, StatementContext statementContext) throws SQLException {
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
}