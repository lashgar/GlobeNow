package myapps.globenow;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;

import java.util.ArrayList;

class ImageInspect {

    private Activity context;
    private AsyncTask<BmpConfig, Void, ArrayList<Bitmap>> asyncTask;

    ImageInspect(Activity context){
        this.context = context;
        asyncTask = null;
    }

     void OpenLargeImage(String imageUrl) {
        LoadImage_(imageUrl);
    }

    /*
    @brief invoke a background asynTask to download imageUrl, calls ShowImage_ once bmp ready
     */
    private void LoadImage_(String imageUrl){
        // Wrap URL in bmpConfig array
        BmpConfig bmpConfig = new BmpConfig();
        bmpConfig.bLoadShort = false;
        bmpConfig.url = imageUrl;
        BmpConfig[] bmpConfigArr = {bmpConfig};
        // create callback
        Utilities.LoadImageUrlCallback callback = new Utilities.LoadImageUrlCallback() {
            @Override
            public void ReceiveBmpList(ArrayList<Bitmap> bmpList) {
                if (bmpList.size() > 0) {
                    ShowImage_(bmpList.get(0));
                } else {
                    Log.d("ImageInspect", "unexpected reply length");
                }
            }
        };
        // Cancel previous asyncTask if any
        if (asyncTask != null) {
            asyncTask.cancel(true);
        }
        // Launch async task
        final boolean bUpdateProgressBar = false;
        final boolean bRoundCorners = false;
        asyncTask = new AsyncBmpLoader(callback, this.context, bUpdateProgressBar, bRoundCorners).execute(bmpConfigArr);
    }

    @SuppressWarnings({"InflateParams"})
    private void ShowImage_(Bitmap bmp){
        // Dismiss task
        asyncTask = null;
        // Load bmp into dialog
        Dialog settingsDialog = new Dialog(context);
        Window window = settingsDialog.getWindow();
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
            settingsDialog.setContentView(context.getLayoutInflater().inflate(R.layout.show_large_image, null, false));

            ImageView imageView = settingsDialog.findViewById(R.id.largeImage);
            imageView.setImageBitmap(bmp);
            settingsDialog.show();
        } else {
            Log.d("ImageInspect", "Unable to get window");
        }
    }
}
