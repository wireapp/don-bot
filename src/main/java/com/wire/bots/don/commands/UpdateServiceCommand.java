package com.wire.bots.don.commands;

import com.wire.bots.don.exceptions.UnknownBotException;
import com.wire.bots.don.model.Service;
import com.wire.bots.don.model.UpdateService;
import com.wire.xenon.WireClient;
import com.wire.xenon.assets.ButtonActionConfirmation;
import com.wire.xenon.assets.MessageText;
import com.wire.xenon.assets.Poll;
import com.wire.xenon.tools.Logger;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class UpdateServiceCommand extends Command {
    private static final String PROFILE_PICTURE = "profile picture";
    private static final String PUBKEY = "pubkey";
    private static final String TOKEN = "token";
    private static final String URL = "url";
    private static final String DESCRIPTION = "description";
    private static final String SUMMARY = "summary";
    private static final String SERVICE_NAME = "service name";

    private final int id;
    private final String cookie;
    private String password;

    UpdateServiceCommand(WireClient client, UUID userId, Jdbi db, String serviceName) throws Exception {
        super(client, userId, db);

        cookie = getUser().cookie;

        String serviceId = findService(cookie, serviceName);

        if (serviceId == null) {
            throw new UnknownBotException("You don't have a bot called: " + serviceName);
        }

        id = serviceDAO.insertService(serviceName);
        if (id == -1) {
            throw new RuntimeException("Something went wrong");
        }

        serviceDAO.updateService(id, "serviceId", serviceId);
        serviceDAO.updateService(id, "name", serviceName);

        Logger.debug("UpdateServiceCommand: id: %s, service: %s, name: %s", id, serviceId, serviceName);

        client.send(new MessageText("Please enter password one more time:"));
    }

    @Override
    public Command onChoice(WireClient client, UUID pollId, String buttonId) throws Exception {
        com.wire.bots.don.DAO.model.Service service = serviceDAO.getService(id);

        service.field = buttonId;

        serviceDAO.updateService(id, "field", service.field);

        client.send(new ButtonActionConfirmation(pollId, buttonId));

        client.send(new MessageText("What should I put there?"));

        Logger.debug("UpdateServiceCommand: id: %s, service: %s, field: %s", id, service.id, service.field);

        return this;
    }

    @Override
    public Command onMessage(WireClient client, String text) throws Exception {
        com.wire.bots.don.DAO.model.Service service = serviceDAO.getService(id);

        String value = text.trim();

        if (password == null) {
            password = value;

            Poll poll = new Poll();
            poll.addText("What would you like to change?");
            poll.addButton(URL, "Base URL");
            poll.addButton(TOKEN, "Authentication token");
            poll.addButton(PUBKEY, "Public key");
            poll.addButton(PROFILE_PICTURE, "Profile picture");
            poll.addButton(DESCRIPTION, "Description");
            poll.addButton(SUMMARY, "Summary");
            poll.addButton(SERVICE_NAME, "Service name");

            client.send(poll);
            return this;
        }

        Logger.debug("UpdateServiceCommand: id: %s, service: %s, field: %s, value: %s",
                id,
                service.id,
                service.field,
                value);

        UpdateService updateService = new UpdateService();

        updateService.url = service.field.equalsIgnoreCase(URL) ? value : null;
        updateService.tokens = service.field.equalsIgnoreCase(TOKEN) ? new String[]{value} : null;
        updateService.pubKeys = service.field.equalsIgnoreCase(PUBKEY) ? new String[]{value} : null;
        updateService.description = service.field.equalsIgnoreCase(DESCRIPTION) ? value : null;
        updateService.summary = service.field.equalsIgnoreCase(SUMMARY) ? value : null;
        updateService.name = service.field.equalsIgnoreCase(SERVICE_NAME) ? value : null;

        if (PROFILE_PICTURE.contains(service.field)) {
            updateService.assets = uploadProfile(cookie, value);
        }

        updateService.password = password;

        boolean b;
        if (updateService.url != null || updateService.tokens != null || updateService.pubKeys != null) {
            b = providerClient.updateServiceConnection(cookie, service.serviceId, updateService);
        } else {
            b = providerClient.updateService(cookie, service.serviceId, updateService);
        }

        if (b) {
            String msg = "Updated bot " + service.name;
            client.send(new MessageText(msg));
            Logger.info(msg);
        } else {
            String msg = "Failed to update bot " + service.name;
            client.send(new MessageText(msg));
            Logger.error(msg);
        }

        return def();
    }

    private String findService(String cookie, String name) throws IOException {
        ArrayList<Service> services = providerClient.listServices(cookie);
        for (Service s : services) {
            if (s.name.compareToIgnoreCase(name) == 0) {
                return s.id;
            }
        }
        return null;
    }
}
