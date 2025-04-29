package com.example.foodorderingapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.foodorderingapp.Activity.DetailActivity;
import com.example.foodorderingapp.Domain.Foods;
import com.example.foodorderingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Item_Menu extends BaseAdapter {

    List<Foods> foodsList;
    Context context;
    LayoutInflater inflater;
    String userId;
    Set<String> wishlistIds;

    // ✅ Interface callback để reload Activity
    public interface OnWishlistChangedListener {
        void onWishlistChanged();
    }

    public void updateData(List<Foods> newFoodList, Set<String> newWishlistIds) {
        this.foodsList.clear();
        this.foodsList = newFoodList;
        this.wishlistIds = newWishlistIds;
        notifyDataSetChanged();
    }


    private OnWishlistChangedListener wishlistChangedListener;

    public Item_Menu(List<Foods> foodsList, Context context, String userId, Set<String> wishlistIds, OnWishlistChangedListener listener) {
        this.foodsList = foodsList;
        this.context = context;
        this.userId = userId;
        this.wishlistIds = wishlistIds;
        this.wishlistChangedListener = listener;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return foodsList.size();
    }

    @Override
    public Object getItem(int position) {
        return foodsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        ImageView img, heart;
        TextView title, price, description, time;
    }

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//
//        ViewHolder holder;
//
//        if (convertView == null) {
//            convertView = inflater.inflate(R.layout.viewholder_food_menu, parent, false);
//            holder = new ViewHolder();
//            holder.img = convertView.findViewById(R.id.imgView_Item_Menu_Viewholder);
//            holder.title = convertView.findViewById(R.id.textView_ItemTitle_Menu_Viewholder);
//            holder.price = convertView.findViewById(R.id.textView_ItemPrice_Menu_Viewholder);
//            holder.description = convertView.findViewById(R.id.textView_ItemDescription_Menu_Viewholder);
//            holder.time = convertView.findViewById(R.id.textView_ItemTime_Menu_Viewholder);
//            holder.heart = convertView.findViewById(R.id.imageView_AddToWishList_Menu_Viewholder); // thêm dòng này
//            convertView.setTag(holder);
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }
//
//        Foods item = foodsList.get(position);
//        holder.title.setText(item.getTitle());
//        holder.price.setText("$" + item.getPrice());
//        holder.time.setText(item.getTimeValue() + " min");
//        holder.description.setText(item.getDescription());
//        Glide.with(context).load(item.getImagePath()).into(holder.img);
//
//        // === 💖 Đoạn xử lý icon yêu thích bắt đầu từ đây ===
//        String foodIdStr = String.valueOf(item.getId());
//        boolean isFavorite = wishlistIds.contains(foodIdStr);
//        holder.heart.setImageResource(isFavorite ? R.drawable.red_heart2 : R.drawable.black_heart);
//
//        holder.heart.setOnClickListener(v -> {
//            boolean nowFavorite = !wishlistIds.contains(foodIdStr);
//            holder.heart.setImageResource(nowFavorite ? R.drawable.red_heart2 : R.drawable.black_heart);
//            animateHeart(holder.heart);
//
//            FirebaseFirestore db = FirebaseFirestore.getInstance();
//            DocumentReference userRef = db.collection("Users").document(userId);
//
//            if (nowFavorite) {
//                userRef.update("wishlist." + foodIdStr, true);
//                wishlistIds.add(foodIdStr);
//            } else {
//                userRef.update("wishlist." + foodIdStr, FieldValue.delete());
//                wishlistIds.remove(foodIdStr);
//            }
//        });
//        // === 💖 Kết thúc đoạn xử lý yêu thích ===
//
//        convertView.setOnClickListener(v -> {
//            Intent intent = new Intent(context, DetailActivity.class);
//            intent.putExtra("object", item);
//            context.startActivity(intent);
//        });
//        return convertView;
//    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.viewholder_food_menu, parent, false);
            holder = new ViewHolder();
            holder.img = convertView.findViewById(R.id.imgView_Item_Menu_Viewholder);
            holder.title = convertView.findViewById(R.id.textView_ItemTitle_Menu_Viewholder);
            holder.price = convertView.findViewById(R.id.textView_ItemPrice_Menu_Viewholder);
            holder.description = convertView.findViewById(R.id.textView_ItemDescription_Menu_Viewholder);
            holder.time = convertView.findViewById(R.id.textView_ItemTime_Menu_Viewholder);
            holder.heart = convertView.findViewById(R.id.imageView_AddToWishList_Menu_Viewholder);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Foods item = foodsList.get(position);
        holder.title.setText(item.getTitle());
        holder.price.setText("$" + item.getPrice());
        holder.time.setText(item.getTimeValue() + " min");
        holder.description.setText(item.getDescription());
        Glide.with(context).load(item.getImagePath()).into(holder.img);

        // Xử lý icon yêu thích
        String foodIdStr = String.valueOf(item.getId());
        boolean isFavorite = wishlistIds.contains(foodIdStr);
        holder.heart.setImageResource(isFavorite ? R.drawable.red_heart2 : R.drawable.black_heart);

        holder.heart.setOnClickListener(v -> {
            boolean nowFavorite = !wishlistIds.contains(foodIdStr);
            holder.heart.setImageResource(nowFavorite ? R.drawable.red_heart2 : R.drawable.black_heart);
            animateHeart(holder.heart);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("Users").document(userId);

            if (nowFavorite) {
                userRef.update("wishlist." + foodIdStr, true)
                        .addOnSuccessListener(unused -> {
                            wishlistIds.add(foodIdStr);
                            if (wishlistChangedListener != null) wishlistChangedListener.onWishlistChanged();
                        });
            } else {
                userRef.update("wishlist." + foodIdStr, FieldValue.delete())
                        .addOnSuccessListener(unused -> {
                            wishlistIds.remove(foodIdStr);
                            if (wishlistChangedListener != null) wishlistChangedListener.onWishlistChanged();
                        });
            }
        });

        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", item);
            context.startActivity(intent);
        });

        return convertView;
    }

    private void animateHeart(ImageView heart) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0.7f, 1f, // scale từ nhỏ -> bình thường theo X
                0.7f, 1f, // scale theo Y
                Animation.RELATIVE_TO_SELF, 0.5f, // pivot X ở giữa
                Animation.RELATIVE_TO_SELF, 0.5f  // pivot Y ở giữa
        );
        scaleAnimation.setDuration(200); // thời gian hiệu ứng
        scaleAnimation.setInterpolator(new OvershootInterpolator()); // mượt mà hơn
        heart.startAnimation(scaleAnimation);
    }


}
