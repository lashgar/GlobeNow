package myapps.globenow;

import android.graphics.Bitmap;

import java.math.BigInteger;

/**
 * Created by Ahmad on 2017-11-28.
 */

public class EventInstance {
    public String status;
    public String url;
    public String media;
    public String prettytext;
    public BigInteger tweetid;
    public String source;
    public int authorid;
    public String text;
    public float ml_rating;
    public String prettyauthor;
    public Bitmap bmp;
    public boolean bExpanded; // if true, click on the event opens url
    public String textShort;
}
