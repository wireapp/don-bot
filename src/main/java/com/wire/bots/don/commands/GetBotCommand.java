package com.wire.bots.don.commands;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wire.bots.don.db.Manager;
import com.wire.bots.don.exceptions.NotAuthenticatedException;
import com.wire.bots.don.model.Service;
import com.wire.bots.sdk.WireClient;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: dejankovacevic
 * Date: 25/10/16
 * Time: 17:21
 */
public class GetBotCommand extends Command {
    GetBotCommand(WireClient client, String userId, Manager db, String botName) throws Exception {
        super(client, userId, db);

        if (!isAuthenticated()) {
            throw new NotAuthenticatedException();
        }

        String cookie = getUser().cookie;
        ArrayList<Service> services = providerClient.listServices(cookie);
        for (Service s : services) {
            if (s.name.compareToIgnoreCase(botName) == 0) {
                //Service service = be.getService(cookie, s.id);
                ObjectMapper mapper = new ObjectMapper();
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                mapper.enable(SerializationFeature.INDENT_OUTPUT);

                client.sendText(mapper.writeValueAsString(s));
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
