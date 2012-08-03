//package com.siu.bicyclette.service;
//
//import android.util.Log;
//import com.siu.bicyclette.util.ByteUtils;
//import com.siu.bicyclette.util.UrlUtils;
//import com.siu.bicyclette.model.StationStatus;
//
//import java.io.BufferedInputStream;
//import java.io.IOException;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
// */
//public class StationStatusResource {
//
//    private static final String URL = "http://bicyclette.indri.fr/api?c=paris";
//    private static final int SIZE = 4 * 1300;
//
//    public ArrayList<StationStatus> getStationsStatus() {
//
//        URL url = UrlUtils.getUrl(URL);
//
//        byte[] bytes = new byte[SIZE];
//
//        BufferedInputStream bis = null;
//        HttpURLConnection urlConnection = null;
//
//        try {
//            urlConnection = (HttpURLConnection) url.openConnection();
//            urlConnection.setConnectTimeout(15 * 1000);
//
//            Log.d(getClass().getName(), "Connection opened to : " + URL);
//
//            long time = System.currentTimeMillis();
//
//            bis = new BufferedInputStream(urlConnection.getInputStream());
//            bis.read(bytes);
//
//            Log.d(getClass().getName(), "Content downloaded and parsed in " + (System.currentTimeMillis() - time) + " ms");
//
//        } catch (IOException e) {
//            Log.w(getClass().getName(), "Error during reading connection stream", e);
//            return null;
//
//        } finally {
//
//            if (null != bis) {
//
//                try {
//                    bis.close();
//                } catch (IOException e) {
//
//                }
//            }
//
//            if (null != urlConnection) {
//                urlConnection.disconnect();
//            }
//        }
//
//        ArrayList<StationStatus> stationStatuses = new ArrayList<StationStatus>();
//
//        for (int i = 0; i < bytes.length; i = i + 5) {
//            try {
//                stationStatuses.add(new StationStatus(ByteUtils.unsignedShortToInt(bytes, i + 1), bytes[i + 3], bytes[i + 4]));
//            } catch (ArrayIndexOutOfBoundsException e) {
//                Log.w(getClass().getName(), "Error getting station status", e);
//                continue;
//            }
//        }
//
//        return stationStatuses;
//    }
//}
