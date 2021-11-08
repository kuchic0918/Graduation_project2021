package com.example.graduation_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CommunityDetailActivity extends AppCompatActivity {


    String hisUid,myUid,myEmail,myName,myDp,
            postId,pLikes,hisDp,hisName,pUri;

    boolean mProcessComment = false;
    boolean mProcessLike =false;

    ProgressDialog pd;

    ImageView userPicture,CImage;
    TextView uNameR,pTimeR,PTitleR,PContentR,pLikesR ,pCommentsR;
    ImageButton moreBtn;
    Button likeBtn;
    LinearLayout profileLayout;
    RecyclerView recyclerView;

    List<ModelComment> commentList;
    AdapterComments adapterComments;

    //댓글 창 view
    EditText commentEt;
    ImageButton sendBtn;
    ImageView cAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_detail);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("댓글입력 창");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        //
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        //view 초기화
        userPicture = findViewById(R.id.userPicture);
        CImage = findViewById(R.id.CImage);
        uNameR = findViewById(R.id.pName);
        pTimeR = findViewById(R.id.pTime);
        PTitleR = findViewById(R.id.PTitleR);
        PContentR = findViewById(R.id.PContentR);
        pLikesR = findViewById(R.id.pLike);
        pCommentsR = findViewById(R.id.pComments);
        moreBtn = findViewById(R.id.moreBtn);
        likeBtn = findViewById(R.id.likeBtn);

        profileLayout=findViewById(R.id.profileLayout);
        recyclerView = findViewById(R.id.recyclerView);

        commentEt = findViewById(R.id.commentEt);
        sendBtn = findViewById(R.id.sendBtn);
        cAvatar = findViewById(R.id.cAvatar);

        loadPostInfo();
        
        checkUserStatus();
        
        loadUserInfo();

        setLikes();
        
        actionBar.setSubtitle("내 이메일" + myEmail);

        loadComments();
        
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postComment ();
            }
        });

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likePost();
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOptions();

            }
        });
    }

    private void loadComments() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        commentList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    ModelComment modelComment = ds.getValue(ModelComment.class);

                    commentList.add(modelComment);

                    adapterComments = new AdapterComments(getApplicationContext(), commentList,myUid,postId);
                    recyclerView.setAdapter(adapterComments);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void showMoreOptions() {
        //팝업메뉴 만들기
        PopupMenu popupMenu = new PopupMenu(this,moreBtn, Gravity.END);
        //메뉴에 아이템 추가

        //옵션에 삭제 보이게하기 포스트화면에서
        if (hisUid.equals(myUid))    {
            popupMenu.getMenu().add(Menu.NONE, 0,0, "삭제");
            popupMenu.getMenu().add(Menu.NONE, 1,0, "수정");
        }


        //아이템 클릭 리스너
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id==0) {
                    beginDelete();
                    //삭제 클릭시
                }
                else  if (id==1) {
                    //수정 누를 시
                    //Community 액티비티를 염
                    Intent intent = new Intent(CommunityDetailActivity.this,CommunityActivity.class);
                    intent.putExtra("key" , "editPost");
                    intent.putExtra("editPostId",postId);
                  startActivity(intent);


                }
                return false;
            }
        });
        //메뉴 보여줌
        popupMenu.show();
    }

    private void beginDelete() {
        if (pUri.equals("noImage")){
            //이미지없이 사진 올렸을 때
            deleteWithoutImage();
        }
        else  {
            //이미지있이 올렸을 때
            deleteWithImage();


        }
    }

    private void deleteWithImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("삭제 중..");

        // 1 )이미지가 쓰는 주소 삭제
        // 2) 데이터베이스에서 쓰는 게시글 id 삭제

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pUri);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //이미지 삭제, 데이터베이스에서 바로
                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                                for (DataSnapshot ds: datasnapshot.getChildren()) {
                                    ds.getRef().removeValue();
                                }
                                //삭제
                                Toast.makeText(CommunityDetailActivity.this, "삭제가 완료 되었습니다", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //실패 할경우

                        pd.dismiss();
                        Toast.makeText(CommunityDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteWithoutImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("삭제 중..");
        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for (DataSnapshot ds: datasnapshot.getChildren()) {
                    ds.getRef().removeValue();
                }
                //삭제
                Toast.makeText(CommunityDetailActivity.this, "삭제가 완료 되었습니다", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setLikes() {
        final DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postId).hasChild(myUid)) {
                    //유저가 좋아요 눌르면
                    //아이콘 색과 버튼이 바뀌고 / 문구가 바뀜
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);
                    likeBtn.setText("좋아요 !");
                }
                else {
                    //누르지 않으면
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black,0,0,0);
                    likeBtn.setText("좋아요");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void likePost() {
        mProcessLike = true;

        final DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mProcessLike){
                    if (dataSnapshot.child(postId).hasChild(myUid)) {
                        //좋아요가 눌러져있고 지울때
                        postsRef.child(postId).child("pLikes").setValue(""+ (Integer.parseInt(pLikes)-1));
                        likeRef.child(postId).child(myUid).removeValue();
                        mProcessLike = false;


                    }
                    else {
                        //좋아요 안눌름
                        postsRef.child(postId).child("pLikes").setValue("" +(Integer.parseInt(pLikes)+1));
                        likeRef.child(postId).child(myUid).setValue("Liked"); //set any value
                        mProcessLike = false;


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void postComment() {

        pd = new ProgressDialog(this);
        pd.setMessage("댓글 입력중!");

        String comment = commentEt.getText().toString().trim();

        if (TextUtils.isEmpty(comment)){
            Toast.makeText(this, "댓글을 입력해주세요..", Toast.LENGTH_SHORT).show();
            return;
        }
        String timeStamp = String.valueOf(System.currentTimeMillis());
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        HashMap<String,Object> hashMap = new HashMap<>();

        hashMap.put("cId",timeStamp);
        hashMap.put("comment",comment);
        hashMap.put("timestamp",timeStamp);
        hashMap.put("uid",myUid);
        hashMap.put("uEmail",myEmail);
        hashMap.put("uDp",myDp);
        hashMap.put("uName",myName);

        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                    pd.dismiss();
                        Toast.makeText(CommunityDetailActivity.this, "댓글 입력중..", Toast.LENGTH_SHORT).show();
                        commentEt.setText("");
                        updateCommentCount();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(CommunityDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void updateCommentCount() {
        mProcessComment = true;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mProcessComment){
                    String comments = "" + dataSnapshot.child("pComments").getValue();
                    int newCommentVal = Integer.parseInt(comments) + 1;
                    ref.child("pComments").setValue(""+newCommentVal);
                    mProcessComment = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadUserInfo() {
        Query myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for (DataSnapshot ds: datasnapshot.getChildren()) {
                    myName=""+ds.child("name").getValue();
                    myDp=""+ds.child("image").getValue();
                    try {
                        Picasso.get().load(myDp).placeholder(R.drawable.add_user).into(cAvatar);
                    }
                    catch (Exception e) {
                        Picasso.get().load(R.drawable.add_user).into(cAvatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadPostInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for (DataSnapshot ds: datasnapshot.getChildren()) {
                    String pTitle = "" + ds.child("pTitle").getValue();
                    String pContent = ""+ds.child("pContent").getValue();
                    pLikes = "" + ds.child("pLikes").getValue();
                    String pTimestamp = "" + ds.child("pTime").getValue();
                     pUri = "" + ds.child("pUri").getValue();
                   hisDp = ""+ds.child("uDp").getValue();
                    hisUid = "" + ds.child("uid").getValue();
                    String uEmail = "" + ds.child("uEmail").getValue();
                    hisName = "" + ds.child("uName").getValue();
                    String commentCount = ""+ ds.child("pComments").getValue();


                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    try {
                        calendar.setTimeInMillis(Long.parseLong(pTimestamp));
                    } catch (Exception ex){
                        ex.printStackTrace();
                    }
                    String pTime = DateFormat.format("yyyy/MM/dd hh:mm aa",calendar).toString();
                    
                    PTitleR.setText(pTitle);
                    PContentR.setText(pContent);
                    pLikesR.setText(pLikes + "좋아요");
                    pTimeR.setText(pTime);
                    pCommentsR.setText(commentCount + "pComments");
                    
                    uNameR.setText(hisName);

                    if (pUri.equals("noImage")) {
                        //이미지뷰 숨김
                        CImage.setVisibility(View.GONE);
                    } else {
                        //이미지뷰 숨김
                        CImage.setVisibility(View.VISIBLE); //make sure to correct this
                        try {
                            Picasso.get().load(pUri).into(CImage);
                        } catch (Exception e) {

                        }
                    }
                    try {
                        Picasso.get().load(hisDp).placeholder(R.drawable.add_user).into(userPicture);
                    }catch (Exception e) {
                        Picasso.get().load(R.drawable.add_user).into(userPicture);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    
    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            myEmail = user.getEmail();
            myUid = user.getUid();
        }
        else {
            startActivity(new Intent(this,MainActivity.class));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
    
    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bar,menu);
        
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search_post).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        
      
        return super.onOptionsItemSelected(item);
    }
}