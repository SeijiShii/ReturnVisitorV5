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

public class ClearEditText extends BaseAnimateView{

    public ClearEditText(Context context) {
        this(context, null);
    }

    public ClearEditText(Context context, AttributeSet attrs) {
        super(context,attrs, R.layout.clear_edit_text);

        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
        initCommon();
    }

    @Override
    public void setLayoutParams(BaseAnimateView view) {
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
    }

    private void initCommon(){
        initEditText();
        initXButton();
    }

    private EditText editText;
    private void initEditText(){
        editText = (EditText) getViewById(R.id.edit_text);
    }

    private Button xButton;
    private void initXButton(){
        xButton = (Button) getViewById(R.id.x_button);
        xButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
                ClearEditText.this.changeViewHeight(0, true, null, null);
            }
        });
    }

    public void extract() {
        int target = getContext().getResources().getDimensionPixelSize(R.dimen.ui_height_small);
        changeViewHeight(target, true, null, null);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    public void addTextChangeListener(TextWatcher textWatcher) {
        editText.addTextChangedListener(textWatcher);
    }

    public String getText() {
        return editText.getText().toString();
    }

    public void setText(String text) {
        this.editText.setText(text);
    }

    @Override
    public void postViewExtract(BaseAnimateView view) {

    }
}
