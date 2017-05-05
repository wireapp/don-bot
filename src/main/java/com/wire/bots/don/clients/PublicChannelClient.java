package com.wire.bots.don.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.wire.bots.don.Don;
import com.wire.bots.sdk.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class PublicChannelClient {
    private static final Client client;

    static {
        ClientConfig cfg = new ClientConfig(JacksonJsonProvider.class);
        client = JerseyClientBuilder.createClient(cfg);
    }

    public static boolean setToken(String channelName, String origin, String token) {
        DOM dom = new DOM();
        dom.token = token;
        dom.origin = origin;

        String url = String.format("http://%s:8080", Don.config.getChannelHost());

        Response response = client.target(url).
                path("admin/channels").
                path(channelName).
                request(MediaType.APPLICATION_JSON).
                header("Authorization", Don.config.getChannelSecret()).
                post(Entity.entity(dom, MediaType.APPLICATION_JSON));

        if (response.getStatus() != 200) {
            Logger.warning(response.readEntity(String.class));
            return false;
        }
        return true;
    }

    public static class DOM {
        @JsonProperty
        public String token;
        @JsonProperty
        public String origin;
    }
}
