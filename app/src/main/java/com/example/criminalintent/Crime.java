package com.example.criminalintent;

import java.util.Comparator;
import java.util.Date;
import java.util.UUID;

public class Crime implements Comparable<Crime> {

    private UUID mID;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;
    private boolean mRequiresPolice;
    private String mSuspect;

    public String getSuspect() {
        return mSuspect;
    }

    public void setSuspect(String suspect) {
        mSuspect = suspect;
    }

    public boolean isRequiresPolice() {
        return mRequiresPolice;
    }

    public void setRequiresPolice(boolean requiresPolice) {
        mRequiresPolice = requiresPolice;
    }

    public UUID getID() {
        return mID;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }


    public Crime() {
        this(UUID.randomUUID());
    }

    public Crime(UUID id) {
        mID=id;
        mDate = new Date();
    }

    @Override
    public int compareTo(Crime c) {
        return mID.compareTo(c.mID);
    }
}

