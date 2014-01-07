package de.unipassau.mics.lutzw.simpleeuropeanaquery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
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
        textView.setText(Ids[position].getTitle().get(0));

        //int id = Integer.parseInt(Ids[position]);
        //String imageFile = Model.GetbyId(id).IconFile;

        new DownloadImageTask(imageView)
                .execute(Ids[position].getEdmPreview().get(0));


        // get input stream
//        InputStream ims = null;
//        try {
//            ims = context.getAssets().open(imageFile);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // load image as Drawable
        //Drawable d = Drawable.createFromStream(ims, null);
        // set image to ImageView
        //imageView.setImageDrawable(d);
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
