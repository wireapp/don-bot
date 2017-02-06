package com.wire.bots.don.commands;

import com.wire.bots.don.clients.AdminClient;
import com.wire.bots.don.model.Service;
import com.wire.bots.sdk.Configuration;
import com.wire.bots.sdk.Logger;
import com.wire.bots.sdk.Util;
import com.wire.bots.sdk.WireClient;

import java.io.File;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: dejankovacevic
 * Date: 26/10/16
 * Time: 13:22
 */
public class EnableServiceCommand extends Command {
    public EnableServiceCommand(WireClient client, Configuration config, String botName) throws Exception {
        super(client, config);

        if (!isAuthenticated()) {
            authenticate();
        }

        String cookie = readCookie();
        String password = read(botId, "password");

        ArrayList<Service> services = providerClient.listServices(cookie);
        for (Service s : services) {
            if (s.name.compareToIgnoreCase(botName) == 0) {
                boolean b = providerClient.enableService(cookie, password, s.id);
                if (b) {
                    client.sendText("Enabled " + s.name);

                    File file = new File(String.format("%s/don/.admin", config.cryptoDir));
                    String session = Util.readLine(file);

                    String bot = botId;
                    String provider = read(bot, "provider");

                    String clean = s.name.replaceAll("[^A-Za-z0-9]", "");

                    AdminClient adminClient = new AdminClient(session);
                    String link = adminClient.generateInviteLink(clean, provider, s.id, s.description);
                    if (link != null) {
                        String msg = "Users can start using your bot by clicking on this link: " + link;
                        client.sendText(msg);
                        Logger.info(msg);
                    } else {
                        String msg = "Failed to create the invite link :(";
                        client.sendText(msg);
                        Logger.error(msg);
                    }
                } else {
                    String msg = "Failed to enable bot " + s.name;
                    client.sendText(msg);
                    Logger.error(msg);
                }
                return;
            }
        }

        client.sendText("Could not find bot called: " + botName);
    }

    @Override
    public Command onMessage(WireClient client, String text) throws Exception {
        return null;
    }
}
