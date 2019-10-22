package com.example.testapplication;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetDialog extends BottomSheetDialogFragment {

    private BottomSheetListener mListener;
    private EditText taskEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_layout, container, false);

        taskEditText = v.findViewById(R.id.taskEditText);
        Button addTaskButton = v.findViewById(R.id.addTaskButton);
        Button cancelButton = v.findViewById(R.id.cancelButton);

        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String text = taskEditText.getText().toString();
                if (text.trim().length() > 0)
                {
                    mListener.onAddButtonClicked(text);

                    dismiss();
                } else
                    {
                        Toast.makeText(getActivity(), "You did not enter any text", Toast.LENGTH_SHORT).show();
                    }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                dismiss();
            }
        });

        return v;
    }

    interface BottomSheetListener
    {
        void onAddButtonClicked(String text);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (BottomSheetListener) context;
        } catch (ClassCastException e)
        {
            throw new ClassCastException(context.toString() + " must implement BottomSheetListener.");
        }
    }
}