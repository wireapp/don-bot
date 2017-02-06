package com.wire.bots.don.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wire.bots.don.model.Provider;
import com.wire.bots.sdk.Configuration;
import com.wire.bots.sdk.WireClient;

/**
 * Created with IntelliJ IDEA.
 * User: dejankovacevic
 * Date: 26/10/16
 * Time: 23:38
 */
public class GetSelfCommand extends Command {
    public GetSelfCommand(WireClient client, Configuration config) throws Exception {
        super(client, config);

        if (!isAuthenticated()) {
            authenticate();
        }

        String cookie = readCookie();
        Provider provider = providerClient.getProvider(cookie);
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        client.sendText(mapper.writeValueAsString(provider));
    }


    @Override
    public Command onMessage(WireClient client, String text) throws Exception {
        return null;
    }
}
