package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.helper.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriends extends AppCompatActivity {
    RecyclerView findfriendlist;
    Toolbar toolbar;
    DatabaseReference UserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        findfriendlist=findViewById(R.id.findff_recyclerview);
        toolbar=findViewById(R.id.find_friendstoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        findfriendlist.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(UserRef, Contacts.class).build();
        FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder> adapter =new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Contacts model) {
                holder.username.setText(model.getName());
                holder.userStatus.setText(model.getStatus());
                if (model.getImage()!=null){
                    GetImage(model.getImage(),holder.profileImage);
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String user=getRef(position).getKey();
                        Intent profileIntent=new Intent(FindFriends.this,ProfileActivity.class);
                        profileIntent.putExtra("visited_uid", user);
                        startActivity(profileIntent);
                    }
                });

            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.userdisplaylayout,parent,false);
                FindFriendsViewHolder viewHolder=new FindFriendsViewHolder(view);
                return viewHolder;
            }
        };

        findfriendlist.setAdapter(adapter);
        adapter.startListening();

    }
    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder
    {
        TextView username,userStatus;
        CircleImageView profileImage;
        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.users_profile_name);
            userStatus=itemView.findViewById(R.id.users_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            userStatus.setVisibility(View.VISIBLE);
        }
    }
    private void GetImage(String currentUser, CircleImageView imageView) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Profile Images/" + currentUser + ".jpg");
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(imageView);

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent=new Intent(FindFriends.this,MainActivity.class);
        startActivity(intent);

    }
}