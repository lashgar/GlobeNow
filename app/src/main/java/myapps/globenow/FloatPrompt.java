package myapps.globenow;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

class FloatPrompt {
    FloatPrompt(){}

    static void Show(Context mContext, String text){
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.float_prompt_layout, null,true); // specify the desired layout

        TextView textBox = layout.findViewById(R.id.floatPromptText);
        textBox.setText(text);

        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                50, /* CAUTION: This should be equal to dimens.bottomPanHeight */
                mContext.getResources().getDisplayMetrics()
        );

        Toast toast = new Toast(mContext);
        toast.setGravity(Gravity.BOTTOM, 0, (int)px);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
