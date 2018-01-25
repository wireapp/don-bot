package com.wire.bots.don;

import com.codahale.metrics.MetricRegistry;
import com.waz.model.Messages;
import com.wire.bots.sdk.Logger;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.models.TextMessage;
import com.wire.bots.sdk.server.model.Member;
import com.wire.bots.sdk.server.model.NewBot;
import io.dropwizard.setup.Environment;

public class MessageHandler extends MessageHandlerBase {
    private final Don don;
    private final MetricRegistry metrics;

    MessageHandler(DonConfig config, Environment env) {
        don = new Don(config);
        metrics = env.metrics();
    }

    @Override
    public boolean onNewBot(NewBot newBot) {
        try {
            Logger.info(String.format("onNewBot: botId: %s, user: %s/%s, locale: %s",
                    newBot.id,
                    newBot.origin.id,
                    newBot.origin.name,
                    newBot.locale));

            for (Member member : newBot.conversation.members) {
                if (member.service != null) {
                    Logger.warning("Rejecting NewBot. Provider: %s service: %s",
                            member.service.provider,
                            member.service.id);
                    return false; // we don't want to be in a conv if other bots are there.
                }
            }

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
