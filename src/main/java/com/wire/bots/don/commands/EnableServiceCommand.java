package com.wire.bots.don.commands;

import com.wire.bots.don.Util;
import com.wire.bots.don.db.Database;
import com.wire.bots.don.db.User;
import com.wire.bots.don.model.Service;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.tools.Logger;

import java.io.IOException;
import java.util.ArrayList;

public class EnableServiceCommand extends Command {
    private final String serviceName;

    EnableServiceCommand(WireClient client, String userId, Database db, String serviceName) throws Exception {
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
                    providerClient.enableService(cookie, password, s.id);

                    String link = Util.getInviteLink(s.name, user.provider, s.id);
                    String msg = "Users can start using your bot by clicking on this link: " + link;
                    client.sendText(msg);
                    Logger.info(msg);
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
