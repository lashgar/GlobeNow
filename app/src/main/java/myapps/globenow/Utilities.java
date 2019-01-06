package myapps.globenow;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class Utilities {
    interface LoadImageUrlCallback {
        void ReceiveBmpList(ArrayList<Bitmap> bmpList);
    }
}
