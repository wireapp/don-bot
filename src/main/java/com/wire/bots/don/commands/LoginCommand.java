package com.wire.bots.don.commands;

import com.wire.bots.don.db.Manager;
import com.wire.bots.sdk.Logger;
import com.wire.bots.sdk.WireClient;

public class LoginCommand extends Command {
    private String email;

    public LoginCommand(WireClient client, String userId, Manager db) throws Exception {
        super(client, userId, db);

        client.sendText("Email:");
    }

    @Override
    public Command onMessage(WireClient client, String text) throws Exception {
        if (email == null) {
            email = text.trim();
            client.sendText("Password:");
            return this;
        }

        String password = text.trim();

        try {
            String token = providerClient.login(email, password);

            db.updateCookie(userId, token);

            client.sendText("Token: " + token);
        } catch (Exception e) {
            String msg = String.format("LoginCommand: bot: %s, email: %s, reason: %s",
                    botId, text, e.getMessage());
            Logger.warning(msg);
            client.sendText(e.getMessage());
        }

        return def();
    }
}
