package com.wire.bots.don;

import com.wire.bots.sdk.Configuration;
import com.wire.bots.sdk.Logger;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.models.TextMessage;
import com.wire.bots.sdk.server.model.NewBot;

public class MessageHandler extends MessageHandlerBase {
    private final Don don;

    public MessageHandler(Configuration config) {
        don = new Don(config);
    }

    @Override
    public String getName() {
        return "DevBot";
    }

    @Override
    public int getAccentColour() {
        return 1;
    }

    @Override
    public boolean onNewBot(NewBot newBot) {
        try {
            Logger.info(String.format("onNewBot: botId: %s, user: %s/%s, locale: %s",
                    newBot.id,
                    newBot.origin.id,
                    newBot.origin.name,
                    newBot.locale));

            return don.onNewBot(newBot.origin.id, newBot.origin.name);
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
        return false;
    }

    @Override
    public void onNewConversation(WireClient client) {
        try {
            client.sendText("Some day, and that day may never come," +
                    " I will call upon you to do a service for me. But until that day accept these bots as a " +
                    "gift from me to you.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onText(WireClient client, TextMessage msg) {
        try {
            Logger.info(String.format("onText: bot: %s, from: %s. Txt: %s",
                    client.getId(),
                    msg.getUserId(),
                    msg.getText()));

            don.onMessage(client, msg);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                client.sendText(e.getMessage());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
