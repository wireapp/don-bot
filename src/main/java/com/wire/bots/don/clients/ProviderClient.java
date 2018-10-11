package com.wire.bots.don.clients;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.wire.bots.don.exceptions.FailedAuthenticationException;
import com.wire.bots.don.exceptions.FailedRegistrationException;
import com.wire.bots.don.model.*;
import com.wire.bots.sdk.assets.Picture;
import com.wire.bots.sdk.models.AssetKey;
import com.wire.bots.sdk.tools.Logger;
import com.wire.bots.sdk.tools.Util;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ProviderClient {
    private static final Client client;
    private static final String httpUrl;

    static {
        String env = System.getProperty("env", "prod");
        String domain = env.equals("prod") ? "wire.com" : "zinfra.io";
        httpUrl = String.format("https://%s-nginz-https.%s", env, domain);

        ClientConfig cfg = new ClientConfig(JacksonJsonProvider.class);
        cfg.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);
        client = JerseyClientBuilder.createClient(cfg);
    }

    public Auth register(String name, String email, String password, String url, String desc)
            throws FailedRegistrationException {
        Provider provider = new Provider();
        provider.name = name;
        provider.email = email;
        provider.password = password;
        provider.url = url;
        provider.description = desc;

        Response response = client.target(httpUrl).
                path("provider/register").
                request(MediaType.APPLICATION_JSON).
                post(Entity.entity(provider, MediaType.APPLICATION_JSON));

        if (response.getStatus() != 201) {
            throw new FailedRegistrationException(response.readEntity(String.class));
        }
        return response.readEntity(Auth.class);
    }

    public String login(String email, String password) throws FailedAuthenticationException {
        Auth auth = new Auth();
        auth.email = email;
        auth.password = password;

        Response response = client.target(httpUrl).
                path("provider/login").
                request(MediaType.APPLICATION_JSON).
                post(Entity.entity(auth, MediaType.APPLICATION_JSON));

        if (response.getStatus() != 200) {
            String msg = response.readEntity(String.class);
            Logger.info("Login for: %s failed: %s", email, msg);
            throw new FailedAuthenticationException("Wrong email or password");
        }

        return response.getCookies().get("zprovider").toString();
    }

    public AuthToken newService(String cookie, String name, String url, String desc, String pubKey,
                                String[] tags, ArrayList<Asset> assets)
            throws IOException {
        Service service = new Service();
        service.name = name;
        service.pubKey = pubKey;
        service.url = url;
        service.description = desc;
        service.summary = desc;
        service.tags = tags;
        service.assets = assets;

        Response response = client.target(httpUrl).
                path("provider/services").
                request(MediaType.APPLICATION_JSON).
                header("Cookie", cookie).
                post(Entity.entity(service, MediaType.APPLICATION_JSON));

        if (response.getStatus() > 300) {
            String msg = response.readEntity(String.class);
            Logger.info(msg);
            throw new IOException(msg);
        }

        return response.readEntity(AuthToken.class);
    }

    public ArrayList<Service> listServices(String cookie) throws IOException {
        Response response = client.target(httpUrl).
                path("provider/services").
                request(MediaType.APPLICATION_JSON).
                header("Cookie", cookie).
                get();

        if (response.getStatus() > 300) {
            String msg = response.readEntity(String.class);
            Logger.info(msg);
            throw new IOException(msg);
        }

        return response.readEntity(new GenericType<ArrayList<Service>>() {
        });
    }

    public Service getService(String cookie, String pid, String sid) throws IOException {
        Response response = client.target(httpUrl).
                path("providers").
                path(pid).
                path("services").
                path(sid).
                request(MediaType.APPLICATION_JSON).
                header("Cookie", cookie).
                get();

        return response.readEntity(Service.class);
    }

    public Provider getProvider(String cookie) throws IOException {
        return client.target(httpUrl).
                path("provider").
                request(MediaType.APPLICATION_JSON).
                header("Cookie", cookie).
                get(Provider.class);
    }

    public boolean enableService(String cookie, String password, String id) throws IOException {
        return updateServiceConnection(cookie, password, id, null, null, null, true);
    }

    public boolean disableService(String cookie, String password, String id) throws IOException {
        return updateServiceConnection(cookie, password, id, null, null, null, false);
    }

    public boolean updateServiceConnection(String cookie, String password, String id,
                                           String url, String[] tokens, String[] pubKeys, Boolean enabled)
            throws IOException {
        UpdateService service = new UpdateService();
        service.pubKeys = pubKeys;
        service.url = url;
        service.tokens = tokens;
        service.enabled = enabled;
        service.password = password;

        Response response = client.target(httpUrl).
                path("provider/services").
                path(id).
                path("connection").
                request(MediaType.APPLICATION_JSON).
                header("Cookie", cookie).
                put(Entity.entity(service, MediaType.APPLICATION_JSON));

        if (response.getStatus() > 300) {
            String msg = response.readEntity(String.class);
            Logger.error(msg);
            throw new IOException(msg);
        }

        return response.getStatus() == 200;
    }

    public boolean updateService(String cookie, String password, String id, String description, ArrayList<Asset> assets)
            throws IOException {
        UpdateService service = new UpdateService();
        service.description = description;
        service.password = password;
        service.assets = assets;

        Response response = client.target(httpUrl).
                path("provider/services").
                path(id).
                request(MediaType.APPLICATION_JSON).
                header("Cookie", cookie).
                put(Entity.entity(service, MediaType.APPLICATION_JSON));

        if (response.getStatus() > 300) {
            String msg = response.readEntity(String.class);
            Logger.error(msg);
            throw new IOException(msg);
        }

        return response.getStatus() == 200;
    }

    public boolean deleteService(String cookie, String password, String id) {
        Auth auth = new Auth();
        auth.password = password;

        Response response = client.target(httpUrl).
                path("provider/services").
                path(id).
                request(MediaType.APPLICATION_JSON).
                header("Cookie", cookie).
                build("DELETE", Entity.entity(auth, MediaType.APPLICATION_JSON)).
                invoke();

        if (response.getStatus() > 300)
            Logger.warning(response.readEntity(String.class));

        return response.getStatus() == 200;
    }

    public String uploadProfilePicture(String cookie, Picture image) throws Exception {
        byte[] data = image.getImageData();

        String strMetadata = String.format("{\"public\": %s, \"retention\": \"eternal\"}", true);
        StringBuilder sb = new StringBuilder();

        // Part 1
        sb.append("--frontier\r\n");
        sb.append("Content-Type: application/json; charset=utf-8\r\n");
        sb.append("Content-Length: ")
                .append(strMetadata.length())
                .append("\r\n\r\n");
        sb.append(strMetadata)
                .append("\r\n");

        // Part 2
        sb.append("--frontier\r\n");
        sb.append("Content-Type: ")
                .append(image.getMimeType())
                .append("\r\n");
        sb.append("Content-Length: ")
                .append(data.length)
                .append("\r\n");
        sb.append("Content-MD5: ")
                .append(Util.calcMd5(data))
                .append("\r\n\r\n");

        // Complete
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(sb.toString().getBytes("utf-8"));
        os.write(data);
        os.write("\r\n--frontier--\r\n".getBytes("utf-8"));

        Response response = client.target(httpUrl)
                .path("provider/assets")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Cookie", cookie)
                .post(Entity.entity(os.toByteArray(), "multipart/mixed; boundary=frontier"));

        if (response.getStatus() >= 300) {
            Logger.warning(response.readEntity(String.class));
            throw new IOException(response.getStatusInfo().getReasonPhrase());
        }

        AssetKey assetKey = response.readEntity(AssetKey.class);

        return assetKey.key;
    }
}