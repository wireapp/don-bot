package com.wire.bots.don.commands;

import com.wire.bots.don.db.Manager;
import com.wire.bots.don.db.Service;
import com.wire.bots.don.model.Asset;
import com.wire.bots.don.model.AuthToken;
import com.wire.bots.sdk.Logger;
import com.wire.bots.sdk.WireClient;

import java.util.ArrayList;

public class NewServiceCommand extends Command {
    private final int serviceId;

    public NewServiceCommand(WireClient client, String userId, Manager db) throws Exception {
        super(client, userId, db);

        if (!isAuthenticated()) {
            authenticate();
        }

        client.sendText("What should we call this bot?");

        serviceId = db.insertService();
    }

    @Override
    public Command onMessage(WireClient client, String text) throws Exception {
        Service service = db.getService(serviceId);

        if (service.name == null) {
            db.updateService(serviceId, "name", text);
            client.sendText("What is the base url for this bot?");
            return this;
        }

        if (service.url == null) {
            if (!text.toLowerCase().startsWith("https://")) {
                client.sendText("Please, specify valid https url like: https://example.com");
                return this;
            }
            db.updateService(serviceId, "url", text.toLowerCase());

            client.sendText("Write some description for this bot");
            return this;
        }

        if (service.description == null) {
            db.updateService(serviceId, "description", text);
            client.sendText("Paste the URL for the profile picture");
            return this;
        }

        if (service.profile == null) {
            db.updateService(serviceId, "profile", text);
            client.sendText("Paste rsa public key here");
            return this;
        }

        String pubkey = text;
        if (!pubkey.startsWith("-----BEGIN PUBLIC KEY-----")) {
            client.sendText("Please, specify a valid public key");
            return this;
        }

        if (!pubkey.endsWith("-----END PUBLIC KEY-----")) {
            client.sendText("Please, specify a valid public key");
            return this;
        }

        client.sendText("OK. Here we go...");

        try {
            String cookie = getUser().cookie;
            ArrayList<Asset> assets = uploadProfile(cookie, service.profile);

            AuthToken authToken = providerClient.newService(cookie,
                    service.name,
                    service.url,
                    service.description,
                    pubkey,
                    new String[]{"tutorial"},
                    assets);

            String msg = String.format("Success!\nAuthentication token: %s",
                    authToken.auth_token);

            Logger.info(msg);
            client.sendText(msg);
        } catch (Exception e) {
            client.sendText(e.getMessage());
            e.printStackTrace();
        }
        return def();
    }
}