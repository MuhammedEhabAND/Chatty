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
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    EditText userET , passEt , emailEt ;
    Button registerBtn ;
    FirebaseAuth auth;
    DatabaseReference myRef ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        userET = findViewById(R.id.username);
        passEt = findViewById(R.id.password);
        emailEt = findViewById(R.id.email);
        registerBtn = findViewById(R.id.sign_up_btn);


        auth = FirebaseAuth.getInstance();


        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username_text = userET.getText().toString();
                String email_text = emailEt.getText().toString();
                String password_text = passEt.getText().toString();
                if(TextUtils.isEmpty(username_text) ||TextUtils.isEmpty(email_text)||TextUtils.isEmpty(password_text)){
                    Toast.makeText(RegisterActivity.this, "Please fill all the fields !", Toast.LENGTH_SHORT).show();
                }else{
                    RegisterNow(username_text , email_text , password_text);
                }
            }
        });
    }
    private void RegisterNow(final String username , String email , String password) {
        auth.createUserWithEmailAndPassword(email ,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull  Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            String userid = firebaseUser.getUid();
                            myRef = FirebaseDatabase.getInstance().getReference("MyUsers")
                                    .child(userid);


                            HashMap<String , String> hashMap =  new HashMap<>();
                            hashMap.put("id" , userid);
                            hashMap.put("username" ,username );
                            hashMap.put("imageURL", "default");
                            hashMap.put("status" ,"offline");


                            myRef.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Intent i = new Intent(RegisterActivity.this , MainActivity.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);
                                        finish();
                                    }
                                }
                            });
                        }else {
                            Toast.makeText(RegisterActivity.this, "Invaild Email or Password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}