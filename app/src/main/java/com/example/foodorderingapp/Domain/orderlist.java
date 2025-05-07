package com.example.foodorderingapp.Domain;

public class orderlist {

    public orderlist(){}
    public String itemname;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int id;

    public orderlist( String itemname,int num, int id) {
        this.num = num;
        this.itemname = itemname;
        this.id = id;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getItemname() {
        return itemname;
    }

    public void setItemname(String itemname) {
        this.itemname = itemname;
    }

    public int num;



}
