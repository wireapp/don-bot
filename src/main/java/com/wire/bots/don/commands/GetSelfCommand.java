package com.wire.bots.don.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wire.bots.don.model.Provider;
import com.wire.bots.sdk.WireClient;
import org.skife.jdbi.v2.DBI;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: dejankovacevic
 * Date: 26/10/16
 * Time: 23:38
 */
public class GetSelfCommand extends Command {
    GetSelfCommand(WireClient client, UUID userId, DBI db) throws Exception {
        super(client, userId, db);

        String cookie = getUser().cookie;
        Provider provider = providerClient.getProvider(cookie);
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        client.sendText(mapper.writeValueAsString(provider));
    }

    @Override
    public Command onMessage(WireClient client, String text) {
        return null;
    }
}
