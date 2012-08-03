package com.siu.android.univelo.location;

import android.content.Context;
import com.siu.android.univelo.util.GlobalConstants;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class LastLocationFinderFactory {

    public static LastLocationFinder getLastLocationFinder(Context context) {
        return GlobalConstants.SUPPORTS_GINGERBREAD ? new GingerbreadLastLocationFinder(context) : new LegacyLastLocationFinder(context);
    }
}
