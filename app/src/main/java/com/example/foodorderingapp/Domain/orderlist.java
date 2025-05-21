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

    public orderlist(String itemname, int num, int id, double price, String imagePath) {
        this.itemname = itemname;
        this.num = num;
        this.id = id;
        this.price = price;
        this.imagePath = imagePath;
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

    private String imagePath;


    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    // Trong class orderlist.java
    private double price; // giá 1 món ăn

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

}
