package com.wire.bots.don.commands;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wire.bots.don.model.Service;
import com.wire.xenon.WireClient;
import com.wire.xenon.assets.MessageText;
import org.jdbi.v3.core.Jdbi;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: dejankovacevic
 * Date: 25/10/16
 * Time: 17:21
 */
public class GetBotCommand extends Command {
    GetBotCommand(WireClient client, UUID userId, Jdbi db, String botName) throws Exception {
        super(client, userId, db);

        String cookie = getUser().cookie;
        ArrayList<Service> services = providerClient.listServices(cookie);
        for (Service s : services) {
            if (s.name.compareToIgnoreCase(botName) == 0) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                mapper.enable(SerializationFeature.INDENT_OUTPUT);

                client.send(new MessageText(mapper.writeValueAsString(s)));
                return;
            }
        }

        client.send(new MessageText("Could not find " + botName));
    }

    @Override
    public Command onMessage(WireClient client, String text) {
        return def();
    }
}
