package com.wire.bots.don.commands;

import com.wire.bots.don.db.Database;
import com.wire.bots.don.exceptions.*;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.tools.Logger;

public class DefaultCommand extends Command {
    public DefaultCommand(WireClient client, String userId, Database db) {
        super(client, userId, db);
    }

    @Override
    public Command onMessage(WireClient client, String text) throws Exception {
        text = text.toLowerCase();

        try {
            if (text.startsWith("login")) {
                return new LoginCommand(client, userId, db);
            }

            if (text.startsWith("update bot")) {
                String serviceName = getParams(text.substring("update bot".length()));
                return new UpdateServiceCommand(client, userId, db, serviceName);
            }

            if (text.startsWith("enable bot")) {
                String serviceName = getParams(text.substring("enable bot".length()));
                return new EnableServiceCommand(client, userId, db, serviceName);
            }

            if (text.startsWith("show bot")) {
                String botName = getParams(text.substring("show bot".length()));
                new GetBotCommand(client, userId, db, botName);
                return def();
            }

            if (text.startsWith("delete bot")) {
                return new DeleteBotCommand(client, userId, db);
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

            if (text.startsWith("token")) {
                String botName = getParams(text.substring("token".length()));
                new GetTokenCommand(client, userId, botName, db);
                return def();
            }

            client.sendText("You come to me asking for moar bots.\n" +
                    "But you don't ask with respect. \n" +
                    "You don't offer friendship. \n" +
                    "You don't even think to call me: **Don**\n");
            client.sendText("Here's the usage:\n" +
                    "register\n" +
                    "login\n" +
                    "get self\n" +
                    "create bot\n" +
                    "list my bots\n" +
                    "show bot <name>\n" +
                    "update bot <name>\n" +
                    "delete bot\n" +
                    "enable bot <name>\n" +
                    "token <BotName>\n" +
                    "test bot <name>");
        } catch (NotRegisteredException e) {
            Logger.info(e.getMessage());
            client.sendText("You need to be registered or logged in first");
        } catch (FailedAuthenticationException e) {
            Logger.warning("%s. BotId: %s", e.getMessage(), botId);
            client.sendText("Authentication failed");
        } catch (AlreadyRegisteredException e) {
            Logger.info(e.getMessage());
            client.sendText("You are already registered");
        } catch (MissingBotNameException e) {
            Logger.info(e.getMessage());
            client.sendText("Missing bot name");
        } catch (UnknownBotException | TooManyBotsException e) {
            Logger.info(e.getMessage());
            client.sendText(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            String format = String.format("Please retry\nError: %s", e.getLocalizedMessage());
            Logger.error("Something went wrong: %s", e);
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
