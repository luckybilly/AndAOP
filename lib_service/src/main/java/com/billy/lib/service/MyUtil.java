package com.billy.lib.service;

import android.util.Log;

/**
 * @author billy.qi
 * @since 17/3/29 15:30
 */
public class MyUtil {

    public static String getInfo() {
        Log.i("MyUtil", "getInfo() called.");
        return "myUtil.info";
    }

    public static String getDetail() {
        Log.i("MyUtil", "getDetail() called.");
        return "myUtil.detail";
    }
}
