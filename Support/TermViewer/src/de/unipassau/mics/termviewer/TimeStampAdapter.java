package de.unipassau.mics.termviewer;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

// http://stackoverflow.com/questions/6417550/android-format-timestamp-in-listview-with-cursor-adapter
public class TimeStampAdapter extends CursorAdapter {
    private final LayoutInflater mInflater;
    private String field;

    public TimeStampAdapter(Context context, Cursor cursor, String field) {
        super(context, cursor, false);
        mInflater = LayoutInflater.from(context);
        this.field = field;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.term_entry, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        long time = cursor.getLong(cursor.getColumnIndex("timestamp"));

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);

        String format = "dd.MM.yy kk:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String dateString = sdf.format(cal.getTime());


        ((TextView) view.findViewById(R.id.termEntryTimestamp)).setText(dateString);

        String content = cursor.getString(cursor.getColumnIndex(field));
        ((TextView) view.findViewById(R.id.termEntryText)).setText(content);
    }
}
