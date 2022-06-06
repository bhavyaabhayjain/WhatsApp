package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.LoginSignup.RegisterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    EditText username, userstatus;
    Button update;
    CircleImageView profileimage;
    FirebaseAuth auth;
    String image;
    DatabaseReference RootRef;
    public static final int Gallery_code=1;
    StorageReference UserProfileImg,storageReference;
    String currentUser;
    ProgressDialog progressDialog;
    String timeuploaded,valid;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Initialize();

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSettings();
                if (!TextUtils.isEmpty(username.getText().toString()) && !TextUtils.isEmpty(userstatus.getText().toString())){
                    SendUserToMainActivity();

                }

                
            }
        });
        profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(username.getText().toString()) && !TextUtils.isEmpty(userstatus.getText().toString())){
                    UpdateSettings();
                    Intent intent=new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent,"Select Image From Here"),Gallery_code);
                }
                else{
                    username.setError("Please enter Username");
                    userstatus.setError("Please Enter User Status");
                }

            }
        });
        RetriveData();
    }

    private void RetriveData() {
        RootRef.child("Users").child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    if (snapshot.hasChild("name")&& snapshot.hasChild("status")&& !snapshot.hasChild("image")){
                        username.setVisibility(View.GONE);
                        String uname=snapshot.child("name").getValue().toString();
                        String ustatus=snapshot.child("status").getValue().toString();
                        username.setText(uname);
                        userstatus.setText(ustatus);

                        if (snapshot.hasChild("timeuploaded") && snapshot.hasChild("valid")){
                            timeuploaded=snapshot.child("timeuploaded").getValue().toString();
                                    valid=snapshot.child("valid").getValue().toString();
                        }

                    }
                    else if (snapshot.hasChild("name")&& snapshot.hasChild("status")&& snapshot.hasChild("image")){
                        username.setVisibility(View.GONE);
                        image=snapshot.child("image").getValue().toString();
                        String uname=snapshot.child("name").getValue().toString();
                        String ustatus=snapshot.child("status").getValue().toString();
                        GetImage();
                        username.setText(uname);
                        userstatus.setText(ustatus);

                        if (snapshot.hasChild("timeuploaded") && snapshot.hasChild("valid")){
                            timeuploaded=snapshot.child("timeuploaded").getValue().toString();
                            valid=snapshot.child("valid").getValue().toString();
                        }

                    }
                    else {
                        username.setVisibility(View.VISIBLE);
                        Toast.makeText(getApplicationContext(), "Please update your profile", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    username.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Please update your profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void UpdateSettings() {
        String name=username.getText().toString();
        String status=userstatus.getText().toString();
        if (TextUtils.isEmpty(name)){
            username.setError("Please Enter user name");
        }
        if (TextUtils.isEmpty(status)){
            userstatus.setError("Please Enter user Status");
        }
        if (TextUtils.isEmpty(name) && TextUtils.isEmpty(status)){
            username.setError("Please Enter user name");
            userstatus.setError("Please Enter user status");
        }
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(status)){
            HashMap<String , String> profileMap = new HashMap<>();
            profileMap.put("uid",currentUser);
            profileMap.put("name",name);
            profileMap.put("status",status);
            if (image!=null){
                profileMap.put("image",image);
            }
            if (!TextUtils.isEmpty(timeuploaded) && !TextUtils.isEmpty(valid)){
                profileMap.put("timeuploaded",timeuploaded);
                profileMap.put("valid",valid);

            }
            RootRef.child("Users").child(currentUser).setValue(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(getApplicationContext(), "Data Updated", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String message=task.getException().getLocalizedMessage();
                        Toast.makeText(getApplicationContext(), "Error : "+ message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    private void SendUserToMainActivity() {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void Initialize() {
    username=findViewById(R.id.username_text_edit);
    userstatus=findViewById(R.id.status_edit_text);
    update=findViewById(R.id.update_settings_btn);
    profileimage=findViewById(R.id.profile_image);
    toolbar=findViewById(R.id.settings_app_bar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle("Account Settings");
    progressDialog=new ProgressDialog(this);
    auth=FirebaseAuth.getInstance();
    currentUser=auth.getCurrentUser().getUid();
    RootRef= FirebaseDatabase.getInstance().getReference();
    UserProfileImg= FirebaseStorage.getInstance().getReference().child("Profile Images");
    storageReference=FirebaseStorage.getInstance().getReference().child("Profile Images/"+currentUser+".jpg");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Gallery_code && resultCode==RESULT_OK && data!=null){
            CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this);
        }
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            if (resultCode==RESULT_OK){
                progressDialog.setTitle("Set Profile Image");
                progressDialog.setMessage("Please Wait While we are updating Your Account.....");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                Uri resulturi=result.getUri();
                StorageReference filePath=UserProfileImg.child(currentUser+".jpg");
                filePath.putFile(resulturi).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.cancel();
                        GetImage();
                        RootRef.child("Users").child(currentUser).child("image").setValue(currentUser);
                        Toast.makeText(getApplicationContext(), "Profile Image Uploaded", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    }

    private void GetImage() {
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(profileimage);
            }
        });
    }
}