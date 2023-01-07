package com.example.testapplication;

import android.os.Parcel;
import android.os.Parcelable;

public class SubTask implements Parcelable {
    private String subTaskName;
    private final String subTaskId;

    SubTask(String subTaskName, String subTaskId)
    {
        this.subTaskName = subTaskName;
        this.subTaskId = subTaskId;
    }

    public String getSubTaskName()
    {
        return subTaskName;
    }

    public String getSubTaskId()
    {
        return subTaskId;
    }

    public void setSubTaskName(String subTaskName)
    {
        this.subTaskName = subTaskName;
    }

    protected SubTask(Parcel in)
    {
        subTaskName = in.readString();
        subTaskId = in.readString();
    }

    public static final Creator<SubTask> CREATOR = new Creator<SubTask>()
    {
        @Override
        public SubTask createFromParcel(Parcel in)
        {
            String subTaskName = in.readString();
            String subTaskId = in.readString();
            return new SubTask(subTaskName, subTaskId);
        }

        @Override
        public SubTask[] newArray(int size)
        {
            return new SubTask[size];
        }
    };

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i)
    {
        parcel.writeString(subTaskName);
        parcel.writeString(subTaskId);
    }

    public static class SubTaskBuilder {
        private String subTaskName;
        private String subTaskId;

        public SubTaskBuilder subTaskName(String subTaskName)
        {
            this.subTaskName = subTaskName;
            return this;
        }

        public SubTaskBuilder subTaskId(String subTaskId)
        {
            this.subTaskId = subTaskId;
            return this;
        }

        public SubTask build()
        {
            return new SubTask(subTaskName, subTaskId);
        }

    }
}
