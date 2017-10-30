package com.wire.bots.don;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Util {
    public static String getInviteLink(String convName, String providerId, String serviceId) throws UnsupportedEncodingException {
        return String.format("https://app.wire.com?bot_name=%s&bot_provider=%s&bot_service=%s",
                URLEncoder.encode(convName, "UTF-8"),
                providerId,
                serviceId);
    }
}
