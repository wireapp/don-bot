package com.wire.bots.don.commands;

import com.wire.bots.don.clients.ProviderClient;
import com.wire.bots.don.db.Manager;
import com.wire.bots.don.db.User;
import com.wire.bots.don.exceptions.NotRegisteredException;
import com.wire.bots.don.model.Asset;
import com.wire.bots.don.processing.ImageLoader;
import com.wire.bots.don.processing.ImageProcessor;
import com.wire.bots.sdk.Logger;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.assets.Picture;

import java.sql.SQLException;
import java.util.ArrayList;

public abstract class Command {
    protected final WireClient client;
    protected final String userId;
    protected final String botId;
    protected final Manager db;
    protected static ProviderClient providerClient = new ProviderClient();

    protected Command(WireClient client, String userId, Manager db) {
        this.client = client;
        this.userId = userId;
        botId = client.getId();
        this.db = db;
    }

    public abstract Command onMessage(WireClient client, String text) throws Exception;

    protected Command def() {
        return new DefaultCommand(client, userId, db);
    }

    protected boolean isAuthenticated() throws Exception {
        User user = getUser();
        return user != null && user.cookie != null;
    }

    protected User getUser() throws Exception {
        return db.getUser(userId);
    }

    protected void authenticate() throws Exception {
        User user = getUser();
        if (user.password != null && user.email != null) {
            String cookie = providerClient.authenticate(user.email, user.password);
            int u = db.updateUser(userId, "cookie", cookie);
        } else {
            throw new NotRegisteredException(userId);
        }
    }

    protected void deleteCookie() throws SQLException {
        db.deleteCookie(userId);
    }

    protected ArrayList<Asset> uploadProfile(String cookie, String path) {
        ArrayList<Asset> ret = new ArrayList<>();
        try {
            Picture pic = ImageLoader.loadImage(path);
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

            return ret;
        } catch (Exception e) {
            Logger.info("Failed to set the profile pic: " + e.getMessage());
            return null;
        }
    }
}
