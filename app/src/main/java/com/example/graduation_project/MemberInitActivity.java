package com.example.graduation_project;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MemberInitActivity extends AppCompatActivity {
    private static final String TAG = "MemberInitActivity";
    private EditText edName, edPhoneNumber, edBirthDay, edAddress;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide(); //액션바 숨기기

        setContentView(R.layout.activity_member_init);

        findViewById(R.id.checkButton).setOnClickListener(onClickListener);

        //데이터 가져와보기



        edName = findViewById(R.id.nameEditText);
        edPhoneNumber = findViewById(R.id.phoneNumberEditText);
        edBirthDay = findViewById(R.id.birthDayEditText);
        edAddress = findViewById(R.id.addressEditText);


        //     데이터
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
        final String phoneNumber = ((EditText)findViewById(R.id.phoneNumberEditText)).getText().toString();
        final String birthDay = ((EditText)findViewById(R.id.birthDayEditText)).getText().toString();
        final String address = ((EditText)findViewById(R.id.addressEditText)).getText().toString();

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