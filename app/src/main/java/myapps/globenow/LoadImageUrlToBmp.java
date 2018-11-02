package myapps.globenow;

import android.app.Activity;
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
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Ahmad on 2017-11-28.
 */

public class LoadImageUrlToBmp {
    //to reference the Activity
    private final Activity context;
    public LoadImageUrlToBmp(Activity context){
        this.context = context;
    }

    private Bitmap GenDummyBmp_(){
        byte[] data = new byte[4];
        return BitmapFactory.decodeByteArray(data, 0, 3);
    }
    private Bitmap GetRoundedCornerBitmap_(Bitmap org) {
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

    public AsyncTask<String, Void, ArrayList<Bitmap>> Load(String[] mediaUrls){
        return new LoadImage().execute(mediaUrls);
    }

    private class LoadImage extends AsyncTask<String, Void, ArrayList<Bitmap>> {
        @Override
        protected ArrayList<Bitmap> doInBackground(final String... params) {
            context.runOnUiThread(new Runnable() {
                public void run() {
                    ProgressBar progressBar = context.findViewById(R.id.progressBar);
                    progressBar.setMax(params.length-1);
                    progressBar.setProgress(0);
                }
            });
            Bitmap emptyBmp = GenDummyBmp_();
            ArrayList<Bitmap> bmpList = new ArrayList<>();
            for(String imageUrl:  params)
            {
                context.runOnUiThread(new Runnable() {
                    public void run() {
                        ProgressBar progressBar = context.findViewById(R.id.progressBar);
                        progressBar.setProgress(progressBar.getProgress()+1);
                    }
                });
                Bitmap bmp = emptyBmp;
                try {
                    URL mediaUrl = new URL(imageUrl);
                    bmp = GetRoundedCornerBitmap_(BitmapFactory.decodeStream(mediaUrl.openConnection().getInputStream()));
                } catch (MalformedURLException ex) {
                    Log.d("DownloadImage::doInBackground", ex.toString());
                } catch (IOException ex) {
                    Log.d("DownloadImage::doInBackground", ex.toString());
                    // ex.printStackTrace();
                }
                bmpList.add(bmp);

                // Check if AsyncTask is signalled to cancel
                if (isCancelled()){
                    break;
                }
            }
            Log.d("DownloadImage::doInBackground", "bmpList length:"+String.valueOf(bmpList.size()));
            return bmpList;
        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> bmpList) {
            ((Main2Activity)context).ReceiveBmpListAsync(bmpList);
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}

    }
}
