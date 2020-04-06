package com.wire.bots.don.clients;

import com.wire.bots.don.DonService;
import com.wire.bots.don.model.Service;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;

public class SearchClient {
    private final Client client;
    private final String httpUrl;
    private final String token;

    public SearchClient(String token) {
        this.token = token;
        httpUrl = DonService.instance.getConfig().apiHost;
        client = DonService.instance.getClient();
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
