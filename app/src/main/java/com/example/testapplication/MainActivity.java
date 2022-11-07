package com.example.testapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
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

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

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
            case R.id.addList:
                Intent addIntent = new Intent(this, AddListActivity.class);
                addIntent.putParcelableArrayListExtra(KEY_NAME, categories);
                startActivity(addIntent);
                return true;
            case R.id.renameList:
                Intent renameIntent = new Intent(this, RenameListActivity.class);
                renameIntent.putParcelableArrayListExtra(KEY_NAME, categories);
                renameIntent.putExtra(KEY_NAME_TWO, spinner.getSelectedItemPosition());
                startActivity(renameIntent);
                return true;
            case R.id.deleteList:
                deleteListDialog();
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
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(userId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categories.clear();

                if (dataSnapshot.getValue() == null)
                {
                    String autoGeneratedId = databaseReference.push().getKey();

                    categories.add(new Category("My List", autoGeneratedId));
                    databaseReference.child(autoGeneratedId).child(autoGeneratedId).setValue("My List");
                    spinnerAdapter.notifyDataSetChanged();
                }
                else
                {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        categories.add(new Category(snapshot.child(snapshot.getKey()).getValue().toString(), snapshot.getKey()));
                        spinnerAdapter.notifyDataSetChanged();
                    }
                    loadData();
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

    private void deleteListDialog()
    {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(userId);

        int spinnerPosition = spinner.getSelectedItemPosition();
        String categoryId = categories.get(spinnerPosition).getCategoryId();

        if (categories.size() > 1)
        {
            databaseReference.child(categoryId).child("Tasks").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists())
                    {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        deleteList();
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.DialogTheme);
                        builder.setTitle("Delete this list?")
                                .setMessage("All tasks in this list will be deleted")
                                .setPositiveButton("Delete", dialogClickListener)
                                .setNegativeButton("Cancel", dialogClickListener).show();
                    }
                    else
                    {
                        deleteList();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else
        {
            Toast.makeText(this, "You cannot delete default list", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteList()
    {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(userId);

        int spinnerPosition = spinner.getSelectedItemPosition();
        String categoryId = categories.get(spinnerPosition).getCategoryId();

        SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS, 0);
        SharedPreferences.Editor sharedPrefsEditor = sharedPrefs.edit();

        sharedPrefsEditor.putInt(SPINNER_CHOICE, 0);
        sharedPrefsEditor.apply();

        categories.remove(spinnerPosition);

        databaseReference.child(categoryId).removeValue();

        loadData();
        Toast.makeText(this, "List deleted", Toast.LENGTH_SHORT).show();
    }

    private void onAddTaskClick()
    {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(userId);

        String text = taskEditText.getText().toString();

        if(spinner.getSelectedItem() != null)
        {

            if (text.trim().length() > 0)
            {
                String categoryId = categories.get(spinner.getSelectedItemPosition()).getCategoryId();
                String autoGeneratedId = databaseReference.child(categoryId).push().getKey();

                list.add(new Task(text, autoGeneratedId));

                recyclerViewAdapter.notifyDataSetChanged();

                taskEditText.getText().clear();

                databaseReference.child(categoryId).child("Tasks").child(autoGeneratedId).setValue(text);

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
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(userId);
            String categoryId = categories.get(spinner.getSelectedItemPosition()).getCategoryId();

            String taskId = list.get(viewHolder.getBindingAdapterPosition()).getTaskId();

            list.remove(viewHolder.getBindingAdapterPosition());

            databaseReference.child(categoryId).child("Tasks").child(taskId).removeValue();

            recyclerViewAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorRed))
                    .addSwipeLeftActionIcon(R.drawable.ic_delete_24dp)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
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
        String userId = mAuth.getCurrentUser().getUid();
        String categoryId = categories.get(spinner.getSelectedItemPosition()).getCategoryId();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(userId).child(categoryId).child("Tasks");

            databaseReference.addValueEventListener(new ValueEventListener() {
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