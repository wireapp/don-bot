package com.wire.bots.don.commands;

import com.wire.bots.don.DAO.model.User;
import com.wire.bots.don.exceptions.FailedRegistrationException;
import com.wire.bots.don.model.Auth;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.tools.Logger;
import org.skife.jdbi.v2.DBI;

import java.util.UUID;

public class RegisterCommand extends Command {
    private String email;

    RegisterCommand(WireClient client, UUID userId, DBI db) throws Exception {
        super(client, userId, db);

        client.sendText("What is your email?");
    }

    @Override
    public Command onMessage(WireClient client, String text) throws Exception {
        if (email == null) {
            if (!isValidEmail(text)) {
                client.sendText("Please, specify a valid email address");
                return this;
            }

            email = text.trim().toLowerCase();
            client.sendText("Enter your password:");
            return this;
        }

        String password = text.trim();

        User user = getUser();

        if (user.email != null && user.email.equals(email)) {
            String msg = String.format("Attempt to register multiple times with the same email address. Bot: %s" +
                            ", email: %s",
                    botId, email);
            Logger.info(msg);
            client.sendText("This email address has been already used to register");
            return def();
        }

        try {
            String homepage = "https://";
            String desc = "You know, for the bots";
            Auth register = providerClient.register(user.name, email, password, homepage, desc);

            userDAO.updateUser(userId, email, register.id);

            client.sendText("OK. I sent verification email to: " + email);
        } catch (FailedRegistrationException e) {
            client.sendText(e.getMessage());

            String msg = String.format("FailedRegistrationException: bot: %s, email: %s, reason: %s", botId, email, e);
            Logger.warning(msg);
        }

        return def();
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && email.length() >= 6;
    }
}
