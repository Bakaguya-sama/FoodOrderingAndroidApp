package com.example.foodorderingapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.widget.TextView;


import com.example.foodorderingapp.Domain.orderlist;
import com.example.foodorderingapp.R;

import java.util.ArrayList;

public class orderlistview_Adapter extends BaseAdapter {
    ArrayList<orderlist> orderlists;
    Context context;

    public orderlistview_Adapter(Context context, ArrayList<orderlist> orderlists) {
        this.context = context;
        this.orderlists = orderlists;

    }
    @Override
    public int getCount() {
        return orderlists.size();
    }

    @Override
    public Object getItem(int position) {
        return orderlists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    static class ViewHolder {
        TextView txtItem;
        TextView txtNum;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;


        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.layout, parent, false);
            holder = new ViewHolder();
            holder.txtItem = convertView.findViewById(R.id.name);
            holder.txtNum = convertView.findViewById(R.id.num);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        orderlist item = orderlists.get(position);
        holder.txtItem.setText(item.getItemname());
        holder.txtNum.setText("x"+item.getNum()+"");


        return convertView;
    }

}
