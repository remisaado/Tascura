package com.example.testapplication;

import android.os.Parcel;
import android.os.Parcelable;

public class Category implements Parcelable {
    private String categoryName;
    private String categoryId;

    public Category(String categoryName, String categoryId)
    {
        this.categoryName = categoryName;
        this.categoryId = categoryId;
    }

    protected Category(Parcel in) {
        categoryName = in.readString();
        categoryId = in.readString();
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    public String getCategoryName()
    {
        return categoryName;
    }

    public String getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(categoryName);
        dest.writeString(categoryId);
    }

    @Override
    public String toString() {
        return categoryName;
    }
}
