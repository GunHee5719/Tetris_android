package com.example.myproject1_tetris;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by 장건희 on 2017-07-10.
 */

public class ScoreActivity extends AppCompatActivity{

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        Button goToMain = (Button)findViewById(R.id.gotomain);

        goToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent mainIntent = new Intent(ScoreActivity.this,MainActivity.class);
                startActivity(mainIntent);
            }
        });
    }
    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("※메인 화면으로※");
        builder.setMessage("메인 화면으로 돌아가시겠습니까?");
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                Intent a = new Intent(ScoreActivity.this,MainActivity.class);
                startActivity(a);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    // 여기에 DB에서 점수 불러오고, 갱신하는 부분 포함 되어야 함!!
}
