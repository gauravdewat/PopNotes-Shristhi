package com.example.mon00b.popnotes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SignUp extends AppCompatActivity {

    private Button register;
    private EditText Username, Email, Password, Cnfmpassword;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        register = (Button)findViewById(R.id.register);
        Username = (EditText)findViewById(R.id.Username);
        Email = (EditText)findViewById(R.id.Email);
        Password = (EditText)findViewById(R.id.Password);
        Cnfmpassword = (EditText)findViewById(R.id.cnfmPassword);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register.setEnabled(false);
                SignUP();
            }
        });
    }

    void SignUP(){
        final String username = Username.getText().toString().trim();
        String email = Email.getText().toString().trim();
        String password = Password.getText().toString();
        String cnfmpassword = Cnfmpassword.getText().toString();

        if (username.isEmpty()){
            register.setEnabled(true);
            Username.setError("REQUIRED");
            return;
        }
        else if (email.isEmpty()){
            register.setEnabled(true);
            Email.setError("REQUIRED");
            return;
        }
        else if (password.isEmpty()){
            register.setEnabled(true);
            Password.setError("REQUIRED");
            return;
        }
        else if (cnfmpassword.isEmpty()){
            register.setEnabled(true);
            Cnfmpassword.setError("REQUIRED");
            return;
        }
        else if (password.length()<6){
            register.setEnabled(true);
            Password.setError("TOO SHORT");
        }
        else if (!Objects.equals(cnfmpassword, password)){
            register.setEnabled(true);
            Cnfmpassword.setError("DID NOT MATCH");
            return;
        }
        else{
            final ProgressDialog progressDialog = new ProgressDialog(SignUp.this);
            progressDialog.setMessage("Registering...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressDialog.dismiss();
                    if (task.isSuccessful()){
                        Intent intent = new Intent(SignUp.this,Main.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                        FirebaseUser user = mAuth.getCurrentUser();

                        databaseReference.child(user.getUid()).setValue(username);
                    }
                    else {
                        register.setEnabled(true);
                        Toast.makeText(SignUp.this,
                                "Registration Failed...",Toast.LENGTH_SHORT).show();

                    }
                }
            });

        }


    }
}
