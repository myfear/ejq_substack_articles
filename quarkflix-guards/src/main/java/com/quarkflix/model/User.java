package com.quarkflix.model;

public class User {
    private String username;
    private int age;
    private boolean vip;

    public User(String username, int age, boolean vip) {
        this.username = username;
        this.age = age;
        this.vip = vip;
    }

    public String getUsername() { return username; }
    public int getAge() { return age; }
    public boolean isVip() { return vip; }

    @Override
    public String toString() {
        return "User{username='" + username + "'}";
    }
}