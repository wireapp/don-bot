package com.wire.bots.don.clients;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.wire.bots.don.model.Service;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: dejankovacevic
 * Date: 27/10/16
 * Time: 21:29
 */
public class SearchClient {
    private final Client client;
    private final String httpUrl;
    private final String token;

    public SearchClient(String token) {
        this.token = token;
        String env = System.getProperty("env", "prod");
        String domain = env.equals("prod") ? "wire.com" : "zinfra.io"; //fixme: remove zinfra
        httpUrl = String.format("https://%s-nginz-https.%s", env, domain);

        ClientConfig cfg = new ClientConfig(JacksonJsonProvider.class);
        client = JerseyClientBuilder.createClient(cfg);
    }

    public ArrayList<Service> search(String tags, String start) throws IOException {
        Response response = client.target(httpUrl).
                path("services").
                queryParam("tags", tags).
                queryParam("start", start).
                request(MediaType.APPLICATION_JSON).
                header("Authorization", "Bearer " + token).
                get();

        if (response.getStatus() != 200) {
            throw new IOException(response.readEntity(String.class));
        }

        return response.readEntity(new GenericType<ArrayList<Service>>() {
        });
    }
}
