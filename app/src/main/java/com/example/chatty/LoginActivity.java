package com.example.chatty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

public class LoginActivity extends AppCompatActivity {
    EditText emailETLogin , passETLogin;
    Button LoginBtn , SignUpBtn ;
    FirebaseAuth auth;
    DatabaseReference myRef ;
    FirebaseUser firebaseUser;

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser != null){
            Intent i = new Intent(LoginActivity.this ,MainActivity.class);
            startActivity(i);
            finish();
        }



    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailETLogin = findViewById(R.id.email_login);
        passETLogin = findViewById(R.id.password_login);
        LoginBtn = findViewById(R.id.login);
        SignUpBtn = findViewById(R.id.sign_up_btn);

        auth = FirebaseAuth.getInstance();



        SignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this , RegisterActivity.class);
                startActivity(i);

            }
        });

        LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email_text = emailETLogin.getText().toString();
                String pass_text  = passETLogin.getText().toString();
                if(TextUtils.isEmpty(email_text)||TextUtils.isEmpty(pass_text)){
                    Toast.makeText(LoginActivity.this, "Please fill all the fields !", Toast.LENGTH_SHORT).show();
                }else{

                    auth.signInWithEmailAndPassword(email_text , pass_text)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        Intent i = new Intent(LoginActivity.this , MainActivity.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);
                                        finish();

                                    }else{
                                        Toast.makeText(LoginActivity.this, "Login Failed !" , Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }
}