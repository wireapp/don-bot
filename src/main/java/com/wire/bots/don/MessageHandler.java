package com.wire.bots.don;

import com.waz.model.Messages;
import com.wire.bots.don.DAO.UserDAO;
import com.wire.bots.don.DAO.model.User;
import com.wire.bots.don.commands.Command;
import com.wire.bots.don.commands.DefaultCommand;
import com.wire.xenon.MessageHandlerBase;
import com.wire.xenon.WireClient;
import com.wire.xenon.assets.MessageText;
import com.wire.xenon.backend.models.Member;
import com.wire.xenon.backend.models.NewBot;
import com.wire.xenon.backend.models.SystemMessage;
import com.wire.xenon.models.EditedTextMessage;
import com.wire.xenon.models.TextMessage;
import com.wire.xenon.tools.Logger;
import org.jdbi.v3.core.Jdbi;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MessageHandler extends MessageHandlerBase {
    private final UserDAO userDAO;
    private final Jdbi jdbi;
    private final ConcurrentHashMap<UUID, Command> commands = new ConcurrentHashMap<>(); // <botId, command>

    MessageHandler(Jdbi jdbi) {
        userDAO = jdbi.onDemand(UserDAO.class);
        this.jdbi = jdbi;
    }

    @Override
    public boolean onNewBot(NewBot newBot, String auth) {
        try {
            Logger.info(String.format("onNewBot: botId: %s, user: %s/%s, locale: %s",
                    newBot.id,
                    newBot.origin.id,
                    newBot.origin.name,
                    newBot.locale));

            for (Member member : newBot.conversation.members) {
                if (member.service != null) {
                    Logger.warning("Rejecting NewBot. Provider: %s service: %s",
                            member.service.providerId,
                            member.service.id);
                    return false; // we don't want to be in a conv if other bots are there.
                }
            }

            User user = userDAO.getUser(newBot.origin.id);
            if (user == null)
                userDAO.insertUser(newBot.origin.id, newBot.origin.name);
            return true;
        } catch (Exception e) {
            Logger.error("onNewBot: %s", e);
            return false;
        }
    }

    @Override
    public void onMemberJoin(WireClient client, SystemMessage message) {
        for (UUID userId : message.users) {
            try {
                User user = userDAO.getUser(userId);
                if (user == null) {
                    userDAO.insertUser(userId, client.getUser(userId).name);
                }
            } catch (Exception e) {
                Logger.error("onMemberJoin: %s", e);
            }
        }
    }

    @Override
    public void onNewConversation(WireClient client, SystemMessage message) {
        try {
            MessageText msg = new MessageText("Some day, and that day may never come," +
                    " I will call upon you to do a service for me. But until that day accept these bots as a " +
                    "gift from me to you.");
            client.send(msg);
        } catch (Exception e) {
            Logger.error("onNewConversation: %s", e);
        }
    }

    @Override
    public void onText(WireClient client, TextMessage msg) {
        try {
            UUID bot = client.getId();
            Command command = commands.computeIfAbsent(bot, k -> new DefaultCommand(client, msg.getUserId(), jdbi));

            commands.put(bot, command.onMessage(client, msg.getText()));
        } catch (Exception e) {
            Logger.error("onText: %s", e);
            try {
                client.send(new MessageText(e.getMessage()));
            } catch (Exception e1) {
                Logger.error("sendText: %s", e1);
            }
        }
    }

    @Override
    public void onEvent(WireClient client, UUID userId, Messages.GenericMessage event) {
        try {
            // User clicked on a Poll Button
            if (event.hasButtonAction()) {
                final UUID botId = client.getId();
                final Command command = commands.computeIfAbsent(botId, k -> new DefaultCommand(client, userId, jdbi));

                final Messages.ButtonAction buttonAction = event.getButtonAction();
                final String buttonId = buttonAction.getButtonId();
                final UUID pollId = UUID.fromString(buttonAction.getReferenceMessageId());
                commands.put(botId, command.onChoice(client, pollId, buttonId));
            }
        } catch (Exception e) {
            Logger.error("onEvent: bot: %s, user: %s, msg: %s",
                    client.getId(),
                    userId,
                    event.getMessageId());
        }
    }

    @Override
    public void onEditText(WireClient client, EditedTextMessage msg) {
        onText(client, msg);
    }
}
