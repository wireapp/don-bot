package com.wire.bots.don.commands;

import com.wire.bots.don.Don;
import com.wire.bots.don.clients.AdminClient;
import com.wire.bots.don.clients.PublicChannelClient;
import com.wire.bots.don.db.Manager;
import com.wire.bots.don.db.User;
import com.wire.bots.don.model.Asset;
import com.wire.bots.don.model.AuthToken;
import com.wire.bots.sdk.Logger;
import com.wire.bots.sdk.Util;
import com.wire.bots.sdk.WireClient;

import java.io.File;
import java.util.ArrayList;

public class NewChannelCommand extends Command {
    public NewChannelCommand(WireClient client, String userId, Manager db) throws Exception {
        super(client, userId, db);

        if (!isAuthenticated()) {
            authenticate();
        }

        client.sendText("What should we call this channel?");
    }

    @Override
    public Command onMessage(WireClient client, String text) throws Exception {
        String name = text.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
        String description = "Public channel";

        try {
            User user = getUser();
            String cookie = user.cookie;

            String url = String.format("https://%s:443/channels/%s", Don.config.getChannelHost(), name);
            String pubkey = Util.readFile(new File(Don.config.getPathPubKey()));

            Logger.info("Registering public channel: %s", url);

            String profile = "https://i2.wp.com/www.davidebowman.com/wp-content/uploads/2011/01/radio-tower.gif";
            ArrayList<Asset> assets = uploadProfile(cookie, profile);

            AuthToken result = providerClient.newService(cookie,
                    name,
                    url,
                    description,
                    pubkey,
                    new String[]{"tutorial"},
                    assets);

            Logger.info("Public channel: id: %s, token: %s", result.id, result.auth_token);

            boolean b = PublicChannelClient.setToken(name, user.id, result.auth_token);
            if (!b) {
                client.sendText("Failed to create a public channel with that name");
                providerClient.deleteService(cookie, user.password, result.id);
                return def();
            }

            providerClient.enableService(cookie, user.password, result.id);

            File file = new File(Don.config.getPathAdmin());
            String admin = Util.readLine(file);

            String link = AdminClient.generateInviteLink(name, user.provider, result.id, description, admin);
            if (link != null) {
                String msg = String.format("Public channel: `%s` created. Click here: %s to subscribe.",
                        name,
                        link);
                client.sendText(msg);
            } else {
                String msg = "Failed to create the invite link :(";
                client.sendText(msg);
                Logger.error(msg);
            }
        } catch (Exception e) {
            client.sendText(e.getMessage());
            e.printStackTrace();
        }
        return def();
    }
}
