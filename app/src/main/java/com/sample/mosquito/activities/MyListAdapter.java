package com.sample.mosquito.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sample.mosquito.R;
import com.sample.mosquito.mqtt.Publication;

public class MyListAdapter extends ArrayAdapter<Publication> {
    private Publication[] publications;
    private ViewModel[] viewModels;
    private final LayoutInflater inflater;

    public MyListAdapter(Context context, Publication[] publications) {
        super(context, R.layout.item_list, publications);
        this.publications = publications;
        this.viewModels = new ViewModel[publications.length];
        this.inflater = LayoutInflater.from(context);
    }

    public void addItem(Publication publication) {
        Publication[] newArray = new Publication[publications.length + 1];
        for (int i = 0; i < publications.length; i++) newArray[i] = publications[i];
        newArray[publications.length] = publication;
        publications = newArray;

        ViewModel[] newArray2 = new ViewModel[viewModels.length + 1];
        for (int i = 0; i < viewModels.length; i++) newArray2[i] = viewModels[i];
        newArray[viewModels.length] = publication;
        viewModels = newArray2;

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return publications.length;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (viewModels[position] == null) {
            viewModels[position] = new ViewModel(inflater.inflate(R.layout.item_list, null, false), publications[position]);
        }
        return viewModels[position].root;
    }

    private class ViewModel {

        public View root;
        public TextView textDateTime, textMessage;

        public ViewModel(View root, Publication publication) {
            this.root = root;
            textMessage = root.findViewById(R.id.textMessage);
            textDateTime = root.findViewById(R.id.textDateTime);
            textMessage.setText(publication.message);
            textDateTime.setText(publication.dateTime);
        }
    }
}
