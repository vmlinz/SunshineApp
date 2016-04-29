package com.example.sunshine.app.ui.details;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sunshine.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vmlinz on 4/29/16.
 */
public class DetailsExtraInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    public static final int ITEM_TYPE_LABEL = 0;
    public static final int ITEM_TYPE_VALUE = 1;

    private List<String> items = new ArrayList<>();

    public void setItems(List<String> items) {
        this.items = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM_TYPE_LABEL:
                viewHolder = new DetailsExtraInfoLabelViewHolder(inflater.inflate(R.layout.recycler_item_details_extra_info_label, parent, false));
                break;
            case ITEM_TYPE_VALUE:
                viewHolder = new DetailsExtraInfoValueViewHolder(inflater.inflate(R.layout.recycler_item_details_extra_info_value, parent, false));
                break;
            default:
                viewHolder = null;
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case ITEM_TYPE_LABEL:
                ((DetailsExtraInfoLabelViewHolder)holder).textView.setText(items.get(position));
                break;
            case ITEM_TYPE_VALUE:
                ((DetailsExtraInfoValueViewHolder)holder).textView.setText(items.get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position % 2 == 0) ? ITEM_TYPE_LABEL : ITEM_TYPE_VALUE;
    }

    public class DetailsExtraInfoLabelViewHolder extends RecyclerView.ViewHolder{
        public TextView textView;
        public DetailsExtraInfoLabelViewHolder(View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.details_extra_info_label_text_view);
        }
    }
    public class DetailsExtraInfoValueViewHolder extends RecyclerView.ViewHolder{
        public TextView textView;
        public DetailsExtraInfoValueViewHolder(View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.details_extra_info_value_text_view);
        }
    }
}
