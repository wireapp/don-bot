//
// Wire
// Copyright (C) 2016 Wire Swiss GmbH
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see http://www.gnu.org/licenses/.
//

package com.wire.bots.don.processing;

import com.wire.bots.sdk.Util;
import com.wire.bots.sdk.assets.Picture;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ImageLoader {
    public static Picture loadImage(String path) throws Exception {
        try (InputStream fis = getStream(path)) {
            String mime = getMimeType(path);
            return loadImage(fis, mime);
        }
    }

    public static Picture loadImage(InputStream is, String mime) throws Exception {
        byte[] bytes = Util.toByteArray(is);
        return new Picture(bytes, mime);
    }

    private static InputStream getStream(String path) throws IOException {
        if (path.startsWith("http"))
            return new BufferedInputStream(new URL(path).openStream());
        else
            return new FileInputStream(path);
    }

    private static String getMimeType(String path) throws IOException {
        if (path.endsWith(".jpg"))
            return "image/jpeg";
        if (path.endsWith(".jpeg"))
            return "image/jpeg";
        if (path.endsWith(".gif"))
            return "image/gif";
        if (path.endsWith(".png"))
            return "image/png";

        throw new IOException("Unsupported mime type for: " + path);
    }
}
