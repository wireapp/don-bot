package com.wire.bots.don.commands;

import com.wire.bots.don.clients.SslClient;
import com.wire.bots.don.db.Database;
import com.wire.bots.don.db.User;
import com.wire.bots.don.model.Service;
import com.wire.bots.sdk.WireClient;

import java.util.ArrayList;

public class TestBotCommand extends Command {
    TestBotCommand(WireClient client, String userId, Database db, String botName) throws Exception {
        super(client, userId, db);

        User user = getUser();
        String cookie = user.cookie;
        ArrayList<Service> services = providerClient.listServices(cookie);
        for (Service s : services) {
            if (s.name.compareToIgnoreCase(botName) == 0) {
                try (SslClient sslClient = new SslClient(s.public_keys[0].pem)) {
                    String response = sslClient.testService(s);
                    client.sendText(response);
                    return;
                }
            }
        }

        client.sendText("Could not find " + botName);
    }

    @Override
    public Command onMessage(WireClient client, String text) {
        return def();
    }
}
