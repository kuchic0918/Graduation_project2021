package com.example.graduation_project;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class InfoFragment extends Fragment{
    ViewGroup viewGroup;
    ImageView imageProfile;
    TextView userName;

    private static final String TAG = "MainActivity"; //logout

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_info,container,false);

        imageProfile = viewGroup.findViewById(R.id.imageProfile);

        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                getActivity().startActivityForResult(intent,101);
            }
        });


        viewGroup.findViewById(R.id.logout).setOnClickListener(onClickListener);
        viewGroup.findViewById(R.id.info_edit).setOnClickListener(onClickListener);

        //set user name
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getEmail();
            userName = viewGroup.findViewById(R.id.userId);
            userName.setText(name);

        }
        //set user name



        return viewGroup;
    }

    //image
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == getActivity().RESULT_OK) {
            if(resultCode == 101) {
                ContentResolver contentResolver = getActivity().getContentResolver();
                try {
                    InputStream inputStream = contentResolver.openInputStream(data.getData());
                    Bitmap image = BitmapFactory.decodeStream(inputStream);
                    imageProfile.setImageBitmap(image);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity().getApplicationContext(), "이미지 로드 오류", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    //image


    //logout
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.logout:
                    FirebaseAuth.getInstance().signOut();
                    myStartActivity(SignUpActivity.class);
                    break;

                case R.id.info_edit:
                    myStartActivity(MemberInitActivity.class);
                    break;
            }
        }
    };

    private void myStartActivity(Class c) {
        Intent intent = new Intent(getActivity(), c);

        startActivity(intent);
    }

    //logout
    
}