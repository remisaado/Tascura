package com.example.testapplication;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Task implements Parcelable {
    private final String taskName;
    private final String taskId;
    private final String taskNotes;
    private final ArrayList<String> subTasksList;

    Task(String taskName, String taskId, String taskNotes, ArrayList<String> subTasksList)
    {
        this.taskName = taskName;
        this.taskId = taskId;
        this.taskNotes = taskNotes;
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

    public String getTaskNotes()
    {
        return taskNotes;
    }

    public ArrayList<String> getSubTasksList()
    {
        return subTasksList;
    }

    protected Task(Parcel in)
    {
        taskName = in.readString();
        taskId = in.readString();
        taskNotes = in.readString();
        subTasksList = in.createStringArrayList();
    }

    public static final Creator<Task> CREATOR = new Creator<Task>()
    {
        @Override
        public Task createFromParcel(Parcel in)
        {
            return new Task(in);
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
        parcel.writeString(taskNotes);
        parcel.writeStringList(subTasksList);
    }

    public static class TaskBuilder {
        private String taskName;
        private String taskId;
        private String taskNotes;
        private ArrayList<String> subTasksList;

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

        public TaskBuilder taskNotes(String taskNotes)
        {
            this.taskNotes = taskNotes;
            return this;
        }

        public TaskBuilder subTasksList(ArrayList<String> subTasksList)
        {
            this.subTasksList = subTasksList;
            return this;
        }

        public Task build()
        {
            return new Task(taskName, taskId, taskNotes, subTasksList);
        }

    }
}
