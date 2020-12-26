package com.wire.bots.don.commands;

import com.wire.bots.don.DAO.ServiceDAO;
import com.wire.bots.don.DAO.UserDAO;
import com.wire.bots.don.DAO.model.User;
import com.wire.bots.don.clients.ProviderClient;
import com.wire.bots.don.model.Asset;
import com.wire.bots.don.processing.ImageLoader;
import com.wire.bots.don.processing.ImageProcessor;
import com.wire.xenon.WireClient;
import com.wire.xenon.assets.Picture;
import com.wire.xenon.tools.Logger;
import org.jdbi.v3.core.Jdbi;

import java.util.ArrayList;
import java.util.UUID;

public abstract class Command {
    protected final WireClient client;
    protected final UUID userId;
    final UUID botId;
    final UserDAO userDAO;
    final ServiceDAO serviceDAO;
    protected final Jdbi db;
    protected static ProviderClient providerClient = new ProviderClient();


    protected Command(WireClient client, UUID userId, Jdbi db) {
        this.client = client;
        this.userId = userId;
        this.db = db;
        this.botId = client.getId();
        userDAO = db.onDemand(UserDAO.class);
        serviceDAO = db.onDemand(ServiceDAO.class);
    }

    public abstract Command onMessage(WireClient client, String text) throws Exception;

    Command def() {
        return new DefaultCommand(client, userId, db);
    }

    protected User getUser() {
        return userDAO.getUser(userId);
    }

    void deleteCookie() {
        userDAO.deleteCookie(userId);
    }

    ArrayList<Asset> uploadProfile(String cookie, String path) {
        ArrayList<Asset> ret = new ArrayList<>();
        try {
            Picture pic = ImageLoader.loadImage(path);
            pic.setRetention("eternal");

            Picture mediumImage = ImageProcessor.getMediumImage(pic);
            Picture smallImage = ImageProcessor.getSmallImage(pic);

            Asset asset = new Asset();
            asset.key = providerClient.uploadProfilePicture(cookie, smallImage);
            asset.size = "preview";
            ret.add(asset);

            asset = new Asset();
            asset.key = providerClient.uploadProfilePicture(cookie, mediumImage);
            asset.size = "complete";
            ret.add(asset);

        } catch (Exception e) {
            Logger.info("Failed to set the profile pic: " + e.getMessage());
        }
        return ret;
    }
}
