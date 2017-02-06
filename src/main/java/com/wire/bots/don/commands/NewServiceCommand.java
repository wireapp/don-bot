package com.wire.bots.don.commands;

import com.wire.bots.don.model.Asset;
import com.wire.bots.don.model.AuthToken;
import com.wire.bots.sdk.Configuration;
import com.wire.bots.sdk.Logger;
import com.wire.bots.sdk.WireClient;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: dejankovacevic
 * Date: 25/10/16
 * Time: 15:24
 */
public class NewServiceCommand extends Command {
    private final String base;

    public NewServiceCommand(WireClient client, Configuration config) throws Exception {
        super(client, config);

        if (!isAuthenticated()) {
            authenticate();
        }

        client.sendText("What should we call this bot?");

        Random rnd = new Random();
        base = "" + rnd.nextInt();
    }

    @Override
    public Command onMessage(WireClient client, String text) throws Exception {
        String bot = botId;

        String cookie = readCookie();

        String name = read(bot, base + "name");
        if (name == null) {
            write(bot, base + "name", text);
            client.sendText("What is the base url for this bot?");
            return this;
        }

        String url = read(bot, base + "url");
        if (url == null) {
            if (!text.toLowerCase().startsWith("https://")) {
                client.sendText("Please, specify valid https url like: https://example.com");
                return this;
            }
            write(bot, base + "url", text.toLowerCase());
            client.sendText("Write some description for this bot");
            return this;
        }

        String desc = read(bot, base + "description");
        if (desc == null) {
            write(bot, base + "description", text);
            client.sendText("Paste the URL for the profile picture");
            return this;
        }

        String profile = read(bot, base + "profile");
        if (profile == null) {
            write(bot, base + "profile", text);
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
            ArrayList<Asset> assets = uploadProfile(cookie, profile);

            AuthToken authToken = providerClient.newService(cookie,
                    name,
                    url,
                    desc,
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