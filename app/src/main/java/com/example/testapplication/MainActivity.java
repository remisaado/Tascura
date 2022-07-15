package com.example.testapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements RecyclerViewAdapter.OnItemListener, AdapterView.OnItemSelectedListener {

    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerViewAdapter recyclerViewAdapter;
    ArrayAdapter<Category> spinnerAdapter;
    EditText taskEditText;
    View addTaskButton;
    Spinner spinner;
    Toolbar toolbar;

    public static final String KEY_NAME = "com.example.testapplication.KEY";

    ArrayList<Task> list = new ArrayList<>();
    ArrayList<Category> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        taskEditText = findViewById(R.id.taskEditText);
        addTaskButton = findViewById(R.id.addTaskButton);
        spinner = findViewById(R.id.spinner);
        toolbar = findViewById(R.id.toolbar);

        if (toolbar != null)
        {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        setSpinner();

        initRecyclerView();

        taskEditText.setOnEditorActionListener(editorActionListener);

        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {onAddTaskClick();}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.addList:
                Intent intent = new Intent(this, AddListActivity.class);
                intent.putParcelableArrayListExtra(KEY_NAME, categories);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void setSpinner()
    {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("CategoryList");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categories.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    categories.add(new Category(snapshot.getValue().toString(), snapshot.getKey()));
                    spinnerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        spinnerAdapter = new ArrayAdapter(this, R.layout.custom_title, android.R.id.text1, categories);
        spinnerAdapter.setDropDownViewResource(R.layout.custom_spinner);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(this);
    }

    private void initRecyclerView()
    {
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerViewAdapter = new RecyclerViewAdapter(list, this);
        recyclerView.setAdapter(recyclerViewAdapter);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.divider_line, null));
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    private TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

            onAddTaskClick();

            return true;
        }
    };

    private void onAddTaskClick()
    {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("TaskList");

        String text = taskEditText.getText().toString();
        String spinnerValue;

        if(spinner.getSelectedItem() != null)
        {
            spinnerValue = spinner.getSelectedItem().toString();

            if (text.trim().length() > 0)
            {
                String autoGeneratedId = myRef.child(spinnerValue).push().getKey();

                list.add(new Task(text, autoGeneratedId));

                recyclerViewAdapter.notifyDataSetChanged();

                taskEditText.getText().clear();

                myRef.child(spinnerValue).child(autoGeneratedId).setValue(text);

                Toast.makeText(this, "New task added", Toast.LENGTH_SHORT).show();
            } else
                {
                    Toast.makeText(this, "You did not enter any text", Toast.LENGTH_SHORT).show();
                }
        } else
            {
                Toast.makeText(this, "Spinner is empty", Toast.LENGTH_SHORT).show();
            }

    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("TaskList");
            String spinnerValue = spinner.getSelectedItem().toString();

            String taskId = list.get(viewHolder.getAdapterPosition()).getTaskId();

            list.remove(viewHolder.getAdapterPosition());

            myRef.child(spinnerValue).child(taskId).removeValue();

            recyclerViewAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onItemClick(int position)
    {
        String taskName = list.get(position).getTaskName();
        Intent intent = new Intent(this, TaskItemActivity.class);
        intent.putExtra(KEY_NAME, taskName);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        String spinnerValue = spinner.getSelectedItem().toString();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("TaskList").child(spinnerValue);

            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    list.clear();
                    recyclerViewAdapter.notifyDataSetChanged();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        list.add(new Task(snapshot.getValue().toString(), snapshot.getKey()));
                        recyclerViewAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
    }
}