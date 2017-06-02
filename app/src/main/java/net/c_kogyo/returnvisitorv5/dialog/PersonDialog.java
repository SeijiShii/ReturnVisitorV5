package net.c_kogyo.returnvisitorv5.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.RVData;
import net.c_kogyo.returnvisitorv5.util.ConfirmDialog;
import net.c_kogyo.returnvisitorv5.view.PriorityRater;

/**
 * Created by SeijiShii on 2017/02/20.
 */

public class PersonDialog extends DialogFragment {

    private static Person mPerson;
    private static PersonDialogListener mListener;
    private static boolean isPersonEdited;
    private static boolean mShowPriorityRater;

    private static PersonDialog instance;

    public static PersonDialog getInstance(Person person,
                                           PersonDialogListener listener,
                                           boolean showPriorityRater) {

        try {
            mPerson = (Person) person.clone();
        } catch (CloneNotSupportedException e) {

        }
        mListener = listener;
        isPersonEdited = false;
        mShowPriorityRater = showPriorityRater;

        if (instance == null) {
            instance = new PersonDialog();
        }
        return instance;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        initCommon();
        builder.setView(view);

        builder.setTitle(R.string.person);

        return builder.create();

    }

    private View view;
    private void initCommon(){

        view = View.inflate(getActivity(), R.layout.person_dialog, null);
        initNameText();
        initSexRadioButtons();
        initAgeSpinner();
        initPriorityRater();
        initNoteText();
        initOkButton();
        initCancelButton();
        initDeleteButton();

    }

    private EditText nameText;
    private void initNameText() {
        nameText = (EditText) view.findViewById(R.id.name_text);

        if (mPerson.getName() != null || !mPerson.getName().equals("")){
            nameText.setText(mPerson.getName());
        }

        nameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                isPersonEdited = true;
                refreshOkButton();
            }
        });
    }

    private RadioButton maleButton, femaleButton;
    private void initSexRadioButtons(){

        maleButton = (RadioButton) view.findViewById(R.id.male_button);
        femaleButton = (RadioButton) view.findViewById(R.id.female_button);

        switch (mPerson.getSex()) {
            case MALE:
                maleButton.setChecked(true);
                femaleButton.setChecked(false);
                break;
            case FEMALE:
                femaleButton.setChecked(true);
                maleButton.setChecked(false);
                break;
        }

        maleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mPerson.setSex(Person.Sex.MALE);
                    femaleButton.setChecked(false);
                    isPersonEdited = true;
                    refreshOkButton();
                }
            }
        });

        femaleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mPerson.setSex(Person.Sex.FEMALE);
                    maleButton.setChecked(false);
                    isPersonEdited = true;
                    refreshOkButton();
                }
            }
        });

    }

    private Spinner ageSpinner;
    private void initAgeSpinner() {

        final Person.Age oldAge = mPerson.getAge();

        ageSpinner = (Spinner) view.findViewById(R.id.age_spinner);

        AgeSpinnerAdapter adapter = new AgeSpinnerAdapter();

        ageSpinner.setAdapter(adapter);

        ageSpinner.setSelection(mPerson.getAge().num());

        ageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mPerson.setAge(Person.Age.getEnum(i));

                if (Person.Age.getEnum(i) != oldAge) {
                    isPersonEdited = true;
                    refreshOkButton();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void initPriorityRater() {

        PriorityRater priorityRater = (PriorityRater) view.findViewById(R.id.priority_rater);
        if (mShowPriorityRater) {
            priorityRater.setPriority(mPerson.getPriority());
            priorityRater.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            priorityRater.setOnPrioritySetListener(new PriorityRater.OnPrioritySetListener() {
                @Override
                public void onPriorityChanged(Person.Priority priority) {
                    mPerson.setPriority(priority);
                    isPersonEdited = true;
                    refreshOkButton();
                }
            });
        } else {
            priorityRater.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        }

    }

    private AutoCompleteTextView noteText;
    private void initNoteText() {

        noteText = (AutoCompleteTextView) view.findViewById(R.id.note_text);
        noteText.setText(mPerson.getNote());

//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, RVDB.getInstance().noteCompleteList.getList());
//        noteText.setAdapter(adapter);
        noteText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                mPerson.setNote(editable.toString());
                isPersonEdited = true;
                refreshOkButton();
            }
        });
    }

    private Button okButton;
    private void initOkButton() {

        okButton = (Button) view.findViewById(R.id.ok_button);

        refreshOkButton();

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mPerson.setName(nameText.getText().toString());
                mPerson.setNote(noteText.getText().toString());

                if (mListener != null) {
                    mListener.onOkClick(mPerson);
                }
                dismiss();
            }
        });

    }

    private void refreshOkButton() {

        okButton.setEnabled(isPersonEdited);

        if (isPersonEdited) {
            okButton.setAlpha(1f);

        } else {
            okButton.setAlpha(0.3f);
        }
    }

    private void initCancelButton() {

        Button cancelButton = (Button) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (mListener != null) {
                    mListener.onCloseDialog();
                }
            }
        });
    }

    private void initDeleteButton() {

        Button deleteButton = (Button) view.findViewById(R.id.delete_button);
        if (RVData.getInstance().personList.contains(mPerson)) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // DONE: 2017/03/05 削除処理 -> RecordVisitActivity内の削除動作テストに譲る
                    ConfirmDialog.confirmAndDeletePerson(getActivity(),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (mListener != null) {
                                        mListener.onDeleteClick(mPerson);
                                    }
                                    PersonDialog.this.dismiss();
                                }
                            }, mPerson);
                }
            });
        } else {
            deleteButton.setVisibility(View.INVISIBLE);
        }
    }

    class AgeSpinnerAdapter extends BaseAdapter {

        String[] ageArray;
        public AgeSpinnerAdapter() {
            ageArray = getActivity().getResources().getStringArray(R.array.age_array);
        }

        @Override
        public int getCount() {
            return ageArray.length;
        }

        @Override
        public Object getItem(int i) {
            return ageArray[i];
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = View.inflate(getActivity(), R.layout.age_spinner_option, null);
            TextView textView = ( TextView) view.findViewById(R.id.text_view);
            textView.setText(ageArray[i]);
            return view;
        }
    }

    public interface PersonDialogListener {

        void onOkClick(Person person);

        void onDeleteClick(Person person);

        void onCloseDialog();
    }

}
