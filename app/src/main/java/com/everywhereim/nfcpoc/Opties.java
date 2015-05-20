package com.everywhereim.nfcpoc;

/**
 * Created by Riekelt on 13-5-2015.
 */

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Opties extends Activity implements OnClickListener {


    Button buttonSave;
    EditText patientEdit;
    public static String patientNumberString = "geen";
    TextView patientView;
    EditText dokterEdit;
    public static String dokterNumberString = "geen";
    TextView dokterView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.opties);
        getActionBar().setTitle("Opties");
        buttonSave = (Button) findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(this);


        patientView = new TextView(this);
        patientView = (TextView) findViewById(R.id.patientView);
        patientView.setText("Huidig patientsunmmer: " + patientNumberString);
        dokterView = new TextView(this);
        dokterView = (TextView) findViewById(R.id.dokterView);
        dokterView.setText("Huidig doktersnummer: " + dokterNumberString);
    }

    //method that handles what happens if you click the save button
    public void buttonSaveClick() {

        patientEdit = (EditText) findViewById(R.id.nameField);
        dokterEdit = (EditText) findViewById(R.id.dokterField);
        CharSequence toastText = "Nothing";
        patientEdit.setInputType(InputType.TYPE_CLASS_TEXT);
        dokterEdit.setInputType(InputType.TYPE_CLASS_TEXT);

        //If the user hits save even though nothing is put in the edittext field, we give feedback using a toast message
        if (patientEdit.getText().toString().matches("")){
            Toast.makeText(getApplicationContext(), "Vul alstublieft uw patientnummer in", Toast.LENGTH_SHORT).show();
        } else{
            //Display a confirmation toast
            patientNumberString = patientEdit.getText().toString();
            try {
                MainActivity.patientNumber = Integer.valueOf(patientNumberString);
                patientView.setText("Current: " + patientNumberString);
                toastText = "Uw patientnummer is " + MainActivity.patientNumber + ". Druk alstublieft op de teurg knop!";
                Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException n) {
            Toast.makeText(getApplicationContext(),"Vul alleen numerieke karakters in", Toast.LENGTH_SHORT).show();
        }

        }

        if (dokterEdit.getText().toString().matches("")){
            Toast.makeText(getApplicationContext(), "Vul alstublieft het nummer van uw dokter in", Toast.LENGTH_SHORT).show();
        } else{
            //Display a confirmation toast
            dokterNumberString = dokterEdit.getText().toString();
            try {
                MainActivity.dokterNumber = Integer.valueOf(dokterNumberString);
                dokterView.setText("Current: " + dokterNumberString);
                toastText = "Uw doktersnummer is " + MainActivity.dokterNumber + ". Druk alstublieft op de teurg knop!";
                Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException n) {
                Toast.makeText(getApplicationContext(),"Vul alleen numerieke karakters in", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSave:
                //execute method when button is clicked
                buttonSaveClick();
                break;

        }

    }
}
