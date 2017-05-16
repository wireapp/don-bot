package com.wire.bots.don;

import com.codahale.metrics.MetricRegistry;
import com.waz.model.Messages;
import com.wire.bots.sdk.Logger;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.models.TextMessage;
import com.wire.bots.sdk.server.model.NewBot;
import io.dropwizard.setup.Environment;

public class MessageHandler extends MessageHandlerBase {
    private final Don don;
    private final MetricRegistry metrics;
    private final DonConfig config;

    public MessageHandler(DonConfig config, Environment env) {
        this.config = config;
        don = new Don(config);
        metrics = env.metrics();
    }

    @Override
    public String getName() {
        return config.getBotName();
    }

    @Override
    public int getAccentColour() {
        return config.getAccentColour();
    }

    /**
     * @return Asset key for the small profile picture. If NULL is returned the default key will be used
     */
    public String getSmallProfilePicture() {
        return config.getSmallProfile();
    }

    /**
     * @return Asset key for the big profile picture. If NULL is returned the default key will be used
     */
    public String getBigProfilePicture() {
        return config.getBigProfile();
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

    @Override
    public void onEditText(WireClient client, TextMessage msg) {
        onText(client, msg);
    }

    @Override
    public void onEvent(WireClient client, String userId, Messages.GenericMessage genericMessage) {
        if (genericMessage.hasConfirmation()) {
            metrics.meter("engagement.delivery").mark();
        }
        if (genericMessage.hasText()) {
            metrics.meter("engagement.txt.received").mark();
        }
    }
}
