package com.billy.andaop;

import android.util.Log;

/**
 * Aop的基本用法：统计方法执行时长
 * @author billy.qi
 * @since 17/3/22 18:14
 */
public class Aop {
    private static final String TAG = "Aop";
    private TimeWatcher timeWatcher;
    private String className, methodName;
    private String methodDesc;
    private String fullMethodInfo;

    public static Aop beforeStart(String className, String methodName, String methodDesc) {
        Aop aop = new Aop();
        aop.className = className;
        aop.methodName = methodName;
        aop.methodDesc = methodDesc;
        aop.timeWatcher = new TimeWatcher();
        aop.timeWatcher.start();
        aop.fullMethodInfo = className + "." + methodName + methodDesc;
//        aop.log("beforeStart");
        return aop;
    }

    public static void afterEnd(Aop aop) {
        aop.timeWatcher.stop();
//        aop.log("afterEnd");
        Log.i(TAG, aop.fullMethodInfo + " cost:" + aop.timeWatcher.getTotalTimeAsString());
    }

    private void log(String type) {
        if ("beforeStart".equals(type)) {
            Log.w(TAG, type + ":" + fullMethodInfo);
        } else {
            Log.e(TAG, type + ":" + fullMethodInfo);
        }
    }
}
