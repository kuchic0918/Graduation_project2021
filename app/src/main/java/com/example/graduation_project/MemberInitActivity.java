package com.example.graduation_project;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.ByteBuffer;


public class MemberInitActivity<mAuth> extends AppCompatActivity {
    private static final String TAG = "MemberInitActivity";
    private EditText edName, edPhoneNumber, edBirthDay, edAddress;
    private String name, phonenum, birthday, address, email;
    ImageView ImgUserPhoto;
    private String profilePath;
    private FirebaseAuth mAuth;
    static int PReqCode = 1;
    static int REQUSETCODE = 1;
    Uri pickedImgUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_init);
        findViewById(R.id.checkButton).setOnClickListener(onClickListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.checkButton:
                    profileUpdate();
                    break;
            }
        }
    };

    private void profileUpdate() {
        String name = ((EditText)findViewById(R.id.nameEditText)).getText().toString();
        String phoneNumber = ((EditText)findViewById(R.id.phoneNumberEditText)).getText().toString();
        String birthDay = ((EditText)findViewById(R.id.birthDayEditText)).getText().toString();
        String address = ((EditText)findViewById(R.id.addressEditText)).getText().toString();

        if(name.length() > 0 && phoneNumber.length() > 9 && birthDay.length() > 5 && address.length() > 0){
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            MemberInfo memberInfo = new MemberInfo(name, phoneNumber, birthDay, address);
            if(user != null){

                db.collection("users").document(user.getUid()).set(memberInfo)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                startToast("회원정보 등록을 성공하였습니다.");
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                startToast("회원정보 등록에 실패하였습니다.");
                                Log.w(TAG, "Error writing document", e);
                            }
                        });
            }
        }else {
            startToast("회원정보를 입력해주세요.");
        }
    }
    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
            }

