package com.wire.bots.don.commands;

import com.wire.bots.don.db.Database;
import com.wire.bots.don.db.User;
import com.wire.bots.don.model.Service;
import com.wire.bots.sdk.WireClient;

import java.util.ArrayList;

public class DeleteBotCommand extends Command {
    private String password;

    DeleteBotCommand(WireClient client, String userId, Database db) throws Exception {
        super(client, userId, db);

        client.sendText("Please enter password one more time:");
    }

    @Override
    public Command onMessage(WireClient client, String text) throws Exception {
        if (password == null) {
            password = text.trim();
            client.sendText("Bot name:");
            return this;
        }

        User user = getUser();
        String cookie = user.cookie;
        ArrayList<Service> services = providerClient.listServices(cookie);
        for (Service s : services) {
            if (s.name.compareToIgnoreCase(text) == 0) {
                providerClient.deleteService(cookie, password, s.id);

                boolean deleteChannel = false;//PublicChannelClient.deleteChannel(s.name, user.id, s.auth_tokens[0]);

                String txt = deleteChannel ? String.format("Deleted channel: **%s**", s.name)
                        : String.format("Deleted bot: **%s**", s.name);
                client.sendText(txt);
                return def();
            }
        }

        client.sendText("Could not find: " + text);
        return def();
    }
}
