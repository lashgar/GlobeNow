package myapps.globenow;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {
    // map
    private GoogleApiClient mGoogleApiClient;
    private int PLACE_PICKER_REQUEST = 1;

    private final int SPLASH_DISPLAY_LENGTH = 1000;

    // Timeline and data loader
    LoadTodayJson jsonLoader;
    LoadImageUrlToBmp bmpLoader;
    EventListAdapter eventListAdapter;
    ArrayList<EventInstance> eventListArray;
    ListView timeLineListView;

    TextSwitcher townName;
    ProgressBar progressBar;
    TextSwitcher dateTextView;

    // Current view configuration
    Date currentDate;
    String currentLocationCode;
    String currentLocationName;

    // Caching
    String k_preferenceName = "Timeline";
    String k_sPLastGeoLat = "LastGeoLat";
    String k_sPLastGeoLng = "LastGeoLng";

    // Dynamic loading / paging
    JSONArray jsonArrayEvents;
    JSONArray jsonArrayAuthors;
    Boolean bPendingJsonLoader;
    Boolean bPendingBmpLoader;
    AsyncTask<String, Void, String> asyncTaskJsonLoader;
    AsyncTask<String, Void, ArrayList<Bitmap>> asyncTaskBmpLoader;

    /*
    @brief called from OnCreate to initialize modules
     */
    private void initializeMainActivity(){
        // Load cache
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(k_preferenceName, MODE_PRIVATE);
        Float latitude = sharedPreferences.getFloat(k_sPLastGeoLat,48.4207253242786f);
        Float longitude = sharedPreferences.getFloat(k_sPLastGeoLng,-123.38951110839844f);
        String[] closetsTown = GPSTracker.getClosestCity(latitude, longitude);
        currentLocationCode = closetsTown[0];
        currentLocationName = closetsTown[1];

        currentDate = new Date(); // default to Today
        dateTextView = (TextSwitcher) findViewById(R.id.TextBoxDate);
        dateTextView.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                // TODO Auto-generated method stub
                // create new textView and set the properties like clolr, size etc
                TextView myText = new TextView(Main2Activity.this);
                myText.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                myText.setTextSize(20);
                myText.setTextColor(Color.argb(0xFF, 0xD5, 0, 0));
                myText.setTypeface(Typeface.create("sans-serif-smallcaps", Typeface.BOLD));
                myText.setShadowLayer(5, 0, 0, Color.BLACK);
                myText.setPadding(0, 0, 0, 0);
                return myText;
            }
        });
        dateTextView.setText("Today");

        // progress bar
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));

        // Set ListView Adapter
        eventListArray = new ArrayList<EventInstance>();
        eventListAdapter = new EventListAdapter(this, R.layout.listview_row_noimage, R.id.textView2, eventListArray);
        timeLineListView = (ListView)findViewById(R.id.ListView1);
        timeLineListView.setAdapter(eventListAdapter);
        jsonLoader = new LoadTodayJson(this, timeLineListView);
        bmpLoader = new LoadImageUrlToBmp(this);

        townName = (TextSwitcher)findViewById(R.id.textView4);
        townName.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                // TODO Auto-generated method stub
                // create new textView and set the properties like clolr, size etc
                TextView myText = new TextView(Main2Activity.this);
                myText.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                myText.setTextSize(30);
                myText.setTextColor(Color.argb(0xFF, 0xD5, 0, 0));
                myText.setTypeface(Typeface.create("sans-serif-smallcaps", Typeface.BOLD));
                myText.setShadowLayer(5, 0, 0, Color.BLACK);
                myText.setPadding(0, 4, 0, 0);
                return myText;
            }
        });

        // Dynamic loading / paging initialize
        bPendingJsonLoader = false;
        bPendingBmpLoader = false;

        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        RefreshListViewFromCurrent_();

        // taskbar setup
        final View buttonlocation = findViewById(R.id.imageButton2);
        buttonlocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // check if GPS enabled
                GPSTracker gps = new GPSTracker(Main2Activity.this);
                if(gps.canGetLocation()){
                    float latitude = (float)gps.getLatitude();
                    float longitude = (float)gps.getLongitude();
                    RefreshListViewFromGeo_(latitude, longitude);
                }else{
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }
            }
        });

        Calendar newCalendar = Calendar.getInstance();
        final DatePickerDialog  StartTime = new DatePickerDialog(this, R.style.datepicker,
                new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar newCldr = Calendar.getInstance();
                        newCldr.set(year, monthOfYear, dayOfMonth);
                        Date newDate = newCldr.getTime();
                        RefreshListViewFromDate_(newDate);
                    }
                }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        final View buttoncalendar = findViewById(R.id.imageButton);
        buttoncalendar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                StartTime.show();
            }
        });

        // initialize Place picker
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        final View buttonexplore = findViewById(R.id.imageButton3);
        buttonexplore.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(Main2Activity.this), PLACE_PICKER_REQUEST);
                    // Log.d("LocationPicker", "Closed the activity");
                } catch (GooglePlayServicesNotAvailableException e) {
                    // Log.d("LocationPicker", "Service unavailable");
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e) {
                    // Log.d("LocationPicker", "Repairable Exception");
                    e.printStackTrace();
                }
            }
        });

        timeLineListView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if ( (!bPendingJsonLoader) && // No pending JSon to Load
                     (!bPendingBmpLoader) &&  // No pending BMP to load
                     timeLineListView!=null &&
                     timeLineListView.getAdapter()!=null &&
                     timeLineListView.getLastVisiblePosition() == timeLineListView.getAdapter().getCount() -1 &&
                     timeLineListView.getChildAt(timeLineListView.getChildCount() - 1)!=null &&
                     timeLineListView.getChildAt(timeLineListView.getChildCount() - 1).getBottom() <= timeLineListView.getHeight())
                {
                    bPendingJsonLoader = Boolean.TRUE;
                    Toast.makeText(getApplicationContext(), "Scrolled all the way down", Toast.LENGTH_LONG).show();
                }
            }
        });

        Log.d("LOADER", "Initialized");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Log.d("LocationPicker", "ResultCalled ");
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                float latitude = (float)place.getLatLng().latitude;
                float longitude = (float)place.getLatLng().longitude;
                RefreshListViewFromGeo_(latitude, longitude);
                /*
                String address = String.format("%s", place.getAddress());
                stBuilder.append("Name: ");
                stBuilder.append(placename);
                stBuilder.append("\n");
                stBuilder.append("Latitude: ");
                stBuilder.append(latitude);
                stBuilder.append("\n");
                stBuilder.append("Logitude: ");
                stBuilder.append(longitude);
                stBuilder.append("\n");
                stBuilder.append("Address: ");
                stBuilder.append(address);
                // tvPlaceDetails.setText(stBuilder.toString());
                Toast.makeText(getApplicationContext(), stBuilder.toString(), Toast.LENGTH_LONG).show();
                */
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "connection failed", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.splash_screen);
        setContentView(R.layout.activity_main2);
        initializeMainActivity();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void RefreshListViewFromCurrent_(){
        RefreshListViewFromCityCode_(currentLocationCode, currentLocationName, (float)0.0);
    }

    public void RefreshListViewFromCityCode_(String citycode, String cityName, float distance){
        if(LaunchAsyncJsonLoader_(citycode, currentDate)) {

            Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
            Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);

            // set the animation type of textSwitcher
            townName.setInAnimation(in);
            townName.setOutAnimation(out);

            if (distance > 40) {
                // note distance
                Toast.makeText(getApplicationContext(), "Our app is not tailored for this area, we show results of the closest city: " + cityName + " (" + Float.toString(distance) + " km away)", Toast.LENGTH_LONG).show();
            }
            townName.setText(cityName);
        }
    }

    private void RefreshListViewFromDate_(Date dateToLoad){
        if(LaunchAsyncJsonLoader_(currentLocationCode, dateToLoad)) {

            String formattedDate = new SimpleDateFormat("EEE MMM, dd").format(currentDate);
            Date today = new Date();
            if (today.getTime() == dateToLoad.getTime()) {
                formattedDate = "Today";
            }
            Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
            Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);

            // set the animation type of textSwitcher
            dateTextView.setInAnimation(in);
            dateTextView.setOutAnimation(out);

            dateTextView.setText(formattedDate);
        }
    }

    private void RefreshListViewFromGeo_(float lat, float lng)
    {
        String[] closetsTown = GPSTracker.getClosestCity(lat, lng);
        String cityCode = closetsTown[0];
        String cityName = closetsTown[1];

        // Cache last location
        CacheNewLocation(lat, lng);

        // Update view
        float distance = Float.parseFloat(closetsTown[2]);
        RefreshListViewFromCityCode_(cityCode, cityName, distance);
    }

    private void CacheNewLocation(float lat, float lng)
    {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(k_preferenceName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(k_sPLastGeoLat, lat);
        editor.putFloat(k_sPLastGeoLng, lng);
        editor.commit();
    }

    private boolean isTwitterAppInstalled(){
        try{
            ApplicationInfo info = this.getPackageManager().
                    getApplicationInfo("com.twitter.android", 0 );
            return true;
        } catch( PackageManager.NameNotFoundException e ){
            return false;
        }
    }

    private Bitmap GenDummyBmp_(){
        byte[] data = new byte[4];
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, 3);
        return bmp;
    }

    private Boolean LaunchAsyncJsonLoader_(String citycode, Date dateToLoad)
    {
        // Cancel pending Async if any
        if (bPendingJsonLoader){
            asyncTaskJsonLoader.cancel(true);
        }
        if(bPendingBmpLoader){
            asyncTaskBmpLoader.cancel(true);
        }
        
        // Launch new Async task
        currentLocationCode = citycode;
        currentDate = dateToLoad;
        asyncTaskJsonLoader = jsonLoader.updateListView(currentLocationCode, currentDate);
        bPendingJsonLoader = Boolean.TRUE;
        return Boolean.TRUE;
    }

    private Boolean LaunchAsyncBmpLoader_(ArrayList<String> mediaUrls)
    {
        // Cancel pending Async if any
        if(bPendingBmpLoader){
            asyncTaskBmpLoader.cancel(true);
        }

        // Launch new Async task
        asyncTaskBmpLoader = bmpLoader.Load(mediaUrls.toArray(new String[mediaUrls.size()]));
        bPendingBmpLoader =Boolean.TRUE;
        return Boolean.TRUE;
    }

    public void ReceiveBmpListAsync(ArrayList<Bitmap> bmpList){
        if (!bPendingBmpLoader){
            throw new AssertionError("Unexpected BMP batch received");
        }
        bPendingBmpLoader = Boolean.FALSE;

        // Process new BMP batch
        for(int i = 0; i < bmpList.size(); i++){
            EventInstance eventInstance = eventListArray.get(i);
            eventInstance.bmp = bmpList.get(i);
            eventListArray.set(i, eventInstance);
        }
        eventListAdapter.notifyDataSetChanged();
    }

    public void ReceiveJsonResponseAsync(String json){
        if (!bPendingJsonLoader){
            throw new AssertionError("Unexpected JSON received");
        }
        bPendingJsonLoader = Boolean.FALSE;

        // Process new JSon
        try {
            eventListArray.clear();
            JSONObject obj = new JSONObject(json);
            jsonArrayEvents = obj.getJSONArray("data");
            jsonArrayAuthors = new JSONArray();
            boolean jsonHasAuthors = obj.has("authors");
            if(jsonHasAuthors){
                jsonArrayAuthors = obj.getJSONArray("authors");
            }
            final int length = Math.min(jsonArrayEvents.length(),24);

            boolean bIsTwitterAppInstalled = isTwitterAppInstalled();
            ArrayList<String> mediaUrls = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                JSONObject jo_inside = jsonArrayEvents.getJSONObject(i);
                String status = jo_inside.getString("status");
                if(status.equals("merged") || status.equals("dead")){
                    // redundant; no need to include this entry
                    continue;
                }

                // get author, text, and media
                String author = jo_inside.getString("prettyauthor");
                String text = jo_inside.getString("prettytext");
                List<String> allmedia = Arrays.asList(jo_inside.getString("media").split(",")); // FIXME: not tested
                String media = allmedia.get(0);
                int authorId = -1;
                if((media.equals("") || media.equals("none")) && jsonHasAuthors && jo_inside.has("authorid") && jo_inside.getInt("authorid")!=-1){
                    // lack of media, use the author cover photo
                    authorId = jo_inside.getInt("authorid");
                    media = jsonArrayAuthors.getJSONObject(authorId).getString("coverphoto");
                    // Log.d("MediaURL", "has authorid> "+author);
                }

                // get callback URL
                String eventsource = jo_inside.getString("source");
                Object thing = jo_inside.get("tweetid");
                BigInteger id = BigInteger.ZERO;
                String url = "";
                if(thing instanceof String){
                    id = new BigInteger(jo_inside.getString("tweetid"));
                    if(eventsource.equals("Twitter")) {
                        // Event extracted from Twitter tweets
                        if (bIsTwitterAppInstalled) {
                            url = "https://twitter.com/statuses/" + jo_inside.getString("tweetid");
                        } else {
                            url = "https://twitter.com/i/web/status/" + jo_inside.getString("tweetid");
                        }
                    }else if(eventsource.equals("FBEvent")){
                        // Facebook Event
                        url = "https://www.facebook.com/events/" + jo_inside.getString("tweetid");
                    }else if(eventsource.equals("Instagram")){
                        // Facebook Event
                        url = jo_inside.getString("url");
                    }else{
                        // unimplemented event
                    }
                }
                // Will load the batch asyncly
                mediaUrls.add(media);

                // Create entry
                EventInstance newEntry = new EventInstance();
                newEntry.status = status;
                newEntry.url = url;
                newEntry.media = media;
                newEntry.prettytext = text;
                newEntry.tweetid = id;
                newEntry.source = eventsource;
                newEntry.authorid = authorId;
                newEntry.ml_rating = 0.0f; // FIXME: Get from request
                newEntry.prettyauthor = author;
                newEntry.bmp = GenDummyBmp_();
                // Push to Array
                eventListArray.add(newEntry);
            }
            // Invoke image loader and fetch images
            Log.d("MainThread", "evenListArray size: "+String.valueOf(eventListArray.size()));
            Log.d("MainThread", "mediaUrl size: "+String.valueOf(mediaUrls.size()));
            LaunchAsyncBmpLoader_(mediaUrls);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        eventListAdapter.notifyDataSetChanged();

        timeLineListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EventInstance eventInstance = eventListArray.get(position);
                String sourceurl = eventInstance.url;
                if(sourceurl.equals("")){
                    Toast.makeText(view.getContext(), "Sorry! No extra information is available on this event.", Toast.LENGTH_LONG).show();
                }else {
                    // Open a URL
                    // TODO: Use In-App browser
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(sourceurl));
                    view.getContext().startActivity(browserIntent);
                }
            }
        });
    }
}
