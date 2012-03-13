package com.siu.bicyclette.util;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public class StringUtils {

    public static boolean isEmpty(String string) {

        if (null == string || string.trim().equals("")) {
            return true;
        }

        return false;
    }
}
