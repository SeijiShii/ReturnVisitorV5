package net.c_kogyo.returnvisitorv5.dialogcontents;

import android.content.Context;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import net.c_kogyo.returnvisitorv5.R;
import net.c_kogyo.returnvisitorv5.data.Person;
import net.c_kogyo.returnvisitorv5.data.RVData;

/**
 * Created by SeijiShii on 2017/02/20.
 */

public class PersonDialog extends FrameLayout {

    private Person mPerson;
    private OnPersonEditFinishListener mListener;
    private boolean isPersonEdited;

    public PersonDialog(Context context, Person person, OnPersonEditFinishListener listener) {
        super(context);

        mPerson = person;
        mListener = listener;
        isPersonEdited = false;

        initCommon();
    }

    public PersonDialog(Context context, AttributeSet attrs) {
        super(context, attrs);

        initCommon();
    }

    private View view;
    private void initCommon(){

        view = LayoutInflater.from(getContext()).inflate(R.layout.person_dialog, this);
        initNameText();
        initSexRadioButtons();
        initAgeSpinner();
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
                renewOkButton();
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
                    renewOkButton();
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
                    renewOkButton();
                }
            }
        });

    }

    private AppCompatSpinner ageSpinner;
    private void initAgeSpinner() {

        final Person.Age oldAge = mPerson.getAge();

        ageSpinner = (AppCompatSpinner) findViewById(R.id.age_spinner);

        AgeSpinnerAdapter adapter = new AgeSpinnerAdapter();

        ageSpinner.setAdapter(adapter);

        ageSpinner.setSelection(mPerson.getAge().num());

        ageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mPerson.setAge(Person.Age.getEnum(i));

                if (Person.Age.getEnum(i) != oldAge) {
                    isPersonEdited = true;
                    renewOkButton();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private AutoCompleteTextView noteText;
    private void initNoteText() {

        noteText = (AutoCompleteTextView) findViewById(R.id.note_text);
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
                renewOkButton();
            }
        });
    }

    private Button okButton;
    private void initOkButton() {

        okButton = (Button) findViewById(R.id.ok_button);

        renewOkButton();

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mPerson.setName(nameText.getText().toString());
                mPerson.setNote(noteText.getText().toString());
                mListener.onFinishEdit(mPerson);

            }
        });

    }

    private void renewOkButton() {

        okButton.setEnabled(isPersonEdited);

        if (isPersonEdited) {
            okButton.setAlpha(1f);

        } else {
            okButton.setAlpha(0.3f);
        }
    }

    private void initCancelButton() {

        Button cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                finish();
            }
        });
    }

    private void initDeleteButton() {

        Button deleteButton = (Button) findViewById(R.id.delete_button);
        if (RVData.getInstance().getPersonList().contains(mPerson)) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO: 2017/03/05 削除処理 
                }
            });
        } else {
            deleteButton.setVisibility(View.INVISIBLE);
        }
    }

    class AgeSpinnerAdapter extends BaseAdapter {

        String[] ageArray;
        public AgeSpinnerAdapter() {
            ageArray = getContext().getResources().getStringArray(R.array.age_array);
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
            view = LayoutInflater.from(getContext()).inflate(R.layout.age_spinner_option, null);
            TextView textView = ( TextView) view.findViewById(R.id.text_view);
            textView.setText(ageArray[i]);
            return view;
        }
    }

    public interface OnPersonEditFinishListener {
        void onFinishEdit(Person person);
    }
}
