package com.example.testapplication;

import java.util.ArrayList;

public class Task {
    private final String taskName;
    private final String taskId;
    private String taskNotes;
    private ArrayList<String> subTasksList;

    public Task(String taskName, String taskId)
    {
        this.taskName = taskName;
        this.taskId = taskId;
    }

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
