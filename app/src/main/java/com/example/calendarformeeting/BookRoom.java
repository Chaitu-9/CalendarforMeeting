package com.example.calendarformeeting;

import com.example.calendarquickstart.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.calendar.*;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.model.*;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookRoom extends Activity {
    public static final String EXTRA_MESSAGE = "com.example.calendarformeeting.MESSAGE";
    public static final String IS_EVENT_CREATED = "com.example.calendarformeeting.IS_EVENT_CREATED";
    CalendarTasks calendar = new CalendarTasks();
    GoogleAccountCredential mCredential;
    FreeBusyResponse resp;
    String busyTime;
    com.google.api.services.calendar.Calendar.Freebusy.Query fbq;
    private TextView mOutputText;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";

    public com.google.api.services.calendar.Calendar mService = null;

    private static final String[] SCOPES = {CalendarScopes.CALENDAR};
    private boolean isRoomBusy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
        setContentView(R.layout.book_room);

        Button button30 = (Button) findViewById(R.id.button30);
        button30.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bookRoomForSpecifiedTime(30);
            }
        });

        Button button60 = (Button) findViewById(R.id.button60);
        button60.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bookRoomForSpecifiedTime(60);
            }
        });

        Button button90 = (Button) findViewById(R.id.button90);
        button90.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bookRoomForSpecifiedTime(90);
            }
        });
    }

    private void bookRoomForSpecifiedTime(int minutes){
        Intent intent = new Intent(BookRoom.this,BookingStatus.class);
        EditText editText = (EditText) findViewById(R.id.meetingName);
        String message = editText.getText().toString();

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.calendar.Calendar.Builder(transport, jsonFactory, mCredential)
                .setApplicationName("Meeting Room Application")
                .build();

        boolean isEventCreated = false;
        try {
            isEventCreated = calendar.createEvent(mService, mCredential, message, CalendarIDs.SANTA_ANA_ID, minutes, isRoomBusy);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        intent.putExtra(EXTRA_MESSAGE, message);
        intent.putExtra(IS_EVENT_CREATED, isEventCreated);
        startActivity(intent);
    }
}

