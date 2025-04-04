package com.example.foodorderingapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.foodorderingapp.Activity.DetailActivity;
import com.example.foodorderingapp.Domain.Foods;
import com.example.foodorderingapp.R;

import java.util.ArrayList;

public class BestFoodsAdapter extends RecyclerView.Adapter<BestFoodsAdapter.viewholder> {

    ArrayList<Foods> items;

    public BestFoodsAdapter(ArrayList<Foods> items) {
        this.items = items;
    }

    Context context;
    @NonNull
    @Override
    public BestFoodsAdapter.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_best_deal, parent, false);
        return new viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull BestFoodsAdapter.viewholder holder, int position) {
        holder.titleText.setText(items.get(position).getTitle());
        holder.priceText.setText(String.valueOf("$" + items.get(position).getPrice()));
        holder.starText.setText(String.valueOf(items.get(position).getStar() + " min"));
        holder.timeText.setText(String.valueOf(items.get(position).getTimeValue()));

        Glide.with(context)
                .load(items.get(position).getImagePath())
                .transform(new CenterCrop(), new RoundedCorners(30))
                .into(holder.pic);


        holder.itemView.setOnClickListener(v ->
                {
                    Intent intent=new Intent(context, DetailActivity.class);
                    intent.putExtra("object", items.get(position));
                    context.startActivity(intent);


                }
        );
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        TextView titleText, priceText, starText, timeText;
        ImageView pic;
        public viewholder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            priceText = itemView.findViewById(R.id.priceText);
            starText = itemView.findViewById(R.id.starText);
            timeText = itemView.findViewById(R.id.timeText);
            pic = itemView.findViewById(R.id.pic);
        }
    }
}
