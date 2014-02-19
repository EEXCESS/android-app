package com.aware.plugin.automatic_query;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;

import eu.europeana.api.client.EuropeanaApi2Item;

public class EuropeanaApi2ResultAdapter extends ArrayAdapter<EuropeanaApi2Item> {

    private final Context context;
    private final EuropeanaApi2Item[] Ids;
    private final int rowResourceId;

    public EuropeanaApi2ResultAdapter(Context context, int textViewResourceId, EuropeanaApi2Item[] objects) {

        super(context, textViewResourceId, objects);

        this.context = context;
        this.Ids = objects;
        this.rowResourceId = textViewResourceId;

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(rowResourceId, parent, false);


        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
        TextView textView = (TextView) rowView.findViewById(R.id.textView);

        if (Ids[position] != null) {
            if (Ids[position].getTitle() != null && Ids[position].getTitle().size() > 0) {
                textView.setText(Ids[position].getTitle().get(0));
            }

            if (Ids[position].getEdmPreview() != null && Ids[position].getEdmPreview().size() > 0) {
                new DownloadImageTask(imageView)
                        .execute(Ids[position].getEdmPreview().get(0));
            }
        } else {
            System.out.println("Ids[position] is null for position " + position);
        }

        return rowView;

    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}
