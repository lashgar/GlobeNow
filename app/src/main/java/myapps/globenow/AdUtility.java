package myapps.globenow;

import android.app.Activity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;

import java.util.ArrayList;

public class AdUtility {
    // AdMob
    private ArrayList<UnifiedNativeAd> unifiedNativeAdArrayList;
    private AdLoader adLoader;
    private final int k_eventToAdRatio = 10; // show one ad for every 10 events
    private int k_adLocationOrder = 8 % k_eventToAdRatio;
    private Activity context;

    void init(Activity context){
        // Initialize AdMob
        this.context = context;

        unifiedNativeAdArrayList = new ArrayList<>();
        MobileAds.initialize(this.context, this.context.getResources().getString(R.string.admob_app_id));
        adLoader = new AdLoader.Builder(this.context, "/6499/example/native")
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        // Show the ad.
                        // Log.d("NewAd",unifiedNativeAd.getHeadline());
                        unifiedNativeAdArrayList.add(unifiedNativeAd);
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        // Handle the failure by logging, altering the UI, and so on.
                        // Log.d("NewAd", "failed "+Integer.toString(errorCode));
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .setReturnUrlsForImageAssets(false) // populate image and uri both
                        .setImageOrientation(NativeAdOptions.ORIENTATION_LANDSCAPE)
                        .setRequestMultipleImages(false) // fetch only one image
                        .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_BOTTOM_RIGHT)
                        .build())
                .build();
    }

    public void PreLoadAds(int timelineLength){
        adLoader.loadAds(new AdRequest.Builder().build(), timelineLength / k_eventToAdRatio + 1);
    }

    public UnifiedNativeAd GetAd(int idx){
        if(unifiedNativeAdArrayList.size()!=0) {
            return unifiedNativeAdArrayList.get(idx % unifiedNativeAdArrayList.size());
        } else {
            return null;
        }
    }


    private int GetEventToAdRatio(){
        return k_eventToAdRatio;
    }


    public boolean IsAdPosition(int position) {
        // Load only one ad atm
        int positionRemainder = (position % GetEventToAdRatio());
        return (positionRemainder == k_adLocationOrder) && (GetNumLoadedAds(position) == 0);
    }


    public int GetNumLoadedAds(int position){
        // Load only one ad atm
        if (position <= k_adLocationOrder){
            return 0;
        }else{
            return 1;
        }
        /*
        int positionRemainder = (position % GetEventToAdRatio());
        int nAdsSoFar = position / GetEventToAdRatio();
        if (positionRemainder > k_adLocationOrder) {
            nAdsSoFar += 1;
        }
        return nAdsSoFar;
        */
    }
}
