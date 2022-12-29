package com.example.testapplication;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SubTasksRecyclerViewAdapter extends RecyclerView.Adapter <SubTasksRecyclerViewAdapter.MyViewHolder> {

    private final ArrayList<SubTask> list;
    private final Task task;
    private final String categoryId;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

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
        holder.editText.setText(list.get(position).getSubTaskName());
        String currentText = holder.editText.getText().toString();
        holder.customEditTextListener.updateCurrentText(currentText);
        holder.customEditTextListener.updatePosition(position);
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
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String text = charSequence.toString();
            String taskId = task.getTaskId();
            String subTaskId = list.get(position).getSubTaskId();

            if (mAuth.getCurrentUser() != null)
            {
                String userId = mAuth.getCurrentUser().getUid();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                        .child(userId).child(categoryId).child("Tasks").child(taskId).child("SubTasksList");

                if (currentText != null && !currentText.isEmpty())
                {
                    list.get(position).setSubTaskName(text);
                    databaseReference.child(subTaskId).setValue(text);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }
}
