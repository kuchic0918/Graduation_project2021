package com.example.graduation_project;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WalkFragment extends Fragment implements View.OnClickListener {
    ListView listView;
    ArrayAdapter adapter;
    EditText edit;

    ArrayList<String> items = new ArrayList<String>();
    ViewGroup viewGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_walk, container, false);

        Button btn = viewGroup.findViewById(R.id.button);
        btn.setOnClickListener(this);
        edit = viewGroup.findViewById(R.id.edit);

        listView = viewGroup.findViewById(R.id.listview);
        adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, items);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(listener);
        return viewGroup;
    }

    AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            String str = (String) adapterView.getAdapter().getItem(i);
            int nIdx = str.indexOf("[ 전화번호 ]");
            int nIdx2 = str.indexOf("[ 주소 ]");
            String str1 = str.substring(nIdx+11,nIdx2-1);

            Toast.makeText(getActivity(), str.substring(nIdx,nIdx2-1), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+str1));
            startActivity(intent);

        }
    };


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
            //네트워크를 통해서 xml문서를 읽어오기..
            new Thread() {
                @Override
                public void run() {

                    items.clear();
                    String str = edit.getText().toString();
                    String location = URLEncoder.encode(str);
                    String adress = "http://api.kcisa.kr/openapi/service/rest/convergence2019/getConver03?serviceKey=c4f3567b-9b6d-4fc5-9a23-3f1749cccec4&pageNo=1&numOfRows=15&keyword=동물병원&where=" + location;



                    try {

                        URL url = new URL(adress);


                        InputStream is = url.openStream(); //바이트스트림
                        InputStreamReader isr = new InputStreamReader(is);

                        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                        XmlPullParser xpp = factory.newPullParser();
                        xpp.setInput(isr);

                        int eventType = xpp.getEventType();

                        String tagName;
                        StringBuffer buffer = null;

                        while (eventType != XmlPullParser.END_DOCUMENT) {

                            switch (eventType) {
                                case XmlPullParser.START_TAG:
                                    tagName = xpp.getName();
                                    if (tagName.equals("item")) {
                                        buffer = new StringBuffer();

                                    } else if (tagName.equals("title")) {
                                        buffer.append("[ 병원 이름 ] : ");
                                        xpp.next();
                                        buffer.append(xpp.getText() + "\n");


                                    } else if (tagName.equals("venue")) {
                                        buffer.append("[ 주소 ] : ");
                                        xpp.next();
                                        buffer.append(xpp.getText() + "\n");

                                    } else if (tagName.equals("reference")) {
                                        buffer.append("[ 전화번호 ] : ");
                                        xpp.next();
                                        buffer.append(xpp.getText() + "\n");

                                    } else if (tagName.equals("state")) {
                                        buffer.append("[ 영업 상태 ] : ");
                                        xpp.next();
                                        buffer.append(xpp.getText() + "\n");
                                        buffer.append("--------------------------------------------------------------------");
                                    }
                                    break;

                                case XmlPullParser.TEXT:
                                    break;

                                case XmlPullParser.END_TAG:
                                    tagName = xpp.getName();
                                    if (tagName.equals("item")) {

                                        items.add(buffer.toString());

                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                adapter.notifyDataSetChanged();
                                            }
                                        });
                                    }
                                    break;
                            }

                            eventType = xpp.next();
                        }


                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    }


                }
            }.start();
        }
    }
}