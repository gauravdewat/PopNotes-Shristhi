package com.example.mon00b.popnotes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private Button login;
    private TextView signup;
    private EditText EmailLogin, PasswordLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
/*
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(this, Main.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }*/

        EmailLogin= (EditText)findViewById(R.id.EmailLogin);
        PasswordLogin = (EditText)findViewById(R.id.PasswordLogin);

        mAuth = FirebaseAuth.getInstance();

        login = (Button)findViewById(R.id.but_login);
        signup = (TextView) findViewById(R.id.signup);



        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login.setEnabled(false);
                LoginUser();
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this,SignUp.class));
            }
        });

    }
    void LoginUser(){
        String email = EmailLogin.getText().toString().trim();
        String password = PasswordLogin.getText().toString().trim();


        if (email.isEmpty()){
            EmailLogin.setError("REQUIRED");
            login.setEnabled(true);
            return;
        }
        else if (password.isEmpty()){
            PasswordLogin.setError("REQUIRED");
            login.setEnabled(true);
            return;
        }
        else {
            final ProgressDialog progressDialog = new ProgressDialog(Login.this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Signing In...");
            progressDialog.show();


            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    // progressDialog.dismiss();
                    if (task.isSuccessful()){
                        progressDialog.dismiss();
                        Intent intent = new Intent(Login.this, Main.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    else {
                        progressDialog.dismiss();
                        login.setEnabled(true);
                        Toast.makeText(Login.this,"Login Failed",Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }


    }
}