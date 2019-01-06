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

/*
@brief for image processing kernels
 */
class BmpLib {
    //to reference the Activity
    private final Activity context;

    BmpLib(Activity context){
        this.context = context;
    }

    Bitmap GenDummyBmp(){
        byte[] data = new byte[4];
        return BitmapFactory.decodeByteArray(data, 0, 3);
    }

    Bitmap ScaleToView(Bitmap org, Boolean bLoadShort){
        // Calculate new dimension to fit in view
        Point p = new Point();
        context.getWindowManager().getDefaultDisplay().getSize(p);
        float desiredWidth = !bLoadShort ? (p.x - 60) : (p.x / 3 - 60) ;
        float rate = (desiredWidth)/(float)org.getWidth();
        int newW = (int)(rate*org.getWidth());
        int newH = (int)(rate*org.getHeight());

        // Scale image into view
        return Bitmap.createScaledBitmap(org, newW, newH, false);
    }

    Bitmap GetRoundedCornerBitmap(Bitmap input) {
        int newW = input.getWidth();
        int newH = input.getHeight();

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
}
