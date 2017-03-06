package com.wire.bots.don;

import com.wire.bots.sdk.Configuration;

public class DonConfig extends Configuration {
    private String botName;
    private int    accentColour;
    private String smallProfile;
    private String bigProfile;

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

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public void setAccentColour(int accentColour) {
        this.accentColour = accentColour;
    }

    public void setSmallProfile(String smallProfile) {
        this.smallProfile = smallProfile;
    }

    public void setBigProfile(String bigProfile) {
        this.bigProfile = bigProfile;
    }
}
