package com.example.graduation_project;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import static android.content.Context.MODE_NO_LOCALIZED_COLLATORS;

public class CalendarFragment extends Fragment {
    ViewGroup viewGroup;

    public CalendarView calendarView;
    public EditText contextEditText;
    public TextView diaryTextView,textView;
    public Button cha_Btn;
    public Button del_Btn;
    public Button save_Btn;
    public String str = null;
    public String f_name = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = (CalendarView)viewGroup.findViewById(R.id.calendarView);
        contextEditText = viewGroup.findViewById(R.id.contextEditText);
        diaryTextView = viewGroup.findViewById(R.id.diaryTextView);
        textView = viewGroup.findViewById(R.id.D_textView);
        cha_Btn = viewGroup.findViewById(R.id.cha_Btn);
        del_Btn = viewGroup.findViewById(R.id.del_Btn);
        save_Btn = viewGroup.findViewById(R.id.save_Btn);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                diaryTextView.setVisibility(View.VISIBLE);
                save_Btn.setVisibility(View.VISIBLE); // 저장 버튼이 Visible
                contextEditText.setVisibility(View.VISIBLE); // EditText가 Visible
                textView.setVisibility(View.INVISIBLE); // 저장된 일기 textView가 Invisible
                cha_Btn.setVisibility(View.INVISIBLE); // 수정 Button이 Invisible
                del_Btn.setVisibility(View.INVISIBLE); // 삭제 Button이 Invisible
                diaryTextView.setText(String.format("%d / %d / %d",year,month+1,dayOfMonth));
                contextEditText.setText(""); // EditText에 공백값 넣기
                checkDay(year,month,dayOfMonth);
            }

        });


        save_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveDiary(f_name);
                str = contextEditText.getText().toString();
                textView.setText(str);
                save_Btn.setVisibility(View.INVISIBLE);
                cha_Btn.setVisibility(View.VISIBLE);
                del_Btn.setVisibility(View.VISIBLE);
                contextEditText.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.VISIBLE);

            }
        });

        return viewGroup;
    }

    public void checkDay ( int cYear, int cMonth, int cDay){
        f_name = "" + cYear + "-" + (cMonth + 1) + "" + "-" + cDay + ".txt";//저장할 파일 이름설정
        FileInputStream fis = null;//FileStream fis 변수
        try {
            fis = getActivity().openFileInput(f_name);
            byte[] fileData = new byte[fis.available()];
            fis.read(fileData);
            fis.close();

            str = new String(fileData);

            contextEditText.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.VISIBLE);
            textView.setText(str);

            save_Btn.setVisibility(View.INVISIBLE);
            cha_Btn.setVisibility(View.VISIBLE);
            del_Btn.setVisibility(View.VISIBLE);

            cha_Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    contextEditText.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.INVISIBLE);
                    contextEditText.setText(str);

                    save_Btn.setVisibility(View.VISIBLE);
                    cha_Btn.setVisibility(View.INVISIBLE);
                    del_Btn.setVisibility(View.INVISIBLE);
                    textView.setText(contextEditText.getText());
                }

            });
            del_Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    textView.setVisibility(View.INVISIBLE);
                    contextEditText.setText("");
                    contextEditText.setVisibility(View.VISIBLE);
                    save_Btn.setVisibility(View.VISIBLE);
                    cha_Btn.setVisibility(View.INVISIBLE);
                    del_Btn.setVisibility(View.INVISIBLE);
                    removeDiary(f_name);
                }
            });
            if (textView.getText() == null) {
                textView.setVisibility(View.INVISIBLE);
                diaryTextView.setVisibility(View.VISIBLE);
                save_Btn.setVisibility(View.VISIBLE);
                cha_Btn.setVisibility(View.INVISIBLE);
                del_Btn.setVisibility(View.INVISIBLE);
                contextEditText.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("WrongConstant")
    public void removeDiary (String readDay){
        FileOutputStream fos = null;

        try {
            fos = getActivity().openFileOutput(readDay, MODE_NO_LOCALIZED_COLLATORS);
            String content = "";
            fos.write((content).getBytes());
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("WrongConstant")
    public void saveDiary (String readDay){
        FileOutputStream fos = null;

        try {
            fos = getActivity().openFileOutput(readDay, MODE_NO_LOCALIZED_COLLATORS);
            String content = contextEditText.getText().toString();
            fos.write((content).getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}



