package com.example.myproject1_tetris;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.MissingFormatArgumentException;

/**
 * Created by 장건희 on 2017-07-10.
 */

public class InGameActivity extends AppCompatActivity{
    private boolean gameStart = false;
    private int displayWidth=0;
    private int displayHeight=0;
    private float garoR=0;
    private float seroR=0;

    private TextView nowScore;
    private TextView nowLevel;
    private Button buttonLeft;
    private  Button buttonRight;
    private Button buttonUp;
    private  Button buttonDown;
    private Button buttonSave;
    private Button buttonSpace;
    private  Button menu;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingame);

        DrawIngame DI = new DrawIngame(this);
        LinearLayout.LayoutParams paramlinear = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        addContentView(DI,paramlinear);

        Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        displayWidth = display.getWidth();      // 갤럭시S8+는 1080
        displayHeight = display.getHeight();    // 갤럭시S8+는 2094
        garoR = displayWidth/1080;
        seroR = displayHeight/2094;

        TextView startMessage = new TextView(InGameActivity.this);
        startMessage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        startMessage.setTextColor(Color.BLACK);
        startMessage.setPadding(90,870,0,0);
        startMessage.setTextSize(17);
        startMessage.setText("Touch Here To Start Game!!");
        addContentView(startMessage,paramlinear);
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("※게임 종료※");
        builder.setMessage("실행중인 게임을 종료하시겠습니까?\n\n(진행중이던 게임은 저장되지 않습니다)");
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                Intent a = new Intent(InGameActivity.this,MainActivity.class);
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

    public boolean onTouchEvent(MotionEvent event){

        if(!gameStart && event.getX()<735*garoR && event.getX()>40*garoR && event.getY()>180*seroR && event.getY()<1551*seroR){
            setContentView(R.layout.activity_ingame);
            DrawIngame DI = new DrawIngame(this);
            LinearLayout.LayoutParams paramlinear = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            addContentView(DI,paramlinear);

            nowScore = (TextView)findViewById(R.id.nowscore);
            nowLevel = (TextView)findViewById(R.id.level);
            buttonLeft = (Button)findViewById(R.id.buttonleft);
            buttonRight = (Button)findViewById(R.id.buttonright);
            buttonUp = (Button)findViewById(R.id.buttonup);
            buttonDown = (Button)findViewById(R.id.buttondown);
            buttonSave = (Button)findViewById(R.id.buttonsave);
            buttonSpace = (Button)findViewById(R.id.buttonspace);
            menu = (Button)findViewById(R.id.pause);
            PlayTetris tetrisStart = new PlayTetris(this,nowScore,nowLevel, buttonLeft,buttonRight,buttonUp,buttonDown,buttonSave,buttonSpace,garoR,seroR);

            gameStart = true;
            return true;
        }
        return false;
    }

    class DrawIngame extends View {
        public DrawIngame(Context context) {
            super(context);
        }

        protected void onDraw(Canvas canvas) {
            float garo = (float)50;
            float sero = (float)190;
            float width = (float)66.6;

            super.onDraw(canvas);
            Paint pnt = new Paint();
            pnt.setColor(Color.GRAY);
            canvas.drawRect(40*garoR,180*seroR,735*garoR,1551*seroR,pnt);       // 전체 큰 상자, 일반 회색

            pnt.setColor(Color.LTGRAY);
            canvas.drawRect(760*garoR,280*seroR,1040*garoR,550*seroR,pnt);      // Next 밑의 상자
            canvas.drawRect(760*garoR,645*seroR,1040*garoR,915*seroR,pnt);      // Save 밑의 상자

            pnt.setColor(Color.DKGRAY);
            for(int a=0;a<20;a++){                      // 전체 큰 상자 내부에 다크 그레이로 네모들을 총 200개 그림
                for(int b=0;b<10;b++){
                    canvas.drawRect(garo*garoR,sero*seroR,(garo+width)*garoR,(sero+width)*seroR,pnt);
                    garo=garo+width+1;
                }
                garo=50;
                sero=sero+width+1;
            }

        }
    }
}
