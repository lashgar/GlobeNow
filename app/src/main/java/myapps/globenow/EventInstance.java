package myapps.globenow;

import android.graphics.Bitmap;

import java.math.BigInteger;

/**
 * Created by Ahmad on 2017-11-28.
 */

class EventInstance {
    String status;
    String url;
    String media;
    String prettytext;
    BigInteger tweetid;
    String source;
    int authorid;
    String text;
    float ml_rating;
    String prettyauthor;
    Bitmap bmp;
    boolean bExpanded; // if true, click on the event opens url
    String textShort;
}
