package com.example.myproject1_tetris;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by 장건희 on 2017-07-17.
 */

public class GameOverActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameover);

        TextView signMyScore = (TextView) findViewById(R.id.signmyscore);
        TextView singBestScore = (TextView)findViewById(R.id.signbestscore);

        Button goToMain = (Button)findViewById(R.id.gotomain2);
        Button goToScore = (Button)findViewById(R.id.gotoscore);

        Intent intent = getIntent();
        final int myScore = intent.getExtras().getInt("score");
        signMyScore.setText("내 점수 : " + myScore);


        goToMain.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                finish();
                Intent a = new Intent(GameOverActivity.this, MainActivity.class);
                startActivity(a);
            }
        });

        goToScore.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                finish();
                Intent a = new Intent(GameOverActivity.this, ScoreActivity.class);
                startActivity(a);
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
                Intent a = new Intent(GameOverActivity.this,MainActivity.class);
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
}
