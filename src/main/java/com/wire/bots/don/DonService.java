package com.wire.bots.don;

import com.wire.lithium.Server;
import com.wire.xenon.MessageHandlerBase;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class DonService extends Server<DonConfig> {
    public static DonService instance;

    public static void main(String[] args) throws Exception {
        new DonService().run(args);
    }

    @Override
    public void initialize(Bootstrap<DonConfig> bootstrap) {
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));

        instance = (DonService) bootstrap.getApplication();
    }

    @Override
    protected MessageHandlerBase createHandler(DonConfig config, Environment env) {
        return new MessageHandler(jdbi);
    }
}
