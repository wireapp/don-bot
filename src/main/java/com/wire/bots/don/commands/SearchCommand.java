package com.wire.bots.don.commands;

import com.wire.bots.don.db.Manager;
import com.wire.bots.sdk.WireClient;

/**
 * Created with IntelliJ IDEA.
 * User: dejankovacevic
 * Date: 27/10/16
 * Time: 21:46
 */
public class SearchCommand extends Command {
    public SearchCommand(WireClient client, String userId, Manager db, String params) throws Exception {
        super(client, userId, db);

//        String[] split = params.split(" ");
//        SearchClient sc = new SearchClient(client.getToken());
//        ArrayList<Service> services = sc.search(split[0], split[1]);
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.enable(SerializationFeature.INDENT_OUTPUT);
//
//        client.sendText(mapper.writeValueAsString(services));
    }

    @Override
    public Command onMessage(WireClient client, String text) throws Exception {
        return null;
    }
}
