package com.wire.bots.don.commands;

import com.wire.bots.don.clients.ProviderClient;
import com.wire.bots.don.exceptions.FailedAuthenticationException;
import com.wire.bots.don.exceptions.NotRegisteredException;
import com.wire.bots.don.model.Asset;
import com.wire.bots.don.processing.ImageLoader;
import com.wire.bots.don.processing.ImageProcessor;
import com.wire.bots.sdk.Configuration;
import com.wire.bots.sdk.Logger;
import com.wire.bots.sdk.Util;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.assets.Picture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: dejankovacevic
 * Date: 25/10/16
 * Time: 14:39
 */
public abstract class Command {
    protected final String botId;
    protected final WireClient client;
    protected final Configuration config;
    protected final ProviderClient providerClient;

    protected Command(WireClient client, Configuration config) {
        this.client = client;
        this.config = config;
        this.botId = client.getId();
        providerClient = new ProviderClient();
    }

    public abstract Command onMessage(WireClient client, String text) throws Exception;

    protected void write(String bot, String name, String value) throws IOException {
        File file = new File(String.format("%s/%s/don/.%s", config.cryptoDir, bot, name));
        if (!file.exists())
            file.createNewFile();
        Util.writeLine(value, file);
    }

    protected String read(String bot, String name) throws IOException {
        File file = new File(String.format("%s/%s/don/.%s", config.cryptoDir, bot, name));
        return file.exists() ? Util.readLine(file) : null;
    }

    protected String readCookie() throws IOException {
        return read(botId, "cookie");
    }

    protected Command def() {
        return new DefaultCommand(client, config);
    }

    protected boolean isAuthenticated() throws IOException {
        return read(botId, "cookie") != null;
    }

    protected boolean isRegistered() throws IOException {
        return read(botId, "email") != null;
    }

    protected void authenticate() throws IOException, NotRegisteredException, FailedAuthenticationException {
        String bot = botId;
        String password = read(bot, "password");
        String email = read(bot, "email");
        if (password != null && email != null) {
            String cookie = providerClient.authenticate(email, password);
            write(bot, "cookie", cookie);
        } else {
            throw new NotRegisteredException(bot);
        }
    }

    protected void deleteCookie() {
        File file = new File(String.format("%s/%s/don/.%s", config.cryptoDir, botId, "cookie"));
        file.delete();
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
