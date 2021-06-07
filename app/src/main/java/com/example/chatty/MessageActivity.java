package com.example.chatty;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatty.Adapter.MessageAdapter;
import com.example.chatty.Fragments.APIService;
import com.example.chatty.Model.Chat;
import com.example.chatty.Model.Users;
import com.example.chatty.Notification.Client;
import com.example.chatty.Notification.Data;
import com.example.chatty.Notification.MyResponse;
import com.example.chatty.Notification.Sender;
import com.example.chatty.Notification.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ktx.Firebase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {
    TextView username;
    CircleImageView imageView;
    FirebaseUser fuser;
    EditText  msg_et;
    ImageButton sendbtn;
    DatabaseReference reference;
    Intent intent;
    MessageAdapter messageAdapter;
    List<Chat> mChat;
    RecyclerView recyclerView ,recyclerViewy;
    String userid;
    ValueEventListener seenListener;
    APIService apiService;
    boolean notify =false;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        imageView = findViewById(R.id.image_profile);
        username = findViewById(R.id.username_message);
        msg_et = findViewById(R.id.text_send);
        sendbtn = findViewById(R.id.send_btn);
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        recyclerView = findViewById(R.id.rv);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        intent = getIntent();
        userid = intent.getStringExtra("userid");
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Users users=snapshot.getValue(Users.class);
                username.setText(users.getUsername());
                if(users.getImageURL().equals("default")){
                    imageView.setImageResource(R.mipmap.ic_launcher);

                }else{
                    Glide.with(MessageActivity.this ).load(users.getImageURL()).into(imageView);

                }
                readMessage(fuser.getUid() , userid , users.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        sendbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                notify=true;
                String msg = msg_et.getText().toString();
                if(!msg.equals("")){
                    sendMessage(fuser.getUid(),userid, msg);
                }else{

                }
                msg_et.setText("");
            }
        });


        SeenMessage(userid);





    }

    private void SeenMessage(String userid){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener =reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    Chat chat = snapshot1.getValue(Chat.class);
                    if(chat.getReceiver().equals(fuser.getUid())&& chat.getSender().equals(userid)){
                        HashMap<String , Object> hashMap = new HashMap<>();
                        hashMap.put("isseen" , true);
                        snapshot1.getRef().updateChildren(hashMap);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String sender,String receiver , String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String , Object>hashMap = new HashMap<>();
        hashMap.put("sender" , sender);
        hashMap.put("receiver" , receiver);
        hashMap.put("message" , message);
        hashMap.put("isseen" , false);



        reference.child("Chats").push().setValue(hashMap);


        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(fuser.getUid())
                .child(userid);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef.child("id").setValue(userid);

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


        final String msg = message;
        reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Users user = snapshot.getValue(Users.class);
                if(notify){
                sendNotification(receiver , user.getUsername() , msg);
                }
                notify= false;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private void sendNotification(String receiver , final String username ,final String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query =tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            final String userid = intent.getStringExtra("userid");
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    Token token =snapshot1.getValue(Token.class);
                    Data data = new Data(fuser.getUid() ,R.mipmap.ic_launcher, username+": "+message ,"New Message",
                            userid);
                    Sender sender = new Sender(data , token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code()==200){
                                        if(response.body().success!=1){
                                            Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }


    private void readMessage(String myid , String userid ,  String imageurl){
        mChat = new ArrayList<>();
        reference =  FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                mChat.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Chat chat =  dataSnapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(myid)&&chat.getSender().equals(userid)||
                            chat.getReceiver().equals(userid)&&chat.getSender().equals(myid)){
                        mChat.add(chat);

                    }
                    messageAdapter =  new MessageAdapter(MessageActivity.this , mChat , imageurl);
                    recyclerView.setAdapter(messageAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private  void  CheckStatus(String status){
        reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(fuser.getUid());
        HashMap<String ,Object> hashMap = new HashMap<>();
        hashMap.put("status" , status);
        reference.updateChildren(hashMap);

    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckStatus("online");

    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        CheckStatus("offline");
    }

}