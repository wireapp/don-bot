package com.wire.bots.don.commands;

import com.wire.bots.don.DAO.model.Service;
import com.wire.bots.don.exceptions.TooManyBotsException;
import com.wire.bots.don.model.Asset;
import com.wire.bots.don.model.AuthToken;
import com.wire.xenon.WireClient;
import com.wire.xenon.assets.MessageText;
import com.wire.xenon.tools.Logger;
import org.jdbi.v3.core.Jdbi;

import java.util.ArrayList;
import java.util.UUID;

public class NewServiceCommand extends Command {
    private final int serviceId;

    NewServiceCommand(WireClient client, UUID userId, Jdbi db) throws Exception {
        super(client, userId, db);

        ArrayList<com.wire.bots.don.model.Service> services = providerClient.listServices(getUser().cookie);
        if (services.size() >= 20)
            throw new TooManyBotsException("You have too many bots already. Try deleting some that are not in use");

        client.send(new MessageText("What should we call this bot?"));

        serviceId = serviceDAO.insertService(null);
    }

    @Override
    public Command onMessage(WireClient client, String text) throws Exception {
        Service service = serviceDAO.getService(serviceId);

        if (service.name == null) {
            serviceDAO.updateService(serviceId, "name", text);
            client.send(new MessageText("What is the base url for this bot?"));
            return this;
        }

        if (service.url == null) {
            if (!text.toLowerCase().startsWith("https://")) {
                client.send(new MessageText("Please, specify valid https url like: https://example.com"));
                return this;
            }
            serviceDAO.updateService(serviceId, "url", text.toLowerCase());

            client.send(new MessageText("Write some description for this bot"));
            return this;
        }

        if (service.description == null) {
            serviceDAO.updateService(serviceId, "description", text);
            client.send(new MessageText("Paste the URL for the profile picture"));
            return this;
        }

        if (service.profile == null) {
            serviceDAO.updateService(serviceId, "profile", text);
            client.send(new MessageText("Paste rsa public key here"));
            return this;
        }

        String pubkey = text.trim();
        if (!pubkey.startsWith("-----BEGIN PUBLIC KEY-----") || !pubkey.endsWith("-----END PUBLIC KEY-----")) {
            client.send(new MessageText("Please, specify a valid public key"));
            return this;
        }

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

            String msg = String.format("Success!\nAuthentication token: `%s`\nTo enable this bot run: `enable bot %s`",
                    authToken.auth_token,
                    service.name);

            Logger.info(msg);
            client.send(new MessageText(msg));
        } catch (Exception e) {
            client.send(new MessageText(e.getMessage()));
            Logger.error("NewServiceCommand: %s", e);
        }
        return def();
    }
}