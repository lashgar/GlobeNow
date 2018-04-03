package myapps.globenow;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

import java.text.SimpleDateFormat;
import java.util.Date;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {
    // map
    private GoogleApiClient mGoogleApiClient;
    private int PLACE_PICKER_REQUEST = 1;

    private final int SPLASH_DISPLAY_LENGTH = 1000;

    // ListView eventListings;
    LoadTodayJson jsonLoader;
    // TextView townName;
    TextSwitcher townName;
    ProgressBar progressBar;
    TextSwitcher dateTextView;

    // Current view
    Date currentDate;
    String currentLocationCode;

    // GPSTracker gps;

    private void initializeMainActivity(){

        currentDate = new Date();
        currentLocationCode = "yyj";
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

        // Set ListView
        jsonLoader = new LoadTodayJson(this, (ListView)findViewById(R.id.ListView1));
        // townName = (TextView)findViewById(R.id.textView4);
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

        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        refreshListViewLocation(currentLocationCode, "Victoria", (float)0.0);

        // taskbar setup
        final View buttonlocation = findViewById(R.id.imageButton2);
        buttonlocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // check if GPS enabled
                GPSTracker gps = new GPSTracker(Main2Activity.this);
                if(gps.canGetLocation()){
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    String[] closetsTown = GPSTracker.getClosestCity(latitude, longitude);
                    String cityCode = closetsTown[0];
                    String cityName = closetsTown[1];
                    float distance = Float.parseFloat(closetsTown[2]);
                    // \n is for new line
                    // Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude + "\n " + cityName, Toast.LENGTH_LONG).show();
                    gps.stopUsingGPS();
                    refreshListViewLocation(cityCode, cityName, distance);
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
                        // activitydate.setText(dateFormatter.format(newDate.getTime()));
                        refreshListViewDate(newDate);
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
                // StringBuilder stBuilder = new StringBuilder();
                // String placename = String.format("%s", place.getName());
                String latitude = String.valueOf(place.getLatLng().latitude);
                String longitude = String.valueOf(place.getLatLng().longitude);
                String[] closetsTown = GPSTracker.getClosestCity(Float.parseFloat(latitude), Float.parseFloat(longitude));
                String cityCode = closetsTown[0];
                String cityName = closetsTown[1];
                float distance = Float.parseFloat(closetsTown[2]);
                refreshListViewLocation(cityCode, cityName, distance);
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
    };

    public void refreshListViewLocation(String citycode, String cityName, float distance){
        currentLocationCode = citycode;
        jsonLoader.updateListView(currentLocationCode, currentDate);

        Animation in = AnimationUtils.loadAnimation(this,android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this,android.R.anim.slide_out_right);

        // set the animation type of textSwitcher
        townName.setInAnimation(in);
        townName.setOutAnimation(out);

        if(distance>40){
            // note distance
            Toast.makeText(getApplicationContext(), "Our app is not tailored for this area, we show results of the closest city: "+cityName +" ("+Float.toString(distance)+" km away)", Toast.LENGTH_LONG).show();
        }
        townName.setText(cityName);
    };

    public void refreshListViewDate(Date dateToLoad){
        currentDate = dateToLoad;
        jsonLoader.updateListView(currentLocationCode, currentDate);

        String formattedDate = new SimpleDateFormat("EEE MMM, dd").format(currentDate);
        Date today = new Date();
        if(today.getTime()==dateToLoad.getTime()){
            // FIXME
            formattedDate = "Today";
        }
        Animation in = AnimationUtils.loadAnimation(this,android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this,android.R.anim.slide_out_right);

        // set the animation type of textSwitcher
        dateTextView.setInAnimation(in);
        dateTextView.setOutAnimation(out);

        dateTextView.setText(formattedDate);
    };

}
