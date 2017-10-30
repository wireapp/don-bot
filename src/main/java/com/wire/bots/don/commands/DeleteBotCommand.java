package com.wire.bots.don.commands;

import com.wire.bots.don.clients.PublicChannelClient;
import com.wire.bots.don.db.Manager;
import com.wire.bots.don.db.User;
import com.wire.bots.don.model.Service;
import com.wire.bots.sdk.WireClient;

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

                boolean deleteChannel = PublicChannelClient.deleteChannel(s.name, user.id, s.auth_tokens[0]);

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
