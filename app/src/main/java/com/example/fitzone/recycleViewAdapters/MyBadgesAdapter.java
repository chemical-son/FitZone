package com.example.fitzone.recycleViewAdapters;

import static com.example.fitzone.common_functions.StaticFunctions.getHostUrl;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitzone.activites.R;
import com.example.fitzone.retrofit_requists.data_models.badges.BadgesResponse;
import com.example.fitzone.retrofit_requists.data_models.user_profile_data.Badge;

import java.util.List;

public class MyBadgesAdapter extends RecyclerView.Adapter<MyBadgesAdapter.ViewHolder> {

    private Activity activity;
    private List<Badge> myData;
    private LayoutInflater myInflater;
    private BadgesAdapter.ItemClickListener myClickListener;

    public MyBadgesAdapter(Activity activity, List<Badge> mData) {
        if (activity == null)
            return;

        this.activity = activity;
        this.myInflater = LayoutInflater.from(activity);
        this.myData = mData;
    }

    // allows clicks events to be caught
    public void setClickListener(BadgesAdapter.ItemClickListener itemClickListener) {
        this.myClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView badgeImage;
        TextView nameOfBadge;

        ViewHolder(View itemView) {
            super(itemView);
            badgeImage = itemView.findViewById(R.id.badge_image);
            nameOfBadge = itemView.findViewById(R.id.badge_text);

            itemView.findViewById(R.id.badge_item).setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (myClickListener != null)
                myClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    @NonNull
    @Override
    public MyBadgesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = myInflater.inflate(R.layout.badge_item, parent, false);
        return new MyBadgesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyBadgesAdapter.ViewHolder holder, int position) {
        holder.nameOfBadge.setText(myData.get(position).getName());
        Glide.with(activity)
                .load(getHostUrl(activity) + myData.get(position).getImage())
                .centerCrop()
                .placeholder(R.drawable.loading_spinner)
                .into(holder.badgeImage);
    }

    @Override
    public int getItemCount() {
        return myData.size();
    }
}
