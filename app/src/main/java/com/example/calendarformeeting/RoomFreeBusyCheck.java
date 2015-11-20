package com.example.calendarformeeting;

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
import android.widget.TextView;

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
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.FreeBusyRequest;
import com.google.api.services.calendar.model.FreeBusyRequestItem;
import com.google.api.services.calendar.model.FreeBusyResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.example.calendarquickstart.R.*;
import static com.example.calendarquickstart.R.layout.*;

public class RoomFreeBusyCheck extends Activity {
    private static final String PREF_ACCOUNT_NAME = "accountName";
    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;
    Calendar mService;
    AsyncTask roomFreeBusyCheck;
    TextView welcome;


    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(room_free_busy_check);

        welcome = (TextView) findViewById(id.welcome);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Calendar API ...");

        String SCOPES = CalendarScopes.CALENDAR;

        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));


        roomFreeBusyCheck = new AsyncTask<Object, Void, String>() {
            String isRoomBusy;
            @Override
            protected String doInBackground(Object... params) {
                try {
                    Date startDate = new Date();
                    Date endDate = new Date(startDate.getTime() + 60000 * 30);
                    DateTime startDateTime = new DateTime(startDate);
                    DateTime endDateTime = new DateTime(endDate);
                    FreeBusyRequest req = new FreeBusyRequest();
                    req.setTimeMin(startDateTime).setTimeZone("Asia/Calcutta");
                    req.setTimeMax(endDateTime).setTimeZone("Asia/Calcutta");
                    List<FreeBusyRequestItem> requestItems = new ArrayList<>();
                    requestItems.add(new FreeBusyRequestItem().setId(CalendarIDs.SANTA_ANA_ID));
                    req.setItems(requestItems);

                    HttpTransport transport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

                    mService = new Calendar.Builder(transport, jsonFactory, mCredential)
                            .setApplicationName("Meeting Room Application")
                            .build();
                    final Calendar.Freebusy.Query fbq = mService.freebusy().query(req);
                    FreeBusyResponse resp = fbq.execute();
                    String JSONString = resp.toString();
                    JSONObject json = new JSONObject(JSONString);
                    JSONObject getCalendars = json.getJSONObject("calendars");
                    JSONObject getCalId = getCalendars.getJSONObject(CalendarIDs.SANTA_ANA_ID);
                    String busyTime = getCalId.getString("busy");

                    if (busyTime.equals("[]")) {
                        isRoomBusy = "free";
                    } else {
                        isRoomBusy = "busy";
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                return isRoomBusy;
            }

            @Override
            protected void onPostExecute(String s) {
                if (s.equals("free")) {
                    Intent intent = new Intent(RoomFreeBusyCheck.this, BookRoom.class);
                    startActivity(intent);
                } else if (s.equals("busy")) {
                    Intent intent = new Intent(RoomFreeBusyCheck.this, BookingStatus.class);
                    startActivity(intent);
                }
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            refreshResults();
        } else {
            welcome.setText("Google Play Services required: " +"after installing, close and relaunch this app.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mCredential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                     welcome.setText("Account unspecified.");
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != RESULT_OK) {
                    chooseAccount();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void refreshResults() {
        if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                new MakeRequestTask(mCredential) {
                    @Override
                    protected List<String> doInBackground(Void... params) {
                        return null;
                    }
                }.execute();
            } else {
                welcome.setText("No network connection available.");
            }
        }
    }

    private void chooseAccount() {
        startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS) {
            return false;
        }
        return true;
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                connectionStatusCode,
                RoomFreeBusyCheck.this,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private abstract class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {

        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(transport, jsonFactory, credential)
                    .setApplicationName("Meeting Room Application")
                    .build();
        }


        @Override
        protected void onPreExecute() {
            CalendarTasks calendarTasks = new CalendarTasks();
            try {
                calendarTasks.isRoomBusy(mService, mCredential, CalendarIDs.SANTA_ANA_ID,30);
            } catch (IOException | InterruptedException | JSONException e) {
                e.printStackTrace();
            }
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            roomFreeBusyCheck.execute();
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            BookRoom.REQUEST_AUTHORIZATION);
                } else {
                    welcome.setText("The following error occurred:\n" + mLastError.getMessage());
                }
            } else {
                welcome.setText("Request cancelled.");
            }
        }

    }
}
