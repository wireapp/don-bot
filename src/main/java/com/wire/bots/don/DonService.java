package com.wire.bots.don;

import com.wire.bots.sdk.Configuration;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.Server;
import io.dropwizard.setup.Environment;

public class DonService extends Server<Configuration> {
    public static void main(String[] args) throws Exception {
        new DonService().run(args);
    }

    @Override
    protected MessageHandlerBase createHandler(Configuration config) {
        return new MessageHandler(config);
    }

    @Override
    protected void onRun(Configuration config, Environment env) {
        addTask(new ImportSqlV1(config), env);
    }
}
