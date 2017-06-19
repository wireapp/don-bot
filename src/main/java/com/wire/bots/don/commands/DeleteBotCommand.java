package com.wire.bots.don.commands;

import com.wire.bots.don.Don;
import com.wire.bots.don.clients.AdminClient;
import com.wire.bots.don.clients.PublicChannelClient;
import com.wire.bots.don.db.Manager;
import com.wire.bots.don.db.User;
import com.wire.bots.don.model.Service;
import com.wire.bots.sdk.Util;
import com.wire.bots.sdk.WireClient;

import java.io.File;
import java.util.ArrayList;

public class DeleteBotCommand extends Command {
    DeleteBotCommand(WireClient client, String userId, Manager db, String botName) throws Exception {
        super(client, userId, db);

        if (!isAuthenticated()) {
            authenticate();
        }

        User user = getUser();
        String cookie = user.cookie;
        String password = user.password;
        ArrayList<Service> services = providerClient.listServices(cookie);
        for (Service s : services) {
            if (s.name.compareToIgnoreCase(botName) == 0) {
                providerClient.deleteService(cookie, password, s.id);

                File file = new File(Don.config.getPathAdmin());
                String admin = Util.readLine(file);

                String clean = s.name.replaceAll("[^A-Za-z0-9]", "");

                AdminClient.deleteLink(clean, admin);

                boolean deleteChannel = PublicChannelClient.deleteChannel(clean, user.id, s.auth_tokens[0]);

                String txt = deleteChannel ? String.format("Deleted channel: **%s**", s.name)
                        : String.format("Deleted bot: **%s**", s.name);
                client.sendText(txt);
                return;
            }
        }

        client.sendText("Could not find " + botName);
    }

    @Override
    public Command onMessage(WireClient client, String text) throws Exception {
        return def();
    }
}
