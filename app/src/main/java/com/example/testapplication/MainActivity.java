package com.example.testapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    ProgressBar progressBar;
    FirebaseAuth mAuth;

    public static final String KEY_NAME = "com.example.testapplication.KEY";
    public static final String KEY_NAME_TWO = "com.example.testapplication.KEYTWO";
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SPINNER_CHOICE = "spinnerChoice";

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
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        if (toolbar != null)
        {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        initRecyclerView();

        taskEditText.setOnEditorActionListener(editorActionListener);

        addTaskButton.setOnClickListener(v -> onAddTaskClick());
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null)
        {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
        else
        {
            setSpinner();
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
            case R.id.manageLists:
                Intent addIntent = new Intent(this, AddListActivity.class);
                addIntent.putParcelableArrayListExtra(KEY_NAME, categories);
                startActivity(addIntent);
                return true;
            case R.id.renameList:
                Intent renameIntent = new Intent(this, RenameListActivity.class);
                renameIntent.putParcelableArrayListExtra(KEY_NAME, categories);
                renameIntent.putExtra(KEY_NAME_TWO, spinner.getSelectedItemPosition());
//                startActivity(renameIntent);
                return true;
            case R.id.logOut:
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void setSpinner()
    {
        String userId = mAuth.getCurrentUser().getUid();
        final DatabaseReference categoryDBRef = FirebaseDatabase.getInstance().getReference(userId).child("CategoryList");

        categoryDBRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categories.clear();

                if (dataSnapshot.getValue() == null)
                {
                    String autoGeneratedId = categoryDBRef.push().getKey();

                    categories.add(new Category("My List", autoGeneratedId));
                    categoryDBRef.child(autoGeneratedId).setValue("My List");
                    spinnerAdapter.notifyDataSetChanged();
                }

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

        loadData();
    }

    void loadData()
    {
        SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS, 0);
        int spinnerChoice = sharedPrefs.getInt(SPINNER_CHOICE, 0);
        spinner.setSelection(spinnerChoice, true);
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

    private TextView.OnEditorActionListener editorActionListener = (v, actionId, event) -> {

        onAddTaskClick();

        return true;
    };

    private void onAddTaskClick()
    {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference taskDBRef = FirebaseDatabase.getInstance().getReference(userId).child("TaskList");

        String text = taskEditText.getText().toString();
        String spinnerValue;

        if(spinner.getSelectedItem() != null)
        {
            spinnerValue = spinner.getSelectedItem().toString();

            if (text.trim().length() > 0)
            {
                String autoGeneratedId = taskDBRef.child(spinnerValue).push().getKey();

                list.add(new Task(text, autoGeneratedId));

                recyclerViewAdapter.notifyDataSetChanged();

                taskEditText.getText().clear();

                taskDBRef.child(spinnerValue).child(autoGeneratedId).setValue(text);

                Toast.makeText(this, "New task added", Toast.LENGTH_SHORT).show();
            } else
                {
                    Toast.makeText(this, "You did not enter any text", Toast.LENGTH_SHORT).show();
                }
        } else
            {
                Toast.makeText(this, "No list chosen", Toast.LENGTH_SHORT).show();
            }

    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            String userId = mAuth.getCurrentUser().getUid();
            DatabaseReference taskDBRef = FirebaseDatabase.getInstance().getReference(userId).child("TaskList");
            String spinnerValue = spinner.getSelectedItem().toString();

            String taskId = list.get(viewHolder.getBindingAdapterPosition()).getTaskId();

            list.remove(viewHolder.getBindingAdapterPosition());

            taskDBRef.child(spinnerValue).child(taskId).removeValue();

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
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference taskDBRef = FirebaseDatabase.getInstance().getReference(userId).child("TaskList").child(spinnerValue);

            taskDBRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    list.clear();
                    recyclerViewAdapter.notifyDataSetChanged();

                    progressBar.setVisibility(View.VISIBLE);

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        list.add(new Task(snapshot.getValue().toString(), snapshot.getKey()));
                        recyclerViewAdapter.notifyDataSetChanged();
                    }

                    progressBar.setVisibility(View.GONE);
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

    @Override
    protected void onPause() {
        super.onPause();

        int spinnerChoice = spinner.getSelectedItemPosition();
        SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS, 0);
        SharedPreferences.Editor sharedPrefsEditor = sharedPrefs.edit();
        sharedPrefsEditor.putInt(SPINNER_CHOICE, spinnerChoice);
        sharedPrefsEditor.apply();
    }
}