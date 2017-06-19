package com.wire.bots.don.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.wire.bots.don.Don;
import com.wire.bots.sdk.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class PublicChannelClient {
    private static final Client client;
    private static final String HTTP_CHANNEL_LOCAL_DEFAULT_SVC_CLUSTER_LOCAL = "http://channel-local.default.svc.cluster.local";

    static {
        ClientConfig cfg = new ClientConfig(JacksonJsonProvider.class);
        cfg.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);
        client = JerseyClientBuilder.createClient(cfg);
    }

    public static boolean createChannel(String channelName, String origin, String token) {
        DOM dom = new DOM();
        dom.token = token;
        dom.origin = origin;

        Response response = client.target(HTTP_CHANNEL_LOCAL_DEFAULT_SVC_CLUSTER_LOCAL).
                path("admin/channels").
                path(channelName).
                request(MediaType.APPLICATION_JSON).
                header("Authorization", Don.config.getChannel().getSecret()).
                post(Entity.entity(dom, MediaType.APPLICATION_JSON));

        if (response.getStatus() != 200) {
            Logger.warning(response.readEntity(String.class));
            return false;
        }
        return true;
    }

    public static boolean deleteChannel(String channelName, String origin, String token) {
        DOM dom = new DOM();
        dom.token = token;
        dom.origin = origin;

        Response response = client.target(HTTP_CHANNEL_LOCAL_DEFAULT_SVC_CLUSTER_LOCAL).
                path("admin/channels").
                path(channelName).
                request(MediaType.APPLICATION_JSON).
                header("Authorization", Don.config.getChannel().getSecret()).
                build("DELETE", Entity.entity(dom, MediaType.APPLICATION_JSON)).
                invoke();

        if (response.getStatus() > 300) {
            Logger.warning(response.readEntity(String.class));
            return false;
        }
        return true;
    }

    public static class DOM {
        @JsonProperty
        String token;
        @JsonProperty
        String origin;
    }
}
