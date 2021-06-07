package com.example.chatty.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatty.MessageActivity;
import com.example.chatty.Model.Chat;
import com.example.chatty.Model.Users;
import com.example.chatty.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.reflect.KVisibility;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private CircleImageView isntseen;
    private Context context;
    private List<Users> mUsers;
    private Boolean isChat;
    String theLastMessage;
    public UserAdapter(Context context, List<Users> mUsers, Boolean isChat) {
        this.mUsers = mUsers;
        this.context = context;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users users = mUsers.get(position);
        holder.username.setText(users.getUsername());

        if (users.getImageURL().equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(context).load(users.getImageURL()).into(holder.profile_image);

        }if(isChat){
            lastMessage(users.getId() ,holder.last_msg);

        }else
        {
            holder.last_msg.setVisibility(View.GONE);
        }


        if (isChat) {
            if (users.getStatus().equals("online")) {
                holder.statusImageON.setVisibility(View.VISIBLE);
                holder.statusImageOFF.setVisibility(View.GONE);
            } else {
                holder.statusImageON.setVisibility(View.GONE);
                holder.statusImageOFF.setVisibility(View.VISIBLE);
            }
        } else {
            holder.statusImageON.setVisibility(View.GONE);
            holder.statusImageOFF.setVisibility(View.GONE);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, MessageActivity.class);
                i.putExtra("userid", users.getId());
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public CircleImageView profile_image;
        public CircleImageView statusImageON;
        public CircleImageView statusImageOFF;
        private TextView last_msg;

        public ViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username_item);
            profile_image = itemView.findViewById(R.id.image);
            statusImageON = itemView.findViewById(R.id.img_on);
            statusImageOFF = itemView.findViewById(R.id.img_off);
            isntseen = itemView.findViewById(R.id.isnt_seen);
            last_msg = itemView.findViewById(R.id.last_msg);
        }
    }

    private void lastMessage(final String userid, final TextView last_msg) {
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                        theLastMessage = chat.getMessage();
                        if(!chat.isIsseen()){
                            isntseen.setVisibility(View.VISIBLE);
                            Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
                            last_msg.setTypeface(boldTypeface);
                        }
                    }

                }
                switch (theLastMessage) {
                    case "default":
                        last_msg.setText("No Message");
                        break;

                    default:
                        last_msg.setText(theLastMessage);
                        break;
                }
                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}


