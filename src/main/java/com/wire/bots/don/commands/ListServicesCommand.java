package com.wire.bots.don.commands;

import com.wire.bots.don.model.Service;
import com.wire.bots.sdk.Configuration;
import com.wire.bots.sdk.WireClient;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: dejankovacevic
 * Date: 25/10/16
 * Time: 15:25
 */
public class ListServicesCommand extends Command {
    public ListServicesCommand(WireClient client, Configuration config) throws Exception {
        super(client, config);

        if (!isAuthenticated()) {
            authenticate();
        }

        String cookie = readCookie();

        ArrayList<Service> services = providerClient.listServices(cookie);
        if (services.isEmpty())
            client.sendText("You have no bots yet :(. Type: `create bot` to create your first bot");

        for (Service s : services)
            client.sendText(String.format("name: %s, description: %s", s.name, s.description));
    }

    @Override
    public Command onMessage(WireClient client, String text) {
        return def();
    }
}
