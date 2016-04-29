package com.example.sunshine.app.ui.details;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.sunshine.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vmlinz on 4/29/16.
 */
public class DetailsInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<String> items = new ArrayList<>();

    public void setItems(List<String> items) {
        this.items = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case 0:
                View iconView = inflater.inflate(R.layout.recycler_item_details_info_icon_large, parent, false);
                viewHolder = new DetailsInfoIconViewHolder(iconView);
                break;
            case 1:
                View highView = inflater.inflate(R.layout.recycler_item_details_info_high_temp_large, parent, false);
                viewHolder = new DetailsInfoHighViewHolder(highView);
                break;
            case 2:
                View statusView = inflater.inflate(R.layout.recycler_item_details_info_status_small, parent, false);
                viewHolder = new DetailsInfoStatusViewHolder(statusView);
                break;
            case 3:
                View lowView = inflater.inflate(R.layout.recycler_item_details_info_low_temp_small, parent, false);
                viewHolder = new DetailsInfoLowViewHolder(lowView);
                break;
            default:
                viewHolder = null;
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 0:
                DetailsInfoIconViewHolder detailsInfoIconViewHolder = (DetailsInfoIconViewHolder) holder;
                Glide.with(detailsInfoIconViewHolder.itemView.getContext())
                        .load(items.get(position))
                        .into(detailsInfoIconViewHolder.imageView);
                break;
            case 1:
                DetailsInfoHighViewHolder detailsInfoHighViewHolder = (DetailsInfoHighViewHolder) holder;
                detailsInfoHighViewHolder.textView.setText(items.get(position));
                break;
            case 2:
                DetailsInfoStatusViewHolder detailsInfoStatusViewHolder = (DetailsInfoStatusViewHolder) holder;
                detailsInfoStatusViewHolder.textView.setText(items.get(position));
                break;
            case 3:
                DetailsInfoLowViewHolder detailsInfoLowViewHolder = (DetailsInfoLowViewHolder) holder;
                detailsInfoLowViewHolder.textView.setText(items.get(position));
                break;
            default:
                break;
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < getItemCount()) {
            return position;
        } else {
            return -1;
        }
    }

    public class DetailsInfoIconViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public DetailsInfoIconViewHolder(View itemView) {
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.details_info_icon_image_view);
        }
    }

    public class DetailsInfoHighViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public DetailsInfoHighViewHolder(View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.details_info_high_temp_text_view);
        }
    }

    public class DetailsInfoStatusViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public DetailsInfoStatusViewHolder(View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.details_info_status_text_view);
        }
    }

    public class DetailsInfoLowViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public DetailsInfoLowViewHolder(View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.details_info_low_temp_text_view);
        }
    }

}
