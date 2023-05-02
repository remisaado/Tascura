package com.task.tascura;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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
        implements RecyclerViewAdapter.OnItemListener, AdapterView.OnItemSelectedListener, NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    TextView deleteAccountButton;
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
    ValueEventListener valueEventListenerOne;
    ValueEventListener valueEventListenerTwo;

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

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);
        recyclerView = findViewById(R.id.recyclerView);
        taskEditText = findViewById(R.id.taskEditText);
        addTaskButton = findViewById(R.id.addTaskButton);
        spinner = findViewById(R.id.spinner);
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null)
        {
            // Sets the current users email address to the TextView in the navigation header
            View headerView = navigationView.getHeaderView(0);
            TextView userEmailAddress = headerView.findViewById(R.id.userEmailAddress);
            userEmailAddress.setText(mAuth.getCurrentUser().getEmail());
        }

        firebaseHelper = new FirebaseHelper();

        if (toolbar != null)
        {
            // Sets the toolbar as the action bar.
            setSupportActionBar(toolbar);

            if (getSupportActionBar() != null)
            {
                // Hides the title of the activity from being displayed in the toolbar.
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        initRecyclerView();

        deleteAccountButton.setOnClickListener(v -> onDeleteAccountClick());

        taskEditText.setOnEditorActionListener(editorActionListener);

        addTaskButton.setOnClickListener(v -> onAddTaskClick());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // This method is called when an item in the drawer menu is selected.
        // It checks the ID of the selected item and performs the appropriate action.

        int id = item.getItemId();

        if (id == R.id.addList)
        {
            Intent addIntent = new Intent(this, AddListActivity.class);
            addIntent.putParcelableArrayListExtra(KEY_NAME, categories);
            startActivity(addIntent);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        else if (id == R.id.renameList)
        {
            Intent renameIntent = new Intent(this, RenameListActivity.class);
            renameIntent.putParcelableArrayListExtra(KEY_NAME, categories);
            renameIntent.putExtra(KEY_NAME_TWO, spinner.getSelectedItemPosition());
            startActivity(renameIntent);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        else if (id == R.id.deleteList)
        {
            deleteListDialog();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        else if (id == R.id.logOut)
        {
            removeValueEventListeners();
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null)
        {
            // If no user is logged in, starts the LoginActivity.
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
        else
        {
            // If a user is logged in, calls setSpinner function.
            setSpinner();
        }
    }

    void setSpinner()
    {
        // This method sets and populates the spinner from the Firebase database.

        DatabaseReference databaseReference = firebaseHelper.getDatabaseReference();

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categories.clear();

                if (dataSnapshot.getValue() == null)
                {
                    // If there are no categories in the database,
                    // a default category/list is created

                    String autoGeneratedId = databaseReference.push().getKey();

                    if (autoGeneratedId != null)
                    {
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
                            // If there are categories in the database
                            // they are populated in the categories ArrayList.

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
        };
        databaseReference.addValueEventListener(valueEventListener);
        this.valueEventListenerOne = valueEventListener;

        // Sets up an ArrayAdapter to populate the Spinner with a
        // custom layout for the title and drop-down items
        // and sets the adapter to the spinner.
        spinnerAdapter = new ArrayAdapter<>(this, R.layout.custom_title, android.R.id.text1, categories);
        spinnerAdapter.setDropDownViewResource(R.layout.custom_spinner);
        spinner.setAdapter(spinnerAdapter);
        // OnItemSelectedListener is attached to spinner to handle user selections.
        spinner.setOnItemSelectedListener(this);
    }

    private void removeValueEventListeners()
    {
        // Removes ValueEventListeners, this method is called
        // before logging out to prevent error messages on log out.
        DatabaseReference databaseReferenceOne = firebaseHelper.getDatabaseReference();

        if (valueEventListenerOne != null)
        {
            databaseReferenceOne.removeEventListener(valueEventListenerOne);
            valueEventListenerOne = null;
        }

        String categoryId = categories.get(spinner.getSelectedItemPosition()).getCategoryId();
        DatabaseReference databaseReferenceTwo = firebaseHelper.getDatabaseReference()
                .child(categoryId).child(DatabaseNodes.TASKS);

        if (valueEventListenerTwo != null)
        {
            databaseReferenceTwo.removeEventListener(valueEventListenerTwo);
            valueEventListenerTwo = null;
        }
    }

    void loadData()
    {
        // Loads the latest spinner choice from shared preferences
        // and sets it to the selection of the spinner.
        SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS, 0);
        int spinnerChoice = sharedPrefs.getInt(SPINNER_CHOICE, 0);
        spinner.setSelection(spinnerChoice, true);
    }

    void setData()
    {
        // Sets/saves the current spinner choice to shared preferences.
        int spinnerChoice = spinner.getSelectedItemPosition();
        SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS, 0);
        SharedPreferences.Editor sharedPrefsEditor = sharedPrefs.edit();
        sharedPrefsEditor.putInt(SPINNER_CHOICE, spinnerChoice);
        sharedPrefsEditor.apply();
    }

    private void initRecyclerView()
    {
        // Initializes and sets up a RecyclerView with a
        // LinearLayoutManager and a RecyclerViewAdapter to display a list of items.
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerViewAdapter = new RecyclerViewAdapter(list, this);
        recyclerView.setAdapter(recyclerViewAdapter);
        // Attaches an ItemTouchHelper to the RecyclerView for swipe functionality.
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        // Adds a divider between each item in the list using a DividerItemDecoration.
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.divider_line, null));
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    private final TextView.OnEditorActionListener editorActionListener = (v, actionId, event) ->
    {
        // Calls onAddTaskClick method when the IME Button
        // is pressed in the taskEditText EditText.

        onAddTaskClick();

        return true;
    };

    private void deleteListDialog()
    {
        int spinnerPosition = spinner.getSelectedItemPosition();
        String categoryId = categories.get(spinnerPosition).getCategoryId();

        DatabaseReference databaseReference = firebaseHelper.getDatabaseReference()
                .child(categoryId).child(DatabaseNodes.TASKS);

        if (categories.size() > 1)
        {
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    if (snapshot.exists())
                    {
                        // If the chosen list contains tasks, a dialog will pop up
                        // cautioning the user that all tasks in the list will be deleted.
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
                                .setNegativeButton(getString(R.string.dialog_delete_negative_button), dialogClickListener)
                                .show();
                    }
                    else
                    {
                        // If the list does not contain any tasks, the list will be deleted.
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
            // If only one category exists, a toast will appear
            // saying that the default list cannot be deleted.
            Toast.makeText(this, this.getString(R.string.toast_cannot_delete_default_list), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteList()
    {
        // Deletes the chosen list from the categories
        // ArrayList and the Firebase database.

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

        if (spinnerPosition == 0)
        {
            // Calls populateLists() manually since onItemSelected is not called if the
            // selection of the spinner does not change but remains at the same index
            populateLists();
        }
    }

    private void onAddTaskClick()
    {
        // Adds a task to the list ArrayList and the Firebase database.

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

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT)
    {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target)
        {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction)
        {
            // Deletes a task item from the list ArrayList and the
            // Firebase database on left swipe in the RecyclerView.

            String categoryId = categories.get(spinner.getSelectedItemPosition()).getCategoryId();
            String taskId = list.get(viewHolder.getBindingAdapterPosition()).getTaskId();

            DatabaseReference databaseReference = firebaseHelper.getDatabaseReference()
                    .child(categoryId).child(DatabaseNodes.TASKS).child(taskId);

            list.remove(viewHolder.getBindingAdapterPosition());

            databaseReference.removeValue();
            recyclerViewAdapter.notifyItemRemoved(viewHolder.getBindingAdapterPosition());
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive)
        {
            // Draws a red background and a trash can icon to the left swipe.

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
        // Opens the TaskItemActivity of the specific
        // task that is pressed on in the RecyclerView.
        Task task = list.get(position);
        Intent intent = new Intent(this, TaskItemActivity.class);
        intent.putExtra(KEY_NAME, task);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        populateLists();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
    }

    void populateLists()
    {
        // This method is called when an item is selected in the spinner.
        String categoryId = categories.get(spinner.getSelectedItemPosition()).getCategoryId();

        DatabaseReference databaseReference = firebaseHelper.getDatabaseReference()
                .child(categoryId).child(DatabaseNodes.TASKS);

        ValueEventListener valueEventListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                // Clears list ArrayList that populates the RecyclerView,
                // and updates the list with the data of the spinner selection.
                list.clear();
                recyclerViewAdapter.notifyDataSetChanged();

                // Shows progress bar while retrieving data.
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
                        Object subValue = subTask.getValue();
                        Object subNameValue = subTask.child(DatabaseNodes.SUB_TASK_NAME).getValue();
                        Object subIsCheckedValue = subTask.child(DatabaseNodes.IS_CHECKED).getValue();

                        if (subValue != null && subNameValue != null && subIsCheckedValue != null)
                        {
                            boolean subIsChecked = (boolean) subIsCheckedValue;

                            SubTask newSubTask = new SubTask.SubTaskBuilder()
                                    .subTaskName(subNameValue.toString())
                                    .subTaskId(subTask.getKey())
                                    .isChecked(subIsChecked)
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
                // Removes progress bar
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.w("Database error", databaseError.toException());
                Toast.makeText(MainActivity.this, "Operation failed. Please try again later.", Toast.LENGTH_LONG).show();
            }
        };
        databaseReference.addValueEventListener(valueEventListener);
        this.valueEventListenerTwo = valueEventListener;
        // Saves the categoryId of the selected spinner item to shared preferences.
        SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS, 0);
        SharedPreferences.Editor sharedPrefsEditor = sharedPrefs.edit();
        sharedPrefsEditor.putString(CATEGORY_ID_CHOICE, categoryId);
        sharedPrefsEditor.apply();

        setData();
    }

    private void onDeleteAccountClick()
    {
        // Opens a dialog upon account deletion cautioning the user that account and all
        // data will permanently be deleted, requires account password for confirmation.
        EditText input = new EditText(this);
        input.setBackground(ContextCompat.getDrawable(this, R.drawable.edit_text_dialog));
        input.setTextColor(ContextCompat.getColor(this, R.color.colorText));
        input.setHintTextColor(ContextCompat.getColor(this, R.color.colorHintGray));
        input.setHint("Password");
        input.setSingleLine();
        input.setPadding(28, 28, 28, 28);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which)
            {
                case DialogInterface.BUTTON_POSITIVE:
                    deleteAccount(input.getText().toString());
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.DialogTheme);
        builder.setTitle(getString(R.string.dialog_delete_account_title))
                .setMessage(getString(R.string.dialog_delete_account_message))
                .setView(input)
                .setPositiveButton(getString(R.string.dialog_delete_positive_button), dialogClickListener)
                .setNegativeButton(getString(R.string.dialog_delete_negative_button), dialogClickListener)
                .show();
    }

    private void deleteAccount(String password)
    {
        // Deletes account data from Firebase Database
        // and account from Firebase Authentication.
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null)
        {
            String email = mAuth.getCurrentUser().getEmail();
            if (email != null)
            {
                AuthCredential credential = EmailAuthProvider.getCredential(email, password);

                user.reauthenticate(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(DatabaseNodes.USERS).child(user.getUid());
                        removeValueEventListeners();
                        userRef.removeValue();

                        user.delete().addOnCompleteListener(task1 -> {
                            Toast.makeText(MainActivity.this, R.string.toast_user_account_deleted, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        });
                    }
                    else
                    {
                        Toast.makeText(this, R.string.toast_password_is_invalid, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

}