package myapps.globenow;

import android.app.Activity;
import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Created by Ahmad on 2017-11-28.
 */

public class LoadTodayJson {
    //to reference the Activity
    private final Activity context;

    public LoadTodayJson(Activity context){
        this.context = context;
    }

    private class DownloadJson extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String json = "";
            try{
                String serverAddr = context.getResources().getString(R.string.globenow_json_server);
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

                    // Check if AsyncTask is signalled to cancel
                    if (isCancelled()){
                        break;
                    }
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
            return json;
        }

        @Override
        protected void onPostExecute(String jsonString) {
            ((Main2Activity)context).ReceiveJsonResponseAsync(jsonString);
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}

    }

    public AsyncTask<String, Void, String> updateListView(String citycode, Date dateToLoad) {
        // String encodedURL = "http://ece.uvic.ca/"+URLEncoder.encode("~lashgar/rss/yvr/2017-11-20.json","UTF-8");
        String dateFile = new SimpleDateFormat("yyyy-MM-dd").format(dateToLoad)+".json";
        String location = citycode+"/";
        return new DownloadJson().execute(location+dateFile);
    }
}
