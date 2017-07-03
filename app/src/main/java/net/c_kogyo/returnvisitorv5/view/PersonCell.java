package net.c_kogyo.returnvisitorv5.view;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.Constants;
import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.util.ConfirmDialog;

/**
 * Created by SeijiShii on 2017/05/25.
 */

public abstract class PersonCell extends BaseAnimateView {

    private Person mPerson;
    private boolean mShowEditButton;

    private PersonCellListener mListener;

    public PersonCell(@NonNull Context context,
                      Person person,
                      boolean showEditButton,
                      @Nullable PersonCellListener listener) {
        super(context, context.getResources().getDimensionPixelSize(R.dimen.ui_height_small), R.layout.person_cell);

        mPerson = person;
        mShowEditButton = showEditButton;
        mListener = listener;

        initCommon();
    }

    public PersonCell(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, R.layout.person_cell);
    }

    private ImageView personMarker;
    private TextView personText;
    private void initCommon() {
        personMarker = (ImageView) getViewById(R.id.person_marker);
        personText = (TextView) getViewById(R.id.person_text);

        initEditButton();

        refreshData(null);
    }

    private Button editButton;
    private void initEditButton() {
        editButton = (Button) getViewById(R.id.person_edit_button);
        if (mShowEditButton) {
            editButton.setVisibility(VISIBLE);
            editButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopup();
                }
            });
        } else {
            editButton.setVisibility(INVISIBLE);
        }
    }

    private void showPopup() {
        PopupMenu popupMenu = new PopupMenu(getContext(), editButton);
        popupMenu.getMenuInflater().inflate(R.menu.person_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.edit:
                        if (mListener != null) {
                            mListener.onClickEdit(mPerson);
                        }
                        return true;
                    case R.id.delete:
                        ConfirmDialog.confirmAndDeletePerson(getContext(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mListener != null) {
                                    mListener.onClickDelete(mPerson);
                                }
                            }
                        }, mPerson);
                        return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    public interface PersonCellListener {

        void onClickDelete(Person person);

        void onClickEdit(Person person);
    }

    public void refreshData(@Nullable Person person) {

        if (person != null) {
            mPerson = person;
        }

        personMarker.setBackgroundResource(Constants.buttonRes[mPerson.getPriority().num()]);
        personText.setText(mPerson.toString(getContext()));
    }

    public Person getPerson() {
        return mPerson;
    }

}
