package com.example.graduation_project;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class InfoFragment extends Fragment{
    ViewGroup viewGroup;
    ImageView imageProfile;

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

        return viewGroup;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101) {
            if(resultCode == getActivity().RESULT_OK) {
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
    
}