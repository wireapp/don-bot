package com.wire.bots.don;

import com.wire.bots.don.commands.Command;
import com.wire.bots.don.commands.DefaultCommand;
import com.wire.bots.sdk.Configuration;
import com.wire.bots.sdk.Logger;
import com.wire.bots.sdk.Util;
import com.wire.bots.sdk.WireClient;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: dejankovacevic
 * Date: 24/10/16
 * Time: 22:08
 */
public class Don {
    private final Configuration config;
    private final ConcurrentHashMap<String, Command> commands = new ConcurrentHashMap<>();

    public Don(Configuration config) {
        this.config = config;
    }

    public boolean onNewBot(String bot, String userId, String name) throws IOException {
        File userHome = new File(String.format("%s/don/%s/", config.cryptoDir, userId));
        File botFile = new File(userHome.getAbsolutePath() + "/.bot");

        if (userHome.exists()) {
            String oldBot = botFile.exists() ? Util.readLine(botFile) : "unknown";
            Logger.info(String.format("Multiple bots for user: %s/%s. Original bot: %s",
                    userId,
                    name,
                    oldBot));
            return false; // user already has a conv with Don
        }

        userHome.mkdirs();
        Util.writeLine(bot, botFile);

        File home = new File(String.format("%s/%s/don", config.cryptoDir, bot));
        home.mkdirs();

        write(bot, "user", userId);
        write(bot, "name", name);
        return true;
    }

    public void onMessage(WireClient client, String text) throws Exception {
        String bot = client.getId();
        Command command = commands.get(bot);
        if (command == null) {
            command = new DefaultCommand(client, config);
            commands.put(bot, command);
        }

        commands.put(bot, command.onMessage(client, text));
    }

    private void write(String bot, String name, String value) throws IOException {
        File file = new File(String.format("%s/%s/don/.%s", config.cryptoDir, bot, name));
        Util.writeLine(value, file);
    }

    private String read(String bot, String name) throws IOException {
        File file = new File(String.format("%s/%s/don/.%s", config.cryptoDir, bot, name));
        return Util.readLine(file);
    }

    public void onBotRemoved(String botId) throws IOException {
        String userId = getUserId(botId);
        File userHome = new File(String.format("%s/don/%s/", config.cryptoDir, userId));

        if (userHome.exists()) {
            File botFile = new File(userHome.getAbsolutePath() + "/.bot");
            String oldBot = botFile.exists() ? Util.readLine(botFile) : "unknown";
            if (oldBot.equals(botId)) {
                botFile.delete();
                userHome.delete();
            }
        }
    }

    private String getUserId(String botId) throws IOException {
        return read(botId, "user");
    }
}
