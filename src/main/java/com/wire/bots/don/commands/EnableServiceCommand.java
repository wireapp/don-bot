package com.wire.bots.don.commands;

import com.wire.bots.don.db.Database;
import com.wire.bots.don.db.User;
import com.wire.bots.don.model.Service;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.tools.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class EnableServiceCommand extends Command {
    private final String serviceName;

    EnableServiceCommand(WireClient client, UUID userId, Database db, String serviceName) throws Exception {
        super(client, userId, db);
        this.serviceName = serviceName;

        client.sendText("Please enter password one more time");
    }

    @Override
    public Command onMessage(WireClient client, String password) throws Exception {
        User user = getUser();
        String cookie = user.cookie;

        ArrayList<Service> services = providerClient.listServices(cookie);
        for (Service s : services) {
            if (s.name.compareToIgnoreCase(serviceName) == 0) {
                try {
                    boolean enableService = providerClient.enableService(cookie, password, s.id);
                    if (enableService) {
                        String msg = String.format("Service was enabled. Service code:\n`%s:%s`", user.provider, s.id);
                        client.sendText(msg);
                        Logger.info(msg);
                    } else {
                        Logger.error("Failed to enable service: %s", s.id);
                        client.sendText("Failed to enable the service");
                    }
                } catch (IOException e) {
                    client.sendText(e.getMessage());
                    return def();
                }

                return def();
            }
        }

        client.sendText("Could not find bot called: " + serviceName);
        return def();
    }
}
