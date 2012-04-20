package com.siu.bicyclette.util;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public final class UrlUtils {

    private UrlUtils() {
    }

    public static URL getUrl(String urlAsString) {

        URL url;
        try {
            url = new URL(urlAsString);
        } catch (MalformedURLException e) {
            Log.w(UrlUtils.class.getName(), "Invalid format for url : " + urlAsString, e);
            return null;
        }

        return url;
    }
}
