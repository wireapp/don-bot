package com.wire.bots.don.clients;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.wire.bots.don.exceptions.FailedAuthenticationException;
import com.wire.bots.don.exceptions.FailedRegistrationException;
import com.wire.bots.don.model.*;
import com.wire.bots.sdk.Logger;
import com.wire.bots.sdk.Util;
import com.wire.bots.sdk.assets.IAsset;
import com.wire.bots.sdk.models.AssetKey;
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

/**
 * Created with IntelliJ IDEA.
 * User: dejankovacevic
 * Date: 24/10/16
 * Time: 13:00
 */
public class ProviderClient {
    private final Client client;
    private final String httpUrl;

    public ProviderClient() {
        String env = System.getProperty("env", "prod");
        String domain = env.equals("prod") ? "wire.com" : "zinfra.io"; //fixme: remove zinfra
        httpUrl = String.format("https://%s-nginz-https.%s", env, domain);

        ClientConfig cfg = new ClientConfig(JacksonJsonProvider.class);
        cfg.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);
        client = JerseyClientBuilder.createClient(cfg);
    }

    public Auth register(String name, String email, String url, String desc)
            throws IOException, FailedRegistrationException {
        Provider provider = new Provider();
        provider.name = name;
        provider.email = email;
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

    public String authenticate(String email, String password) throws FailedAuthenticationException {
        Auth auth = new Auth();
        auth.email = email;
        auth.password = password;

        Response response = client.target(httpUrl).
                path("provider/login").
                request(MediaType.APPLICATION_JSON).
                post(Entity.entity(auth, MediaType.APPLICATION_JSON));

        if (response.getStatus() != 200) {
            String msg = response.readEntity(String.class);
            throw new FailedAuthenticationException(msg);
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

    public String uploadProfilePicture(String cookie, IAsset image) throws Exception {
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
                .append(image.getEncryptedData().length)
                .append("\r\n");
        sb.append("Content-MD5: ")
                .append(Util.calcMd5(image.getEncryptedData()))
                .append("\r\n\r\n");

        // Complete
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(sb.toString().getBytes("utf-8"));
        os.write(image.getEncryptedData());
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