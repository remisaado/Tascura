package com.example.testapplication;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Task implements Parcelable {
    private final String taskName;
    private final String taskId;
    private final String taskInformation;
    private final ArrayList<SubTask> subTasksList;

    Task(String taskName, String taskId, String taskInformation, ArrayList<SubTask> subTasksList)
    {
        this.taskName = taskName;
        this.taskId = taskId;
        this.taskInformation = taskInformation;
        this.subTasksList = subTasksList;
    }

    public String getTaskName()
    {
        return taskName;
    }

    public String getTaskId()
    {
        return taskId;
    }

    public String getTaskInformation()
    {
        return taskInformation;
    }

    public ArrayList<SubTask> getSubTasksList()
    {
        return subTasksList;
    }

    protected Task(Parcel in)
    {
        taskName = in.readString();
        taskId = in.readString();
        taskInformation = in.readString();
        subTasksList = in.createTypedArrayList(SubTask.CREATOR);
    }

    public static final Creator<Task> CREATOR = new Creator<Task>()
    {
        @Override
        public Task createFromParcel(Parcel in) {
            String taskName = in.readString();
            String taskId = in.readString();
            String taskInformation = in.readString();
            ArrayList<SubTask> subTasksList = in.createTypedArrayList(SubTask.CREATOR);
            return new Task(taskName, taskId, taskInformation, subTasksList);
        }

        @Override
        public Task[] newArray(int size)
        {
            return new Task[size];
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
        parcel.writeString(taskName);
        parcel.writeString(taskId);
        parcel.writeString(taskInformation);
        parcel.writeTypedList(subTasksList);
    }

    public static class TaskBuilder {
        private String taskName;
        private String taskId;
        private String taskInformation;
        private ArrayList<SubTask> subTasksList;

        public TaskBuilder taskName(String taskName)
        {
            this.taskName = taskName;
            return this;
        }

        public TaskBuilder taskId(String taskId)
        {
            this.taskId = taskId;
            return this;
        }

        public TaskBuilder taskInformation(String taskInformation)
        {
            this.taskInformation = taskInformation;
            return this;
        }

        public TaskBuilder subTasksList(ArrayList<SubTask> subTasksList)
        {
            this.subTasksList = subTasksList;
            return this;
        }

        public Task build()
        {
            return new Task(taskName, taskId, taskInformation, subTasksList);
        }

    }
}
