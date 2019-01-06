package myapps.globenow;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
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

    EventListAdapter(Activity context, int resource, int textViewID, List<EventInstance> eventsList){
        super(context, resource, textViewID, eventsList);
        this.context=context;
    }

    @NonNull
    public View getView(final int position, View view, @NonNull ViewGroup parent) {
        Log.d("LOADER", "Returning view");
        final Main2Activity main2Activity =((Main2Activity)context);
        final BackgroundAnimation animGenerator = new BackgroundAnimation();

        // Calculate the position to load considering loaded ads
        // Making sure ads are interleaved while all events are loaded
        EventInstance eventInstance = getItem(position-main2Activity.GetNumLoadedAds(position));
        boolean bHasTitle = eventInstance != null && !eventInstance.title.equals("");
        View rowView = new View(context);

        if (main2Activity.IsAdPosition(position))
        {
            // Inflate view with ad layout
            LayoutInflater inflater = (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null) {
                UnifiedNativeAdView adView = (UnifiedNativeAdView) inflater
                        .inflate(R.layout.listview_row_ad, parent, false);
                // Get the ad
                UnifiedNativeAd ad = main2Activity.GetAd(main2Activity.GetNumLoadedAds(position));
                // Fill the ad in layout
                if (ad != null) {
                    TextView bodyView = adView.findViewById(R.id.ad_text);
                    bodyView.setText(ad.getBody());
                    TextView headlineView = adView.findViewById(R.id.ad_header);
                    String textBody = "Sponsored ad by " + ad.getHeadline();
                    headlineView.setText(textBody);
                    MediaView mediaView = adView.findViewById(R.id.ad_media);
                    adView.setMediaView(mediaView);
                    adView.setNativeAd(ad);
                }
                // Attach callbacks to view
                // Convert adView to View
                rowView = adView;
            }
        }
        else{
            // Common elements
            TextView bodyTextField;
            TextView headerTextField;
            ImageButton shareButton;
            ImageButton openButton;
            ImageButton expandCollapseButton;
            boolean bLoadImage = (eventInstance != null) && (!eventInstance.media.equals("")) && !eventInstance.media.equals("none");
            if(bLoadImage)
            {
                // Inflate view with image layout
                LayoutInflater inflater=context.getLayoutInflater();
                // specify the desired layout
                if (main2Activity.IsWideScreen()) {
                    // Reduce image size
                    rowView=inflater.inflate(R.layout.listview_row_withimage_highdp, parent,false);
                    TextView titleTextField = rowView.findViewById(R.id.wi_titleTextView);
                    String textTitle = eventInstance.title;
                    titleTextField.setText(textTitle);
                } else if (bHasTitle) {
                    // Compact view as we have a nice title to show
                    rowView=inflater.inflate(R.layout.listview_row_withimage_compact, parent,false);
                    TextView titleTextField = rowView.findViewById(R.id.wi_titleTextView);
                    String textTitle = eventInstance.title;
                    titleTextField.setText(textTitle);
                } else {
                    rowView=inflater.inflate(R.layout.listview_row_withimage, parent,false);
                }
                // Get layout elements
                bodyTextField = rowView.findViewById(R.id.wi_contentTextView);
                headerTextField = rowView.findViewById(R.id.wi_authorTextView);
                ImageView imageView = rowView.findViewById(R.id.imageView6);
                // Fill the image
                if (eventInstance.bmp == null) {
                    imageView.setImageResource(R.mipmap.noimage);
                } else {
                    imageView.setImageBitmap(eventInstance.bmp);
                }
            }
            else
            {
                // Inflate view with no-image layout
                LayoutInflater inflater=context.getLayoutInflater();
                rowView=inflater.inflate(R.layout.listview_row_noimage, parent, false); // specify the desired layout
                // Get layout elements
                bodyTextField = rowView.findViewById(R.id.wo_contentTextView);
                headerTextField = rowView.findViewById(R.id.wo_authorTextView);
            }

            if (eventInstance == null){
                return rowView;
            }

            // Fill the event in view
            String textBody = eventInstance.bExpanded ? eventInstance.prettytext : eventInstance.textShort;
            // Fill body
            bodyTextField.setText(main2Activity.GetSpannedText(textBody));
            // Fill header
            boolean bLongAuthorName = eventInstance.prettyauthor.length()>50;
            String sanitizedAuthorName = bLongAuthorName? eventInstance.prettyauthor.substring(0,25) + " ᠁": eventInstance.prettyauthor;
            String author = "By " + sanitizedAuthorName;
            headerTextField.setText(author);

            // Get buttons
            shareButton = rowView.findViewById(R.id.shareButton);
            openButton = rowView.findViewById(R.id.openButton);
            expandCollapseButton = rowView.findViewById(R.id.expandCollapseButton);

            // Set proper view for expand/collapse button
            if (eventInstance.bExpanded) {
                expandCollapseButton.setImageResource(R.mipmap.iconcollapse);
            } else {
                expandCollapseButton.setImageResource(R.mipmap.iconexpand);
            }

            // Attach callbacks to actions on the view
            bodyTextField.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    // Do expand/collapse after OpenUrl to allow one click before OpenUrl
                    main2Activity.EventClickExpandCollapseText(position);
                }
            });
            shareButton.setBackground(animGenerator.GenerateTransitionDrawable(context));
            shareButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    animGenerator.RunAnimationCircleTransition(v);
                    main2Activity.EventClickShare(position);
                }
            });
            openButton.setBackground(animGenerator.GenerateTransitionDrawable(context));
            openButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    animGenerator.RunAnimationCircleTransition(v);
                    main2Activity.EventClickOpenUrl(position);
                }
            });
            expandCollapseButton.setBackground(animGenerator.GenerateTransitionDrawable(context));
            expandCollapseButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    animGenerator.RunAnimationCircleTransition(v);
                    main2Activity.EventClickExpandCollapseText(position);
                }
            });
        }
        return rowView;
    }

}
