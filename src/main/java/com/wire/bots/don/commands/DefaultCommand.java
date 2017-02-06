package com.wire.bots.don.commands;

import com.wire.bots.don.exceptions.*;
import com.wire.bots.sdk.Configuration;
import com.wire.bots.sdk.Logger;
import com.wire.bots.sdk.WireClient;

/**
 * Created with IntelliJ IDEA.
 * User: dejankovacevic
 * Date: 25/10/16
 * Time: 14:43
 */
public class DefaultCommand extends Command {
    public DefaultCommand(WireClient client, Configuration config) {
        super(client, config);
    }

    @Override
    public Command onMessage(WireClient client, String text) throws Exception {
        text = text.toLowerCase();

        try {
            if (text.startsWith("update bot")) {
                String botName = getParams(text.substring("update bot".length()));
                return new UpdateServiceCommand(client, config, botName);
            }

            if (text.startsWith("enable bot")) {
                String botName = getParams(text.substring("enable bot".length()));
                new EnableServiceCommand(client, config, botName);
                return def();
            }

            if (text.startsWith("show bot")) {
                String botName = getParams(text.substring("show bot".length()));
                new GetBotCommand(client, config, botName);
                return def();
            }

            if (text.startsWith("delete bot")) {
                String botName = getParams(text.substring("delete bot".length()));
                new DeleteBotCommand(client, config, botName);
                return def();
            }

            if (text.startsWith("list my bots")) {
                new ListServicesCommand(client, config);
                return def();
            }

            if (text.startsWith("get self")) {
                new GetSelfCommand(client, config);
                return def();
            }

            if (text.startsWith("register")) {
                return new RegisterCommand(client, config);
            }

            if (text.startsWith("create bot")) {
                return new NewServiceCommand(client, config);
            }

            if (text.startsWith("search bot")) {
                String botName = getParams(text.substring("search bot".length()));
                new SearchCommand(client, config, botName);
                return def();
            }

            client.sendText("You come to me asking for moar bots.\nBut you don't ask with respect. " +
                    "You don't offer friendship. " +
                    "You don't even think to call me: \"Don\"");
            client.sendText("\nUsage:\nregister\nget self\ncreate bot\nlist my bots" +
                    "\nshow bot <name>\nupdate bot <name>\ndelete bot <name>\nenable bot <name>");
        } catch (NotRegisteredException e) {
            Logger.info(e.getMessage());
            client.sendText("Not registered yet");
        } catch (FailedAuthenticationException e) {
            Logger.info(e.getMessage() + ". BotId: " + botId);
            client.sendText("Authentication failed");
        } catch (AlreadyRegisteredException e) {
            Logger.info(e.getMessage());
            client.sendText("You are already registered");
        } catch (MissingBotNameException e) {
            Logger.info(e.getMessage());
            client.sendText("Missing bot name");
        } catch (UnknownBotException e) {
            Logger.info(e.getMessage());
            client.sendText(e.getMessage());
        } catch (Exception e) {
            Logger.info(e.getMessage() + ". BotId: " + botId);
            client.sendText("Something went terribly wrong. Please try again.\n" + e.getMessage());
            deleteCookie();
        }
        return def();
    }

    private String getParams(String name) throws MissingBotNameException {
        name = name.trim();
        if (name.isEmpty())
            throw new MissingBotNameException();
        return name;
    }
}
