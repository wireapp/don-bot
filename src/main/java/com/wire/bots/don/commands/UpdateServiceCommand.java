package com.wire.bots.don.commands;

import com.wire.bots.don.exceptions.UnknownBotException;
import com.wire.bots.don.model.Asset;
import com.wire.bots.don.model.Service;
import com.wire.bots.sdk.Configuration;
import com.wire.bots.sdk.Logger;
import com.wire.bots.sdk.WireClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: dejankovacevic
 * Date: 26/10/16
 * Time: 11:16
 */
public class UpdateServiceCommand extends Command {
    private static final String PROFILE_PICTURE = "profile picture";
    private static final String PUBKEY = "pubkey";
    private static final String TOKEN = "token";
    private static final String URL = "url";
    private static final String DESCRIPTION = "description";
    private final String base;
    private final String cookie;

    protected UpdateServiceCommand(WireClient client, Configuration config, String name)
            throws Exception {
        super(client, config);

        Random rnd = new Random();
        base = "" + rnd.nextInt();

        if (!isAuthenticated()) {
            authenticate();
        }

        cookie = readCookie();

        String id = findService(cookie, name);

        if (id == null) {
            throw new UnknownBotException("You don't have a bot called: " + name);
        }

        write(botId, base + "id", id);
        write(botId, base + "name", name);

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
        String bot = botId;

        String field = read(bot, base + "field");
        if (field == null) {
            field = text.toLowerCase();
            if (!(URL + TOKEN + PUBKEY + PROFILE_PICTURE + DESCRIPTION).contains(field)) {
                String txt = String.format("It must be one of these: `%s` `%s` `%s` `%s` `%s`"
                        , URL
                        , TOKEN
                        , PUBKEY
                        , PROFILE_PICTURE
                        , DESCRIPTION);
                client.sendText(txt);
                return this;
            }

            write(bot, base + "field", field);
            client.sendText("What should I put there?");
            return this;
        }

        String password = read(botId, "password");
        String name = read(bot, base + "name");
        String id = read(bot, base + "id");

        String value = text;

        String url = field.equals(URL) ? value.toLowerCase() : null;
        String[] tokens = field.equals(TOKEN) ? new String[]{value} : null;
        String[] pubkeys = field.equals(PUBKEY) ? new String[]{value} : null;
        String description = field.equals(DESCRIPTION) ? value : null;

        ArrayList<Asset> assets = null;
        if (PROFILE_PICTURE.contains(field)) {
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
