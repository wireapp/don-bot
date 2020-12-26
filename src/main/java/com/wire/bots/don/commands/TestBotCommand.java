package com.wire.bots.don.commands;

import com.wire.bots.don.DAO.model.User;
import com.wire.bots.don.clients.SslClient;
import com.wire.bots.don.model.Service;
import com.wire.xenon.WireClient;
import com.wire.xenon.assets.MessageText;
import org.jdbi.v3.core.Jdbi;

import java.util.ArrayList;
import java.util.UUID;

public class TestBotCommand extends Command {
    TestBotCommand(WireClient client, UUID userId, Jdbi db, String botName) throws Exception {
        super(client, userId, db);

        User user = getUser();
        String cookie = user.cookie;
        ArrayList<Service> services = providerClient.listServices(cookie);
        for (Service s : services) {
            if (s.name.compareToIgnoreCase(botName) == 0) {
                final String pem = s.public_keys[0].pem;
                try (SslClient sslClient = new SslClient(pem)) {
                    String response = sslClient.testService(s);
                    client.send(new MessageText(response));
                    return;
                }
            }
        }

        client.send(new MessageText("Could not find " + botName));
    }

    @Override
    public Command onMessage(WireClient client, String text) {
        return def();
    }
}
