package com.example.graduation_project;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

public class CommunityActivity extends AppCompatActivity  {
    private static final String TAG = "CommunityActivity";
    private FirebaseUser user;
    Button CommunityFragment;
    private ArrayList<String> pathList = new ArrayList<>();
    private LinearLayout parent;
    private RelativeLayout buttonsBackgroundLayout;
    private ImageView selectedImageVIew;
    private EditText selectedEditText;
    private int pathCount, successCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        Button image = (Button)findViewById(R.id.image);

        image.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivity(intent);
            }
        });
        findViewById(R.id.check).setOnClickListener(onClickListener);

    }





        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.check:
                        profileUpdate();
                        break;
                }
            }
        };


        private void profileUpdate() {
            final String title = ((EditText) findViewById(R.id.titleEditText)).getText().toString();
            final String contents = ((EditText) findViewById(R.id.contentsEditText)).getText().toString();

            if (title.length() > 0 && contents.length() > 0) {
                user = FirebaseAuth.getInstance().getCurrentUser();
                CommunityInfo communityInfo = new CommunityInfo(title, contents, user.getUid());
                uploader(communityInfo);

            } else {
                startToast("회원정보를 입력해주세요.");
            }
        }

        private void uploader(CommunityInfo communityInfo){
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("posts").add(communityInfo)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                        }
                    });
        }

        private void startToast(String msg) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }

    }