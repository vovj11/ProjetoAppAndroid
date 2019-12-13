package com.androidapps.italo.d20simulator;

public class Rolls {

    String rollId;
    int diceType;
    int rollValue;
    String rollDate;
    double rollLat;
    double rollLng;

    public Rolls() {

    }

    public Rolls(String rollId, int diceType, int rollValue, String rollDate, double rollLat, double rollLng) {
        this.rollId = rollId;
        this.diceType = diceType;
        this.rollValue = rollValue;
        this.rollDate = rollDate;
        this.rollLat = rollLat;
        this.rollLng = rollLng;
    }

    public String getRollId() {
        return rollId;
    }

    public int getDiceType() {
        return diceType;
    }

    public String getRollDate() {
        return rollDate;
    }

    public int getRollValue() {
        return rollValue;
    }

    public double getRollLat() {
        return rollLat;
    }

    public double getRollLng() {
        return rollLng;
    }
}
