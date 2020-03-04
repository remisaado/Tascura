package com.example.testapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
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
import android.widget.Toolbar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements RecyclerViewAdapter.OnItemListener, AdapterView.OnItemSelectedListener {

    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerViewAdapter recyclerViewAdapter;
    EditText taskEditText;
    View addTaskButton;
    Toolbar toolBar;

    public static final String TASK_NAME = "com.example.testapplication.TASK";

    ArrayList<Task> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        taskEditText = findViewById(R.id.taskEditText);
        addTaskButton = findViewById(R.id.addTaskButton);
        toolBar = findViewById(R.id.toolBar);
        toolBar.inflateMenu(R.menu.menu);

        setSpinner();

        initRecyclerView();

        taskEditText.setOnEditorActionListener(editorActionListener);

        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {onAddTaskClick();}
        });

        for (int i = 1; i < 16; i++)
        {
            list.add(new Task("Task " + i));
        }
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
                Toast.makeText(this, "addList selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menuItem2:
                Toast.makeText(this, "menuItem2 selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menuItem3:
                Toast.makeText(this, "menuItem3 selected", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void setSpinner()
    {
        String[] categories = {"Category one", "Category two", "Category three", "Category four", "Category five"};

        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter(this, R.layout.custom_title, android.R.id.text1, categories);

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
        //FirebaseDatabase database = FirebaseDatabase.getInstance();
        //DatabaseReference myRef = database.getReference("List");

        String text = taskEditText.getText().toString();
        if (text.trim().length() > 0)
        {
            list.add(new Task(text));
            recyclerViewAdapter.notifyDataSetChanged();

            taskEditText.getText().clear();

            //myRef.push().setValue(text);

            Toast.makeText(this, "New task added", Toast.LENGTH_SHORT).show();
        } else
        {
            Toast.makeText(this, "You did not enter any text", Toast.LENGTH_SHORT).show();
        }
    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            list.remove(viewHolder.getAdapterPosition());
            recyclerViewAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onItemClick(int position)
    {
        String taskName = list.get(position).getTaskName();
        Intent intent = new Intent(this, TaskItemActivity.class);
        intent.putExtra(TASK_NAME, taskName);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
    }
}