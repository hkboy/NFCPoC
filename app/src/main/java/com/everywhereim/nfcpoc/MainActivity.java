package com.everywhereim.nfcpoc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Activity for reading data from an NDEF Tag.
 *
 * @author Ralf Wondratschek
 *
 */
public class MainActivity extends Activity implements OnClickListener {

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";

    private TextView mTextView;
    private TextView medTitel;
    private NfcAdapter mNfcAdapter;
    private String onSticker;
    public static int medicijnID;
    public static String naam;
    public static String beschrijving;
    public static String fotoNaam;
    public static ImageView fotoView;
    public static Button butIngenomen;
    public static int patientNumber;
    public static int dokterNumber;
    final String PREFS_NAME = "MyPrefsFile";
    public static boolean mgtSuccess;
    MedReg mgt = new MedReg();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);
        setContentView(R.layout.activity_main);
        //Declarables
        mTextView = (TextView) findViewById(R.id.textView_explanation);
        medTitel = (TextView) findViewById(R.id.textView_confirmation);
        fotoView = (ImageView) findViewById(R.id.fotoView);
        butIngenomen = (Button) findViewById(R.id.buttonIngenomen);
        butIngenomen.setOnClickListener(this);
        butIngenomen.setEnabled(false);
        mTextView.setMovementMethod(new ScrollingMovementMethod());


        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);



        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, R.string.noNFCSupport, Toast.LENGTH_LONG).show();
            finish();
            return;

        }

        if (!mNfcAdapter.isEnabled()) { //Checkt of NFC uitstaat
            mTextView.setText(R.string.NFCuit);
        } else {
            mTextView.setText(R.string.NFCBegin);
        }

        handleIntent(getIntent());


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fragment_quotes_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//If pressed on the settings button
        switch (id) {
            case R.id.action_settings: {
                startActivity(new Intent(this, Opties.class));
                return true;

            }
        }

    return super.onOptionsItemSelected(item);
    }



    public void setTextToTextView(JSONArray jsonArray) {
//
        int nummer = 0;
        try {
            nummer = Integer.valueOf(onSticker);
        } catch (NumberFormatException n) {
            //Do Nothing
        }
        JSONObject json = null;
        //Grabs data from Json database, put the correct data in strings and write to screen
        try {
            json = jsonArray.getJSONObject(nummer);
            naam = json.getString("naam");
            medicijnID = Integer.valueOf(json.getString("id"));
            medTitel.setText(naam);
            beschrijving = json.getString("beschrijving");
            mTextView.setText(beschrijving);

            //Listens to a name provided from the database and grab the appropriate photo
            fotoNaam = json.getString("foto");
            int resID = getResources().getIdentifier(fotoNaam, "drawable", getPackageName());
            fotoView.setImageResource(resID);

            mgtSuccess = false;  //Resets after a new scan
            if (nummer == 0) {
                //If there is an invalid sticker, you can't submit the data
                butIngenomen.setEnabled(false);
            } else {
                butIngenomen.setEnabled(true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e ) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        setupForegroundDispatch(this, mNfcAdapter);
    }


    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, mNfcAdapter);

        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this mmethod gets called, when the user attaches a Tag to the device.
         */
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);

            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
    }

    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter  The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    @SuppressLint("ClickableViewAccessibility")
//    @Override
    public void onClick(View v) {
        if ((R.id.buttonIngenomen == v.getId())) { //If the button is pressed
            if (patientNumber<=0 || dokterNumber <=0) {
                //Shows an error if there is no patientsnumber or doctorsnumber inserted.
                Toast.makeText(this, "U moet nog uw patientsnummer invoeren.", Toast.LENGTH_LONG).show();
            }
            else {
                mgt.submitMed();
                try {
                    Thread.sleep(1000L);    // one second
                }
                catch (Exception e) {}     // this never happen... nobody check for it

                if (mgtSuccess) { //If the SQL statement was valid
                    Toast.makeText(this, getString(R.string.confirmed), Toast.LENGTH_LONG).show();
                    fotoView.setImageResource(R.drawable.confirmed);
                }
                butIngenomen.setEnabled(false);
            }
        }
    }

    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; //Don't pay attention to this, it just works ¯\_(?)_/¯

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        //        @Override
        protected void onPostExecute(String result) { //JSONArray jsonArray
            if (result != null) {
                onSticker = result;
                //If the sticker is invalid, it returns to this one and shows a pre-determined error message
                medTitel.setText("1");

                //Shows this after a scan. This is a safe-guard for when there is no internet connection. Is only visible for <.5 seconds
                new GetAllMedsTask().execute(new ApiConnector());
                medTitel.setText("Er is iets foutgegaan");
                mTextView.setText("Als u dit scherm langer dan enkele seconden ziet, kan het zijn dat uw internet-verbinding is weggevallen. Verbindt uw telefoon met het internet en probeer het opnieuw");
                fotoView.setImageDrawable(null);
                butIngenomen.setEnabled(false);
                }

            }
        }



    public class GetAllMedsTask extends AsyncTask<ApiConnector, Long, JSONArray> {
        @Override
        public JSONArray doInBackground(ApiConnector... params) {
            //executed on background thread
            return params[0].getAllMeds();


        }
//
        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            //executed on main thread
            setTextToTextView(jsonArray);
        }
//    }
    }
}