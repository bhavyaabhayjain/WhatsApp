package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    String receiverUID ,senderUserId,Current_state;
    CircleImageView userProfImg;
    TextView userProfileName , userProStatus;
    FirebaseAuth auth;
    String image;
    Button DeclineReq,SendMsgReq;
    DatabaseReference RootRef,ChatReqRef,ContactRef,NotificationRef;
    StorageReference userProfileImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        receiverUID = getIntent().getStringExtra("visited_uid");
        Intialize();
        retriveUserInfo();
    }

    private void Intialize() {
        auth=FirebaseAuth.getInstance();
        senderUserId=auth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();
        userProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");
        ChatReqRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef= FirebaseDatabase.getInstance().getReference().child("Notifications");
        userProfileName=findViewById(R.id.User_name_profile_activity);
        userProStatus=findViewById(R.id.User_status_profile_activity);
        SendMsgReq=findViewById(R.id.send_message_btn);
        DeclineReq=findViewById(R.id.Cancel_request__btn);
        userProfImg=findViewById(R.id.profile_image);
        Current_state="new";


    }

    public void retriveUserInfo(){
        RootRef.child("Users").child(receiverUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    if (snapshot.hasChild("name")&& snapshot.hasChild("status")&& !snapshot.hasChild("image"))
                    {

                        String uname=snapshot.child("name").getValue().toString();
                        String ustatus=snapshot.child("status").getValue().toString();
                        userProfileName.setText(uname);
                        userProStatus.setText(ustatus);
                        ManageChatRequest();
                    }
                    else if (snapshot.hasChild("name")&& snapshot.hasChild("status")&& snapshot.hasChild("image"))
                    {

                        image=snapshot.child("image").getValue().toString();
                        String uname=snapshot.child("name").getValue().toString();
                        String ustatus=snapshot.child("status").getValue().toString();
                        GetImage(receiverUID,userProfImg);
                        userProfileName.setText(uname);
                        userProStatus.setText(ustatus);
                        ManageChatRequest();

                    }
                    else
                    {
                        ManageChatRequest();
                        Toast.makeText(getApplicationContext(), "Please Update your Profile", Toast.LENGTH_SHORT).show();
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void ManageChatRequest() {
        ChatReqRef.child(senderUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(receiverUID)) {
                            String request_type = snapshot.child(receiverUID).child("request_type")
                                    .getValue().toString();
                            if (request_type.equals("sent")) {
                                Current_state = "request_sent";
                                SendMsgReq.setText("Cancel Chat Request");
                            } else if (request_type.equals("received")) {
                                Current_state = "request_received";
                                SendMsgReq.setText("Accept Chat Request");
                                DeclineReq.setVisibility(View.VISIBLE);
                                DeclineReq.setEnabled(true);
                                DeclineReq.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CanelRequestSent();
                                    }
                                });
                            }
                        }
                        else {
                            ContactRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.hasChild(receiverUID)) {
                                                Current_state = "friends";
                                                SendMsgReq.setText("Remove this Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        if (!senderUserId.equals(receiverUID))
        {
            SendMsgReq.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SendMsgReq.setEnabled(false);
                    if (Current_state.equals("new"))
                    {
                        SendChatReq();
                    }
                    else if (Current_state.equals("request_sent"))
                    {
                        CanelRequestSent();
                    }
                    else if (Current_state.equals("request_received"))
                    {
                        AcceptChatRequest();
                    }
                    else  if (Current_state.equals("friends"))
                    {
                        RemoveSpecificConatact();
                    }
                }
            });
        }
        else
        {
            SendMsgReq.setVisibility(View.INVISIBLE);
        }


    }

    private void RemoveSpecificConatact() {
        ContactRef.child(senderUserId).child(receiverUID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            SendMsgReq.setEnabled(true);
                            Current_state="new";
                            SendMsgReq.setText("Send Message");
                            DeclineReq.setVisibility(View.INVISIBLE);
                            DeclineReq.setEnabled(false);
                        }
                    }
                });
    }

    private void AcceptChatRequest() {
        ContactRef.child(senderUserId).child(receiverUID)
                .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                { ContactRef.child(receiverUID).child(senderUserId)
                        .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    ChatReqRef.child(senderUserId).child(receiverUID)
                                            .removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        ChatReqRef.child(receiverUID).child(senderUserId)
                                                                .removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            SendMsgReq.setEnabled(true);
                                                                            Current_state = "friends";
                                                                            SendMsgReq.setText("Remove this Contact");
                                                                            DeclineReq.setVisibility(View.INVISIBLE);
                                                                            DeclineReq.setEnabled(false);
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                }
                                            });
                                }
                            }
                        });
                }


            }
        });
    }

    private void SendChatReq() {
        ChatReqRef.child(senderUserId).child(receiverUID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ChatReqRef.child(receiverUID).child(senderUserId)
                                .child("request_type").setValue("received")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                           @Override
                                                           public void onComplete(@NonNull Task<Void> task) {

                                                               if (task.isSuccessful()) {
                                                                   HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                                   chatNotificationMap.put("from", senderUserId);
                                                                   chatNotificationMap.put("type", "request");

                                                                   NotificationRef.child(receiverUID).push()
                                                                           .setValue(chatNotificationMap)
                                                                           .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                               @Override
                                                                               public void onComplete(@NonNull Task<Void> task) {
                                                                                   if (task.isSuccessful()) {
                                                                                       SendMsgReq.setEnabled(true);
                                                                                       Current_state = "request_sent";
                                                                                       SendMsgReq.setText("Cancel Chat Request");
                                                                                   }
                                                                               }
                                                                           });
                                                               }

                                                           }
                                                       }
                                );
                    }}
                );

    }

    private void CanelRequestSent() {
        ChatReqRef.child(senderUserId).child(receiverUID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ChatReqRef.child(receiverUID).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                SendMsgReq.setEnabled(true);
                                                SendMsgReq.setText("Send Message");
                                                Current_state = "new";
                                                DeclineReq.setVisibility(View.INVISIBLE);
                                                DeclineReq.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void GetImage(String currentUser, CircleImageView imageView) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().
                child("Profile Images/" + currentUser + ".jpg");
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(imageView);
            }
        });
    }

}