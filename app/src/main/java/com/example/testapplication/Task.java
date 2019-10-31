package com.example.testapplication;

import java.util.ArrayList;

public class Task {
    private String taskName;
    private ArrayList<Task> subTasks;

    public Task(String taskName, ArrayList<Task> subTasks)
    {
        this.taskName = taskName;
        this.subTasks = subTasks;
    }

    public String getTaskName()
    {
        return taskName;
    }

    public void addSubTask(String subTask)
    {
        this.subTasks.add(new Task(subTask, new ArrayList<Task>()));
    }
}
