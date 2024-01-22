package com.wire.bots.don.clients;

import com.wire.bots.don.DonService;
import com.wire.bots.don.exceptions.FailedAuthenticationException;
import com.wire.bots.don.exceptions.FailedRegistrationException;
import com.wire.bots.don.model.*;
import com.wire.xenon.assets.Picture;
import com.wire.xenon.models.AssetKey;
import com.wire.xenon.tools.Logger;
import com.wire.xenon.tools.Util;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ProviderClient {
    private WebTarget target;

    public ProviderClient() {
        Client client = DonService.instance.getClient();
        target = client.target(DonService.instance.getConfig().apiHost);
    }

    public Auth register(String name, String email, String password, String url, String desc)
            throws FailedRegistrationException {
        Provider provider = new Provider();
        provider.name = name;
        provider.email = email;
        provider.password = password;
        provider.url = url;
        provider.description = desc;

        Response response = target.
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

        Response response = target.
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

        Response response = target.
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
        Response response = target.
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

    public Service getService(String cookie, String pid, String sid) {
        Response response = target.
                path("providers").
                path(pid).
                path("services").
                path(sid).
                request(MediaType.APPLICATION_JSON).
                header("Cookie", cookie).
                get();

        return response.readEntity(Service.class);
    }

    public Provider getProvider(String cookie) {
        return target.
                path("provider").
                request(MediaType.APPLICATION_JSON).
                header("Cookie", cookie).
                get(Provider.class);
    }

    public boolean enableService(String cookie, String password, String id) throws IOException {
        UpdateService service = new UpdateService();
        service.password = password;
        service.enabled = true;

        return updateServiceConnection(cookie, id, service);
    }

    public boolean updateServiceConnection(String cookie, String id, UpdateService service) throws IOException {
        Response response = target.
                path("provider/services").
                path(id).
                path("connection").
                request(MediaType.TEXT_PLAIN).
                header("Cookie", cookie).
                put(Entity.entity(service, MediaType.APPLICATION_JSON));

        if (response.getStatus() > 300) {
            String msg = response.readEntity(String.class);
            Logger.error(msg);
        }

        return response.getStatus() == 200;
    }

    public boolean updateService(String cookie, String id, UpdateService service) {

        Response response = target.
                path("provider/services").
                path(id).
                request(MediaType.TEXT_PLAIN).
                header("Cookie", cookie).
                put(Entity.entity(service, MediaType.APPLICATION_JSON));

        if (response.getStatus() > 300) {
            String msg = response.readEntity(String.class);
            Logger.error(msg);
        }

        return response.getStatus() == 200;
    }

    public boolean deleteService(String cookie, String password, String id) {
        Auth auth = new Auth();
        auth.password = password;

        Response response = target.
                path("provider/services").
                path(id).
                request(MediaType.TEXT_PLAIN).
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
        os.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        os.write(data);
        os.write("\r\n--frontier--\r\n".getBytes(StandardCharsets.UTF_8));

        Response response = target
                .path("provider/assets")
                .request(MediaType.TEXT_PLAIN)
                .header("Cookie", cookie)
                .post(Entity.entity(os.toByteArray(), "multipart/mixed; boundary=frontier"));

        if (response.getStatus() >= 300) {
            Logger.warning(response.readEntity(String.class));
            throw new IOException(response.getStatusInfo().getReasonPhrase());
        }

        AssetKey assetKey = response.readEntity(AssetKey.class);

        return assetKey.id;
    }
}