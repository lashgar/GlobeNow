package myapps.globenow;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import java.util.ArrayList;

/**
 * Created by Ahmad on 2017-11-28.
 */

class LoadImageUrlToBmp {
    private Activity context;
    private Utilities.LoadImageUrlCallback callback;

    LoadImageUrlToBmp(Activity context, Utilities.LoadImageUrlCallback callback){
        this.context = context;
        this.callback = callback;
    }

    AsyncTask<BmpConfig, Void, ArrayList<Bitmap>> Load(BmpConfig[] bmpConfigArr){
        final boolean bUpdateProgressBar = true;
        final boolean bRoundCorners = true;
        return new AsyncBmpLoader(this.callback, this.context, bUpdateProgressBar, bRoundCorners).execute(bmpConfigArr);
    }
}
