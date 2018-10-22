package myapps.globenow;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Random;

public class CustomListAdapter extends ArrayAdapter {
    //to reference the Activity
    private final Activity context;

    //to store the animal images
    private final Integer[] imageIDarray;

    //to store the list of countries
    private final String[] nameArray;

    //to store the list of countries
    private final String[] infoArray;

    // to store the list of URLs of media
    private final String[] mediaURLs;

    // to store the list of URLs of media
    private final Bitmap[] bitmapArray;

    GradientDrawable gradientDrawable;

    public CustomListAdapter(Activity context,
                             String[] nameArrayParam,
                             String[] infoArrayParam,
                             Integer[] imageIDArrayParam,
                             String[] mediaURLArray,
                             Bitmap[] bitmapArray){

        super(context,R.layout.listview_row_noimage, nameArrayParam);
        this.context=context;
        this.imageIDarray = imageIDArrayParam;
        this.nameArray = nameArrayParam;
        this.infoArray = infoArrayParam;
        this.mediaURLs = mediaURLArray;
        this.bitmapArray = bitmapArray;

        // gradient drawable
        gradientDrawable = new GradientDrawable();
        gradientDrawable.setColors(new int[]{0xFF610627,0x19654217});
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setSize(800,80);
    }
    public View getView(int position, View view, ViewGroup parent) {
        if(!mediaURLs[position].equals("") && !mediaURLs[position].equals("none")){
            // With image
            // inflate the row
            LayoutInflater inflater=context.getLayoutInflater();
            View rowView=inflater.inflate(R.layout.listview_row_withimage, null,true); // specify the desired layout

            //this code gets references to objects in the listview_row_withimage.xml file
            TextView nameTextField = rowView.findViewById(R.id.textView8);
            TextView infoTextField = rowView.findViewById(R.id.textView9);
            ImageView imageView = rowView.findViewById(R.id.imageView6);

            //this code sets the values of the objects to values from the arrays
            nameTextField.setText(nameArray[position]);
            String author = "by "+infoArray[position];
            infoTextField.setText(author);
            imageView.setImageBitmap(bitmapArray[position]);
            return rowView;
        }else {
            // without image
            LayoutInflater inflater=context.getLayoutInflater();
            View rowView=inflater.inflate(R.layout.listview_row_noimage, null,true); // specify the desired layout

            //this code gets references to objects in the listview_row_noimage.xml file
            TextView nameTextField = rowView.findViewById(R.id.textView2);
            TextView infoTextField = rowView.findViewById(R.id.textView3);

            //this code sets the values of the objects to values from the arrays
            nameTextField.setText(nameArray[position]);
            String author = "By "+infoArray[position];
            infoTextField.setText(author);

            return rowView;
        }
    }
}
