package com.example.testapplication;

public class Task {
    private String taskName;
    private String taskId;

    public Task(String taskName, String taskId)
    {
        this.taskName = taskName;
        this.taskId = taskId;
    }

    public String getTaskName()
    {
        return taskName;
    }

    public String getTaskId()
    {
        return taskId;
    }
}
