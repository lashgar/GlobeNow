package myapps.globenow;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
        EventInstance eventInstance = getItem(position);
        View rowView;
        if(!eventInstance.media.equals("") && !eventInstance.media.equals("none")){
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
