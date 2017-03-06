package com.wire.bots.don.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.wire.bots.sdk.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class AdminClient {
    private static final Client client;
    private static final String httpUrl = "https://wire.com/api/v1/admin/bot/";

    static {
        ClientConfig cfg = new ClientConfig(JacksonJsonProvider.class);
        client = JerseyClientBuilder.createClient(cfg);
    }

    public String generateInviteLink(String botName, String provider, String service, String desc, String cookie) {
        DOM dom = new DOM();
        dom.name = botName.toLowerCase().trim();
        dom.provider = provider;
        dom.service = service;
        dom.description = desc;

        Response response = client.target(httpUrl).
                request(MediaType.APPLICATION_JSON).
                header("Cookie", cookie).
                put(Entity.entity(dom, MediaType.APPLICATION_JSON));

        if (response.getStatus() != 200) {
            Logger.warning(response.readEntity(String.class));
            return null;
        }
        return "wire.com/b/" + dom.name;
    }

    public void deleteLink(String botName, String cookie) {
        Response response = client.target(httpUrl).
                path(botName).
                request(MediaType.APPLICATION_JSON).
                header("Cookie", cookie).
                delete();
        if (response.getStatus() != 200) {
            Logger.warning("Failed to delete the link: " + response.readEntity(String.class));
        }
    }

    public static class DOM {
        @JsonProperty("username")
        public String name;
        @JsonProperty
        public String provider;
        @JsonProperty
        public String service;
        @JsonProperty
        public String description;
    }
}
