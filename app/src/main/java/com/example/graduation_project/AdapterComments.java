package com.example.graduation_project;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Comment;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyHolder> {

    Context context;
    List<ModelComment> commentList;
    String myUid, postId;

    public AdapterComments(Context context, List<ModelComment> commentList, String myUid, String postId) {
        this.context = context;
        this.commentList = commentList;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_comments,viewGroup,false);

        return  new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myholder, int i) {
        String uid = commentList.get(i).getUid();
        String name = commentList.get(i).getuName();
        String email = commentList.get(i).getuEmail();
        String image = commentList.get(i).getuDp();
        String cid = commentList.get(i).getcId();
        String comment = commentList.get(i).getComment();
        String timestamp = commentList.get(i).getTimestamp();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        try {


            calendar.setTimeInMillis(Long.parseLong(timestamp));
        } catch (Exception ex){
            ex.printStackTrace();
        }
        String pTime = DateFormat.format("yyyy/MM/dd hh:mm aa",calendar).toString();

        myholder.nameC.setText(name);
        myholder.commentC.setText(comment);
        myholder.timeC.setText(pTime);

        try {
            Picasso.get().load(image).placeholder(R.drawable.add_user).into(myholder.avatarC);
        }
        catch (Exception e) {

        }
        myholder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myUid.equals(uid)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                    builder.setTitle("삭제");
                    builder.setMessage("댓글을 삭제하겠습니까?");
                    builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteComment(cid);
                        }
                    });
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.create().show();
                }
                else{
                    Toast.makeText(context, "다른사람의 댓글은 삭제할 수 없습니다..", Toast.LENGTH_SHORT).show();

                }
            }
        });


    }

    private void deleteComment(String cid) {
       final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.child("Comments").child(cid).removeValue();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String comments = "" + snapshot.child("pComments").getValue();
                int newCommentval = Integer.parseInt(comments) - 1;
                ref.child("pComments").setValue(""+newCommentval);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        ImageView avatarC;
        TextView nameC,commentC,timeC;


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            avatarC = itemView.findViewById(R.id.avatarC);
            nameC = itemView.findViewById(R.id.nameC);
            commentC = itemView.findViewById(R.id.commentC);
            timeC = itemView.findViewById(R.id.timeC);

        }
    }
}
