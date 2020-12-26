package com.wire.bots.don.commands;

import com.wire.bots.don.DAO.model.User;
import com.wire.bots.don.model.Service;
import com.wire.xenon.WireClient;
import com.wire.xenon.assets.MessageText;
import org.jdbi.v3.core.Jdbi;

import java.util.ArrayList;
import java.util.UUID;

public class DeleteBotCommand extends Command {
    private String password;

    DeleteBotCommand(WireClient client, UUID userId, Jdbi jdbi) throws Exception {
        super(client, userId, jdbi);

        client.send(new MessageText("Please enter password one more time:"));
    }

    @Override
    public Command onMessage(WireClient client, String text) throws Exception {
        if (password == null) {
            password = text.trim();
            client.send(new MessageText("Bot name:"));
            return this;
        }

        User user = getUser();
        String cookie = user.cookie;
        ArrayList<Service> services = providerClient.listServices(cookie);
        for (Service s : services) {
            if (s.name.compareToIgnoreCase(text) == 0) {
                providerClient.deleteService(cookie, password, s.id);

                String txt = String.format("Deleted bot: **%s**", s.name);
                client.send(new MessageText(txt));
                return def();
            }
        }

        client.send(new MessageText("Could not find: " + text));
        return def();
    }
}
