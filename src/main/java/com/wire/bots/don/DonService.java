package com.wire.bots.don;

import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.Server;
import com.wire.bots.sdk.crypto.CryptoDatabase;
import com.wire.bots.sdk.crypto.storage.RedisStorage;
import com.wire.bots.sdk.factories.CryptoFactory;
import com.wire.bots.sdk.factories.StorageFactory;
import com.wire.bots.sdk.state.RedisState;
import io.dropwizard.setup.Environment;

public class DonService extends Server<DonConfig> {
    public static void main(String[] args) throws Exception {
        new DonService().run(args);
    }

    @Override
    protected MessageHandlerBase createHandler(DonConfig config, Environment env) {
        return new MessageHandler(config);
    }

    @Override
    protected void initialize(DonConfig config, Environment env) {
        env.jersey().setUrlPattern("/don/*");
    }

    /**
     * Instructs the framework to use Storage Service for the state.
     * Remove this override in order to use local File system storage
     *
     * @param config Config
     * @return Storage
     */
    @Override
    protected StorageFactory getStorageFactory(DonConfig config) {
        return botId -> new RedisState(botId, config.db);
    }

    /**
     * Instructs the framework to use Crypto Service for the crypto keys.
     * Remove this override in order to store cryptobox onto your local File system
     *
     * @param config Config
     * @return CryptoFactory
     */
    @Override
    protected CryptoFactory getCryptoFactory(DonConfig config) {
        return (botId) -> {
            RedisStorage storage = new RedisStorage(config.db.host, config.db.port, config.db.password);
            return new CryptoDatabase(botId, storage);
        };
    }
}
