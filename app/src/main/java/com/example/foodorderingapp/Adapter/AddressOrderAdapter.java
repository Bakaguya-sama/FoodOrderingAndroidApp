
package com.example.foodorderingapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.example.foodorderingapp.Activity.EditAddressActivity;
import com.example.foodorderingapp.Domain.Address;
import com.example.foodorderingapp.R;

import java.util.List;

public class AddressOrderAdapter extends BaseAdapter {
    List<Address> addressList;
    Context context;
    LayoutInflater inflater;

    public AddressOrderAdapter(List<Address> addressList, Context context) {
        this.addressList = addressList;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return addressList.size();
    }

    @Override
    public Object getItem(int position) {
        return addressList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.viewholder_address, parent, false);
            holder = new ViewHolder();
            holder.addressName = convertView.findViewById(R.id.txtView_AddressName_ViewholderAddress);
            holder.address = convertView.findViewById(R.id.txtView_Address_ViewholderAddress);
            holder.addressNameBox = convertView.findViewById(R.id.txtView_AddressBox_ViewholderAddress);
            holder.defaultBox = convertView.findViewById(R.id.txtView_Default_ViewholderAddress);
            holder.editText = convertView.findViewById(R.id.txtView_Edit_ViewholderAddress);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Address item = addressList.get(position);
        holder.addressName.setText(item.getAddressName());
        holder.address.setText(item.getAddress());
        holder.addressNameBox.setText(item.getAddressName());
        if (item.isDefault()) {
            holder.defaultBox.setVisibility(View.VISIBLE);
        } else {
            holder.defaultBox.setVisibility(View.GONE);
        }

        holder.editText.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditAddressActivity.class);
            intent.putExtra("object", item);
            context.startActivity(intent);
        });
        return convertView;
    }

    static class ViewHolder {
        // khai báo các thành phần giao diện
        TextView addressName, address, addressNameBox, defaultBox, editText;
    }
}
