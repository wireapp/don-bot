package com.wire.bots.don.commands;

import com.wire.bots.don.DAO.model.User;
import com.wire.bots.don.model.Service;
import com.wire.xenon.WireClient;
import com.wire.xenon.assets.MessageText;
import com.wire.xenon.tools.Logger;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class EnableServiceCommand extends Command {
    private final String serviceName;

    EnableServiceCommand(WireClient client, UUID userId, Jdbi jdbi, String serviceName) throws Exception {
        super(client, userId, jdbi);
        this.serviceName = serviceName;

        client.send(new MessageText("Please enter password one more time"));
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
                        client.send(new MessageText(msg));
                        Logger.info(msg);
                    } else {
                        Logger.error("Failed to enable service: %s", s.id);
                        client.send(new MessageText("Failed to enable the service"));
                    }
                } catch (IOException e) {
                    client.send(new MessageText(e.getMessage()));
                    return def();
                }

                return def();
            }
        }

        client.send(new MessageText("Could not find bot called: " + serviceName));
        return def();
    }
}
