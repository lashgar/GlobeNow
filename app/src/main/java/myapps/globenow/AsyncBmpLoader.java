package myapps.globenow;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;

/*
@brief downloads a array of imageUrls and return bmp array
 */
public class AsyncBmpLoader extends AsyncTask<BmpConfig, Void, ArrayList<Bitmap>> {
    private final Utilities.LoadImageUrlCallback callback;
    private WeakReference<Activity> activityReference;
    private BmpLib bmpLib;
    private final boolean bUpdateProgressBar;
    private final boolean bRoundCorners;

    AsyncBmpLoader(Utilities.LoadImageUrlCallback callback, Activity context, boolean bUpdateProgressBar, boolean bRoundCorners){
        this.activityReference = new WeakReference<>(context);
        this.callback = callback;
        this.bUpdateProgressBar = bUpdateProgressBar;
        this.bRoundCorners = bRoundCorners;
        bmpLib = new BmpLib(context);
    }

    @Override
    protected ArrayList<Bitmap> doInBackground(final BmpConfig... params) {
        final Activity context = activityReference.get();
        if (bUpdateProgressBar) {
            context.runOnUiThread(new Runnable() {
                public void run() {
                    ProgressBar progressBar = context.findViewById(R.id.progressBar);
                    progressBar.setMax(params.length - 1);
                    progressBar.setProgress(0);
                }
            });
        }
        Bitmap emptyBmp = bmpLib.GenDummyBmp();
        ArrayList<Bitmap> bmpList = new ArrayList<>();
        for(BmpConfig bmpConfig:  params)
        {
            String imageUrl = bmpConfig.url;
            Boolean bLoadShort = bmpConfig.bLoadShort;
            if (bUpdateProgressBar) {
                context.runOnUiThread(new Runnable() {
                    public void run() {
                        ProgressBar progressBar = context.findViewById(R.id.progressBar);
                        progressBar.setProgress(progressBar.getProgress() + 1);
                    }
                });
            }
            Bitmap bmp = emptyBmp;
            try {
                URL mediaUrl = new URL(imageUrl);
                Bitmap bmpFromSource = BitmapFactory.decodeStream(mediaUrl.openConnection().getInputStream());
                // Log.d("LDIMG", "Source dim: " + bmpFromSource.getWidth() + "x" + bmpFromSource.getHeight());
                bmpFromSource = bmpLib.ScaleToView(bmpFromSource, bLoadShort);
                // Log.d("LDIMG", "Scaling dim: " + bmpFromSource.getWidth() + "x" + bmpFromSource.getHeight());
                if (bRoundCorners) {
                    bmp = bmpLib.GetRoundedCornerBitmap(bmpFromSource);
                } else {
                    bmp = bmpFromSource;
                }

            } catch (IOException ex) {
                Log.d("LDIMG::doInBackground", ex.toString());
                // ex.printStackTrace();
            }
            bmpList.add(bmp);

            // Check if AsyncTask is signalled to cancel
            if (isCancelled()){
                break;
            }
        }
        // Log.d("LDIMG::doInBackground", "bmpList length:"+String.valueOf(bmpList.size()));
        return bmpList;
    }

    @Override
    protected void onPostExecute(ArrayList<Bitmap> bmpList) {
        this.callback.ReceiveBmpList(bmpList);
    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onProgressUpdate(Void... values) {}
}
