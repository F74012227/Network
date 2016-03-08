package com.mmlab.network.controller;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mmlab.network.R;
import com.mmlab.network.model.WifiDoc;

import java.util.List;

public class WifiDocAdapter extends RecyclerView.Adapter<WifiDocAdapter.MyViewHolder> {

    private static final String TAG = "WifiDocAdapter";
    private Context mContext = null;

    private List<WifiDoc> wifiDocs = null;

    public WifiDocAdapter(Context context, List<WifiDoc> wifiDocs) {
        this.wifiDocs = wifiDocs;
        this.mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wifidoc, parent, false));
    }

    public interface OnItemClickLitener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private OnItemClickLitener mOnItemClickLitener;

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        switch (NetworkManager.calculateSignalStength(wifiDocs.get(position).level)) {
            case 1:
                holder.wifirecord_menu.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_signal_wifi_1_bar_indigo_800_36dp));
                break;
            case 2:
                holder.wifirecord_menu.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_signal_wifi_2_bar_indigo_800_36dp));
                break;
            case 3:
                holder.wifirecord_menu.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_signal_wifi_3_bar_indigo_800_36dp));
                break;
            case 4:
                holder.wifirecord_menu.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_signal_wifi_4_bar_indigo_800_36dp));
                break;
            default:
                holder.wifirecord_menu.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_signal_wifi_0_bar_indigo_800_36dp));
        }

        holder.wifirecord_title.setText(wifiDocs.get(position).SSID);
        holder.wifirecord_content.setText(wifiDocs.get(position).getState());

        if (mOnItemClickLitener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemClick(holder.itemView, pos);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemLongClick(holder.itemView, pos);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return wifiDocs.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView wifirecord_title;
        TextView wifirecord_content;
        ImageView wifirecord_menu;

        public MyViewHolder(View view) {
            super(view);
            wifirecord_title = (TextView) view.findViewById(R.id.wifirecord_title);
            wifirecord_content = (TextView) view.findViewById(R.id.wifirecord_content);
            wifirecord_menu = (ImageView) view.findViewById(R.id.wifirecord_menu);
        }
    }
}
