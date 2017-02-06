package com.wire.bots.don.commands;

import com.wire.bots.don.clients.AdminClient;
import com.wire.bots.don.model.Service;
import com.wire.bots.sdk.Configuration;
import com.wire.bots.sdk.Util;
import com.wire.bots.sdk.WireClient;

import java.io.File;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: dejankovacevic
 * Date: 25/10/16
 * Time: 16:44
 */
public class DeleteBotCommand extends Command {
    public DeleteBotCommand(WireClient client, Configuration config, String botName) throws Exception {
        super(client, config);

        if (!isAuthenticated()) {
            authenticate();
        }

        String cookie = readCookie();
        String password = read(botId, "password");
        ArrayList<Service> services = providerClient.listServices(cookie);
        for (Service s : services) {
            if (s.name.compareToIgnoreCase(botName) == 0) {
                providerClient.deleteService(cookie, password, s.id);

                File file = new File(String.format("%s/don/.admin", config.cryptoDir));
                String session = Util.readLine(file);

                String clean = s.name.replaceAll("[^A-Za-z0-9]", "");

                AdminClient adminClient = new AdminClient(session);
                adminClient.deleteLink(clean);

                client.sendText("Deleted " + s.name);
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
