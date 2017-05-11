package com.wire.bots.don;

import com.wire.bots.sdk.Configuration;

public class DonConfig extends Configuration {
    private String botName = "Dev Bot";
    private int accentColour;
    private String smallProfile;
    private String bigProfile;
    private String channelHost;
    private String channelSecret;
    private String pathPubKey;
    private String pathAdmin;

    public String getBotName() {
        return botName;
    }

    public int getAccentColour() {
        return accentColour;
    }

    public String getSmallProfile() {
        return smallProfile;
    }

    public String getBigProfile() {
        return bigProfile;
    }

    public String getChannelHost() {
        return channelHost;
    }

    public String getPathPubKey() {
        return pathPubKey;
    }

    public String getPathAdmin() {
        return pathAdmin;
    }

    public String getChannelSecret() {
        return channelSecret;
    }
}
