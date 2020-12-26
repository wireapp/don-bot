package com.wire.bots.don.commands;

import com.wire.xenon.WireClient;
import com.wire.xenon.assets.MessageText;
import com.wire.xenon.tools.Logger;
import org.jdbi.v3.core.Jdbi;

import java.util.UUID;

public class LoginCommand extends Command {
    private String email;

    LoginCommand(WireClient client, UUID userId, Jdbi db) throws Exception {
        super(client, userId, db);

        client.send(new MessageText("Email:"));
    }

    @Override
    public Command onMessage(WireClient client, String text) throws Exception {
        if (email == null) {
            email = text.trim();
            client.send(new MessageText("Password:"));
            return this;
        }

        String password = text.trim();

        try {
            String token = providerClient.login(email, password);
            client.send(new MessageText("Token: " + token));

            userDAO.updateCookie(userId, token);
        } catch (Exception e) {
            String msg = String.format("LoginCommand: bot: %s, email: %s, reason: %s",
                    botId, email, e.getMessage());
            Logger.warning(msg);
            client.send(new MessageText(e.getMessage()));
        }

        return def();
    }
}
