package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import net.c_kogyo.returnvisitorv5.R;

/**
 * Created by SeijiShii on 2017/02/16.
 */

public class ClearEditText extends RelativeLayout{

    private View view;

    public ClearEditText(Context context) {
        super(context);
        initCommon();
    }

    public ClearEditText(Context context, AttributeSet attrs) {
        super(context,attrs);
        initCommon();
    }

//    public ClearEditText(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//
//
//    }

    private void initCommon(){
        view = LayoutInflater.from(getContext()).inflate(R.layout.clear_edit_text, this);
        initEditText();
        initXButton();
    }

    private EditText editText;
    private void initEditText(){
        editText = (EditText) view.findViewById(R.id.edit_text);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                ClearableEditText.this.onTextChanged(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private Button xButton;
    private void initXButton(){
        xButton = (Button)view.findViewById(R.id.x_button);
        xButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
            }
        });
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }


    //    public abstract void onTextChanged(CharSequence charSequence);
}
