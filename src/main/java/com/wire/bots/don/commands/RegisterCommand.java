package com.wire.bots.don.commands;

import com.wire.bots.don.exceptions.AlreadyRegisteredException;
import com.wire.bots.don.exceptions.FailedRegistrationException;
import com.wire.bots.don.model.Auth;
import com.wire.bots.sdk.Configuration;
import com.wire.bots.sdk.Logger;
import com.wire.bots.sdk.WireClient;

/**
 * Created with IntelliJ IDEA.
 * User: dejankovacevic
 * Date: 25/10/16
 * Time: 14:48
 */
public class RegisterCommand extends Command {
    public RegisterCommand(WireClient client, Configuration config) throws Exception {
        super(client, config);

        if (isAuthenticated()) {
            throw new AlreadyRegisteredException(botId);
        }

        client.sendText("What is your email?");
    }

    @Override
    public Command onMessage(WireClient client, String email) throws Exception {
        String bot = botId;

        if (!email.contains("@") || !email.contains(".") || email.length() < 6) {
            client.sendText("Please, specify a valid email address");
            return this;
        }

        String existingEmail = read(bot, "email");
        if (existingEmail != null && existingEmail.equals(email)) {
            String msg = String.format("Attempt to register multiple times with the same email address. Bot: %s" +
                            ", email: %s",
                    bot, email);
            Logger.info(msg);
            client.sendText("This email address was already used to register");
            return def();
        }

        String name = read(bot, "name");

        try {
            Auth register = providerClient.register(name, email, "https://", "You know, for the bots");

            write(bot, "email", email);
            write(bot, "password", register.password);
            write(bot, "provider", register.id);

            client.sendText("OK. I sent verification email to: " + email);
        } catch (FailedRegistrationException e) {
            String msg = String.format("FailedRegistrationException: bot: %s, email: %s, reason: %s",
                    bot, email, e.getMessage());
            Logger.info(msg);
            client.sendText(e.getMessage());
        }

        return def();
    }
}
