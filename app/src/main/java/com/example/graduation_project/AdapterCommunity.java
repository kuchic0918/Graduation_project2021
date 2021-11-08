package com.example.graduation_project;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;

import android.content.Intent;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;


import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterCommunity extends RecyclerView.Adapter<AdapterCommunity.MyHolder> {

    Context context;
    List<CommunityModel> postList;

    String myUid ;

    private DatabaseReference likeRef; //좋아요를 데이터베이스화
    private DatabaseReference postsRef; //포스트에 반영영

    boolean mProcessLike = false;


    public AdapterCommunity(Context context, List<CommunityModel> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }





        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            //row_community 메모리로 불러옴
            View view = LayoutInflater.from(context).inflate(R.layout.row_community, viewGroup, false);


            return new MyHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyHolder myHolder,  int i) {
            //데이터 받아오는 함수
             final String uid = postList.get(i).getUid();
            String uEmail = postList.get(i).getuEmail();
            String uName = postList.get(i).getuName();
            String uDp = postList.get(i).getuDp();
             final String pId = postList.get(i).getpId();
            String pTitle = postList.get(i).getpTitle();
            String pContent = postList.get(i).getpContent();
             String pUri = postList.get(i).getpUri();
            String pTimestamp = postList.get(i).getpTime();
            String pLikes = postList.get(i).getpLikes(); //총 갯수 나오게하는 기능 포함
            String pComments = postList.get(i).getpComments();

            //시간 나타나게 해줌
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            try {


                calendar.setTimeInMillis(Long.parseLong(pTimestamp));
            } catch (Exception ex){
                ex.printStackTrace();
            }
            String pTime = DateFormat.format("yyyy/MM/dd hh:mm aa",calendar).toString();


            //set data
            myHolder.pName.setText(uName);
            myHolder.pTime.setText(pTime);
            myHolder.PTitleR.setText(pTitle);
            myHolder.PContentR.setText(pContent);
            myHolder.pLike.setText(pLikes+"Likes");
            myHolder.pComments.setText(pComments+"Comments");
            //포스트마다 좋아요
            setLikes(myHolder,pId);





            //user 정보 셋
            try {
                Picasso.get().load(uDp).placeholder(R.drawable.add_user).into(myHolder.userPicture);
            } catch (Exception e) {

            }
            //게시글 이미지 set
            //이미지 없으면 이미지뷰 숨겨서 나옴
            if (pUri.equals("noImage")) {
                //이미지뷰 숨김
                myHolder.CImage.setVisibility(View.GONE);
            } else {
                //이미지뷰 숨김
                myHolder.CImage.setVisibility(View.VISIBLE); //make sure to correct this
                try {
                    Picasso.get().load(pUri).into(myHolder.CImage);
                } catch (Exception e) {

                }
            }

            myHolder.moreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showMoreOptions(myHolder.moreBtn,uid,myUid,pId,pUri);
                }
            });
            myHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   //게시글에 좋아요 총 갯수 , 누가 버튼 눌렀는지
                    final int pLikes = Integer.parseInt(postList.get(i).getpLikes());
                    mProcessLike = true;

                    final String postIde = postList.get(i).getpId();
                    likeRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (mProcessLike){
                                if (dataSnapshot.child(postIde).hasChild(myUid)) {
                                    //좋아요가 눌러져있고 지울때
                                    postsRef.child(postIde).child("pLikes").setValue(""+ (pLikes-1));
                                    likeRef.child(postIde).child(myUid).removeValue();
                                    mProcessLike = false;
                                }
                                else {
                                    //좋아요 안눌름
                                    postsRef.child(postIde).child("pLikes").setValue("" + (pLikes+1));
                                    likeRef.child(postIde).child(myUid).setValue("Liked"); //set any value
                                    mProcessLike = false;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });
            myHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //커뮤니티디테일 액티비티에서 시작
                   Intent intent = new Intent(context,CommunityDetailActivity.class);
                   intent.putExtra("postId",pId);
                   context.startActivity(intent);
                }
            });


        }

    private void setLikes(MyHolder holder, String postKey) {
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postKey).hasChild(myUid)) {
                    //유저가 좋아요 눌르면
                    //아이콘 색과 버튼이 바뀌고 / 문구가 바뀜
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);
                    holder.likeBtn.setText("좋아요 !");
                }
                else {
                    //누르지 않으면
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black,0,0,0);
                    holder.likeBtn.setText("좋아요");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMoreOptions(ImageButton moreBtn, String uId, String myUid, String pId, String pUri) {
        //팝업메뉴 만들기
        PopupMenu popupMenu = new PopupMenu(context,moreBtn, Gravity.END);
        //메뉴에 아이템 추가

        //옵션에 삭제 보이게하기 포스트화면에서
        if (uId.equals(myUid))    {
            popupMenu.getMenu().add(Menu.NONE, 0,0, "삭제");
            popupMenu.getMenu().add(Menu.NONE, 1,0, "수정");
        }
        popupMenu.getMenu().add(Menu.NONE,2,0,"자세히 보기");

        //아이템 클릭 리스너
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id==0) {
                    beginDelete(pId,pUri);
                    //삭제 클릭시
                }
                else  if (id==1) {
                    //수정 누를 시
                    //Community 액티비티를 염
                    Intent intent = new Intent(context,CommunityActivity.class);
                    intent.putExtra("key" , "editPost");
                    intent.putExtra("editPostId",pId);
                    context.startActivity(intent);
                }
                else if (id==2){
                    Intent intent = new Intent(context,CommunityDetailActivity.class);
                    intent.putExtra("postId",pId);
                    context.startActivity(intent);
                }
                return false;
            }
        });
        //메뉴 보여줌
        popupMenu.show();
    }

    private void beginDelete(String pId, String pUri) {
        //이미지 없을때랑 있을때

        if (pUri.equals("noImage")){
            //이미지없이 사진 올렸을 때
            deleteWithoutImage(pId);
        }
        else  {
            //이미지있이 올렸을 때
            deleteWithImage(pId,pUri);


        }
    }

    private void deleteWithImage(String pId, String pUri) {
        //프로그레스 바
       final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("삭제 중..");

        // 1 )이미지가 쓰는 주소 삭제
        // 2) 데이터베이스에서 쓰는 게시글 id 삭제

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pUri);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //이미지 삭제, 데이터베이스에서 바로
                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                                for (DataSnapshot ds: datasnapshot.getChildren()) {
                                    ds.getRef().removeValue();
                                }
                                //삭제
                                Toast.makeText(context, "삭제가 완료 되었습니다", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteWithoutImage(String pId) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("삭제 중..");
        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for (DataSnapshot ds: datasnapshot.getChildren()) {
                    ds.getRef().removeValue();
                }
                //삭제
                Toast.makeText(context, "삭제가 완료 되었습니다", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
        public int getItemCount() {
            return postList.size();
        }
    class MyHolder extends RecyclerView.ViewHolder {

        //row_community에 있는 것들
        ImageView userPicture, CImage;
        TextView PTitleR, PContentR, pLike, pName, pTime , pComments;
        ImageButton moreBtn;
        Button likeBtn, commentBtn;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //view 초기화
            userPicture = itemView.findViewById(R.id.userPicture);
            CImage = itemView.findViewById(R.id.CImage);
            PTitleR = itemView.findViewById(R.id.PTitleR);
            PContentR = itemView.findViewById(R.id.PContentR);
            pLike = itemView.findViewById(R.id.pLike);
            pComments =itemView.findViewById(R.id.pComments);
            pName = itemView.findViewById(R.id.pName);
            pTime = itemView.findViewById(R.id.pTime);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            profileLayout = itemView.findViewById(R.id.profileLayout);

        }
    }




}