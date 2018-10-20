package myapps.globenow;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
// import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
// import android.widget.TextView;
import android.widget.Toast;

// import com.twitter.sdk.android.core.Twitter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

// import static android.provider.AlarmClock.EXTRA_MESSAGE;
// import static com.google.android.gms.analytics.internal.zzy.e;

/**
 * Created by Roshe on 2017-11-28.
 */

public class LoadTodayJson {
    //to reference the Activity
    private final Activity context;
    private ListView listview;

    public LoadTodayJson(Activity context, ListView listView1){
        this.context = context;
        this.listview = listView1;
    }
    private class DownloadJson extends AsyncTask<String, Void, String> {
        List<String> textList = new ArrayList<String>();
        List<String> authorList = new ArrayList<String>();
        List<String> mediaList = new ArrayList<String>();
        List<Bitmap> bitmapList = new ArrayList<Bitmap>();
        List<Integer> imageList = new ArrayList<Integer>();
        List<String> urlList = new ArrayList<String>();
        List<BigInteger> idList = new ArrayList<BigInteger>();

        private boolean isTwitterAppInstalled(){
            try{
                ApplicationInfo info = context.getPackageManager().
                        getApplicationInfo("com.twitter.android", 0 );
                return true;
            } catch( PackageManager.NameNotFoundException e ){
                return false;
            }
        }
        private Bitmap getDummyBmp(){
            byte[] data = new byte[4];
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, 3);
            return bmp;
        }
        public Bitmap getRoundedCornerBitmap(Bitmap org) {
            // Bitmap output = Bitmap.createBitmap(bitmap);
            // scale image into view
            Point p = new Point();
            context.getWindowManager().getDefaultDisplay().getSize(p);
            float rate = (float)(p.x-60)/(float)org.getWidth();
            int neww = (int)(rate*org.getWidth());
            int newh = (int)(rate*org.getHeight());
            Bitmap bitmap = Bitmap.createScaledBitmap(org, neww, newh, false);

            // round corners
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final RectF rectF = new RectF(rect);
            final float roundPx = 120;

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);

