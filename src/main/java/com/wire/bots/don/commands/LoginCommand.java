package com.wire.bots.don.commands;

import com.wire.bots.don.db.Database;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.tools.Logger;

public class LoginCommand extends Command {
    private String email;

    LoginCommand(WireClient client, String userId, Database db) throws Exception {
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
            client.sendText("Token: " + token);

            db.updateCookie(userId, token);
        } catch (Exception e) {
            String msg = String.format("LoginCommand: bot: %s, email: %s, reason: %s",
                    botId, email, e.getMessage());
            Logger.warning(msg);
            client.sendText(e.getMessage());
        }

        return def();
    }
}
