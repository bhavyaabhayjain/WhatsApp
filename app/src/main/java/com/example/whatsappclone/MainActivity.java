package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TextView;

import com.example.whatsappclone.LoginSignup.LoginActivity;
import com.example.whatsappclone.LoginSignup.RegisterActivity;
import com.example.whatsappclone.helper.TabAccesorAdaptor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    ViewPager mainViewPager;
    TabLayout tabLayout;
    TabAccesorAdaptor tabAccesorAdaptor;
    FirebaseAuth auth;
    DatabaseReference Rootref;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth= FirebaseAuth.getInstance();
        FirebaseUser currentUser=auth.getCurrentUser();
        if (currentUser!= null) {
            setContentView(R.layout.activity_main);
            Rootref = FirebaseDatabase.getInstance().getReference();
            toolbar = findViewById(R.id.main_activity_toolbar);
            mainViewPager = findViewById(R.id.main_tab_viewpager);
            tabLayout = findViewById(R.id.main_tabs);
            tabAccesorAdaptor = new TabAccesorAdaptor(getSupportFragmentManager());
            mainViewPager.setAdapter(tabAccesorAdaptor);
            setSupportActionBar(toolbar);
            tabLayout.setupWithViewPager(mainViewPager);
            getSupportActionBar().setTitle("Chatting Application");

        }
        else {
            auth.signOut();
            SendUserToLoginActivity();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=auth.getCurrentUser();
        if (currentUser==null){
            auth.signOut();
            SendUserToLoginActivity();
        }
        else {
            progressDialog=new ProgressDialog(this);
            progressDialog.setTitle("Loading Chats");
            progressDialog.setMessage("Please wait while we are loading your chats");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            updateuserstatusstartactivity("Online");
            VerifyExistenceofuser();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUser currentUser=auth.getCurrentUser();
        if (currentUser!=null){
            updateuserstatusactivity("Offline");
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        FirebaseUser currentUser=auth.getCurrentUser();
        if (currentUser!=null){
            updateuserstatusactivity("Online");
        }
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent main=new Intent(Intent.ACTION_MAIN);
        main.addCategory(Intent.CATEGORY_DEFAULT);
        main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(main);
        FirebaseUser currentUser=auth.getCurrentUser();
        if (currentUser!=null){
            updateuserstatusactivity("Offline");
        }

    }

    private void updateuserstatusactivity(String status) {
        String currentUserId=auth.getCurrentUser().getUid();
        String currentTime,currentDate;
        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat dateFormat=new SimpleDateFormat("MMM dd,yyyy");
        currentDate=dateFormat.format(calendar.getTime());
        SimpleDateFormat timeFormat=new SimpleDateFormat("hhh:mmm a");
        currentTime=timeFormat.format(calendar.getTime());
        HashMap<String , Object> userstateMap = new HashMap<>();
        userstateMap.put("Time",currentTime);
        userstateMap.put("Date",currentDate);
        userstateMap.put("state",status);
        Rootref.child("Users").child(currentUserId).child("userState").updateChildren(userstateMap);
    }
    private void updateuserstatusstartactivity(String status) {
        String currentUserId=auth.getCurrentUser().getUid();
        String currentTime,currentDate;
        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat dateFormat=new SimpleDateFormat("MMM dd,yyyy");
        currentDate=dateFormat.format(calendar.getTime());
        SimpleDateFormat timeFormat=new SimpleDateFormat("hhh:mmm a");
        currentTime=timeFormat.format(calendar.getTime());
        HashMap<String , Object> userstateMap = new HashMap<>();
        userstateMap.put("Time",currentTime);
        userstateMap.put("Date",currentDate);
        userstateMap.put("state",status);
        Rootref.child("Users").child(currentUserId).child("userState").updateChildren(userstateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.cancel();
            }
        });
    }
    private void VerifyExistenceofuser() {
        String currentUserId=auth.getCurrentUser().getUid();
        Rootref.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.child("name")).exists()){

                }
                else {
                    SendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void SendUserToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }
    private void SendUserToSettingsActivity() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId()==R.id.logout){
            FirebaseUser currentUser=auth.getCurrentUser();
            if (currentUser!=null){
                updateuserstatusactivity("Offline");
            }
           auth.signOut();
           SendUserToLoginActivity();
        }
        if (item.getItemId()==R.id.main_settings){
            SendUserToSettingsActivity();
        }
        return true;
    }
}