package com.example.testapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter <RecyclerViewAdapter.MyViewHolder> {

    private ArrayList<Task> list;
    private OnItemListener mOnItemListener;

    RecyclerViewAdapter(ArrayList<Task> list, OnItemListener onItemListener)
    {
        this.list = list;
        this.mOnItemListener = onItemListener;
    }

    public RecyclerViewAdapter(ArrayList<Task> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView textView = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view, parent, false);

        return new MyViewHolder(textView, mOnItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.textView.setText(list.get(position).getTaskName());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
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
            onItemListener.onItemClick(getAdapterPosition());
        }
    }

    public interface OnItemListener
    {
        void onItemClick(int position);
    }
}