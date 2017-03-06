package com.wire.bots.don.commands;

import com.wire.bots.don.db.Manager;
import com.wire.bots.don.db.User;
import com.wire.bots.don.exceptions.AlreadyRegisteredException;
import com.wire.bots.don.exceptions.FailedRegistrationException;
import com.wire.bots.don.model.Auth;
import com.wire.bots.sdk.Logger;
import com.wire.bots.sdk.WireClient;

public class RegisterCommand extends Command {
    public RegisterCommand(WireClient client, String userId, Manager db) throws Exception {
        super(client, userId, db);

        if (isAuthenticated()) {
            throw new AlreadyRegisteredException(botId);
        }

        client.sendText("What is your email?");
    }

    @Override
    public Command onMessage(WireClient client, String email) throws Exception {
        if (!email.contains("@") || !email.contains(".") || email.length() < 6) {
            client.sendText("Please, specify a valid email address");
            return this;
        }

        User user = getUser();

        // just for debug
        if (user == null) {
            db.insertUser(userId, email);
            user = getUser();
        }

        if (user.email != null && user.email.equals(email)) {
            String msg = String.format("Attempt to register multiple times with the same email address. Bot: %s" +
                            ", email: %s",
                    botId, email);
            Logger.info(msg);
            client.sendText("This email address was already used to register");
            return def();
        }

        try {
            Auth register = providerClient.register(user.name, email, "https://", "You know, for the bots");

            db.updateUser(userId, email, register.password, register.id);

            client.sendText("OK. I sent verification email to: " + email);
        } catch (FailedRegistrationException e) {
            String msg = String.format("FailedRegistrationException: bot: %s, email: %s, reason: %s",
                    botId, email, e.getMessage());
            Logger.info(msg);
            client.sendText(e.getMessage());
        }

        return def();
    }

}
