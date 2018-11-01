package myapps.globenow;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
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

public class EventListAdapter extends ArrayAdapter<EventInstance> {
    private Activity context;
    private List<EventInstance> eventInstanceList;
    GradientDrawable gradientDrawable;

    public EventListAdapter(Activity context, int resource, int textViewID, List<EventInstance> eventsList){
        super(context, resource, textViewID, eventsList);
        this.context=context;
        this.eventInstanceList = eventsList;

        // gradient drawable
        gradientDrawable = new GradientDrawable();
        gradientDrawable.setColors(new int[]{0xFF610627,0x19654217});
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setSize(800,80);
    }
    public View getView(int position, View view, ViewGroup parent) {
        Log.d("LOADER", "Returning view");
        Main2Activity main2Activity =((Main2Activity)context);

        // Ad config
        int k_adLocationOrder = 2;
        int positionRemainder = (position % main2Activity.GetEventToAdRatio());
        boolean bIsAdPosition = positionRemainder == k_adLocationOrder;
        int nAdsSoFar = position / main2Activity.GetEventToAdRatio();
        if (positionRemainder > k_adLocationOrder){
            nAdsSoFar += 1;
        }

        // Calculate the position to load considering loaded ads
        // Making sure ads are interleaved while all events are loaded
        EventInstance eventInstance = getItem(position-nAdsSoFar);
        View rowView;

        if (bIsAdPosition) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            UnifiedNativeAdView adView = (UnifiedNativeAdView) inflater
                    .inflate(R.layout.listview_row_ad, null, true);
            UnifiedNativeAd ad = main2Activity.GetAd(nAdsSoFar);
            if (ad!=null) {
                TextView headlineView = adView.findViewById(R.id.ad_text);
                headlineView.setText("Sponsored ad by "+ad.getHeadline());
                MediaView mediaView = adView.findViewById(R.id.ad_media);
                adView.setMediaView(mediaView);
                adView.setNativeAd(ad);
            }
            rowView = adView;
        }
        else if(!eventInstance.media.equals("") && !eventInstance.media.equals("none")){
            // With image
            // inflate the row
            LayoutInflater inflater=context.getLayoutInflater();
            rowView=inflater.inflate(R.layout.listview_row_withimage, null,true); // specify the desired layout

            //this code gets references to objects in the listview_row_withimage.xml file
            TextView nameTextField = rowView.findViewById(R.id.textView8);
            TextView infoTextField = rowView.findViewById(R.id.textView9);
            ImageView imageView = rowView.findViewById(R.id.imageView6);

            //this code sets the values of the objects to values from the arrays
            nameTextField.setText(eventInstance.prettytext);
            String author = "by "+eventInstance.prettyauthor;
            infoTextField.setText(author);
            imageView.setImageBitmap(eventInstance.bmp);
        }else {
            // without image
            LayoutInflater inflater=context.getLayoutInflater();
            rowView=inflater.inflate(R.layout.listview_row_noimage, null,true); // specify the desired layout

            //this code gets references to objects in the listview_row_noimage.xml file
            TextView nameTextField = rowView.findViewById(R.id.textView2);
            TextView infoTextField = rowView.findViewById(R.id.textView3);

            //this code sets the values of the objects to values from the arrays
            nameTextField.setText(eventInstance.prettytext);
            String author = "By "+eventInstance.prettyauthor;
            infoTextField.setText(author);
        }
        return rowView;
    }

}
