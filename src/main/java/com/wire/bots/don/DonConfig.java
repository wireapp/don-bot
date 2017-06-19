package com.wire.bots.don;

import com.wire.bots.sdk.Configuration;

public class DonConfig extends Configuration {
    private String botName = "Dev Bot";
    private int accentColour;
    private String smallProfile;
    private String bigProfile;
    private String pathPubKey;
    private String pathAdmin;
    private Channel channel;

    public Channel getChannel() {
        return channel;
    }

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

    public String getPathPubKey() {
        return pathPubKey;
    }

    public String getPathAdmin() {
        return pathAdmin;
    }

    public static class Channel {
        private String channelUrl;
        private String secret;
        private String publicIP;

        public String getChannelUrl() {
            return channelUrl;
        }

        public String getSecret() {
            return secret;
        }

        public String getPublicIP() {
            return publicIP;
        }
    }
}
