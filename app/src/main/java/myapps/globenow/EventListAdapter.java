package myapps.globenow;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import java.util.List;

/**
 * Created by Ahmad on 2017-11-28.
 */
public class EventListAdapter extends ArrayAdapter<EventInstance> {
    private Activity context;

    public EventListAdapter(Activity context, int resource, int textViewID, List<EventInstance> eventsList){
        super(context, resource, textViewID, eventsList);
        this.context=context;
    }
    public View getView(int position, View view, ViewGroup parent) {
        Log.d("LOADER", "Returning view");
        Main2Activity main2Activity =((Main2Activity)context);

        // Calculate the position to load considering loaded ads
        // Making sure ads are interleaved while all events are loaded
        EventInstance eventInstance = getItem(position-main2Activity.GetNumLoadedAds(position));
        View rowView;

        if (main2Activity.IsAdPosition(position))
        {
            LayoutInflater inflater = (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            UnifiedNativeAdView adView = (UnifiedNativeAdView) inflater
                    .inflate(R.layout.listview_row_ad, null, true);
            UnifiedNativeAd ad = main2Activity.GetAd(main2Activity.GetNumLoadedAds(position));
            if (ad != null) {
                TextView bodyView = adView.findViewById(R.id.ad_text);
                bodyView.setText(ad.getBody());
                TextView headlineView = adView.findViewById(R.id.ad_header);
                headlineView.setText("Sponsored ad by "+ad.getHeadline());
                MediaView mediaView = adView.findViewById(R.id.ad_media);
                adView.setMediaView(mediaView);
                adView.setNativeAd(ad);
            }
            rowView = adView;
        }
        else if(!eventInstance.media.equals("") && !eventInstance.media.equals("none"))
        {
            // With image
            // inflate the row
            LayoutInflater inflater=context.getLayoutInflater();
            rowView=inflater.inflate(R.layout.listview_row_withimage, null,true); // specify the desired layout

            //this code gets references to objects in the listview_row_withimage.xml file
            TextView nameTextField = rowView.findViewById(R.id.textView8);
            TextView infoTextField = rowView.findViewById(R.id.textView9);
            ImageView imageView = rowView.findViewById(R.id.imageView6);

            //this code sets the values of the objects to values from the arrays
            String textBody = eventInstance.bExpanded ? eventInstance.prettytext : eventInstance.textShort;
            nameTextField.setText(textBody);
            String author = "By "+eventInstance.prettyauthor;
            infoTextField.setText(author);
            imageView.setImageBitmap(eventInstance.bmp);
        }
        else
        {
            // Without image
            LayoutInflater inflater=context.getLayoutInflater();
            rowView=inflater.inflate(R.layout.listview_row_noimage, null,true); // specify the desired layout

            //this code gets references to objects in the listview_row_noimage.xml file
            TextView nameTextField = rowView.findViewById(R.id.textView2);
            TextView infoTextField = rowView.findViewById(R.id.textView3);

            //this code sets the values of the objects to values from the arrays
            String textBody = eventInstance.bExpanded ? eventInstance.prettytext : eventInstance.textShort;
            nameTextField.setText(textBody);
            String author = "By "+eventInstance.prettyauthor;
            infoTextField.setText(author);
        }
        return rowView;
    }

}
