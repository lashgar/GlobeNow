package myapps.globenow;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

/**
 * Created by Ahmad on 2017-11-28.
 */
//public class GPSTracker extends Service implements LocationListener {
class GPSTracker {
    private final Context mContext;
    private Main2Activity main2Activity;

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 10; // every 10 seconds

    // Declaring a Location Manager
    private LocationManager locationManager;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    GPSTracker(Context context) {
        mContext = context;
        main2Activity =(Main2Activity)mContext;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        // Initialize location request
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(MIN_TIME_BW_UPDATES);
        mLocationRequest.setFastestInterval(MIN_TIME_BW_UPDATES);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d("GPSTracker","Callback received reply");
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        Log.d("GPSTracker", "Received location. Removing callback.");
                        main2Activity.ReceiveNewLocationFromGPS(location);
                        detachLocationRequestCallback_();
                        break;
                    }
                }
            }
        };
    }

    private void attachLocationRequestCallback_(){
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Log.d("GPSTracker", "Requesting permission");
            ActivityCompat.requestPermissions((Activity)mContext, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, 10);
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);
        Log.d("GPSTracker", "Callback attached");
    }

    private void detachLocationRequestCallback_(){
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        Log.d("GPSTracker", "Callback detached");
    }

    private void RequestLocationUpdate_(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(mContext);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener((Activity)mContext, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d("GPSTracker", "All set to request location");
                attachLocationRequestCallback_();
            }
        });

        task.addOnFailureListener((Activity)mContext, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("GPSTracker", "Failed to request location");
                Toast.makeText(mContext, "GPS unavailable", Toast.LENGTH_LONG).show();
            }
        });
    }

    private static double getDistanceFromLatLonInKm_(double lat1, double lon1, double lat2, double lon2) {
        Location locA = new Location("Source");
        locA.setLatitude(lat1);
        locA.setLongitude(lon1);
        Location locB = new Location("Destination");
        locB.setLatitude(lat2);
        locB.setLongitude(lon2);
        return Math.abs(locA.distanceTo(locB)/1000.0f);
    }

    static String[] getClosestCity(double latitude, double longitude){
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
        double minDistance = GPSTracker.getDistanceFromLatLonInKm_(latitude, longitude, lats[0], lngs[0]);
        int minIdx = 0;
        for(int i=1; i<lngs.length; ++i){
            // double dist = Math.sqrt(Math.pow(latitude-lats[i],2)+Math.pow(longitude-lngs[i],2));;
            double dist = GPSTracker.getDistanceFromLatLonInKm_(latitude, longitude, lats[i], lngs[i]);
            // Log.d("GPS", "Distance from "+names[i]+" "+Double.toString(dist));
            if(dist<minDistance){
                minDistance = dist;
                minIdx = i;
            }
        }
        // Toast.makeText(getApplicationContext(), "Returning location", Toast.LENGTH_LONG).show();
        // Log.d("GPS", "Distance from the closest city is "+Double.toString(mindistance));
        // Log.d("GPS", "city coordinates> "+Double.toString(lats[minidx])+" "+Double.toString(lngs[minidx]));
        // Log.d("GPS", "user coordinates> "+Double.toString(latitude)+" "+Double.toString(longitude));
        String[] result = new String[3];
        result[0] = citycodes[minIdx];
        result[1] = names[minIdx];
        result[2] = Double.toString(minDistance);
        return result;
    }

    // Return location from network immediately and location from GPS async
    public Task<Location> getLocation() {
        try {
            // flag for GPS status
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (!isGPSEnabled) {
                // no network provider is enabled
                Toast.makeText(mContext, "GPS disabled", Toast.LENGTH_LONG).show();
                Log.d("GPSTracker", "No network, no GPS");
                main2Activity.ReceiveNewLocationFromGPS(null);
            } else {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Log.d("GPSTracker", "Requesting permission");
                    ActivityCompat.requestPermissions((Activity)mContext, new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    }, 10);
                }

                // If GPS Enabled get lat/long using GPS Services
                Log.d("GPSTracker", "GPS Enabled");
                return mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                Log.d("GPSTracker", "Success to get last location");
                                if (location == null){
                                    Log.d("GPSTracker", "Requesting location update");
                                    RequestLocationUpdate_();
                                } else {
                                    Log.d("GPSTracker", "Dismiss request location update");
                                    main2Activity.ReceiveNewLocationFromGPS(location);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("GPSTracker", "Failed to get last location");
                            }
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
            main2Activity.ReceiveNewLocationFromGPS(null);
        }
        // Toast.makeText(getApplicationContext(), "Returning location", Toast.LENGTH_LONG).show();
        return null;
    }
}