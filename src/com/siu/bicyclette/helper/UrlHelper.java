package com.siu.bicyclette.helper;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public final class UrlHelper {

    private UrlHelper() {
    }

    public static URL getUrl(String urlAsString) {

        URL url;
        try {
            url = new URL(urlAsString);
        } catch (MalformedURLException e) {
            Log.w(UrlHelper.class.getName(), "Invalid format for url : " + urlAsString, e);
            return null;
        }

        return url;
    }
}
