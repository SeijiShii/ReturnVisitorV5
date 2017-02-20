package net.c_kogyo.returnvisitorv5.dialogcontents;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.Spinner;

import net.c_kogyo.returnvisitorv5.R;

/**
 * Created by SeijiShii on 2017/02/20.
 */

public class PersonDialog extends FrameLayout {

    public PersonDialog(Context context) {
        this(context, null);
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
    }

    private RadioButton maleButton, femaleButton;
    private void initSexRadioButtons(){

        maleButton = (RadioButton) view.findViewById(R.id.male_button);
        femaleButton = (RadioButton) view.findViewById(R.id.female_button);

        maleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    femaleButton.setChecked(false);
                }
            }
        });

        femaleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    maleButton.setChecked(false);
                }
            }
        });

    }

    private Spinner ageSpinner;
    private void initAgeSpinner() {

        ageSpinner = (Spinner) findViewById(R.id.age_spinner);

        ArrayAdapter<CharSequence> adapter
                = ArrayAdapter.createFromResource(getContext(),
                R.array.age_array,
                android.R.layout.simple_spinner_item);

        ageSpinner.setAdapter(adapter);

//        if (mPerson != null) {
//            ageSpinner.setSelection(mPerson.getAge().num());
//        }

        ageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                mPerson.setAge(Person.Age.getEnum(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private AutoCompleteTextView noteText;
    private void initNoteText() {

        noteText = (AutoCompleteTextView) findViewById(R.id.note_text);
//        noteText.setText(mPerson.getNote());

//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, RVData.getInstance().noteCompleteList.getList());
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

//                mPerson.setNote(editable.toString());
            }
        });
    }

    private void initOkButton() {

        Button okButton = (Button) findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                mPerson.setName(nameText.getText().toString());
//                mPerson.setNote(noteText.getText().toString());
//
//                if (getIntent().getIntExtra(Constants.REQUEST_CODE, 0) == Constants.PersonCode.ADD_PERSON_REQUEST_CODE) {
//                    RVData.getInstance().personList.addOrSet(mPerson);
//
//                    RVData.getInstance().noteCompleteList.addToBoth(mPerson.getNote());
//
//                    Intent intent = new Intent();
//                    intent.putExtra(Person.PERSON, mPerson.getId());
//                    setResult(Constants.PersonCode.PERSON_ADDED_RESULT_CODE, intent);
//                } else if (getIntent().getIntExtra(Constants.REQUEST_CODE, 0) == Constants.PersonCode.EDIT_PERSON_REQUEST_CODE) {
//
//                    RVData.getInstance().personList.addOrSet(mPerson);
//
//                    RVData.getInstance().noteCompleteList.addToBoth(mPerson.getNote());
//
//                    Intent intent = new Intent();
//                    intent.putExtra(Person.PERSON, mPerson.getId());
//                    setResult(Constants.PersonCode.PERSON_EDITED_RESULT_CODE, intent);

//                }

//                finish();
            }
        });
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
//        if (RVData.getInstance().personList.contains(mPerson)) {
//            deleteButton.setVisibility(View.VISIBLE);
//            deleteButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                }
//            });
//        } else {
//            deleteButton.setVisibility(View.INVISIBLE);
//        }
    }


}
