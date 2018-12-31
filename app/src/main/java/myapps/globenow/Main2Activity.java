package myapps.globenow;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {
    // map
    private GoogleApiClient mGoogleApiClient;
    private final int PLACE_PICKER_REQUEST = 1;

    // Timeline and data loader
    private LoadTodayJson jsonLoader;
    private LoadImageUrlToBmp bmpLoader;
    private EventListAdapter eventListAdapter;
    private ArrayList<EventInstance> eventListArray;
    private ListView timeLineListView;

    private TextSwitcher townName;
    private TextSwitcher dateTextView;

    // Current view configuration
    private Date currentDate;
    private String currentLocationCode;
    private String currentLocationName;

    // Caching
    private final String k_preferenceName = "Timeline";
    private final String k_sPLastGeoLat = "LastGeoLat";
    private final String k_sPLastGeoLng = "LastGeoLng";

    // Dynamic loading / paging
    private JSONArray jsonArrayEvents;
    private JSONArray jsonArrayAuthors;
    private Boolean bPendingJsonLoader;
    private Boolean bPendingBmpLoader;
    private AsyncTask<String, Void, String> asyncTaskJsonLoader;
    private AsyncTask<String, Void, ArrayList<Bitmap>> asyncTaskBmpLoader;
    private int jsonArrayEventsLastReadIdx;

    // Statistics
    private FirebaseAnalytics mFirebaseAnalytics;

    // AdMob
    private ArrayList<UnifiedNativeAd> unifiedNativeAdArrayList;
    private AdLoader adLoader;
    private final int k_eventToAdRatio = 10; // show one ad for every 10 events
    private int k_adLocationOrder = 8 % k_eventToAdRatio;

    // Text View Expandable
    private final int k_textViewExpandableNNewLine = 2;
    private final int k_textViewExpandableNChars = 140;
    // Source: https://unicode-table.com/en/#1D41E
    // private final String k_ReadMoreInBold = "\uD835\uDC11\uD835\uDC1E\uD835\uDC1A\uD835\uDC1D \uD835\uDC0C\uD835\uDC28\uD835\uDC2B\uD835\uDC1E";

    // GPS Tracker
    GPSTracker m_gpsTracker = null;
    boolean bPendingGpsUpdate = false;

    // Search
    String searchQueryString = "";
    // boolean bSearchInProgress  = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.splash_screen);
        setContentView(R.layout.activity_main2);
        initializeMainActivity();
    }

    /*
    @brief called from OnCreate to initialize modules
     */
    private void initializeMainActivity(){
        // Screen setup
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        // Initialize Firebase
        // WAKE_LOCK is only needed for messaging so we can ignore the error below:
        // https://stackoverflow.com/questions/42520019/does-firebase-cloud-messaging-really-need-a-wake-lock-permission
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Initialize AdMob
        unifiedNativeAdArrayList = new ArrayList<>();
        MobileAds.initialize(this, getResources().getString(R.string.admob_app_id));
        adLoader = new AdLoader.Builder(this, "/6499/example/native")
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        // Show the ad.
                        // Log.d("NewAd",unifiedNativeAd.getHeadline());
                        unifiedNativeAdArrayList.add(unifiedNativeAd);
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        // Handle the failure by logging, altering the UI, and so on.
                        // Log.d("NewAd", "failed "+Integer.toString(errorCode));
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .setReturnUrlsForImageAssets(false) // populate image and uri both
                        .setImageOrientation(NativeAdOptions.ORIENTATION_LANDSCAPE)
                        .setRequestMultipleImages(false) // fetch only one image
                        .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_BOTTOM_RIGHT)
                        .build())
                .build();

        // Load timeline configs from cache
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(k_preferenceName, MODE_PRIVATE);
        Float latitude = sharedPreferences.getFloat(k_sPLastGeoLat,48.4207253242786f);
        Float longitude = sharedPreferences.getFloat(k_sPLastGeoLng,-123.38951110839844f);
        String[] closetsTown = GPSTracker.getClosestCity(latitude, longitude);
        currentLocationCode = closetsTown[0];
        currentLocationName = closetsTown[1];
        currentDate = new Date(); // default to Today (might be cached in future)

        // Initialize GPS
        m_gpsTracker = new GPSTracker(Main2Activity.this);

        // Initialize banners
        initializeTopBanner();
        initializeBotBanner();

        // Set ListView Adapter
        eventListArray = new ArrayList<>();
        eventListAdapter = new EventListAdapter(this, R.layout.listview_row_noimage, R.id.wo_contentTextView, eventListArray);
        timeLineListView = findViewById(R.id.ListView1);
        timeLineListView.setAdapter(eventListAdapter);
        jsonLoader = new LoadTodayJson(this);
        bmpLoader = new LoadImageUrlToBmp(this);

        // Dynamic loading / paging initialize
        bPendingJsonLoader = false;
        bPendingBmpLoader = false;
        RefreshListViewFromCurrent_();

        // Load more on scroll down
        timeLineListView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int targetChildId = timeLineListView.getChildCount() - 1;
                boolean bTimeToLoadMore = timeLineListView!=null &&
                        timeLineListView.getAdapter()!=null &&
                        timeLineListView.getLastVisiblePosition() == (timeLineListView.getAdapter().getCount() - 1) &&
                        timeLineListView.getChildAt(targetChildId)!=null &&
                        timeLineListView.getChildAt(targetChildId).getBottom() <= timeLineListView.getHeight();
                boolean bLegitToLoadMore = (!bPendingJsonLoader) && // No pending JSon to Load
                        (!bPendingBmpLoader);  // No pending BMP to load
                boolean bHaveMoreToLoad = jsonArrayEvents!=null && (jsonArrayEventsLastReadIdx < jsonArrayEvents.length());
                if ( bTimeToLoadMore && bLegitToLoadMore && bHaveMoreToLoad)
                {
                    bPendingBmpLoader = Boolean.TRUE;
                    PopulateTimeline_();
                    // eventListAdapter.notifyDataSetChanged();
                    FloatPrompt.Show(getApplicationContext(), "Loading more events");
                }
            }
        });

        Log.d("LOADER", "Initialized");
    }

    private void initializeTopBanner() {
        final int textColor = Color.argb(0xFF, 177-80, 198-80, 207-80);
        final Typeface fontFamily = ResourcesCompat.getFont(this, R.font.corbelb);
        // Current Stream Date
        dateTextView = findViewById(R.id.TextBoxDate);
        dateTextView.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                // create new textView and set the properties like clolr, size etc
                TextView myText = new TextView(Main2Activity.this);
                myText.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                myText.setTextSize(20);
                myText.setTextColor(textColor);
                myText.setTypeface(fontFamily);
                myText.setShadowLayer(1, 0, 0, Color.BLACK);
                myText.setPadding(0, 0, 0, 0);
                return myText;
            }
        });
        dateTextView.setText("Today");

        // City Code
        townName = findViewById(R.id.textView4);
        townName.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                // create new textView and set the properties like clolr, size etc
                TextView myText = new TextView(Main2Activity.this);
                myText.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                myText.setTextSize(30);
                myText.setTextColor(textColor);
                myText.setTypeface(fontFamily);
                myText.setShadowLayer(2, 0, 0, Color.BLACK);
                myText.setPadding(0, 4, 0, 0);
                return myText;
            }
        });
    }

    private void initializeBotBanner() {
        final int progressBarColor = Color.argb(0xFF, 177, 198, 207);
        // progress bar
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setProgressTintList(ColorStateList.valueOf(progressBarColor));

        // Current location button
        final View buttonLocation = findViewById(R.id.imageButton2);
        buttonLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // check if GPS enabled
                if (!bPendingGpsUpdate) {
                    bPendingGpsUpdate = true;
                    Log.d("VERBOSE", "Calling GPSTracker");
                    m_gpsTracker.getLocation(); // update location
                }
            }
        });

        // initialize Place picker
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0, this)
                .addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API)
                .build();

        // Location picker button
        final View buttonExplore = findViewById(R.id.imageButton3);
        buttonExplore.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(Main2Activity.this), PLACE_PICKER_REQUEST);
                    // Log.d("LocationPicker", "Closed the activity");
                } catch (GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException e) {
                    // Log.d("LocationPicker", "Service unavailable");
                    e.printStackTrace();
                }
            }
        });

        // SEARCH EVENTS
        SearchBoxHide_();
        SearchReset(); // clear up the current query

        // Search button callback
        final View buttonSearch = findViewById(R.id.imageButton4);
        buttonSearch.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                SearchBoxShow_();
            }
        });

        // Managing search box
        final EditText searchQueryEditBox = findViewById(R.id.searchQuery);
        searchQueryEditBox.setOnEditorActionListener(
            new EditText.OnEditorActionListener(){
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                            actionId == EditorInfo.IME_ACTION_DONE ||
                            event != null &&
                            event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        if (event == null || !event.isShiftPressed()) {
                            // The user is done typing.
                            SearchSetQuery_(v.getText().toString());
                            // remove search box
                            SearchBoxHide_();
                            // Load into pipeline
                            FlushTimeline_(); // FIXME: this does not flush timeline
                            TextSwitcher tw = findViewById(R.id.TextBoxDate);
                            TextView tv = (TextView)tw.getCurrentView();
                            FloatPrompt.Show(v.getContext(), "Searching " + tv.getText().toString());
                            PopulateTimeline_();
                            return true; // consume.
                        }
                    }
                    return false;
                }
            }
        );

        // Calendar navigation
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

        // Calendar button
        final View buttonCalendar = findViewById(R.id.imageButton);
        buttonCalendar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                StartTime.show();
            }
        });
    }

    /*
     * Search related routines
     */
    public void SearchBoxHide_()
    {
        LinearLayout searchLayout = findViewById(R.id.searchLayout);
        searchLayout.setVisibility(LinearLayout.GONE);
        EditText searchQueryEditBox = findViewById(R.id.searchQuery);
        searchQueryEditBox.clearFocus();

        // remove keyboard
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchQueryEditBox.getWindowToken(), 0);
        }
    }

    private void SearchBoxShow_()
    {
        LinearLayout searchLayout = findViewById(R.id.searchLayout);
        searchLayout.setVisibility(LinearLayout.VISIBLE);
        TextView prompt = findViewById(R.id.searchInteractivePrompt);
        prompt.setText(getResources().getString(R.string.searchFirstPrompt));

        // set focus to EditBox
        final EditText searchQueryEditBox = findViewById(R.id.searchQuery);
        searchQueryEditBox.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(searchQueryEditBox, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private static boolean SearchString(String src, String what) {
        // Coded by: https://stackoverflow.com/a/25379180/1501388
        final int length = what.length();
        if (length == 0)
            return true; // Empty string is contained

        final char firstLo = Character.toLowerCase(what.charAt(0));
        final char firstUp = Character.toUpperCase(what.charAt(0));

        for (int i = src.length() - length; i >= 0; i--) {
            // Quick check before calling the more expensive regionMatches() method:
            final char ch = src.charAt(i);
            if (ch != firstLo && ch != firstUp)
                continue;

            if (src.regionMatches(true, i, what, 0, length))
                return true;
        }

        return false;
    }

    private void SearchReset()
    {
        searchQueryString = "";
    }

    private void SearchSetQuery_(String keyWord)
    {
        searchQueryString = keyWord;
    }

    public String SearchGetQuery(){
        return searchQueryString;
    }

    public String SearchHighlightMatch(String textBody){
        String searchQuery = SearchGetQuery();
        String regex = "(?i)"+searchQuery;
        if (searchQuery.equals("")) {
            return textBody;
        }else{
            return textBody.replaceAll(regex, "<font color='#EE0000'>" + searchQuery + "</font>");
        }

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
    public void onDestroy(){
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Log.d("LocationPicker", "ResultCalled ");
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                float latitude = (float)place.getLatLng().latitude;
                float longitude = (float)place.getLatLng().longitude;
                RefreshListViewFromGeo_(latitude, longitude);
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        FloatPrompt.Show(getApplicationContext(), "connection failed");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
                FloatPrompt.Show(getApplicationContext(), "Oops location not covered");
                FloatPrompt.Show(getApplicationContext(), "Showing nearest location");
            }
            townName.setText(cityName);
        }
    }

    private void RefreshListViewFromDate_(Date dateToLoad){
        if(LaunchAsyncJsonLoader_(currentLocationCode, dateToLoad)) {

            String formattedDate = new SimpleDateFormat("EEE MMM dd", Locale.CANADA).format(currentDate);
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

    private void PopulateTimeline_()
    {
        final int k_maxFetchPerRound = 10;   // number of events to load every round
        // Process new JSon
        try
        {
            boolean jsonHasAuthors = jsonArrayAuthors.length()!=0;
            // boolean bIsTwitterAppInstalled = isTwitterAppInstalled();
            ArrayList<String> mediaUrls = new ArrayList<>();
            int nEnqueued = 0;
            while((nEnqueued<k_maxFetchPerRound) && (jsonArrayEventsLastReadIdx < jsonArrayEvents.length())) {
                // Get next element
                JSONObject jo_inside = jsonArrayEvents.getJSONObject(jsonArrayEventsLastReadIdx);
                jsonArrayEventsLastReadIdx += 1;

                // Verify state
                String status = jo_inside.getString("status");
                if( status.equals("merged") || status.equals("dead")){
                    // redundant; no need to include this entry
                    continue;
                }

                // get author, text, ml_rating and media
                String author = jo_inside.getString("prettyauthor");
                String text = jo_inside.getString("prettytext");
                if (!searchQueryString.equals(""))
                {
                    if (!SearchString(text, searchQueryString)){
                        // This should not be shown in results
                        continue;
                    }

                }
                boolean bExpanded = !TextViewExpandableIsLong_(text);
                String textShort = TextViewExpandableGetShort_(text);
                List<String> allmedia = Arrays.asList(jo_inside.getString("media").split(",")); // FIXME: not tested
                String media = allmedia.get(0);
                int authorId = -1;
                if((media.equals("") || media.equals("none")) && jsonHasAuthors && jo_inside.has("authorid") && jo_inside.getInt("authorid")!=-1){
                    // lack of media, use the author cover photo
                    authorId = jo_inside.getInt("authorid");
                    media = jsonArrayAuthors.getJSONObject(authorId).getString("coverphoto");
                    // Log.d("MediaURL", "has authorid> "+author);
                }
                float ml_rating = Float.valueOf(jo_inside.getString("ml_rating"));

                // get callback URL
                String eventsource = jo_inside.getString("source");
                Object thing = jo_inside.get("tweetid");
                BigInteger id = BigInteger.ZERO;
                String url = "";
                if(thing instanceof String){
                    id = new BigInteger(jo_inside.getString("tweetid"));
                    switch (eventsource){
                        case "Twitter":
                            // Event extracted from Twitter tweets
                            url = "https://twitter.com/i/web/status/" + jo_inside.getString("tweetid");
                            break;
                        case "FBEvent":
                            // Facebook Event
                            url = "https://www.facebook.com/events/" + jo_inside.getString("tweetid");
                            break;
                        case "Instagram":
                            // Event extracted from Instagram posts
                            url = jo_inside.getString("url");
                            break;
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
                newEntry.ml_rating = ml_rating;
                newEntry.prettyauthor = author;
                newEntry.bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.loadinggrey);
                newEntry.bExpanded = bExpanded;
                newEntry.textShort = textShort;

                // Push to Array
                eventListArray.add(newEntry);

                // Update stats
                nEnqueued+=1;
            }
            if (eventListArray.size() == 0) {
                // Show no event found
                FloatPrompt.Show(this, "No event found");
            }else{
                // Invoke image loader and fetch images
                Log.d("MainThread", "evenListArray size: " + String.valueOf(eventListArray.size()));
                Log.d("MainThread", "mediaUrl size: " + String.valueOf(mediaUrls.size()));
            }
            LaunchAsyncBmpLoader_(mediaUrls);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    /*
    Cache Management
     */
    private void CacheNewLocation(float lat, float lng)
    {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(k_preferenceName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(k_sPLastGeoLat, lat);
        editor.putFloat(k_sPLastGeoLng, lng);
        editor.apply();
    }

    private void FlushTimeline_()
    {
        timeLineListView.smoothScrollToPosition(0);
        eventListArray.clear();
        jsonArrayEventsLastReadIdx = 0;
        eventListAdapter.notifyDataSetChanged();
    }

    /*
    AsyncCommunications: TX
     */
    private Boolean LaunchAsyncJsonLoader_(String cityCode, Date dateToLoad)
    {
        // Cancel pending Async if any
        if (bPendingJsonLoader){
            asyncTaskJsonLoader.cancel(true);
        }
        if(bPendingBmpLoader){
            asyncTaskBmpLoader.cancel(true);
        }
        FloatPrompt.Show(this, "Loading events");

        // Launch new Async task
        FlushTimeline_();
        SearchReset();
        currentLocationCode = cityCode;
        currentDate = dateToLoad;
        asyncTaskJsonLoader = jsonLoader.updateListView(currentLocationCode, currentDate);
        bPendingJsonLoader = Boolean.TRUE;

        // Log Firebase
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "FetchCode");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, cityCode);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "text");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        return Boolean.TRUE;
    }

    private void LaunchAsyncBmpLoader_(ArrayList<String> mediaUrls)
    {
        // Cancel pending Async if any
        if(bPendingBmpLoader){
            asyncTaskBmpLoader.cancel(true);
        }

        // Launch new Async task
        asyncTaskBmpLoader = bmpLoader.Load(mediaUrls.toArray(new String[0]));
        bPendingBmpLoader =Boolean.TRUE;
    }

    /*
    AsynCommunications: RX
     */
    public void ReceiveBmpListAsync(ArrayList<Bitmap> bmpList){
        if (!bPendingBmpLoader){
            throw new AssertionError("Unexpected BMP batch received");
        }

        // Process new BMP batch
        int offset = eventListArray.size()-bmpList.size();
        for(int i = 0; i < bmpList.size(); i++){
            EventInstance eventInstance = eventListArray.get(i+offset);
            eventInstance.bmp = bmpList.get(i);
            eventListArray.set(i+offset, eventInstance);
        }
        eventListAdapter.notifyDataSetChanged();
        bPendingBmpLoader = Boolean.FALSE;
    }

    public void ReceiveJsonResponseAsync(String json){
        if (!bPendingJsonLoader){
            throw new AssertionError("Unexpected JSON received");
        }

        // Process new JSon
        try {
            JSONObject obj = new JSONObject(json);
            jsonArrayEvents = obj.getJSONArray("data");
            jsonArrayAuthors = new JSONArray();
            boolean jsonHasAuthors = obj.has("authors");
            if(jsonHasAuthors){
                jsonArrayAuthors = obj.getJSONArray("authors");
            }
            PopulateTimeline_();
            // Pre-load ads for this json
            adLoader.loadAds(new AdRequest.Builder().build(), jsonArrayEvents.length() / k_eventToAdRatio + 1);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        bPendingJsonLoader = Boolean.FALSE;
    }

    public void ReceiveNewLocationFromGPS(Location location){
        if(location!=null){
            float latitude = (float) location.getLatitude();
            float longitude = (float) location.getLongitude();
            RefreshListViewFromGeo_(latitude, longitude);
        }else{
            Log.d("VERBOSE", "Location is null");
        }
        bPendingGpsUpdate = false;
    }

    /*
    Handlers for click on elements in ItemView
     */
    public void EventClickOpenUrl(int position){
        EventInstance eventInstance = eventListArray.get(position - GetNumLoadedAds(position));
        String sourceUrl = eventInstance.url;
        if (IsAdPosition(position)) {
            // TODO: Not sure if bundle should have specific fields
            Bundle extraInfo = new Bundle();
            extraInfo.putString("itemClicked", "ad_media");
            GetAd(GetNumLoadedAds(position)).performClick(extraInfo);
        } else if (sourceUrl.equals("")) {
            FloatPrompt.Show(this, "No link to open");
        } else {
            // Open a URL
            final boolean bOpenUrlWithChrome = false; // TODO: Move this to app/settings
            if (bOpenUrlWithChrome) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl));
                startActivity(browserIntent);
            } else {
                Intent inAppBrowser = new Intent(Main2Activity.this, WebViewActivity.class);
                startActivity(inAppBrowser.putExtra("urlToShow", sourceUrl));
            }
        }
    }

    public void EventClickExpandCollapseText(int position){
        EventInstance eventInstance = eventListArray.get(position - GetNumLoadedAds(position));
        if (!IsAdPosition(position)) {
            // collapse if expanded, expand if collapsed
            eventInstance.bExpanded = !eventInstance.bExpanded;
            eventListAdapter.notifyDataSetChanged();
        }
    }

    public void EventClickShare(int position) {
        EventInstance eventInstance = eventListArray.get(position - GetNumLoadedAds(position));
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check this out: "+eventInstance.url + " via GlobeNow Android App");
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share link via"));
    }

    /*
    AdMob
     */
    UnifiedNativeAd GetAd(int idx){
        if(unifiedNativeAdArrayList.size()!=0) {
            return unifiedNativeAdArrayList.get(idx % unifiedNativeAdArrayList.size());
        } else {
            return null;
        }
    }

    int GetEventToAdRatio(){
        return k_eventToAdRatio;
    }

    boolean IsAdPosition(int position) {
        // Load only one ad atm
        int positionRemainder = (position % GetEventToAdRatio());
        return (positionRemainder == k_adLocationOrder) && (GetNumLoadedAds(position) == 0);
        /*
        return positionRemainder == k_adLocationOrder;
        */
    }

    int GetNumLoadedAds(int position){
        // Load only one ad atm
        if (position <= k_adLocationOrder){
            return 0;
        }else{
            return 1;
        }
        /*
        int positionRemainder = (position % GetEventToAdRatio());
        int nAdsSoFar = position / GetEventToAdRatio();
        if (positionRemainder > k_adLocationOrder) {
            nAdsSoFar += 1;
        }
        return nAdsSoFar;
        */
    }

    /*
    TextViewExpandable
     */
    boolean TextViewExpandableIsLong_(String text)
    {
        // Count new lines
        int nNewLines = 0;
        for (int i = 0; i < text.length(); ++i){
            if (text.charAt(i) == '\n'){
                nNewLines += 1;
            }
        }
        return (nNewLines > k_textViewExpandableNNewLine) || (text.length() > k_textViewExpandableNChars);
    }

    String TextViewExpandableGetShort_(String text){
        final String k_readMoreInBold = "<b> Read More </b>"; //"\uD835\uDC2B\uD835\uDC1E\uD835\uDC1A\uD835\uDC1D \uD835\uDC26\uD835\uDC28\uD835\uDC2B\uD835\uDC1E";
        int breakIdx = 0;
        int nNewLines = 0;
        boolean bTimeToBreak = false;
        for(int i = 0; i < text.length(); ++i){
            char cChar = text.charAt(i);
            if (cChar == '\n'){
                nNewLines += 1;
            }
            if (nNewLines > k_textViewExpandableNNewLine || i > k_textViewExpandableNChars){
                bTimeToBreak = true;
            }
            if (bTimeToBreak && Character.isWhitespace(cChar)){
                breakIdx = i;
                break;
            }
        }
        return text.substring(0, breakIdx) + " ᠁ " + k_readMoreInBold;
    }
}
