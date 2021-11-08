package com.example.graduation_project;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CommunityFragment extends Fragment {

    FirebaseAuth firebaseAuth;

    ImageView imageView;

    RecyclerView recyclerView;
    List<CommunityModel> postList;
    AdapterCommunity adapterCommunity;

public CommunityFragment () {

}



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);




        //초기화
        firebaseAuth = FirebaseAuth.getInstance();
        //리사이클러 뷰와 그것들의 속성
        recyclerView = view.findViewById(R.id.communityRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //최근거부터 오래된 순으로 게시글 나열
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //레이아웃에 리사이클러 뷰 넣기
        recyclerView.setLayoutManager(layoutManager);

        //포스트 리스트 초기화화
       postList = new ArrayList<>();

       loadPosts();


        return view;
    }

    private void loadPosts() {
    //모든 게시글의 경로
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //이 ref로부터 모든 데이터 받아옴
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    CommunityModel communityModel = ds.getValue(CommunityModel.class);

                    postList.add(communityModel);

                    //adapter
                    adapterCommunity = new AdapterCommunity(getActivity(),postList);
                    //아답터를 리사이클러 뷰에 보이게함
                    recyclerView.setAdapter(adapterCommunity);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //error 난 경우
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void searchPosts(String searchQuery )    {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //이 ref로부터 모든 데이터 받아옴
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    CommunityModel communityModel = ds.getValue(CommunityModel.class);

                    if (communityModel.getpTitle().toLowerCase().contains(searchQuery.toLowerCase())||
                            communityModel.getpContent().toLowerCase().contains(searchQuery.toLowerCase())) {

                        postList.add(communityModel);
                    }


                    //adapter
                    adapterCommunity = new AdapterCommunity(getActivity(),postList);
                    //아답터를 리사이클러 뷰에 보이게함
                    recyclerView.setAdapter(adapterCommunity);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //error 난 경우
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_bar, menu);

        //메뉴 나오게함

        //게시글 제목이나 내용 검색하면 띄우게해줌
        MenuItem item = menu.findItem(R.id.action_search_post);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //검색 리스너
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //서치버튼 누르면 불러오는 것
                if (!TextUtils.isEmpty(s)) {
                    searchPosts(s);
                }
                else {
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s)) {
                    searchPosts(s);
                }
                else {
                    loadPosts();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_add_post) {
            startActivity(new Intent(getActivity(), CommunityActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}



















