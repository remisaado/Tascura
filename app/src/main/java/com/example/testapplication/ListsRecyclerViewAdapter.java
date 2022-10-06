package com.example.testapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ListsRecyclerViewAdapter extends RecyclerView.Adapter<ListsRecyclerViewAdapter.MyViewHolder> {

    private ArrayList<Category> categories;

    ListsRecyclerViewAdapter(ArrayList<Category> categories)
    {
        this.categories = categories;
    }

    @NonNull
    @Override
    public ListsRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView textView = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view, parent, false);

        return new MyViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(@NonNull ListsRecyclerViewAdapter.MyViewHolder holder, int position) {
        holder.textView.setText(categories.get(position).getCategoryName());
    }

    @Override
    public int getItemCount() {
        return categories == null ? 0 : categories.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView textView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.singleView);
        }
    }
}