            return output;
        }
        @Override
        protected String doInBackground(String... params) {
            // Twitter.initialize(context);

            String json = "";
            try{
                String fileToLook = params[0]; // new SimpleDateFormat("yyyy-MM-dd").format(new Date())+".json";
                String encodedURL = serverAddr+fileToLook; //2017-11-20.json";
                // Log.d("doInBackground", encodedURL);
                URL url = new URL(encodedURL);
                // Read all the text returned by the server
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String str = "";
                while ((str = in.readLine()) != null) {
                    // Log.d("doInBackground", str);
                    // str is one line of text; readLine() strips the newline character(s)
                    json+=str;
                }
                in.close();
                // Log.d("doInBackground", json);
            } catch (MalformedURLException ex) {
                // Log.d("doInBackground", ex.toString());
                return ex.toString();
            } catch (IOException ex) {
                // Log.d("doInBackground", ex.toString());
                // ex.printStackTrace();
                return ex.toString();
            }
            // process the json
            try {
                JSONObject obj = new JSONObject(json);
                JSONArray jsonArrayEvents = obj.getJSONArray("data");
                JSONArray jsonArrayAuthors = new JSONArray();
                boolean jsonHasAuthors = obj.has("authors");
                if(jsonHasAuthors){
                    jsonArrayAuthors = obj.getJSONArray("authors");
                }
                final int length = Math.min(jsonArrayEvents.length(),24);
                context.runOnUiThread(new Runnable() {
                    public void run() {
                        ProgressBar progressBar = (ProgressBar) context.findViewById(R.id.progressBar);
                        progressBar.setMax(length-1);
                        progressBar.setProgress(0);
                    }
                });
                boolean bIsTwitterAppInstalled = isTwitterAppInstalled();
                for (int i = 0; i < length; i++) {
                    context.runOnUiThread(new Runnable() {
                        public void run() {
                            ProgressBar progressBar = (ProgressBar) context.findViewById(R.id.progressBar);
                            progressBar.setProgress(progressBar.getProgress()+1);
                        }
                    });

                    JSONObject jo_inside = jsonArrayEvents.getJSONObject(i);
                    //Log.d("Details-->", jo_inside.getString("formule"));
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
                    if((media.equals("") || media.equals("none")) && jsonHasAuthors && jo_inside.has("authorid") && jo_inside.getInt("authorid")!=-1){
                        // lack of media, use the author cover photo
                        media = jsonArrayAuthors.getJSONObject(jo_inside.getInt("authorid")).getString("coverphoto");
                        // Log.d("MediaURL", "has authorid> "+author);
                    }
                    textList.add(text);
                    authorList.add(author);
                    mediaList.add(media);

                    // get callback URL
                    String eventsource = jo_inside.getString("source");
                    Object thing = jo_inside.get("tweetid");
                    if(thing instanceof String){
                        BigInteger id = new BigInteger(jo_inside.getString("tweetid"));
                        if(eventsource.equals("Twitter")) {
                            // Event extracted from Twitter tweets
                            if (bIsTwitterAppInstalled) {
                                urlList.add("https://twitter.com/statuses/" + jo_inside.getString("tweetid"));
                            } else {
                                urlList.add("https://twitter.com/i/web/status/" + jo_inside.getString("tweetid"));
                            }
                            idList.add(id);
                        }else if(eventsource.equals("FBEvent")){
                            // Facebook Event
                            urlList.add("https://www.facebook.com/events/" + jo_inside.getString("tweetid"));
                            idList.add(id);
                        }else if(eventsource.equals("Instagram")){
                            // Facebook Event
                            urlList.add(jo_inside.getString("url"));
                            idList.add(id);
                        }else{
                            // unimplemented event
                            urlList.add("");
                            idList.add(BigInteger.ZERO);
                        }
                    }else{
                        urlList.add("");
                        idList.add(BigInteger.ZERO);
                    }

                    // Download the media
                    if(!media.equals("") && !media.equals("none")){
                        URL url = new URL(media);
                        try {
                            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                    // Log.d("MediaURL", "Malformed URL as Media");).getInputStream());
                            bitmapList.add(getRoundedCornerBitmap(bmp));
                        }catch (java.net.MalformedURLException ex){
                            bitmapList.add(getDummyBmp());
                            // Log.d("MediaURL", "IO Exception for URL as Media");
                        }catch (java.io.IOException ex) {
                            bitmapList.add(getDummyBmp());
                        }
                    }else{
                        bitmapList.add(getDummyBmp());
                    }
                }
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            /*
            ProgressBar progressBar = (ProgressBar) context.findViewById(R.id.
            context.runOnUiThread(new Runnable() {
                public void run() {
                }
            });progressBar);
                    progressBar.setVisibility(View.INVISIBLE);
            */
            return json;
        }

        @Override
        protected void onPostExecute(String jsonString) {
            String[] textArray = textList.toArray(new String[textList.size()]);
            String[] authorArray = authorList.toArray(new String[authorList.size()]);
            String[] mediaArray = mediaList.toArray(new String[mediaList.size()]);
            Bitmap[] bitmapArray = bitmapList.toArray(new Bitmap[bitmapList.size()]);
            Integer[] imageArray = imageList.toArray(new Integer[imageList.size()]);
            final String[] urlArray = urlList.toArray(new String[urlList.size()]);
            // final BigInteger[] idArray = idList.toArray(new BigInteger[idList.size()]);

            CustomListAdapter whatever = new CustomListAdapter(context, textArray, authorArray, imageArray, mediaArray, bitmapArray);
            listview.setAdapter(whatever);
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {
                    String sourceurl = urlArray[position];
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

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    public void updateListView(String citycode, Date dateToLoad) {
        // String encodedURL = "http://ece.uvic.ca/"+URLEncoder.encode("~lashgar/rss/yvr/2017-11-20.json","UTF-8");
        String dateFile = new SimpleDateFormat("yyyy-MM-dd").format(dateToLoad)+".json";
        String location = citycode+"/";
        new DownloadJson().execute(location+dateFile);
    }
}
