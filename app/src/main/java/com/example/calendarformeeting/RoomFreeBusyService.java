package com.example.calendarformeeting;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.FreeBusyRequest;
import com.google.api.services.calendar.model.FreeBusyRequestItem;
import com.google.api.services.calendar.model.FreeBusyResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RoomFreeBusyService extends IntentService {
    public static final String ROOM_ID = "com.example.calendarformeeting.roomfreebusyservice";

    public RoomFreeBusyService() {
        super("RoomFreeBusyService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {





        String msg = intent.getStringExtra(ROOM_ID);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(RoomFreeBusyCheck.ResponseReceiver.ROOM_ID);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(ROOM_ID, msg);
        sendBroadcast(broadcastIntent);
    }
}