package finch.archerhud.app;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.List;


public class NotificationCollectorMonitorService extends Service {

    /**
     * {@link Log#isLoggable(String, int)}
     * <p>
     * IllegalArgumentException is thrown if the tag.length() > 23.
     */
    private static final String TAG = "NCMS";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() called");
        ensureCollectorRunning();

        startNotification(null, null);
//        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    private Notification notification;
    private NotificationManager mNotificationManager;

    private Notification getNormalNotification(String contentText, Bitmap icon) {
        final Intent mainIntent = MainActivity.sMainIntent;
        MainActivity.mNCMS = this;
        int flags = PendingIntent.FLAG_CANCEL_CURRENT; 
        if( null == context) {
            return null;
        }
        final PendingIntent pendingMainIntent = PendingIntent.getActivity(context, 0, mainIntent, flags); 


        final String channelID = "id";

        NotificationCompat.Builder builder
                = new NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.mipmap.ic_notification_foreground)
                .setAutoCancel(false) 
                .setContentText(contentText)
                .setOngoing(true)      
                .setContentIntent(pendingMainIntent)
                .setChannelId(channelID);
//                .build();
        if (null != icon) {
            builder.setLargeIcon(icon);
        }
        notification = builder.build();
        return notification;
    }


    void startNotification(String contentText, Bitmap icon) {

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification = getNormalNotification(null == contentText ? "GMaps Notify Monitor Service" : contentText, icon);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String channelID = "id";
            NotificationChannel channel = new NotificationChannel(
                    channelID,
                    "Notify Monitor",
                    NotificationManager.IMPORTANCE_MAX);
            channel.enableLights(false);
            //it had a bug which is vibration cannot be disabled normally.
            channel.setVibrationPattern(new long[]{0});
            channel.enableVibration(true);

            mNotificationManager.createNotificationChannel(channel);
        } else {
            notification.vibrate = new long[]{0};
        }

//        mNotificationManager.notify(R.integer.notify_id, notification);
        startForeground(1, notification);
    }

    private void stopNotification() {
//        log("stopNotification");

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void ensureCollectorRunning() {
        ComponentName collectorComponent = new ComponentName(this, /*NotificationListenerService Inheritance*/ NotificationMonitor.class);
        Log.v(TAG, "ensureCollectorRunning collectorComponent: " + collectorComponent);
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        boolean collectorRunning = false;
        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);
        if (runningServices == null) {
            Log.w(TAG, "ensureCollectorRunning() runningServices is NULL");
            return;
        }
        for (ActivityManager.RunningServiceInfo service : runningServices) {
            if (service.service.equals(collectorComponent)) {
                Log.w(TAG, "ensureCollectorRunning service - pid: " + service.pid + ", currentPID: " + Process.myPid() + ", clientPackage: " + service.clientPackage + ", clientCount: " + service.clientCount
                        + ", clientLabel: " + ((service.clientLabel == 0) ? "0" : "(" + getResources().getString(service.clientLabel) + ")"));
                if (service.pid == Process.myPid() /*&& service.clientCount > 0 && !TextUtils.isEmpty(service.clientPackage)*/) {
                    collectorRunning = true;
                }
            }
        }
        if (collectorRunning) {
            Log.d(TAG, "ensureCollectorRunning: collector is running");
            return;
        }
        Log.d(TAG, "ensureCollectorRunning: collector not running, reviving...");
        toggleNotificationListenerService();
    }

    private void toggleNotificationListenerService() {
        Log.d(TAG, "toggleNotificationListenerService() called");
        ComponentName thisComponent = new ComponentName(this, /*getClass()*/ NotificationMonitor.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

