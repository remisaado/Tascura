package com.task.tascura;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;

public class RecyclerViewAdapter extends RecyclerView.Adapter <RecyclerViewAdapter.MyViewHolder> {

    private final ArrayList<Task> list;
    private final OnItemListener mOnItemListener;
    private final RecyclerViewAdapter adapter;

    RecyclerViewAdapter(ArrayList<Task> list, OnItemListener onItemListener)
    {
        this.list = list;
        this.mOnItemListener = onItemListener;
        adapter = this;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        // Inflate the layout for each item in the RecyclerView.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view, parent, false);
        // Return a new ViewHolder with the inflated layout and the OnItemListener.
        return new MyViewHolder(view, mOnItemListener, list, adapter);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        // Bind data to the ViewHolder for each item in the RecyclerView.

        holder.textView.setText(list.get(position).getTaskName());

        Context mContext = holder.textView.getContext();
        ImageView checkmarkBoxView = holder.itemView.findViewById(R.id.checkmark_box);
        TextView textView = holder.textView;

        checkIfChecked(checkmarkBoxView, textView, mContext, position);
    }

    private void checkIfChecked(ImageView checkmarkBoxView, TextView textView, Context mContext, int position)
    {
        // Check if the task at the current position is checked and update the checkmark box and text accordingly.
        if (list.get(position).getIsChecked())
        {
            // Set the checkmark box image to the checked circle and add a strike-through to the text.
            checkmarkBoxView.setImageResource(R.drawable.ic_checked_circle_36dp);
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            textView.setTextColor(mContext.getResources().getColor(R.color.colorStrikeThroughGray));
        }
        else
        {
            // Set the checkmark box image to the unchecked circle and remove the strike-through from the text.
            checkmarkBoxView.setImageResource(R.drawable.ic_unchecked_circle_36dp);
            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            textView.setTextColor(mContext.getResources().getColor(R.color.colorText));
        }
    }

    @Override
    public int getItemCount()
    {
        // Returns the number of items in the list.
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView textView;
        OnItemListener onItemListener;
        ArrayList<Task> list;
        private final RecyclerViewAdapter adapter;

        MyViewHolder(@NonNull View itemView, OnItemListener onItemListener, ArrayList<Task> list, RecyclerViewAdapter adapter) {
            super(itemView);

            textView = itemView.findViewById(R.id.singleView);
            ImageView checkmarkBoxView = itemView.findViewById(R.id.checkmark_box);
            this.onItemListener = onItemListener;
            this.list = list;
            this.adapter = adapter;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(view -> {
                onLongClick(getBindingAdapterPosition());
                return true;
            });
            checkmarkBoxView.setOnClickListener(v -> onCheckMarkClick(getBindingAdapterPosition()));
        }

        private void onLongClick(int position)
        {
            // Handles the long click event on an item in the RecyclerView,
            // opens a Dialog with an EditText with the name of the task to edit the task name.

            String text = textView.getText().toString();
            // Creates the EditText for user input and style the look and behavior of it.
            final EditText input = new EditText(itemView.getContext());
            input.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.edit_text_dialog));
            input.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.colorText));
            input.setText(text);
            input.setSingleLine();
            input.setImeOptions(EditorInfo.IME_ACTION_SEND);
            input.setPadding(28, 28, 28, 28);

            // Create an AlertDialog for the user to edit the task name.
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext(), R.style.DialogTheme);
            builder.setTitle(R.string.dialog_edit_title)
                    .setMessage(R.string.dialog_edit_message)
                    .setView(input)
                    .setPositiveButton(R.string.dialog_edit_positive_button, (dialogInterface, i) ->
                    {
                        // When the positive button is clicked, save the edited task name.
                        String editedText = input.getText().toString();
                        if (!editedText.isEmpty())
                        {
                        saveEditedTaskName(itemView.getContext(), editedText, position);
                        }
                    })
                    .setNegativeButton(R.string.dialog_edit_negative_button, (dialogInterface, i) -> dialogInterface.cancel());
            // Show the AlertDialog.
            AlertDialog dialog = builder.show();

            input.setOnEditorActionListener((textView, actionId, keyEvent) ->
            {
                // Set the listener for the send button on the EditText.
                if (actionId == EditorInfo.IME_ACTION_SEND)
                {
                    // When the send button is clicked, save the edited task name and dismiss the dialog.
                    String editedText = input.getText().toString();
                    if (!editedText.isEmpty())
                    {
                        saveEditedTaskName(itemView.getContext(), editedText, position);
                    }
                    dialog.dismiss();
                    return true;
                }
                return false;
            });
        }

        private void saveEditedTaskName(Context mContext, String editedText, int position)
        {
            // Method to save the edited task name.

            SharedPreferences sharedPrefs = mContext.getSharedPreferences(MainActivity.SHARED_PREFS, 0);
            String categoryId = sharedPrefs.getString(MainActivity.CATEGORY_ID_CHOICE, "");

            FirebaseHelper firebaseHelper = new FirebaseHelper();
            if (position != RecyclerView.NO_POSITION)
            {
                Task task = list.get(position);
                String taskId = task.getTaskId();

                DatabaseReference databaseReference = firebaseHelper.getDatabaseReference();
                databaseReference.child(categoryId).child(DatabaseNodes.TASKS)
                        .child(taskId).child(DatabaseNodes.TASK_NAME).setValue(editedText);
            }
            else
            {
                Toast.makeText(mContext, "Operation failed, please try again later.", Toast.LENGTH_SHORT).show();
            }

            adapter.notifyItemChanged(position);
        }

        private void onCheckMarkClick(int position)
        {
            // Toggle the task's checked state when the checkmark box is clicked.

            SharedPreferences sharedPrefs = textView.getContext().getSharedPreferences(MainActivity.SHARED_PREFS, 0);
            String categoryId = sharedPrefs.getString(MainActivity.CATEGORY_ID_CHOICE, "");

            Task task = list.get(position);

            String taskId = task.getTaskId();

            FirebaseHelper firebaseHelper = new FirebaseHelper();
            DatabaseReference databaseReference = firebaseHelper.getDatabaseReference()
                    .child(categoryId).child(DatabaseNodes.TASKS).child(taskId);

            HashMap<String, Object> taskUpdates = new HashMap<>();
            taskUpdates.put(DatabaseNodes.IS_CHECKED, !task.getIsChecked());
            // Updates the value of isChecked in the Firebase database.
            databaseReference.updateChildren(taskUpdates);

            task = new Task.TaskBuilder()
                    .taskName(task.getTaskName())
                    .taskId(task.getTaskId())
                    .taskInformation(task.getTaskInformation())
                    .subTasksList(task.getSubTasksList())
                    .isChecked(!task.getIsChecked())
                    .build();

            list.set(position, task);

            adapter.notifyItemChanged(position);
        }

        @Override
        public void onClick(View v)
        {
            onItemListener.onItemClick(getBindingAdapterPosition());
        }
    }

    public interface OnItemListener
    {
        void onItemClick(int position);
    }
}