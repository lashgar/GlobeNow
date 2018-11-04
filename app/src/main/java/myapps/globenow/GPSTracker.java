package myapps.globenow;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;

/**
 * Created by Ahmad on 2017-11-28.
 */
public class GPSTracker extends Service implements LocationListener {

    private final Context mContext;

    // flag for GPS status
    private boolean canGetLocation = false;

    private Location location; // location
    private double latitude; // latitude
    private double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    public GPSTracker(Context context) {
        this.mContext = context;
        getLocation();
    }

    public static double getDistanceFromLatLonInKm(double lat1, double lon1, double lat2, double lon2) {
        Location locA = new Location("Source");
        locA.setLatitude(lat1);
        locA.setLongitude(lon1);
        Location locB = new Location("Destination");
        locB.setLatitude(lat2);
        locB.setLongitude(lon2);
        return Math.abs(locA.distanceTo(locB)/1000.0f);
    }

    public static String[] getClosestCity(double latitude, double longitude){
        /*
        citycode = "ika"
        city_location_names = ['Tehran']
        city_location_geo = [35.688801596916846,51.38827085494995,28]
        citycode = "jfk"
        city_location_names = ['New York', 'JFK', 'NYC']
        city_location_geo = [40.703129470358384,-73.98852545768023,29.02344477028665]
        citycode = "lax"
        city_location_names = ['Los Angeles', 'LAX', 'LA', 'Hollywood', 'Beverly Hills']
        city_location_geo = [34.049701073519905,-118.24200441129506,41.342512276733366]
        citycode = "sfo"
        city_location_names = ['San Francisco', 'SFO', 'SF']
        city_location_geo = [37.76789229972227,-122.44152825325727,38.55999218447994]
        citycode = "sjc"
        city_location_names = ['San Jose', 'SJC']
        city_location_geo = [37.33893677039791,-121.92242424935102,27.504213283367633]
        citycode = "yeg"
        city_location_names = ['Edmonton', 'YEG']
        city_location_geo = [53.54411393062082,-113.48993682913715,29]
        citycode = "yhm"
        city_location_names = ['Hamilton', 'Burlington', 'YHM', 'HamOnt']
        city_location_geo = [43.243802230314806,-79.80084230192006,28.692351953016836]
        citycode = "yhz"
        city_location_names = ['Halifax', 'YHZ']
        city_location_geo = [44.64901353557669,-63.57588958766428,29]
        citycode = "ykf"
        city_location_names = ['Kitchener', 'Waterloo', 'Guelph', 'Cambridge', 'YKF']
        city_location_geo = [43.47115990069644,-80.36718746647239,24.35463390984477]
        citycode = "yow"
        city_location_names = ['Ottawa', 'YOW']
        city_location_geo = [45.42142647027999,-75.69715690638986,29]
        citycode = "yvr"
        city_location_names = ['Vancouver', 'YVR']
        city_location_geo = [49.272912086405164,-123.1314697349444,18.195101776492344]
        citycode = "ywg"
        city_location_names = ['Winnipeg', 'YWG']
        city_location_geo = [49.89519560311849,-97.1384782793757,29]
        citycode = "yxe"
        city_location_names = ['Saskatoon', 'YXE']
        city_location_geo = [52.13307446184987,-106.67053413417307,29]
        citycode = "yxu"
        city_location_names = ['London', 'YXU', 'LdnOnt']
        city_location_geo = [42.98335257355123,-81.24169908463955,24]
        citycode = "yyc"
        city_location_names = ['Calgary', 'YYC']
        city_location_geo = [51.048498082007974,-114.07081317927805,29]
        citycode = "yyj"
        city_location_names = ['Victoria', 'YYJ', 'Sidney', 'Esquimalt', 'Langford']
        city_location_geo = [48.4207253242786, -123.38951110839844, 28.350637341552005]
        citycode = "yyz"
        city_location_names = ['Toronto', 'YYZ']
        city_location_geo = [43.728833909894526,-79.43280030973256,33.217215665778305]
        */
        String[] citycodes = {
                "jfk",
                "lax",
                "sfo",
                "sjc",
                "yeg",
                "yhm",
                "yhz",
                "ykf",
                "yow",
                "yvr",
                "ywg",
                "yxe",
                "yxu",
                "yyc",
                "yyj",
                "yyz"};
        double[] lats = {
                40.703129470358384, // JFK
                34.049701073519905, // LAX
                37.76789229972227,  // SFO
                37.33893677039791,  // SJC
                53.54411393062082,  // YEG
                43.243802230314806, // YHM
                44.64901353557669,  // YHZ
                43.47115990069644,  // YKF
                45.42142647027999,  // YOW
                49.272912086405164, // YVR
                49.89519560311849,  //
                52.13307446184987,
                42.98335257355123,
                51.048498082007974,
                48.4207253242786,
                43.728833909894526};
        double[] lngs = {
                -73.98852545768023,  // JFK
                -118.24200441129506, // LAX
                -122.44152825325727, // SFO
                -121.92242424935102, // SJC
                -113.48993682913715, // YEG
                -79.80084230192006,  // YHM
                -63.57588958766428,  // YHZ
                -80.36718746647239,  // YKF
                -75.69715690638986,  // YOW
                -123.1314697349444,  // YVR
                -97.1384782793757,
                -106.67053413417307,
                -81.24169908463955,
                -114.07081317927805,
                -123.38951110839844,
                -79.43280030973256};
        String[] names = {
                "New York",
                "Los Angeles",
                "San Francisco",
                "San Jose",
                "Edmonton",
                "Hamilton",
                "Halifax",
                "Kitchener",
                "Ottawa",
                "Vancouver",
                "Winnipeg",
                "Saskatoon",
                "London",
                "Calgary",
                "Victoria",
                "Toronto"};
        // double mindistance = Math.sqrt(Math.pow(latitude-lats[0],2)+Math.pow(longitude-lngs[0],2));
        double mindistance = GPSTracker.getDistanceFromLatLonInKm(latitude, longitude, lats[0], lngs[0]);
        int minidx = 0;
        for(int i=1; i<lngs.length; ++i){
            // double dist = Math.sqrt(Math.pow(latitude-lats[i],2)+Math.pow(longitude-lngs[i],2));;
            double dist = GPSTracker.getDistanceFromLatLonInKm(latitude, longitude, lats[i], lngs[i]);
            // Log.d("GPS", "Distance from "+names[i]+" "+Double.toString(dist));
            if(dist<mindistance){
                mindistance = dist;
                minidx = i;
            }
        }
        // Toast.makeText(getApplicationContext(), "Returning location", Toast.LENGTH_LONG).show();
        // Log.d("GPS", "Distance from the closest city is "+Double.toString(mindistance));
        // Log.d("GPS", "city coordinates> "+Double.toString(lats[minidx])+" "+Double.toString(lngs[minidx]));
        // Log.d("GPS", "user coordinates> "+Double.toString(latitude)+" "+Double.toString(longitude));
        String[] result = new String[3];
        result[0] = citycodes[minidx];
        result[1] = names[minidx];
        result[2] = Double.toString(mindistance);
        return result;
    }

    public Location getLocation() {
        try {

            // flag for GPS status
            boolean isGPSEnabled = false;

            // flag for network status
            boolean isNetworkEnabled = false;

            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                // Log.d("VERBOSE", "No network, no GPS");
            } else {
                if (ActivityCompat.checkSelfPermission((Activity)mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission((Activity)mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity)mContext, new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    }, 10);
                }
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    // Log.d("VERBOSE", "Network");
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    // Log.d("VERBOSE", "Network2");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }

                }

                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        // Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Toast.makeText(getApplicationContext(), "Returning location", Toast.LENGTH_LONG).show();
        return location;
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * Function to get latitude
     * */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     * */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to check GPS/wifi enabled
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}