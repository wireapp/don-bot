package com.wire.bots.don.commands;

import com.wire.bots.don.db.Manager;
import com.wire.bots.don.exceptions.*;
import com.wire.bots.sdk.Logger;
import com.wire.bots.sdk.WireClient;

public class DefaultCommand extends Command {
    public DefaultCommand(WireClient client, String userId, Manager db) {
        super(client, userId, db);
    }

    @Override
    public Command onMessage(WireClient client, String text) throws Exception {
        text = text.toLowerCase();

        try {
            if (text.startsWith("update bot")) {
                String serviceName = getParams(text.substring("update bot".length()));
                return new UpdateServiceCommand(client, userId, db, serviceName);
            }

            if (text.startsWith("enable bot")) {
                String serviceName = getParams(text.substring("enable bot".length()));
                new EnableServiceCommand(client, userId, db, serviceName);
                return def();
            }

            if (text.startsWith("show bot")) {
                String botName = getParams(text.substring("show bot".length()));
                new GetBotCommand(client, userId, db, botName);
                return def();
            }

            if (text.startsWith("delete bot")) {
                String botName = getParams(text.substring("delete bot".length()));
                new DeleteBotCommand(client, userId, db, botName);
                return def();
            }

            if (text.startsWith("list my bots")) {
                new ListServicesCommand(client, userId, db);
                return def();
            }

            if (text.startsWith("get self")) {
                new GetSelfCommand(client, userId, db);
                return def();
            }

            if (text.startsWith("register")) {
                return new RegisterCommand(client, userId, db);
            }

            if (text.startsWith("create bot")) {
                return new NewServiceCommand(client, userId, db);
            }

            if (text.startsWith("create public channel")) {
                return new NewChannelCommand(client, userId, db);
            }

            if (text.startsWith("test bot")) {
                String botName = getParams(text.substring("test bot".length()));
                new TestBotCommand(client, userId, db, botName);
                return def();
            }

            if (text.startsWith("search bot")) {
                String botName = getParams(text.substring("search bot".length()));
                new SearchCommand(client, userId, db, botName);
                return def();
            }

            client.sendText("You come to me asking for moar bots.\nBut you don't ask with respect. " +
                    "You don't offer friendship. " +
                    "You don't even think to call me: \"Don\"");
            client.sendText("\nUsage:\nregister\nget self\ncreate bot\nlist my bots" +
                    "\nshow bot <name>\nupdate bot <name>\ndelete bot <name>\nenable bot <name>\ntest bot <name>\n" +
                    "create public channel");
        } catch (NotRegisteredException e) {
            Logger.info(e.getMessage());
            client.sendText("Not registered yet");
        } catch (FailedAuthenticationException e) {
            Logger.info("%s. BotId: %s", e.getMessage(), botId);
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
        } catch (TooManyBotsException e) {
            Logger.warning(e.getMessage());
            client.sendText(e.getMessage());
        } catch (Exception e) {
            String format = String.format("BotId: %s, Error: %s", botId, e.getLocalizedMessage());
            e.printStackTrace();
            client.sendText(format);
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
