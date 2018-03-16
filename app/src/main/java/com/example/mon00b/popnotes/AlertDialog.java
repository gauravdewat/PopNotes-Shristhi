package com.example.mon00b.popnotes;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by mon00b on 16/3/18.
 */

public class AlertDialog extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    private Button Save, Cancel;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Save = findViewById(R.id.save);
        Cancel = findViewById(R.id.cancel);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Notes");

        Dialog dialog = new Dialog(AlertDialog.this,R.style.Theme_Design_Light_NoActionBar);
        dialog.setContentView(R.layout.upload);
        dialog.getWindow().setLayout(600,400);
        dialog.show();

        final TextView key = dialog.findViewById(R.id.key);
        key.setText("string from previous activity");

        final EditText Note = dialog.findViewById(R.id.note);

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
                    databaseReference.child(NotesKey).setValue(model);
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AlertDialog.this,Main.class));
    }
}
