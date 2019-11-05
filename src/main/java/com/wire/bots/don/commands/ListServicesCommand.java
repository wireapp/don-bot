package com.wire.bots.don.commands;

import com.wire.bots.don.db.Database;
import com.wire.bots.don.model.Service;
import com.wire.bots.sdk.WireClient;

import java.util.ArrayList;
import java.util.UUID;

public class ListServicesCommand extends Command {
    ListServicesCommand(WireClient client, UUID userId, Database db) throws Exception {
        super(client, userId, db);

        ArrayList<Service> services = providerClient.listServices(getUser().cookie);
        if (services.isEmpty()) {
            client.sendText("You have no bots yet :(. Type: `create bot` to create your first bot");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (Service s : services) {
            sb.append(String.format("**%s**", s.name));
            sb.append(String.format(" : %s", s.description));
            sb.append("\n");
        }
        client.sendText(sb.toString());
    }

    @Override
    public Command onMessage(WireClient client, String text) {
        return def();
    }
}
