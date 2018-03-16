package com.example.mon00b.popnotes;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Main extends AppCompatActivity {

    private Button searchBtn, addBtn;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchBtn = findViewById(R.id.srchBtn);
        addBtn = findViewById(R.id.addBtn);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Notes");


        final Dialog dialog = new Dialog(Main.this,R.style.Theme_Design_Light_NoActionBar);
        dialog.setContentView(R.layout.upload);
        dialog.getWindow().setLayout(900,1300);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();

            }

        });


        final TextView key = dialog.findViewById(R.id.key);
        key.setText("string from previous activity");
        final EditText Note = dialog.findViewById(R.id.note);


        final Button Cancel = dialog.findViewById(R.id.cancel);
        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Note.setText("");
                dialog.dismiss();

            }
        });

        final Button Save = dialog.findViewById(R.id.save);
        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Save.setEnabled(false);
                final String userid = mAuth.getCurrentUser().getUid();
                final String NotesKey = databaseReference.push().getKey();
                final String note = Note.getText().toString().trim();
                final String Key = key.getText().toString().trim();

                if (true){
                    Model model = new Model();
                    model.setNotes(note);
                    model.setKey(Key);
                    model.setUserid(userid);
                    databaseReference.child(NotesKey).setValue(model);
                    dialog.dismiss();
                }

            }
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
        if (item.getItemId()==R.id.logout){
            mAuth.signOut();
            finish();
            Intent logoutIntent = new Intent(this, Login.class);
            startActivity(logoutIntent);
        }
        return true;
    }
}
