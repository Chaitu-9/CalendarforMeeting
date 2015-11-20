package com.example.calendarformeeting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class BookingStatus extends AppCompatActivity {

    private static java.io.File DATA_STORE_DIR;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent displayIntent = getIntent();
        String message = displayIntent.getStringExtra(BookRoom.EXTRA_MESSAGE);
        boolean isEventCreated = displayIntent.getBooleanExtra(BookRoom.IS_EVENT_CREATED, false);
        TextView textView = new TextView(this);
        textView.setTextSize(20);
        textView.setText(message);

        if(isEventCreated){
            textView.setText(message +" event successfully created");
        }else {
            textView.setText("Cannot create event. Room is busy");
        }
        setContentView(textView);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(BookingStatus.this,RoomFreeBusyCheck.class);
        startActivity(intent);
    }
}
