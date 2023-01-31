package com.example.testapplication;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class SubTasksRecyclerViewAdapter extends RecyclerView.Adapter <SubTasksRecyclerViewAdapter.MyViewHolder> {

    private final ArrayList<SubTask> list;
    private final Task task;
    private final String categoryId;
    private final FirebaseHelper firebaseHelper = new FirebaseHelper();

    SubTasksRecyclerViewAdapter(ArrayList<SubTask> list, Task task, String categoryId)
    {
        this.list = list;
        this.task = task;
        this.categoryId = categoryId;
    }

    @NonNull
    @Override
    public SubTasksRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        EditText editText = (EditText) LayoutInflater.from(parent.getContext()).inflate(R.layout.sub_task_single_view, parent, false);

        return new MyViewHolder(editText, task, categoryId);
    }

    @Override
    public void onBindViewHolder(@NonNull SubTasksRecyclerViewAdapter.MyViewHolder holder, int position) {
        holder.customEditTextListener.updatePosition(position);
        String currentText = list.get(position).getSubTaskName();
        holder.customEditTextListener.updateCurrentText(currentText);
        holder.editText.setText(currentText);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        EditText editText;
        CustomEditTextListener customEditTextListener;

        public MyViewHolder(@NonNull View itemView, Task task, String categoryId) {
            super(itemView);

            editText = itemView.findViewById(R.id.subTaskSingleView);
            this.customEditTextListener = new CustomEditTextListener(task, categoryId);
            editText.addTextChangedListener(customEditTextListener);
        }
    }

    private class CustomEditTextListener implements TextWatcher {

        private int position;
        private final Task task;
        private final String categoryId;
        private String currentText;

        public CustomEditTextListener(Task task, String categoryId)
        {
            this.task = task;
            this.categoryId = categoryId;
        }

        public void updateCurrentText(String currentText)
        {
            this.currentText = currentText;
        }

        public void updatePosition(int position)
        {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {
            String text = charSequence.toString();
            String taskId = task.getTaskId();

            DatabaseReference databaseReference = firebaseHelper.getDatabaseReference();

            if (currentText != null)
            {
                String subTaskId = list.get(position).getSubTaskId();

                list.get(position).setSubTaskName(text);
                databaseReference.child(categoryId).child(DatabaseNodes.TASKS).child(taskId)
                        .child(DatabaseNodes.SUB_TASKS_LIST).child(subTaskId).setValue(text);
            }
        }

        @Override
        public void afterTextChanged(Editable editable)
        {
        }
    }
}
