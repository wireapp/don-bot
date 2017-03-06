package com.wire.bots.don.commands;

import com.wire.bots.don.Don;
import com.wire.bots.don.clients.AdminClient;
import com.wire.bots.don.clients.SslClient;
import com.wire.bots.don.db.Manager;
import com.wire.bots.don.db.User;
import com.wire.bots.don.model.Service;
import com.wire.bots.sdk.Logger;
import com.wire.bots.sdk.Util;
import com.wire.bots.sdk.WireClient;

import java.io.File;
import java.util.ArrayList;

public class EnableServiceCommand extends Command {
    public EnableServiceCommand(WireClient client, String userId, Manager db, String serviceName) throws Exception {
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

                    File file = new File(String.format("%s/don/.admin", Don.config.cryptoDir));
                    String admin = Util.readLine(file);
                    String clean = s.name.replaceAll("[^A-Za-z0-9]", "");

                    AdminClient adminClient = new AdminClient();
                    String link = adminClient.generateInviteLink(clean, user.provider, s.id, s.description, admin);
                    if (link != null) {
                        String msg = "Users can start using your bot by clicking on this link: " + link;
                        client.sendText(msg);
                        Logger.info(msg);
                    } else {
                        String msg = "Failed to create the invite link :(";
                        client.sendText(msg);
                        Logger.error(msg);
                    }

                    if (!SslClient.verifyService(s)) {
                        String msg = String.format("Failed to query: %s.\nMake sure your service is running and this url is visible",
                                s.url + "/bots/status");
                        client.sendText(msg);
                    }
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
