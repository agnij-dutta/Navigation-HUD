package finch.archerhud.app;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MyCrashHandler implements Thread.UncaughtExceptionHandler {

    private static MyCrashHandler crashHandler;
    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
    private static Date curDate = new Date(System.currentTimeMillis());//Get the current time.
    private static String str = formatter.format(curDate);
    private static String TAG = "MyCrashHandler";
    //The default UncaughtException handling class in the system.
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    //The Context object
    private Context mContext;
    //Used to store device information and exception details.
    private Map<String, String> infos = new HashMap<String, String>();


    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        if (!handleException(ex) && mDefaultHandler != null) {
            //If the user doesn't handle it, let the system's default exception handler deal with it.
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.e(TAG, "error : ", e);
            }
            //To exit the program.
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    /**
     * Custom error handling, collecting error information, sending error reports, etc., are all performed here.
     *
     * @param ex
     * @return True if the exception information is handled; otherwise, false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        //Use Toast to display the exception information.
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();//Prepare the MessageQueue for sending messages.
                Toast.makeText(mContext, "Sorry, App hang...", Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }.start();
        //Collect device parameter information
        collectDeviceInfo(mContext);
        //Save log files.
        saveCrashInfo2File(ex);
        return true;
    }


    /**
     * Save error information to a file
     *
     * @param ex
     * @return Return the file name for ease of transferring the file to the server
     */
    private String saveCrashInfo2File(Throwable ex) {

        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            long timestamp = System.currentTimeMillis();

            String fileName = "crash-" + str + "-" + timestamp + ".log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String path = Environment.getExternalStorageDirectory().getPath() + "/crash/";
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes());
                System.out.println(sb.toString());
                fos.close();
            }
            return fileName;
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);
        }
        return null;
    }

    /**
     * Initialize
     *
     * @param context
     */
    public void init(Context context) {
        mContext = context;
        //Get the system's default UncaughtException handler.
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //Set this CrashHandler as the default handler for the program.
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * Collect device parameter information
     *
     * @param ctx
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = ((PackageInfo) pi).versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
                Log.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                Log.e(TAG, "an error occured when collect crash info", e);
            }
        }
    }

    private MyCrashHandler() {
    }

    public static MyCrashHandler instance() {
        if (crashHandler == null) {
            crashHandler = new MyCrashHandler();
        }
        return crashHandler;
    }
}