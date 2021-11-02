package com.example.graduation_project;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.firebase.ui.auth.viewmodel.RequestCodes;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import io.grpc.Context;

public class MemberInitActivity extends AppCompatActivity {
    private static final String TAG = "MemberInitActivity";

    ImageView ImgUserPhoto;
    private String profilePath;
    static int PReqCode = 1;
    static int REQUSETCODE = 1;
    Uri pickedImgUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_init);
        findViewById(R.id.checkButton).setOnClickListener(onClickListener);

        ImgUserPhoto = findViewById(R.id.userPhoto);
        ImgUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 22) {
                    checkAndRequestForPermission();
                } else {
                    openGallery();
                }
            }
        });
    }
            private void openGallery() {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, REQUSETCODE);
            }

            private void checkAndRequestForPermission() {
                if (ContextCompat.checkSelfPermission(MemberInitActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != getPackageManager().PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MemberInitActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        Toast.makeText(MemberInitActivity.this, "요청을 수락해주시기 바랍니다", Toast.LENGTH_SHORT).show();
                    } else {
                        ActivityCompat.requestPermissions(MemberInitActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
                                , PReqCode);
                    }
                } else
                    openGallery();


            }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUSETCODE && data != null){


            pickedImgUri = data.getData();
            ImgUserPhoto.setImageURI(pickedImgUri);
            profilePath = data.getStringExtra("profilePath");
            Bitmap bmp = BitmapFactory.decodeFile(profilePath);
            ImgUserPhoto.setImageBitmap(bmp);

        }
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
       final String name = ((EditText)findViewById(R.id.nameEditText)).getText().toString();
        final  String phoneNumber = ((EditText)findViewById(R.id.phoneNumberEditText)).getText().toString();
        final String birthDay = ((EditText)findViewById(R.id.birthDayEditText)).getText().toString();
        final String address = ((EditText)findViewById(R.id.addressEditText)).getText().toString();

        if(name.length() > 0 && phoneNumber.length() > 9 && birthDay.length() > 5 && address.length() > 0){
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();
            final FirebaseUser user =FirebaseAuth.getInstance().getCurrentUser();
            final StorageReference ImagesRef = storageReference.child("users/" + user.getUid() + "/profileImage.jpg");



            try {
                InputStream stream = new FileInputStream(new File(profilePath));
                UploadTask uploadTask = ImagesRef.putStream(stream);
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return ImagesRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            MemberInfo memberInfo = new MemberInfo(name, phoneNumber, birthDay, address, downloadUri.toString());
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
                        } else {
                            Log.e("로그", "실패");
                        }
                    }
                });
            } catch (FileNotFoundException e) {
                Log.e("로그", "에러: " + e.toString());
            }

        }else {
            startToast("회원정보를 입력해주세요.");
        }
    }

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}