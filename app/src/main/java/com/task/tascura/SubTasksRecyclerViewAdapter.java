package com.task.tascura;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;

public class SubTasksRecyclerViewAdapter extends RecyclerView.Adapter <SubTasksRecyclerViewAdapter.MyViewHolder> {

    private final ArrayList<SubTask> list;
    private final Task task;
    private final FirebaseHelper firebaseHelper = new FirebaseHelper();

    SubTasksRecyclerViewAdapter(ArrayList<SubTask> list, Task task)
    {
        this.list = list;
        this.task = task;
    }

    @NonNull
    @Override
    public SubTasksRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        // Inflate the layout for each item in the RecyclerView.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sub_task_single_view, parent, false);

        // Return a new ViewHolder with the inflated layout.
        return new MyViewHolder(view, task);
    }

    @Override
    public void onBindViewHolder(@NonNull SubTasksRecyclerViewAdapter.MyViewHolder holder, int position)
    {
        // Bind data to the ViewHolder for each item in the RecyclerView.

        holder.customEditTextListener.updatePosition(position);
        String currentText = list.get(position).getSubTaskName();
        holder.customEditTextListener.updateCurrentText(currentText);
        holder.editText.setText(currentText);

        Context mContext = holder.editText.getContext();
        ImageView checkmarkBoxView = holder.itemView.findViewById(R.id.checkmark_box);
        EditText editText = holder.editText;

        checkIfChecked(checkmarkBoxView, editText, mContext, position);

        checkmarkBoxView.setOnClickListener(v -> onCheckMarkClick(mContext, position));
    }

    void checkIfChecked(ImageView checkmarkBoxView, EditText editText, Context mContext, int position)
    {
        // Check if the task at the current position is checked and update the checkmark box and text accordingly.
        if (list.get(position).getIsChecked())
        {
            // Set the checkmark box image to the checked circle and add a strike-through to the text.
            checkmarkBoxView.setImageResource(R.drawable.ic_checked_circle_30dp);
            editText.setPaintFlags(editText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            editText.setTextColor(mContext.getResources().getColor(R.color.colorStrikeThroughGray));
        }
        else
        {
            // Set the checkmark box image to the unchecked circle and remove the strike-through from the text.
            checkmarkBoxView.setImageResource(R.drawable.ic_unchecked_circle_30dp);
            editText.setPaintFlags(editText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            editText.setTextColor(mContext.getResources().getColor(R.color.colorText));
        }
    }

    void onCheckMarkClick(Context mContext, int position)
    {
        // Toggle the task's checked state when the checkmark box is clicked.

        SharedPreferences sharedPrefs = mContext.getSharedPreferences(MainActivity.SHARED_PREFS, 0);
        String categoryId = sharedPrefs.getString(MainActivity.CATEGORY_ID_CHOICE, "");

        SubTask subTask = list.get(position);

        String taskId = task.getTaskId();
        String subTaskId = subTask.getSubTaskId();

        DatabaseReference databaseReference = firebaseHelper.getDatabaseReference()
                .child(categoryId).child(DatabaseNodes.TASKS).child(taskId)
                .child(DatabaseNodes.SUB_TASKS_LIST).child(subTaskId);

        HashMap<String, Object> subTaskUpdates = new HashMap<>();
        subTaskUpdates.put(DatabaseNodes.IS_CHECKED, !subTask.getIsChecked());
        // Updates the value of isChecked in the Firebase database.
        databaseReference.updateChildren(subTaskUpdates);

        subTask = new SubTask.SubTaskBuilder()
                .subTaskName(subTask.getSubTaskName())
                .subTaskId(subTask.getSubTaskId())
                .isChecked(!subTask.getIsChecked())
                .build();

        list.set(position, subTask);

        notifyItemChanged(position);
    }

    @Override
    public int getItemCount()
    {
        // Returns the number of items in the list.
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        EditText editText;
        CustomEditTextListener customEditTextListener;

        public MyViewHolder(@NonNull View itemView, Task task) {
            super(itemView);

            SharedPreferences sharedPrefs = itemView.getContext().getSharedPreferences(MainActivity.SHARED_PREFS, 0);
            String categoryId = sharedPrefs.getString(MainActivity.CATEGORY_ID_CHOICE, "");

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
            // updates the current text of the subtask.
            this.currentText = currentText;
        }

        public void updatePosition(int position)
        {
            // updates the position of the subtask in the list.
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
                // Update the subtask name in the list and in the Firebase database.
                list.get(position).setSubTaskName(text);
                databaseReference.child(categoryId).child(DatabaseNodes.TASKS).child(taskId)
                        .child(DatabaseNodes.SUB_TASKS_LIST).child(subTaskId)
                        .child(DatabaseNodes.SUB_TASK_NAME).setValue(text);
            }
        }

        @Override
        public void afterTextChanged(Editable editable)
        {
        }
    }
}
