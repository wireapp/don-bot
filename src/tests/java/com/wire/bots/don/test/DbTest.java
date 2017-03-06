package com.wire.bots.don.test;

import com.wire.bots.don.db.Manager;
import com.wire.bots.don.db.Service;
import com.wire.bots.don.db.User;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: dejankovacevic
 * Date: 08/02/17
 * Time: 19:01
 */
public class DbTest {

    private static final String TEST_DB = "crypto/test.db";

    @Test
    public void insertUserTest() throws Exception {
        Manager manager = new Manager(TEST_DB);
        String userId = UUID.randomUUID().toString();
        String userName = "user name";
        String email = "test@test.com";
        String password = "secret";
        String provider = UUID.randomUUID().toString();
        String cookie = "cookie";

        int insert = manager.insertUser(userId, userName);
        assert (insert == 1);

        User user = manager.getUser(userId);
        assert (user.id.equals(userId));
        assert (user.name.equals(userName));
        assert (null == user.email);
        assert (null == user.cookie);
        assert (null == user.password);
        assert (null == user.provider);

        int updateUser = manager.updateUser(userId, email, password, provider);
        assert (updateUser == 1);

        user = manager.getUser(userId);
        assert (user.email.equals(email));
        assert (user.name.equals(userName));
        assert (user.provider.equals(provider));
        assert (null == user.cookie);

        int updateCookie = manager.updateUser(userId, "cookie", cookie);
        assert updateCookie == 1;

        user = manager.getUser(userId);
        assert (user.cookie.equals(cookie));
    }

    @Test
    public void updateUserTest() throws Exception {
        Manager manager = new Manager(TEST_DB);
        String userId = UUID.randomUUID().toString();
        String userName = "user name";
        String email = "test@test.com";
        String password = "secret";
        String provider = UUID.randomUUID().toString();
        String cookie = "cookie";

        int insert = manager.insertUser(userId, userName);
        assert (insert == 1);

        // Email
        int updateUser = manager.updateUser(userId, "email", email);
        assert (updateUser == 1);

        User user = manager.getUser(userId);
        assert (user.email.equals(email));

        // Password
        updateUser = manager.updateUser(userId, "password", password);
        assert (updateUser == 1);

        user = manager.getUser(userId);
        assert (user.password.equals(password));

        // ProviderId
        updateUser = manager.updateUser(userId, "provider", provider);
        assert (updateUser == 1);

        user = manager.getUser(userId);
        assert (user.provider.equals(provider));

        // Cookie
        updateUser = manager.updateUser(userId, "cookie", cookie);
        assert (updateUser == 1);

        user = manager.getUser(userId);
        assert (user.cookie.equals(cookie));

    }

    @Test
    public void deleteCookieTest() throws Exception {
        Manager manager = new Manager(TEST_DB);
        String userId = UUID.randomUUID().toString();
        String userName = "user name";
        String cookie = "cookie";

        int insert = manager.insertUser(userId, userName);
        assert (insert == 1);

        User user = manager.getUser(userId);
        assert (null == user.cookie);

        int updateCookie = manager.updateUser(userId, "cookie", cookie);
        assert updateCookie == 1;

        user = manager.getUser(userId);
        assert (user.cookie.equals(cookie));

        int deleteCookie = manager.deleteCookie(userId);
        assert deleteCookie == 1;

        user = manager.getUser(userId);
        assert (null == user.cookie);
    }

    @Test
    public void insertServiceTest() throws Exception {
        Manager manager = new Manager(TEST_DB);
        int id = manager.insertService();

        Service service = manager.getService(id);
        assert (id == service.id);
        assert (null == service.name);
        assert (null == service.url);
        assert (null == service.description);
        assert (null == service.profile);

        String serviceName = "serviceName";
        int updateService = manager.updateService(id, "name", serviceName);
        assert updateService == 1;

        service = manager.getService(id);

        assert service.name.equals(serviceName);

        String url = "https://";
        updateService = manager.updateService(id, "url", url);
        assert updateService == 1;

        service = manager.getService(id);

        assert service.url.equals(url);

        String desc = "https://";
        updateService = manager.updateService(id, "description", desc);
        assert updateService == 1;

        service = manager.getService(id);

        assert service.description.equals(desc);

        String profile = "https://pic.jpeg";
        updateService = manager.updateService(id, "profile", profile);
        assert updateService == 1;

        service = manager.getService(id);

        assert service.profile.equals(profile);

        String serviceId = UUID.randomUUID().toString();
        updateService = manager.updateService(id, "serviceId", serviceId);
        assert updateService == 1;

        service = manager.getService(id);

        assert service.serviceId.equals(serviceId);

        String field = UUID.randomUUID().toString();
        updateService = manager.updateService(id, "field", field);
        assert updateService == 1;

        service = manager.getService(id);

        assert service.field.equals(field);

    }

    @AfterClass
    public static void clean() {
        new File(TEST_DB).delete();
    }
}
