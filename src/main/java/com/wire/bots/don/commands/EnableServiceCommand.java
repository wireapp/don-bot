package com.wire.bots.don.commands;

import com.wire.bots.don.Util;
import com.wire.bots.don.db.Manager;
import com.wire.bots.don.db.User;
import com.wire.bots.don.model.Service;
import com.wire.bots.sdk.Logger;
import com.wire.bots.sdk.WireClient;

import java.util.ArrayList;

public class EnableServiceCommand extends Command {
    EnableServiceCommand(WireClient client, String userId, Manager db, String serviceName) throws Exception {
        super(client, userId, db);

        if (!isAuthenticated()) {
            authenticate();
        }

        User user = getUser();
        String cookie = user.cookie;
        String password = user.password;

        ArrayList<Service> services = providerClient.listServices(cookie);
        for (Service s : services) {
            if (s.name.compareToIgnoreCase(serviceName) == 0) {
                boolean b = providerClient.enableService(cookie, password, s.id);
                if (b) {
                    client.sendText("Enabled " + s.name);

                    String link = Util.getInviteLink(s.name, user.provider, s.id);
                    String msg = "Users can start using your bot by clicking on this link: " + link;
                    client.sendText(msg);
                    Logger.info(msg);
                } else {
                    String msg = "Failed to enable bot " + s.name;
                    client.sendText(msg);
                    Logger.error(msg);
                }
                return;
            }
        }

        client.sendText("Could not find bot called: " + serviceName);
    }

    @Override
    public Command onMessage(WireClient client, String text) throws Exception {
        return null;
    }
}
