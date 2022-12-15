package com.example.testapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class TaskItemActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    SubTasksRecyclerViewAdapter subTasksRecyclerViewAdapter;
    EditText informationEditText;
    EditText subTaskEditText;
    FirebaseAuth mAuth;
    String categoryId;
    String taskId;

    ArrayList<String> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_item);

        RelativeLayout relativeLayout = findViewById(R.id.relativeLayout);
        recyclerView = findViewById(R.id.recyclerView);
        TextView superTaskTextView = findViewById(R.id.superTaskTextView);
        informationEditText = findViewById(R.id.informationEditText);
        subTaskEditText = findViewById(R.id.subTaskEditText);

        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        Task superTask = intent.getParcelableExtra(MainActivity.KEY_NAME);
        categoryId = intent.getStringExtra(MainActivity.KEY_NAME_TWO);

        superTaskTextView.setText(superTask.getTaskName());
        informationEditText.setText(superTask.getTaskNotes());
        list.addAll(superTask.getSubTasksList());
        taskId = superTask.getTaskId();

        initRecyclerView();

        relativeLayout.setOnClickListener(v -> finish());

        subTaskEditText.setOnEditorActionListener(editorActionListener);
    }

    private void initRecyclerView()
    {
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        subTasksRecyclerViewAdapter = new SubTasksRecyclerViewAdapter(list);
        recyclerView.setAdapter(subTasksRecyclerViewAdapter);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.divider_line, null));
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    private final TextView.OnEditorActionListener editorActionListener = (textView, i, keyEvent) -> {

        onAddSubTaskClick();

        return true;
    };

    private void onAddSubTaskClick()
    {
        if (mAuth.getCurrentUser() != null)
        {
            String userId = mAuth.getCurrentUser().getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                    .child(userId).child(categoryId).child("Tasks").child(taskId).child("SubTasksList");

            String text = subTaskEditText.getText().toString();

            if (text.trim().length() > 0)
            {
                list.add(text);

                subTasksRecyclerViewAdapter.notifyItemInserted(list.size() - 1);

                subTaskEditText.getText().clear();

                databaseReference.push().setValue(text);
            }
        }
    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            list.remove(viewHolder.getBindingAdapterPosition());
            subTasksRecyclerViewAdapter.notifyItemRemoved(viewHolder.getBindingAdapterPosition());
        }
    };
}
