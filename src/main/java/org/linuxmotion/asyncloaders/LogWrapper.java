package org.linuxmotion.asyncloaders;
import android.app.Fragment;
//import android.support.v4.app.Fragment;
import android.util.Log;

public class LogWrapper {


    public static void Logd(String tag, String msg){

        if(Constants.DEBUG)Log.d(tag, msg);
    }

    public static void Logdb(String tag, String msg){

        if(Constants.DEBUG_DATABASE)Log.d(tag, msg);
    }

    public static void Logv(String tag, String msg){

        if(Constants.VERBOSE)Log.v(tag, msg);

    }
    public static void Logi(String tag, String msg) {

        if(Constants.INFORMATIONAL)Log.v(tag, msg);

    }
    public static void Loge(String tag, String msg) {

        if(Constants.ERRORS)Log.e(tag, msg);

    }
    public static void Loge(String tag, Exception ex) {

        if(Constants.ERRORS)Log.e(tag,ex.getStackTrace().toString());

    }
    public static void Logw(String tag, String msg) {

        if(Constants.WARNINGS)Log.w(tag, msg);

    }

    public static void Logi(Fragment fragment,String msg) {


        if(Constants.INFORMATIONAL)Log.v(fragment.getClass().getSimpleName(), msg);

    }
}