package com.wire.bots.don.DAO;

import com.wire.bots.don.DAO.model.Service;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

//@UseStringTemplate3StatementLocator
public interface ServiceDAO {
    @SqlUpdate("INSERT INTO Service (Name) VALUES(:name)")
    @GetGeneratedKeys
    int insertService(@Bind("name") String name);

    @SqlQuery("SELECT * FROM Service WHERE Id = :serviceId")
    @RegisterColumnMapper(_Mapper.class)
    Service getService(@Bind("serviceId") int serviceId);

    @SqlUpdate("UPDATE Service SET <column> = :value WHERE id = :serviceId")
    int updateService(@Bind("serviceId") int serviceId, @Define("column") String column, @Bind("value") String value);

    class _Mapper implements ColumnMapper<Service> {
        @Override
        public Service map(ResultSet rs, int columnNumber, StatementContext ctx) throws SQLException {
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