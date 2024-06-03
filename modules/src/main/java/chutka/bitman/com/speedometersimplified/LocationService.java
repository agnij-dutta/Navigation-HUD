package chutka.bitman.com.speedometersimplified;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
//import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import sky4s.garminhud.app.NotificationMonitor;
import sky4s.garminhud.app.R;
import sky4s.garminhud.eUnits;
import androidx.annotation.Nullable;


public class LocationService extends Service implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final long INTERVAL = 200 * 2;
    private static final long FASTEST_INTERVAL = 200 * 1;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;

    public static double speed;
//    public static HUDInterface hud;

    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
//        hud = NotificationMonitor.getGarminHud();
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();


        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(msgReceiver);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onConnected(Bundle bundle) {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException e) {
        }
    }


    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
//        distance = 0;
    }


    @Override
    public void onConnectionSuspended(int cause) {
        if (cause == CAUSE_NETWORK_LOST) { // not tested
//            if (null != hud) {
//                setSpeed((int) speed, false);
//            }
        }
    }


    private void sendSpeedExtraByBroadcast(double speed) {
        Intent intent = new Intent(getString(R.string.broadcast_receiver_main_activity));
        intent.putExtra(getString(R.string.whoami), getString(R.string.broadcast_sender_location_service));
        intent.putExtra(getString(R.string.gps_speed), speed);
        sendBroadcast(intent);

        //update speed to image detect
//        intent.setAction(getString(R.string.broadcast_receiver_image_detect));
//        sendBroadcast(intent);
    }

    @Override
    public void onLocationChanged(Location location) {

        if (!location.hasSpeed()) {
            return;
        }
        //calculating the speed with getSpeed method it returns speed in m/s so we are converting it into kmph
        if (eUnits.Kilometres == NotificationMonitor.getCurrentUnit() || eUnits.None == NotificationMonitor.getCurrentUnit()) {
            speed = location.getSpeed() * 18 / 5;
        } else if (eUnits.Miles == NotificationMonitor.getCurrentUnit()) {
            speed = location.getSpeed() * 2236 / 1000;
        }
        sendSpeedExtraByBroadcast(speed);

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public class LocalBinder extends Binder {

        public LocationService getService() {
            return LocationService.this;
        }


    }


    @Override
    public boolean onUnbind(Intent intent) {
        stopLocationUpdates();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();

        return super.onUnbind(intent);
    }

}

