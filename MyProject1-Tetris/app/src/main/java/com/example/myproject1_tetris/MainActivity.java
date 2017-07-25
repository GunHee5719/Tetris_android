package com.example.myproject1_tetris;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textViewMain = (TextView)findViewById(R.id.textViewMain);
        Button gameStartButtonMain = (Button)findViewById(R.id.gameStartButtonMain);
        Button scoreButtonMain = (Button)findViewById(R.id.scoreButtonMain);

        gameStartButtonMain.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
                Intent inGameIntent = new Intent(MainActivity.this,InGameActivity.class);
                startActivity(inGameIntent);
            }
        });

        scoreButtonMain.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
                Intent scoreIntent = new Intent(MainActivity.this,ScoreActivity.class);
                startActivity(scoreIntent);
            }
        });
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("※게임 종료※");
        builder.setMessage("게임을 종료하시겠습니까?");
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
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
