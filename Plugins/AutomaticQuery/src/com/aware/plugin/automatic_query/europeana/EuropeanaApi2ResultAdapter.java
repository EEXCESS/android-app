package com.aware.plugin.automatic_query.europeana;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aware.plugin.automatic_query.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

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

        // Create global configuration and initialize ImageLoader with this configuration
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheOnDisc(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).defaultDisplayImageOptions(defaultOptions).build();
        ImageLoader.getInstance().init(config);
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
                String url = Ids[position].getEdmPreview().get(0);
                ImageLoader.getInstance().displayImage(url, imageView);
            }
        } else {
            System.out.println("Ids[position] is null for position " + position);
        }

        return rowView;

    }
}
