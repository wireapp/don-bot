package com.wire.bots.don.commands;

import com.wire.bots.don.clients.ProviderClient;
import com.wire.bots.don.db.Database;
import com.wire.bots.don.db.User;
import com.wire.bots.don.model.Asset;
import com.wire.bots.don.processing.ImageLoader;
import com.wire.bots.don.processing.ImageProcessor;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.assets.Picture;
import com.wire.bots.sdk.tools.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public abstract class Command {
    protected final WireClient client;
    protected final UUID userId;
    final UUID botId;
    protected final Database db;
    protected static ProviderClient providerClient = new ProviderClient();

    protected Command(WireClient client, UUID userId, Database db) {
        this.client = client;
        this.userId = userId;
        botId = client.getId();
        this.db = db;
    }

    public abstract Command onMessage(WireClient client, String text) throws Exception;

    Command def() {
        return new DefaultCommand(client, userId, db);
    }

    protected User getUser() throws Exception {
        return db.getUser(userId);
    }

    void deleteCookie() throws SQLException {
        db.deleteCookie(userId);
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
