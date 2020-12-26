package com.wire.bots.don.commands;

import com.wire.bots.don.model.Service;
import com.wire.xenon.WireClient;
import com.wire.xenon.assets.MessageText;
import org.jdbi.v3.core.Jdbi;

import java.util.ArrayList;
import java.util.UUID;

public class ListServicesCommand extends Command {
    ListServicesCommand(WireClient client, UUID userId, Jdbi db) throws Exception {
        super(client, userId, db);

        ArrayList<Service> services = providerClient.listServices(getUser().cookie);
        if (services.isEmpty()) {
            client.send(new MessageText("You have no bots yet :(. Type: `create bot` to create your first bot"));
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (Service s : services) {
            sb.append(String.format("**%s**", s.name));
            sb.append(String.format(" : %s", s.description));
            sb.append("\n");
        }
        client.send(new MessageText(sb.toString()));
    }

    @Override
    public Command onMessage(WireClient client, String text) {
        return def();
    }
}
