package com.siu.bicyclette.ws;

import android.util.Log;
import com.siu.bicyclette.helper.ByteHelper;
import com.siu.bicyclette.helper.UrlHelper;
import com.siu.bicyclette.model.StationStatus;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class StationStatusWS {

    private static final String URL = "http://bicyclette.indri.fr/api";
    private static final int SIZE = 4 * 1201;

    public List<StationStatus> getStationsStatus() {

        URL url = UrlHelper.getUrl(URL);

        byte[] bytes = new byte[SIZE];

        BufferedInputStream bis = null;
        HttpURLConnection urlConnection = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(15 * 1000);

            Log.d(getClass().getName(), "Connection opened to : " + URL);

            long time = System.currentTimeMillis();

            bis = new BufferedInputStream(urlConnection.getInputStream());
            bis.read(bytes);

            Log.d(getClass().getName(), "Content downloaded and parsed in " + (System.currentTimeMillis() - time) + " ms");

        } catch (IOException e) {
            Log.w(getClass().getName(), "Error during reading connection stream", e);
            return null;

        } finally {

            if (null != bis) {

                try {
                    bis.close();
                } catch (IOException e) {

                }
            }

            if (null != urlConnection) {
                urlConnection.disconnect();
            }
        }

        List<StationStatus> stationStatuses = new ArrayList<StationStatus>();

        for (int i = 0; i < bytes.length; i = i + 4) {
            try {
                stationStatuses.add(new StationStatus(ByteHelper.unsignedShortToInt(bytes, i), bytes[i + 2], bytes[i + 3]));
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.w(getClass().getName(), "Error getting station status", e);
                continue;
            }
        }

        return stationStatuses;
    }
}