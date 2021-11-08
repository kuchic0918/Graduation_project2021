package com.example.graduation_project;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class CommunityActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    ActionBar actionBar;
    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;
    //카메라 접근
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;


    //허락
    String[] cameraPermissions;
    String[] storagePermissions;

    EditText Ttext, Ctext;
    ImageView Aimage;
    Button Btnupload;

    //유저 정보
    String name,email,uid,dp;

    //포스트 정보 수정
    String editTitle, editContent,editImage;

    //img uri
    Uri image_uri = null;

    //진행 바
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        actionBar = getSupportActionBar();
        actionBar.setTitle("글쓰기");
        //액션바에서 뒤로가기
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);



        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        pd = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        mAuth = FirebaseAuth.getInstance();

        checkUserStatus();

        Ttext = findViewById(R.id.titletext);
        Ctext = findViewById(R.id.contenttext);
        Aimage = findViewById(R.id.addimage);
        Btnupload = findViewById(R.id.uploadBtn);


        //인텐트로 데이터 받아옴
        Intent intent = getIntent();
        String isUpdateKey = "" + intent.getStringExtra("key");
        String editPostId = "" + intent.getStringExtra("editPostId");
        if (isUpdateKey.equals("editPost")) {
            //update
            actionBar.setTitle("게시글 수정");
            Btnupload.setText("수정");
            loadPostData(editPostId);
        }
        else {
            //add
            actionBar.setTitle("게시글 작성");
            Btnupload.setText("업로드");
        }

        actionBar.setSubtitle(email);


        //유저정보 포스트에 포함
        userDbRef =FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               for (DataSnapshot ds : dataSnapshot.getChildren()) {
                   name = "" +ds.child("name").getValue();
                   email = "" +ds.child("email").getValue();
                   dp = "" + ds.child("image").getValue();



               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




        //이미지 경로 갤러리,카메라 중 선택

        Aimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagepickDialog();
            }
        });

        //버튼 온클릭속성
        Btnupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = Ttext.getText().toString().trim();
                String content = Ctext.getText().toString().trim();
                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(CommunityActivity.this, "제목을 입력해주세요..", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(CommunityActivity.this, "내용을 입력해주세요..", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(isUpdateKey.equals("editPost")) {
                    beginUpdate(title,content,editPostId);

                }
                else {
                    uploadData(title,content);
                }

            }
        });
    }

    private void beginUpdate(String title, String content, String editPostId) {
        pd.setMessage("게시글 수정중 ..");
        pd.show();

        if(!editImage.equals("noImage")) {
            //이미지와 수정할때
            updateWasWithImage(title,content,editPostId);
        }
        else if (Aimage.getDrawable() != null){
            updateWithNowImage(title,content,editPostId);
            //이미지 있이 수정할 때
        }
        else {
            //이미지 없이
            updateWithoutImage(title,content,editPostId);
        }
    }

    private void updateWithoutImage(String title, String content, String editPostId) {

        HashMap<String, Object> hashMap = new HashMap<>();
        //게시글 정보
        hashMap.put("uid",uid);
        hashMap.put("uName",name);
        hashMap.put("uEmail",email);
        hashMap.put("uDp",dp);
        hashMap.put("pTitle",title);
        hashMap.put("pContent",content);
        hashMap.put("pUri","noImage");
        hashMap.put("pLikes","0");
        hashMap.put("pComments","0");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts") ;
        ref.child(editPostId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        pd.dismiss();
                        Toast.makeText(CommunityActivity.this, "수정 중...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(CommunityActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void updateWithNowImage(final String title, final String content, final String editPostId) {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" + "post_" + timeStamp;

        //이미지뷰에서 이미지 가져옴
        Bitmap bitmap = ((BitmapDrawable)Aimage.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //이미지 컴프레스
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] data = baos.toByteArray();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //이미지 업로드 이 url을 받아서
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()) {
                            //url을 받아서 파이어베이스 데이터베이스에 수정해줌

                            HashMap<String, Object> hashMap = new HashMap<>();
                            //게시글 정보
                            hashMap.put("uid",uid);
                            hashMap.put("uName",name);
                            hashMap.put("uEmail",email);
                            hashMap.put("uDp",dp);
                            hashMap.put("pTitle",title);
                            hashMap.put("pContent",content);
                            hashMap.put("pUri",downloadUri);
                            hashMap.put("pLikes","0");

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts") ;
                            ref.child(editPostId)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            pd.dismiss();
                                            Toast.makeText(CommunityActivity.this, "수정 중...", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(CommunityActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(CommunityActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });



    }

    private void updateWasWithImage(String title, String content, String editPostId) {
        //게시글에 이미지가 있을 때 , 이미지 지울 경우
        StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        mPictureRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //이미 삭제 후 새로운 이미지 업로드
                        String timeStamp = String.valueOf(System.currentTimeMillis());
                        String filePathAndName = "Posts/" + "post_" + timeStamp;

                        //이미지뷰에서 이미지 가져옴
                        Bitmap bitmap = ((BitmapDrawable)Aimage.getDrawable()).getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        //이미지 컴프레스
                        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
                        byte[] data = baos.toByteArray();

                        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                        ref.putBytes(data)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                   //이미지 업로드 이 url을 받아서
                                   Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                   while (!uriTask.isSuccessful());

                                   String downloadUri = uriTask.getResult().toString();
                                   if (uriTask.isSuccessful()) {
                                       //url을 받아서 파이어베이스 데이터베이스에 수정해줌

                                       HashMap<String, Object> hashMap = new HashMap<>();
                                       //게시글 정보
                                       hashMap.put("uid",uid);
                                       hashMap.put("uName",name);
                                       hashMap.put("uEmail",email);
                                       hashMap.put("uDp",dp);
                                       hashMap.put("pTitle",title);
                                       hashMap.put("pContent",content);
                                       hashMap.put("pUri",downloadUri);
                                       hashMap.put("pLikes","0");

                                       DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts") ;
                                       ref.child(editPostId)
                                               .updateChildren(hashMap)
                                               .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                   @Override
                                                   public void onSuccess(Void unused) {
                                                        pd.dismiss();
                                                       Toast.makeText(CommunityActivity.this, "수정 중...", Toast.LENGTH_SHORT).show();
                                                   }
                                               })
                                               .addOnFailureListener(new OnFailureListener() {
                                                   @Override
                                                   public void onFailure(@NonNull Exception e) {
                                                       pd.dismiss();
                                                       Toast.makeText(CommunityActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                                   }
                                               });
                                   }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(CommunityActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });




                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(CommunityActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void loadPostData(String editPostId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        //포스트의 세부적인 아이디를 받아옴
        Query fquery = reference.orderByChild("pId").equalTo(editPostId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for (DataSnapshot ds:datasnapshot.getChildren()) {
                    //데이터 받아오는 함수
                    editTitle = ""+ ds.child("pTitle").getValue();
                    editContent="" + ds.child("pContent").getValue();
                    editImage=""+ds.child("pUri").getValue();

                    Ttext.setText(editTitle);
                    Ctext.setText(editContent);



                    //이미지 세팅
                    if (!editImage.equals("noImage")) {
                        try {
                            Picasso.get().load(editImage).into(Aimage);
                        }
                        catch (Exception e) {

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void uploadData(String title, String content) {
        pd.setMessage("업로드 중..");
        pd.show();

        //글 이름, 글-id , 글 시간
        String timeStamp = String.valueOf(System.currentTimeMillis());

        String filePathAndName = "posts/" + "post_" + timeStamp;

        if (Aimage.getDrawable() != null) {
            //이미지뷰에서 이미지 가져옴
            Bitmap bitmap = ((BitmapDrawable)Aimage.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //이미지 컴프레스
            bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
            byte[] data = baos.toByteArray();


            //게시글에 이미지 올릴 때
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //이미지 파베스토리지에 올림
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());

                            String downloadUri = uriTask.getResult().toString();

                            if(uriTask.isSuccessful()){


                                HashMap<Object , String> hashMap = new HashMap<>() ;

                                //게시글 정보 넣기
                                hashMap.put("uid",uid);
                                hashMap.put("uName",name);
                                hashMap.put("uEmail",email);
                                hashMap.put("uDp",dp);
                                hashMap.put("pId",timeStamp);
                                hashMap.put("pTitle",title);
                                hashMap.put("pContent",content);
                                hashMap.put("pUri",downloadUri);
                                hashMap.put("pTime",timeStamp);
                                hashMap.put("pLikes","0");

                                //post data 의 store 경로
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                //이 ref 를 데이터에 저장

                                ref.child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //db에 추가하 때
                                                pd.dismiss();
                                                Toast.makeText(CommunityActivity.this, "게시글 저장", Toast.LENGTH_SHORT).show();
                                                //reset view
                                                Ttext.setText("");
                                                Ctext.setText("");
                                                Aimage.setImageURI(null);
                                                image_uri = null;


                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //post 가 db에 추가되는게 실패했을 때
                                                pd.dismiss();
                                                Toast.makeText(CommunityActivity.this,"" + e.getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                        });




                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        //이미지 업로드 실패
                            pd.dismiss();
                            Toast.makeText(CommunityActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else{
            //이미지 없이 올릴 때

            HashMap<Object , String> hashMap = new HashMap<>() ;

            //게시글 정보 넣기
            hashMap.put("uid",uid);
            hashMap.put("uName",name);
            hashMap.put("uEmail",email);
            hashMap.put("uDp",dp);
            hashMap.put("pId",timeStamp);
            hashMap.put("pTItle",title);
            hashMap.put("pContent",content);
            hashMap.put("pUri","noImage");
            hashMap.put("pTime",timeStamp);
            hashMap.put("pLikes","0");
            hashMap.put("pComments","0");

            //post data 의 store 경로
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            //이 ref 를 데이터에 저장

            ref.child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //db에 추가하 때
                            pd.dismiss();
                            Toast.makeText(CommunityActivity.this, "게시글 저장", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //post 가 db에 추가되는게 실패했을 때
                            pd.dismiss();
                            Toast.makeText(CommunityActivity.this,"" + e.getMessage(),Toast.LENGTH_SHORT).show();
                            Ttext.setText("");
                            Ctext.setText("");
                            Aimage.setImageURI(null);
                            image_uri = null;
                        }
                    });
        }
    }

    private void showImagepickDialog() {
        String[] options = {"카메라", "갤러리"};
        //다이아로그
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("이미지 경로");
        //dailog 띄움
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //선택지
                if (i == 0) {
                    //카메라
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }
                }
                if (i == 1) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                    //갤러리
                }
            }


        });
        builder.create().show();
        //다이어로그 만들고 보여줌
    }

    private void pickFromCamera() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp descr");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);


        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent , IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {


        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent , IMAGE_PICK_GALLERY_CODE);
    }



    private boolean checkStoragePermission() {
        //스토리지에 접근 O/X
        //트루는 가능
        //false 면 불가능
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;

    }

    private void requestStoragePermission() {
        //스토리지 접근 요청
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        //카메라접근 O/X
        //트루는 가능
        //false 면 불가능
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;

    }

    private void requestCameraPermission() {
        //카메라 접근 요청
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }


    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus() {
        //최근 유저 받아옴
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {

            email = user.getEmail();
            uid = user.getUid();



        } else {
            startActivity(new Intent(this, MainActivity2.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //전 액티비티로 이동
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bar, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);

        return super.onCreateOptionsMenu(menu);


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    //권한처리 결과
   @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //권한 수락했을때 불러오는 메소드
       //권한을 처리했을때 (O/X)

       switch (requestCode) {
           case CAMERA_REQUEST_CODE:{
               if (grantResults.length>0){
                   boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                   boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                   if (cameraAccepted && storageAccepted) {
                       //두 권한 모두 부여
                       pickFromCamera();
                   }
                   else{
                       //카메라 , 갤러리 둘다 거절했을때
                       Toast.makeText(this, "카메라 혹은 갤러리 허락이 필요합니다...", Toast.LENGTH_SHORT).show();

                   }
               }
               else{

               }
           }
           break;
           case STORAGE_REQUEST_CODE:{
               if (grantResults.length>0) {
                   boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                   if (storageAccepted) {
                       //갤러리 권한 허락
                       pickFromGallery();
                   }
                   else{
                       //카메라 혹은 갤러리 혹은 둘다 거절했을때
                       Toast.makeText(this, "갤러리 접근 권한이 필요합니다", Toast.LENGTH_SHORT).show();

                   }
               }
               else{

               }

           }
           break;
       }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //카메라 혹은 갤러리에서 이미지를 고른 후 불러옴

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //갤러리에서 얻어온 이미지 uri
                image_uri = data.getData();

                //이미지뷰에 set
                Aimage.setImageURI(image_uri);
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //카메라에서 이미지 골른 uri

                Aimage.setImageURI(image_uri);

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}