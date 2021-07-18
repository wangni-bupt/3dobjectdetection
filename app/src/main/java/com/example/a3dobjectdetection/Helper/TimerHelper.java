package com.example.a3dobjectdetection.Helper;

import android.app.Activity;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

//测试时间
public class TimerHelper {


    public static void Test(Class class1,Method method, String TAG,String message,Object... objects) throws InvocationTargetException, IllegalAccessException {
        long startTime = System.currentTimeMillis(); // 获取开始时间
        // doThing(); // 测试的代码段
        try {
            method.invoke(class1.newInstance(),objects);
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis(); // 获取结束时间
        Log.e(TAG,message+ (endTime - startTime) + "ms");
    }


}
