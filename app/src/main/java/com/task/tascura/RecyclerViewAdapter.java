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

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;

public class RecyclerViewAdapter extends RecyclerView.Adapter <RecyclerViewAdapter.MyViewHolder> {

    private final ArrayList<Task> list;
    private final OnItemListener mOnItemListener;
    private final FirebaseHelper firebaseHelper = new FirebaseHelper();

    RecyclerViewAdapter(ArrayList<Task> list, OnItemListener onItemListener)
    {
        this.list = list;
        this.mOnItemListener = onItemListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view, parent, false);

        return new MyViewHolder(view, mOnItemListener, list);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.textView.setText(list.get(position).getTaskName());

        Context mContext = holder.textView.getContext();
        ImageView checkmarkBoxView = holder.itemView.findViewById(R.id.checkmark_box);
        TextView textView = holder.textView;

        checkIfChecked(checkmarkBoxView, textView, mContext, position);

        checkmarkBoxView.setOnClickListener(v -> onCheckMarkClick(mContext, position));
    }

    void checkIfChecked(ImageView checkmarkBoxView, TextView textView, Context mContext, int position)
    {
        if (list.get(position).getIsChecked())
        {
            checkmarkBoxView.setImageResource(R.drawable.ic_checked_circle_36dp);
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            textView.setTextColor(mContext.getResources().getColor(R.color.colorStrikeThroughGray));
        }
        else
        {
            checkmarkBoxView.setImageResource(R.drawable.ic_unchecked_circle_36dp);
            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            textView.setTextColor(mContext.getResources().getColor(R.color.colorText));
        }
    }

    void onCheckMarkClick(Context mContext, int position)
    {
        SharedPreferences sharedPrefs = mContext.getSharedPreferences(MainActivity.SHARED_PREFS, 0);
        String categoryId = sharedPrefs.getString(MainActivity.CATEGORY_ID_CHOICE, "");

        Task task = list.get(position);

        String taskId = task.getTaskId();

        DatabaseReference databaseReference = firebaseHelper.getDatabaseReference()
                .child(categoryId).child(DatabaseNodes.TASKS).child(taskId);

        HashMap<String, Object> taskUpdates = new HashMap<>();
        taskUpdates.put(DatabaseNodes.IS_CHECKED, !task.getIsChecked());

        databaseReference.updateChildren(taskUpdates);

        task = new Task.TaskBuilder()
                .taskName(task.getTaskName())
                .taskId(task.getTaskId())
                .taskInformation(task.getTaskInformation())
                .subTasksList(task.getSubTasksList())
                .isChecked(!task.getIsChecked())
                .build();

        list.set(position, task);

        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView textView;
        OnItemListener onItemListener;
        ArrayList<Task> list;

        MyViewHolder(@NonNull View itemView, OnItemListener onItemListener, ArrayList<Task> list) {
            super(itemView);

            textView = itemView.findViewById(R.id.singleView);
            this.onItemListener = onItemListener;
            this.list = list;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(view -> {
                onLongClick();
                return true;
            });
        }

        private void onLongClick()
        {
            String text = textView.getText().toString();

            final EditText input = new EditText(itemView.getContext());
            input.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.edit_text_background));
            input.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.colorText));
            input.setText(text);
            input.setSingleLine();
            input.setImeOptions(EditorInfo.IME_ACTION_SEND);
            input.setPadding(28, 28, 28, 28);

            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext(), R.style.DialogTheme);
            builder.setTitle(R.string.dialog_edit_title)
                    .setMessage(R.string.dialog_edit_message)
                    .setView(input)
                    .setPositiveButton(R.string.dialog_edit_positive_button, (dialogInterface, i) -> {
                        String editedText = input.getText().toString();
                        saveEditedTaskName(itemView.getContext(), editedText);
                    })
                    .setNegativeButton(R.string.dialog_edit_negative_button, (dialogInterface, i) -> dialogInterface.cancel());

            AlertDialog dialog = builder.show();

            input.setOnEditorActionListener((textView, actionId, keyEvent) ->
            {
                if (actionId == EditorInfo.IME_ACTION_SEND)
                {
                    String editedText = input.getText().toString();
                    saveEditedTaskName(itemView.getContext(), editedText);
                    dialog.dismiss();
                    return true;
                }
                return false;
            });
        }

        private void saveEditedTaskName(Context mContext, String editedText)
        {
            SharedPreferences sharedPrefs = mContext.getSharedPreferences(MainActivity.SHARED_PREFS, 0);
            String categoryId = sharedPrefs.getString(MainActivity.CATEGORY_ID_CHOICE, "");

            FirebaseHelper firebaseHelper = new FirebaseHelper();

            Task task = list.get(getBindingAdapterPosition());
            String taskId = task.getTaskId();

            DatabaseReference databaseReference = firebaseHelper.getDatabaseReference();
            databaseReference.child(categoryId).child(DatabaseNodes.TASKS)
                    .child(taskId).child(DatabaseNodes.TASK_NAME).setValue(editedText);
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