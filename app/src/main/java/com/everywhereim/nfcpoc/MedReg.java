package com.everywhereim.nfcpoc;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Riekelt on 19-5-2015.
 */
public class MedReg {

    byte[] data;
    HttpPost url2;
    StringBuffer buffer;
    HttpResponse response;
    HttpClient httpclient;
    InputStream inputStream;
    SharedPreferences app_preferences;
    List<NameValuePair> nameValuePairs;
    CheckBox check;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //The format SQL accepts
    Date date = new Date();
    String datum = sdf.format(date);

    public void submitMed() {

        new Thread(new Runnable() {
            public void run() {

                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("medicijnid", String.valueOf(MainActivity.medicijnID)));
                nameValuePairs.add(new BasicNameValuePair("dokterid", String.valueOf(MainActivity.dokterNumber)));
                nameValuePairs.add(new BasicNameValuePair("patientid", String.valueOf(MainActivity.patientNumber)));
                nameValuePairs.add(new BasicNameValuePair("dagmaand", datum));
//                Log.e("Datum:", datum);
                nameValuePairs.add(new BasicNameValuePair("Ingenomen", "1"));

                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost("http://rieke.lt/s/sqlInsert.php");
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpClient.execute(httpPost);
                    HttpEntity entity = response.getEntity();
                    //is = entity.getContent();
                    Log.d("debug", "ENDED TRY");
                    MainActivity.mgtSuccess = true;
                } catch (Exception e) {
                    Log.e("debug", "NOT SENT TO DATABASE");
                    e.printStackTrace();
                }

            }

        }).start();
    }

}

