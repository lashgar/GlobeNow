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
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Ahmad on 2017-11-28.
 */

class LoadImageUrlToBmp {
    //to reference the Activity
    private final Activity context;
    LoadImageUrlToBmp(Activity context){
        this.context = context;
    }

    private Bitmap GenDummyBmp_(){
        byte[] data = new byte[4];
        return BitmapFactory.decodeByteArray(data, 0, 3);
    }
    private Bitmap GetRoundedCornerBitmap_(Bitmap org) {
        final Main2Activity main2Activity =((Main2Activity)context);
        boolean bFullWidth = !main2Activity.IsWideScreen();

        // Calculate new dimension to fit in view
        Point p = new Point();
        context.getWindowManager().getDefaultDisplay().getSize(p);
        float desiredWidth = bFullWidth ? (p.x - 60) : (p.x / 3 - 60) ;
        float rate = (desiredWidth)/(float)org.getWidth();
        int newW = (int)(rate*org.getWidth());
        int newH = (int)(rate*org.getHeight());

        // Scale image into view
        Bitmap input = Bitmap.createScaledBitmap(org, newW, newH, false);
        // Generate target BMP to return
        Bitmap output = Bitmap.createBitmap(newW, newH, Bitmap.Config.ARGB_8888);

        // Attach target to canvas
        Canvas canvas = new Canvas(output);

        // Paint to use for styling draws
        final Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Rounded rectangle Rect and RectF
        final float k_roundPx = 120;
        final Rect rectLarge = new Rect(0, 0, newW, newH);
        final RectF rectFLarge = new RectF(rectLarge);

        // Create mask
        canvas.drawRoundRect(rectFLarge, k_roundPx, k_roundPx, paint);

        // Draw bitmap through mask
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(input,
            rectLarge /* subset of bitmap to draw */,
            rectFLarge /* rectangle destination to fill */,
            paint);

        // Draw outline
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        canvas.drawRoundRect(rectFLarge, k_roundPx, k_roundPx, paint);

        return output;
    }

    AsyncTask<String, Void, ArrayList<Bitmap>> Load(String[] mediaUrls){
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
            ((Main2Activity)context).ReceiveBmpListAsync(bmpList);
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}

    }
}
