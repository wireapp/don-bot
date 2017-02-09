package com.wire.bots.don.commands;

import com.wire.bots.don.db.Manager;
import com.wire.bots.don.exceptions.UnknownBotException;
import com.wire.bots.don.model.Asset;
import com.wire.bots.don.model.Service;
import com.wire.bots.sdk.Logger;
import com.wire.bots.sdk.WireClient;

import java.io.IOException;
import java.util.ArrayList;

public class UpdateServiceCommand extends Command {
    private static final String PROFILE_PICTURE = "profile picture";
    private static final String PUBKEY = "pubkey";
    private static final String TOKEN = "token";
    private static final String URL = "url";
    private static final String DESCRIPTION = "description";
    private final int id;
    private final String cookie;

    protected UpdateServiceCommand(WireClient client, String userId, Manager db, String serviceName) throws Exception {
        super(client, userId, db);

        id = db.insertService();

        if (!isAuthenticated()) {
            authenticate();
        }

        cookie = getUser().cookie;

        String serviceId = findService(cookie, serviceName);

        if (serviceId == null) {
            throw new UnknownBotException("You don't have a bot called: " + serviceName);
        }

        db.updateService(id, "serviceId", serviceId);
        db.updateService(id, "name", serviceName);

        String txt = String.format("What do you want to change? (%s, %s, %s, %s, %s)?"
                , URL
                , TOKEN
                , PUBKEY
                , PROFILE_PICTURE
                , DESCRIPTION);
        client.sendText(txt);
    }

    @Override
    public Command onMessage(WireClient client, String text) throws Exception {
        com.wire.bots.don.db.Service service = db.getService(id);

        if (service.field == null) {
            service.field = text.toLowerCase();
            if (!(URL + TOKEN + PUBKEY + PROFILE_PICTURE + DESCRIPTION).contains(service.field)) {
                String txt = String.format("It must be one of these: `%s` `%s` `%s` `%s` `%s`"
                        , URL
                        , TOKEN
                        , PUBKEY
                        , PROFILE_PICTURE
                        , DESCRIPTION);
                client.sendText(txt);
                return this;
            }

            db.updateService(id, "field", service.field);
            client.sendText("What should I put there?");
            return this;
        }

        String password = getUser().password;
        String name = service.name;
        String id = service.serviceId;

        String value = text;

        String url = service.field.equals(URL) ? value.toLowerCase() : null;
        String[] tokens = service.field.equals(TOKEN) ? new String[]{value} : null;
        String[] pubkeys = service.field.equals(PUBKEY) ? new String[]{value} : null;
        String description = service.field.equals(DESCRIPTION) ? value : null;

        ArrayList<Asset> assets = null;
        if (PROFILE_PICTURE.contains(service.field)) {
            assets = uploadProfile(cookie, value);
        }

        boolean b = false;
        if (url != null || tokens != null || pubkeys != null) {
            b = providerClient.updateServiceConnection(cookie, password, id, url, tokens, pubkeys, null);
        }

        if (assets != null || description != null) {
            b = providerClient.updateService(cookie, password, id, description, assets);
        }

        if (b) {
            String msg = "Updated bot " + name;
            client.sendText(msg);
            Logger.info(msg);
        } else {
            String msg = "Failed to update bot " + name;
            client.sendText(msg);
            Logger.error(msg);
        }

        return def();
    }

    private String findService(String cookie, String name) throws IOException {
        ArrayList<Service> services = providerClient.listServices(cookie);
        for (Service s : services) {
            if (s.name.compareToIgnoreCase(name) == 0) {
                return s.id;
            }
        }
        return null;
    }
}
