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
    private FirebaseHelper firebaseHelper;

    public static final String KEY_NAME = "com.example.TestApplication.KEY";
    public static final String KEY_NAME_TWO = "com.example.TestApplication.KEY-TWO";
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SPINNER_CHOICE = "spinnerChoice";
    public static final String CATEGORY_ID_CHOICE = "categoryIdChoice";

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

        firebaseHelper = new FirebaseHelper();

        if (toolbar != null)
        {
            setSupportActionBar(toolbar);

            if (getSupportActionBar() != null)
            {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
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

        int id = item.getItemId();

        if (id == R.id.addList)
        {
            Intent addIntent = new Intent(this, AddListActivity.class);
            addIntent.putParcelableArrayListExtra(KEY_NAME, categories);
            startActivity(addIntent);
            return true;
        }
        else if (id == R.id.renameList)
        {
            Intent renameIntent = new Intent(this, RenameListActivity.class);
            renameIntent.putParcelableArrayListExtra(KEY_NAME, categories);
            renameIntent.putExtra(KEY_NAME_TWO, spinner.getSelectedItemPosition());
            startActivity(renameIntent);
            return true;
        }
        else if (id == R.id.deleteList)
        {
            deleteListDialog();
            return true;
        }
        else if (id == R.id.logOut)
        {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            return true;
        }
        else
        {
            return super.onOptionsItemSelected(item);
        }
    }

    void setSpinner()
    {
        DatabaseReference databaseReference = firebaseHelper.getDatabaseReference();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categories.clear();

                if (dataSnapshot.getValue() == null) {
                    String autoGeneratedId = databaseReference.push().getKey();

                    if (autoGeneratedId != null) {
                        categories.add(new Category(getString(R.string.default_list_name), autoGeneratedId));
                        databaseReference.child(autoGeneratedId).child(autoGeneratedId).setValue(getString(R.string.default_list_name));
                        spinnerAdapter.notifyDataSetChanged();
                    }
                }
                else
                {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.getKey() != null)
                        {
                            Object value = snapshot.child(snapshot.getKey()).getValue();
                            if (value != null)
                            {
                                    categories.add(new Category(value.toString(), snapshot.getKey()));
                                    spinnerAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                    loadData();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.w("Database error", databaseError.toException());
                Toast.makeText(MainActivity.this, "Operation failed. Please try again later.", Toast.LENGTH_LONG).show();
            }
        });
        spinnerAdapter = new ArrayAdapter<>(this, R.layout.custom_title, android.R.id.text1, categories);
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

    private final TextView.OnEditorActionListener editorActionListener = (v, actionId, event) -> {

        onAddTaskClick();

        return true;
    };

    private void deleteListDialog()
    {
        int spinnerPosition = spinner.getSelectedItemPosition();
        String categoryId = categories.get(spinnerPosition).getCategoryId();

        DatabaseReference databaseReference = firebaseHelper.getDatabaseReference()
                .child(categoryId).child(DatabaseNodes.TASKS);

        if (categories.size() > 1) {
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                            switch (which)
                            {
                                case DialogInterface.BUTTON_POSITIVE:
                                    deleteList();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.DialogTheme);
                        builder.setTitle(getString(R.string.dialog_delete_title))
                                .setMessage(getString(R.string.dialog_delete_message))
                                .setPositiveButton(getString(R.string.dialog_delete_positive_button), dialogClickListener)
                                .setNegativeButton(getString(R.string.dialog_delete_negative_button), dialogClickListener).show();
                    }
                    else
                    {
                        deleteList();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                    Log.w("Database error", databaseError.toException());
                    Toast.makeText(MainActivity.this, "Operation failed. Please try again later.", Toast.LENGTH_LONG).show();
                }
            });
        }
        else
        {
            Toast.makeText(this, this.getString(R.string.toast_cannot_delete_default_list), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteList()
    {
        int spinnerPosition = spinner.getSelectedItemPosition();
        String categoryId = categories.get(spinnerPosition).getCategoryId();

        DatabaseReference databaseReference = firebaseHelper.getDatabaseReference()
                .child(categoryId);

        SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS, 0);
        SharedPreferences.Editor sharedPrefsEditor = sharedPrefs.edit();

        sharedPrefsEditor.putInt(SPINNER_CHOICE, 0);
        sharedPrefsEditor.apply();

        categories.remove(spinnerPosition);

        databaseReference.removeValue();

        loadData();
    }

    private void onAddTaskClick()
    {
        DatabaseReference databaseReference = firebaseHelper.getDatabaseReference();

        String text = taskEditText.getText().toString();

        if (spinner.getSelectedItem() != null)
        {
            if (text.trim().length() > 0)
            {
                String categoryId = categories.get(spinner.getSelectedItemPosition()).getCategoryId();
                String autoGeneratedId = databaseReference.child(categoryId).push().getKey();

                if (autoGeneratedId != null)
                {
                    Task newTask = new Task.TaskBuilder()
                            .taskName(text)
                            .taskId(autoGeneratedId)
                            .taskInformation("")
                            .subTasksList(new ArrayList<>())
                            .isChecked(false)
                            .build();

                    list.add(newTask);

                    recyclerViewAdapter.notifyItemInserted(list.size() - 1);

                    taskEditText.getText().clear();

                    databaseReference.child(categoryId).child(DatabaseNodes.TASKS).child(autoGeneratedId).setValue(newTask);
                }
            }
        }
        else
        {
            Toast.makeText(this, this.getString(R.string.toast_no_list_chosen), Toast.LENGTH_SHORT).show();
        }
    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            String categoryId = categories.get(spinner.getSelectedItemPosition()).getCategoryId();
            String taskId = list.get(viewHolder.getBindingAdapterPosition()).getTaskId();

            DatabaseReference databaseReference = firebaseHelper.getDatabaseReference()
                    .child(categoryId).child(DatabaseNodes.TASKS).child(taskId);

            list.remove(viewHolder.getBindingAdapterPosition());

            databaseReference.removeValue();

            recyclerViewAdapter.notifyItemRemoved(viewHolder.getBindingAdapterPosition());
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
        String categoryId = categories.get(spinner.getSelectedItemPosition()).getCategoryId();
        Task task = list.get(position);
        Intent intent = new Intent(this, TaskItemActivity.class);
        intent.putExtra(KEY_NAME, task);
        intent.putExtra(KEY_NAME_TWO, categoryId);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        String categoryId = categories.get(spinner.getSelectedItemPosition()).getCategoryId();

        DatabaseReference databaseReference = firebaseHelper.getDatabaseReference()
                .child(categoryId).child(DatabaseNodes.TASKS);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();
                recyclerViewAdapter.notifyDataSetChanged();

                progressBar.setVisibility(View.VISIBLE);

                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Object value = snapshot.getValue();
                    Object nameValue = snapshot.child(DatabaseNodes.TASK_NAME).getValue();
                    Object informationValue = snapshot.child(DatabaseNodes.TASK_INFORMATION).getValue();
                    Object isCheckedValue = snapshot.child(DatabaseNodes.IS_CHECKED).getValue();

                    ArrayList<SubTask> subTasks = new ArrayList<>();

                    for (DataSnapshot subTask : snapshot.child(DatabaseNodes.SUB_TASKS_LIST).getChildren())
                    {
                        if (subTask.getValue() != null)
                        {
                            String subValue = subTask.getValue().toString();
                            String subKey = subTask.getKey();
                            SubTask newSubTask = new SubTask.SubTaskBuilder()
                                    .subTaskName(subValue)
                                    .subTaskId(subKey)
                                    .build();

                            subTasks.add(newSubTask);
                        }
                    }

                    if (value != null && nameValue != null && informationValue != null && isCheckedValue != null)
                    {
                        boolean isChecked = (boolean) isCheckedValue;

                        Task newTask = new Task.TaskBuilder()
                                .taskName(nameValue.toString())
                                .taskId(snapshot.getKey())
                                .taskInformation(informationValue.toString())
                                .subTasksList(subTasks)
                                .isChecked(isChecked)
                                .build();

                        list.add(newTask);
                        recyclerViewAdapter.notifyItemInserted(list.size() - 1);
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.w("Database error", databaseError.toException());
                Toast.makeText(MainActivity.this, "Operation failed. Please try again later.", Toast.LENGTH_LONG).show();
            }
        });
        SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS, 0);
        SharedPreferences.Editor sharedPrefsEditor = sharedPrefs.edit();
        sharedPrefsEditor.putString(CATEGORY_ID_CHOICE, categoryId);
        sharedPrefsEditor.apply();
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