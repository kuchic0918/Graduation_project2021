package com.example.graduation_project;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.google.firebase.auth.UserProfileChangeRequest;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
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
                startActivityForResult(intent,101);
            }
        });


        viewGroup.findViewById(R.id.logout).setOnClickListener(onClickListener);
        viewGroup.findViewById(R.id.info_edit).setOnClickListener(onClickListener);

        //set user name & profile photo
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getEmail();
            userName = viewGroup.findViewById(R.id.userId);
            userName.setText(name);
            Uri uri = user.getPhotoUrl();
           // imageProfile.setImageURI(uri);
        }
        //set user name






        return viewGroup;
    }

    //image
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101) {
           if(resultCode == RESULT_OK) {
                try {
                    InputStream inputStream = getActivity().getContentResolver().openInputStream(data.getData());
                    Bitmap image = BitmapFactory.decodeStream(inputStream);

                    imageProfile.setImageBitmap(image);

                    //firebase profile
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    //Context context = getContext();

                    //Uri u = getImageUri(context, image);

                    //uri-----------------------------------

                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG,100,bytes);
                    String path = MediaStore.Images.Media.insertImage(getActivity().getApplicationContext().getContentResolver(), image, "title", "");
                    Uri u = Uri.parse(path);

                    //uri------------------------------------

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(u)
                            .build();

                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getActivity().getApplicationContext(),"이미지 변경", Toast.LENGTH_LONG).show();}}});


                    //f        p
                } catch (Exception e) {
                   e.printStackTrace();
                    Toast.makeText(getActivity().getApplicationContext(), "이미지 로드 오류"+e, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    //image


    //bitmap -> url
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    //b     -> u


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