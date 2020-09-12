package com.mhamza007.chatapp;

public class User {
    String name;
    String image;

    public User(){

    }

    public User(String name, String image){
        this.name = name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
