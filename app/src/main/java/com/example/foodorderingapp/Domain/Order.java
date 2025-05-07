package com.example.foodorderingapp.Domain;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Order {
    public Order(){}
    public String orderid;
    public ArrayList<orderlist> orderlists;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public ArrayList<orderlist> getOrderlists() {
        return orderlists;
    }

    public void setOrderlists(ArrayList<orderlist> orderlists) {
        this.orderlists = orderlists;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String date;

    public Order(String orderid, double total, String time, String date, ArrayList<orderlist> orderlists) {
        this.orderid = orderid;
        this.total = total;
        this.time = time;
        this.date = date;
        this.orderlists = orderlists;
    }

    public String time;
    public double total;



}
