package com.example.testapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

        return new MyViewHolder(view, mOnItemListener);
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
            checkmarkBoxView.setImageResource(R.drawable.ic_checked_box_36dp);
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            textView.setTextColor(mContext.getResources().getColor(R.color.colorStrikeThroughGray));
        }
        else
        {
            checkmarkBoxView.setImageResource(R.drawable.ic_unchecked_box_36dp);
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

        MyViewHolder(@NonNull View itemView, OnItemListener onItemListener) {
            super(itemView);

            textView = itemView.findViewById(R.id.singleView);
            this.onItemListener = onItemListener;

            itemView.setOnClickListener(this);
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