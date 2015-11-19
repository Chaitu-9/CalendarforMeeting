package com.example.calendarformeeting;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
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
import java.util.Timer;
import java.util.TimerTask;

public class CalendarTasks extends Activity {
    private String isRoomBusy;
    public AsyncTask<Void, Void, String> roomFreeBusyCheck;


    public boolean createEvent(final Calendar mService, GoogleAccountCredential mCredential, String meetingName, String roomId, int meetingDuration, boolean isRoomBusy) throws IOException, JSONException, InterruptedException {
        isRoomBusy(mService,mCredential,roomId,meetingDuration);
        if (!isRoomBusy) {
            Date startDate = new Date();
            Date endDate = new Date(startDate.getTime() + 60000 * meetingDuration);
            DateTime startDateTime = new DateTime(startDate);
            DateTime endDateTime = new DateTime(endDate);


            final Event[] event = {new Event().setSummary(meetingName).setDescription("Instant meeting")};


            EventDateTime start = new EventDateTime().setDateTime(startDateTime).setTimeZone("Asia/Calcutta");
            event[0].setStart(start);

            EventDateTime end = new EventDateTime().setDateTime(endDateTime).setTimeZone("Asia/Calcutta");
            event[0].setEnd(end);


            EventAttendee[] attendees = new EventAttendee[]{
                 //   new EventAttendee().setEmail("jeeves@thoughtworks.com"),
                    new EventAttendee().setEmail(roomId)
            };

            event[0].setAttendees(Arrays.asList(attendees));


            EventReminder[] reminderOverrides = new EventReminder[]{
                    new EventReminder().setMethod("email").setMinutes(10),
                    new EventReminder().setMethod("popup").setMinutes(10),
            };


            Event.Reminders reminders = new Event.Reminders().setUseDefault(false)
                    .setOverrides(Arrays.asList(reminderOverrides));
            event[0].setReminders(reminders);

            final String calendarId = "primary";

            AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    try {
                        event[0] = mService.events().insert(calendarId, event[0]).execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(String s) {
                    Intent intent = getIntent();
                }
            };
            task.execute();

            System.out.printf("Event created: %s\n", event[0].getHtmlLink());
            return true;
        } else {
            System.out.println("Event not Created. Room is busy");
            return false;
        }
    }

    public String isRoomBusy(Calendar mService, GoogleAccountCredential mCredential, final String roomId, int minutes) throws IOException, InterruptedException, JSONException {

        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + 60000 * minutes);
        DateTime startDateTime = new DateTime(startDate);
        DateTime endDateTime = new DateTime(endDate);
        FreeBusyRequest req = new FreeBusyRequest();
        req.setTimeMin(startDateTime).setTimeZone("Asia/Calcutta");
        req.setTimeMax(endDateTime).setTimeZone("Asia/Calcutta");
        List<FreeBusyRequestItem> requestItems = new ArrayList<>();
        requestItems.add(new FreeBusyRequestItem().setId(roomId));
        req.setItems(requestItems);

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.calendar.Calendar.Builder(transport, jsonFactory, mCredential)
                .setApplicationName("Meeting Room Application")
                .build();
        final Calendar.Freebusy.Query fbq = mService.freebusy().query(req);
        final FreeBusyResponse[] resp = new FreeBusyResponse[1];

        roomFreeBusyCheck = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    resp[0] = fbq.execute();
                    String JSONString = resp[0].toString();
                    JSONObject json = new JSONObject(JSONString);
                    JSONObject getCalendars = json.getJSONObject("calendars");
                    JSONObject getCalId = getCalendars.getJSONObject(roomId);
                    String busyTime = getCalId.getString("busy");

                    if (busyTime.equals("[]")) {
                        isRoomBusy = "free";
                    }else {
                        isRoomBusy = "busy";
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return isRoomBusy;
            }

            @Override
            protected void onPostExecute(String isRoomBusy) {

            }
        };

        return isRoomBusy;
    }


}
