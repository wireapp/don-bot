package com.wire.bots.don;

import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.Server;
import io.dropwizard.setup.Environment;

public class DonService extends Server<DonConfig> {
    public static void main(String[] args) throws Exception {
        new DonService().run(args);
    }

    @Override
    protected MessageHandlerBase createHandler(DonConfig config, Environment env) {
        return new MessageHandler(config, env);
    }
}
