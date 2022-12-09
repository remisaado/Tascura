package com.example.testapplication;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SubTasksRecyclerViewAdapter extends RecyclerView.Adapter <SubTasksRecyclerViewAdapter.MyViewHolder> {

    private final ArrayList<String> list;

    SubTasksRecyclerViewAdapter(ArrayList<String> list)
    {
        this.list = list;
    }

    @NonNull
    @Override
    public SubTasksRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        EditText editText = (EditText) LayoutInflater.from(parent.getContext()).inflate(R.layout.sub_task_single_view, parent, false);

        return new MyViewHolder(editText, new CustomEditTextListener());
    }

    @Override
    public void onBindViewHolder(@NonNull SubTasksRecyclerViewAdapter.MyViewHolder holder, int position) {
        holder.editText.setText(list.get(position));
        holder.customEditTextListener.updatePosition(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder
    {
        EditText editText;
        CustomEditTextListener customEditTextListener;

        public MyViewHolder(@NonNull View itemView, CustomEditTextListener customEditTextListener) {
            super(itemView);

            editText = itemView.findViewById(R.id.subTaskSingleView);
            this.customEditTextListener = customEditTextListener;
            editText.addTextChangedListener(customEditTextListener);
        }
    }

    private class CustomEditTextListener implements TextWatcher {

        private int position;

        public void updatePosition(int position)
        {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            list.set(position, charSequence.toString());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }
}
