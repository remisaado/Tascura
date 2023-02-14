package com.example.testapplication;

import android.os.Parcel;
import android.os.Parcelable;

public class SubTask implements Parcelable {
    private String subTaskName;
    private final String subTaskId;
    private final boolean isChecked;

    SubTask(String subTaskName, String subTaskId, boolean isChecked)
    {
        this.subTaskName = subTaskName;
        this.subTaskId = subTaskId;
        this.isChecked = isChecked;
    }

    public String getSubTaskName()
    {
        return subTaskName;
    }

    public String getSubTaskId()
    {
        return subTaskId;
    }

    public boolean getIsChecked()
    {
        return isChecked;
    }

    public void setSubTaskName(String subTaskName)
    {
        this.subTaskName = subTaskName;
    }

    public static final Creator<SubTask> CREATOR = new Creator<SubTask>()
    {
        @Override
        public SubTask createFromParcel(Parcel in)
        {
            String subTaskName = in.readString();
            String subTaskId = in.readString();
            boolean isChecked = in.readByte() != 0;
            return new SubTask(subTaskName, subTaskId, isChecked);
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
        parcel.writeByte((byte) (isChecked ? 1 : 0));
    }

    public static class SubTaskBuilder {
        private String subTaskName;
        private String subTaskId;
        private boolean isChecked;

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

        public SubTaskBuilder isChecked(boolean isChecked)
        {
            this.isChecked = isChecked;
            return this;
        }

        public SubTask build()
        {
            return new SubTask(subTaskName, subTaskId, isChecked);
        }

    }
}
