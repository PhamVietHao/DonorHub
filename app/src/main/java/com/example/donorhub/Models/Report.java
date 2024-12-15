package com.example.donorhub.Models;

public class Report {
    private String id;
    private String eventId;
    private String donationSiteId;
    private int amountOfBloodA;
    private int amountOfBloodB;
    private int amountOfBloodO;
    private int amountOfBloodAB;

    // Constructors
    public Report() {}

    public Report(String id, String eventId, String donationSiteId, int amountOfBloodA, int amountOfBloodB, int amountOfBloodO, int amountOfBloodAB) {
        this.id = id;
        this.eventId = eventId;
        this.donationSiteId = donationSiteId;
        this.amountOfBloodA = amountOfBloodA;
        this.amountOfBloodB = amountOfBloodB;
        this.amountOfBloodO = amountOfBloodO;
        this.amountOfBloodAB = amountOfBloodAB;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getDonationSiteId() {
        return donationSiteId;
    }

    public void setDonationSiteId(String donationSiteId) {
        this.donationSiteId = donationSiteId;
    }

    public int getAmountOfBloodA() {
        return amountOfBloodA;
    }

    public void setAmountOfBloodA(int amountOfBloodA) {
        this.amountOfBloodA = amountOfBloodA;
    }

    public int getAmountOfBloodB() {
        return amountOfBloodB;
    }

    public void setAmountOfBloodB(int amountOfBloodB) {
        this.amountOfBloodB = amountOfBloodB;
    }

    public int getAmountOfBloodO() {
        return amountOfBloodO;
    }

    public void setAmountOfBloodO(int amountOfBloodO) {
        this.amountOfBloodO = amountOfBloodO;
    }

    public int getAmountOfBloodAB() {
        return amountOfBloodAB;
    }

    public void setAmountOfBloodAB(int amountOfBloodAB) {
        this.amountOfBloodAB = amountOfBloodAB;
    }
}