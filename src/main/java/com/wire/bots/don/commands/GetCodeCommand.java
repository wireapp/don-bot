package com.wire.bots.don.commands;

import com.wire.bots.don.db.Database;
import com.wire.bots.don.model.Provider;
import com.wire.bots.don.model.Service;
import com.wire.bots.sdk.WireClient;

import java.io.IOException;
import java.util.ArrayList;

public class GetCodeCommand extends Command {
    GetCodeCommand(WireClient client, String userId, String serviceName, Database db) throws Exception {
        super(client, userId, db);

        String cookie = getUser().cookie;
        Provider provider = providerClient.getProvider(cookie);
        Service service = getService(cookie, serviceName);

        if (service != null)
            client.sendText(String.format("`%s:%s`\nUse this code in Team Settings to whitelist this bot for your team",
                    provider.id,
                    service.id));
        else
            client.sendText("Unknown service");
    }

    private Service getService(String cookie, String serviceName) throws IOException {
        ArrayList<Service> services = providerClient.listServices(cookie);
        for (Service s : services) {
            if (s.name.compareToIgnoreCase(serviceName) == 0) {
                return s;
            }
        }
        return null;
    }

    @Override
    public Command onMessage(WireClient client, String text) {
        return null;
    }
}
