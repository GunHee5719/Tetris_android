package com.example.myproject1_tetris;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by 장건희 on 2017-07-11.
 */

public class PlayTetris extends Thread{
    private int[][] blockState = new int[22][12];
    private int gameLevel=1;
    private int gameScore=0;
    private int scoreForLevel=0;
    private int combo=0;
    private int blockDownTime=2000;
    private int sleepCount=0;
    private int gameState=0;

    // 테트리스 블럭 관련 변수
    private TetrisBlock nowBlock;
    private TetrisBlock nextBlock;
    private TetrisBlock saveBlock;

    // 게임실행에 있어, 상태를 표시하기 위한 변수들(게임진행상태 등)
    private int makeBlockMode=0;
    private boolean savePress=true;
    private boolean hasSave = false;
    private boolean spaceEvent=false;

    // 블럭들의 위치를 임시로 저장하기 위한 변수. saveXX는 블록 이동을 위해, rotateXX는 회전을 위해 임시로 사용
    private int[] saveLocG = new int[4];
    private int[] saveLocS = new int[4];
    private int[] rotateLocG = new int[4];
    private int[] rotateLocS= new int[4];

    // 핸들러와 UI표시를 위해 쓰이는 layoutParams 변수
    private LinearLayout.LayoutParams paramlinear = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
    );
    private Handler mHandler = new Handler();

    // 캔버스 색칠 관련 변수
    private Paint pnt = new Paint();
    private Paint pnt2 = new Paint();
    private Paint pnt3 = new Paint();
    private Paint pntDKGray = new Paint();
    private Paint pntLTGray = new Paint();
    private String strColor;
    private String strColor2;
    private String strColor3;

    // 수정해야 할 텍스트 뷰 변수
    private TextView nowScore;
    private TextView nowLevel;
    private Context context;

    // 캔버스 그릴 때 해상도를 고려하기 위한 변수
    private float garoR;
    private float seroR;

    public PlayTetris(final Context context, TextView nowScore, TextView nowLevel, Button buttonLeft, Button buttonRight, Button buttonUp,
                      final Button buttonDown, Button buttonSave, Button buttonSpace, float garoR, float seroR) {

        pntDKGray.setColor(Color.DKGRAY);
        pntLTGray.setColor(Color.LTGRAY);

        for (int a = 0; a < 22; a++) {
            for (int b = 0; b < 12; b++) {
                if (a == 0 || a == 21) {
                    blockState[a][b] = -1;
                } else {
                    if (b == 0 || b == 11) {
                        blockState[a][b] = -1;
                    } else {
                        blockState[a][b] = 0;
                    }
                }
            }
        }

        this.context = context;
        this.nowScore = nowScore;
        this.nowLevel = nowLevel;

        this.garoR = garoR;
        this.seroR = seroR;

        buttonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonLeftEvent();
            }
        });
        buttonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonRightEvent();
            }
        });
        buttonUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonUpEvent();
            }
        });
        buttonDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonDownEvent();
            }
        });
        buttonSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonSpaceEvent();
            }
        });
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonSaveEvent();
            }
        });

        int random = (int)(Math.random()*7);
        TetrisBlock temp = new TetrisBlock(random);
        nextBlock=temp;

        switch(nextBlock.getBlockKind()){
            case 0:
                strColor2="#00FFFF";
                break;
            case 1:
                strColor2="#FFFF00";
                break;
            case 2:
                strColor2="#0000FF";
                break;
            case 3:
                strColor2="#FF8C00";
                break;
            case 4:
                strColor2="#FF0000";
                break;
            case 5:
                strColor2="#7CFC00";
                break;
            case 6:
                strColor2="#8A2BE2";
                break;
            default:
                break;
        }

        makeNewBlock();

        BackThread runnable = new BackThread();
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.start();
    }

    // 새로운 블럭 생성, pnt는 현재 색깔, pnt2는 다음 색깔, nowBlock은 현재 블록, nextBlock은 다음 블록
    public void makeNewBlock(){
        int random = (int)(Math.random()*7);
        TetrisBlock temp = new TetrisBlock(random);

        nowBlock=nextBlock;
        nextBlock=temp;

        gameOverCheck();

        strColor=strColor2;

        switch(nextBlock.getBlockKind()){
            case 0:
                strColor2="#00FFFF";
                break;
            case 1:
                strColor2="#FFFF00";
                break;
            case 2:
                strColor2="#0000FF";
                break;
            case 3:
                strColor2="#FF8C00";
                break;
            case 4:
                strColor2="#FF0000";
                break;
            case 5:
                strColor2="#7CFC00";
                break;
            case 6:
                strColor2="#8A2BE2";
                break;
            default:
                break;
        }
        pnt.setColor(Color.parseColor(strColor));
        pnt2.setColor(Color.parseColor(strColor2));

        PrintNewBlock PNB = new PrintNewBlock(context,makeBlockMode);
        ((InGameActivity) context).addContentView(PNB,paramlinear);
    }

    class BackThread extends Thread{
        public void run(){
            Runnable myRun = new Runnable() {
                @Override
                public void run() {
                    if(checkNeedFix()){
                        fixBlock();
                        completeRow();
                        makeNewBlock();
                    }
                    else{
                        for(int a=0;a<4;a++){
                            saveLocG[a]=nowBlock.getBlockLocG(a);
                            saveLocS[a]=nowBlock.getBlockLocS(a);
                        }
                        nowBlock.moveBlockDown(1);

                        PrintMoveBlock PMB = new PrintMoveBlock(context);
                        ((InGameActivity) context).addContentView(PMB,paramlinear);
                    }
                }
            };

            while(true){
                try{
                    Thread.sleep(blockDownTime/10);
                } catch (InterruptedException e) {
                    ;
                }
                sleepCount++;
                if(sleepCount==9 || spaceEvent){
                    mHandler.post(myRun);
                    sleepCount=0;
                    spaceEvent=false;
                }
                if(gameState==1){
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        ;
                    }

                    ((InGameActivity)context).finish();
                    Intent a = new Intent(context,GameOverActivity.class);
                    a.putExtra("score",gameScore);
                    context.startActivity(a);
                    break;
                }
            }
        }
    }

    class PrintNewBlock extends View {
        private int mode;       // mode=0 -> 새로생성(새블록,next) , mode=1 -> save최초클릭(새블록,next,save) , mode=2 -> save 생성(새블록,save)
        public PrintNewBlock(Context context, int mode) {
            super(context);
            this.mode = mode;
        }

        protected void onDraw(Canvas canvas) {
            float garo = (float)50;
            float sero = (float)190;
            float width = (float)67.6;
            float width2 = (float)66.6;
            float g;
            float s;

            if(mode==0){
                savePress=true;
            }

            super.onDraw(canvas);
            if(mode>0){
                for(int a=0;a<4;a++){
                    g = garo+(saveLocG[a]-1)*width;
                    s = sero+(saveLocS[a]-1)*width;
                    canvas.drawRect(g*garoR,s*seroR,(g+width2)*garoR,(s+width2)*seroR,pntDKGray);
                }
            }

            for(int a=0;a<4;a++){
                g = garo+(nowBlock.getBlockLocG(a)-1)*width;
                s = sero+(nowBlock.getBlockLocS(a)-1)*width;
                canvas.drawRect(g*garoR,s*seroR,(g+width2)*garoR,(s+width2)*seroR,pnt);
            }

            if(mode<2) {
                canvas.drawRect(760*garoR,280*seroR,1040*garoR,550*seroR,pntLTGray);
                width = (float) 61;
                width2 = (float) 60;
                switch (nextBlock.getBlockKind()) {
                    case 0:
                        garo = (float) 778.5;
                        sero = (float) 385;
                        for (int a = 0; a < 4; a++) {
                            g = garo + (a * width);
                            s = sero;
                            canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt2);
                        }
                        break;
                    case 1:
                        garo = (float) 839.5;
                        sero = (float) 354.5;
                        for (int a = 0; a < 2; a++) {
                            g = garo + (a * width);
                            s = sero;
                            canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt2);
                        }
                        for (int a = 0; a < 2; a++) {
                            g = garo + (a * width);
                            s = sero + width;
                            canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt2);
                        }
                        break;
                    case 2:
                        garo = (float) 809;
                        sero = (float) 354.5;
                        g = garo;
                        s = sero;
                        canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt2);
                        s = sero + width;
                        for (int a = 0; a < 3; a++) {
                            g = garo + (a * width);
                            canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt2);
                        }
                        break;
                    case 3:
                        garo = (float) 809;
                        sero = (float) 354.5;
                        g = garo + (2 * width);
                        s = sero;
                        canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt2);
                        s = sero + width;
                        for (int a = 0; a < 3; a++) {
                            g = garo + (a * width);
                            canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt2);
                        }
                        break;
                    case 4:
                        garo = (float) 809;
                        sero = (float) 354.5;
                        for (int a = 0; a < 2; a++) {
                            g = garo + (a * width);
                            s = sero;
                            canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt2);
                        }
                        for (int a = 0; a < 2; a++) {
                            g = garo + (a + 1) * width;
                            s = sero + width;
                            canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt2);
                        }
                        break;
                    case 5:
                        garo = (float) 809;
                        sero = (float) 354.5;
                        for (int a = 0; a < 2; a++) {
                            g = garo + (a + 1) * width;
                            s = sero;
                            canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt2);
                        }
                        for (int a = 0; a < 2; a++) {
                            g = garo + a * width;
                            s = sero + width;
                            canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt2);
                        }
                        break;
                    case 6:
                        garo = (float) 809;
                        sero = (float) 354.5;
                        g = garo + width;
                        s = sero;
                        canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt2);
                        s = sero + width;
                        for (int a = 0; a < 3; a++) {
                            g = garo + (a * width);
                            canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt2);
                        }
                        break;
                    default:
                        break;
                }
            }
            if(mode>0) {
                canvas.drawRect(760*garoR,645*seroR,1040*garoR,915*seroR,pntLTGray);

                width = (float) 61;
                width2 = (float) 60;
                switch (saveBlock.getBlockKind()) {
                    case 0:
                        garo = (float) 778.5;
                        sero = (float) 750;
                        for (int a = 0; a < 4; a++) {
                            g = garo + (a * width);
                            s = sero;
                            canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt3);
                        }
                        break;
                    case 1:
                        garo = (float) 839.5;
                        sero = (float) 719.5;
                        for (int a = 0; a < 2; a++) {
                            g = garo + (a * width);
                            s = sero;
                            canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt3);
                        }
                        for (int a = 0; a < 2; a++) {
                            g = garo + (a * width);
                            s = sero + width;
                            canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt3);
                        }
                        break;
                    case 2:
                        garo = (float) 809;
                        sero = (float) 719.5;
                        g = garo;
                        s = sero;
                        canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt3);
                        s = sero + width;
                        for (int a = 0; a < 3; a++) {
                            g = garo + (a * width);
                            canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt3);
                        }
                        break;
                    case 3:
                        garo = (float) 809;
                        sero = (float) 719.5;
                        g = garo + (2 * width);
                        s = sero;
                        canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt3);
                        s = sero + width;
                        for (int a = 0; a < 3; a++) {
                            g = garo + (a * width);
                            canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt3);
                        }
                        break;
                    case 4:
                        garo = (float) 809;
                        sero = (float) 719.5;
                        for (int a = 0; a < 2; a++) {
                            g = garo + (a * width);
                            s = sero;
                            canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt3);
                        }
                        for (int a = 0; a < 2; a++) {
                            g = garo + (a + 1) * width;
                            s = sero + width;
                            canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt3);
                        }
                        break;
                    case 5:
                        garo = (float) 809;
                        sero = (float) 719.5;
                        for (int a = 0; a < 2; a++) {
                            g = garo + (a + 1) * width;
                            s = sero;
                            canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt3);
                        }
                        for (int a = 0; a < 2; a++) {
                            g = garo + a * width;
                            s = sero + width;
                            canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt3);
                        }
                        break;
                    case 6:
                        garo = (float) 809;
                        sero = (float) 719.5;
                        g = garo + width;
                        s = sero;
                        canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt3);
                        s = sero + width;
                        for (int a = 0; a < 3; a++) {
                            g = garo + (a * width);
                            canvas.drawRect(g * garoR, s * seroR, (g + width2) * garoR, (s + width2) * seroR, pnt3);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    class PrintMoveBlock extends View {
        public PrintMoveBlock(Context context) {
            super(context);
        }

        protected void onDraw(Canvas canvas) {
            float garo = (float)50;
            float sero = (float)190;
            float width = (float)67.6;
            float width2 = (float)66.6;

            super.onDraw(canvas);

            for(int a=0;a<4;a++){
                float g = garo+(saveLocG[a]-1)*width;
                float s = sero+(saveLocS[a]-1)*width;
                canvas.drawRect(g*garoR,s*seroR,(g+width2)*garoR,(s+width2)*seroR,pntDKGray);
            }
            for(int a=0;a<4;a++){
                float g = garo+(nowBlock.getBlockLocG(a)-1)*width;
                float s = sero+(nowBlock.getBlockLocS(a)-1)*width;
                canvas.drawRect(g*garoR,s*seroR,(g+width2)*garoR,(s+width2)*seroR,pnt);
            }
        }
    }

    class PrintFixBlock extends View {
        public PrintFixBlock(Context context) {
            super(context);
        }

        protected void onDraw(Canvas canvas) {
            float garo = (float)50;
            float sero = (float)190;
            float width = (float)67.6;
            float width2 = (float)66.6;

            super.onDraw(canvas);

            for(int a=0;a<4;a++){
                float g = garo+(nowBlock.getBlockLocG(a)-1)*width;
                float s = sero+(nowBlock.getBlockLocS(a)-1)*width;
                canvas.drawRect(g*garoR,s*seroR,(g+width2)*garoR,(s+width2)*seroR,pnt);
            }
        }
    }

    public void buttonLeftEvent(){
        boolean movable = true;

        if(blockState[nowBlock.getBlockLocS(0)][nowBlock.getBlockLocG(0)-1]!=0) movable=false;
        if(blockState[nowBlock.getBlockLocS(1)][nowBlock.getBlockLocG(1)-1]!=0) movable=false;
        if(blockState[nowBlock.getBlockLocS(2)][nowBlock.getBlockLocG(2)-1]!=0) movable=false;
        if(blockState[nowBlock.getBlockLocS(3)][nowBlock.getBlockLocG(3)-1]!=0) movable=false;

        if(movable){
            for(int a=0;a<4;a++){
                saveLocG[a]=nowBlock.getBlockLocG(a);
                saveLocS[a]=nowBlock.getBlockLocS(a);
            }
            nowBlock.moveBlockSide(-1);

            PrintMoveBlock PMB = new PrintMoveBlock(context);
            ((InGameActivity) context).addContentView(PMB,paramlinear);
        }
    }

    public void buttonRightEvent(){
        boolean movable = true;

        if(blockState[nowBlock.getBlockLocS(0)][nowBlock.getBlockLocG(0)+1]!=0) movable=false;
        if(blockState[nowBlock.getBlockLocS(1)][nowBlock.getBlockLocG(1)+1]!=0) movable=false;
        if(blockState[nowBlock.getBlockLocS(2)][nowBlock.getBlockLocG(2)+1]!=0) movable=false;
        if(blockState[nowBlock.getBlockLocS(3)][nowBlock.getBlockLocG(3)+1]!=0) movable=false;

        if(movable){
            for(int a=0;a<4;a++){
                saveLocG[a]=nowBlock.getBlockLocG(a);
                saveLocS[a]=nowBlock.getBlockLocS(a);
            }
            nowBlock.moveBlockSide(1);

            PrintMoveBlock PMB = new PrintMoveBlock(context);
            ((InGameActivity) context).addContentView(PMB,paramlinear);
        }
    }

    public void buttonUpEvent(){
        if(checkRotatable()){
            for(int a=0;a<4;a++){
                saveLocG[a]=nowBlock.getBlockLocG(a);
                saveLocS[a]=nowBlock.getBlockLocS(a);
            }
            nowBlock.rotateBlock(rotateLocG,rotateLocS);

            PrintMoveBlock PMB = new PrintMoveBlock(context);
            ((InGameActivity) context).addContentView(PMB,paramlinear);
        }
    }

    public void buttonDownEvent(){
        boolean movable = true;

        if(blockState[nowBlock.getBlockLocS(0)+1][nowBlock.getBlockLocG(0)]!=0) movable=false;
        if(blockState[nowBlock.getBlockLocS(1)+1][nowBlock.getBlockLocG(1)]!=0) movable=false;
        if(blockState[nowBlock.getBlockLocS(2)+1][nowBlock.getBlockLocG(2)]!=0) movable=false;
        if(blockState[nowBlock.getBlockLocS(3)+1][nowBlock.getBlockLocG(3)]!=0) movable=false;

        if(movable){
            for(int a=0;a<4;a++){
                saveLocG[a]=nowBlock.getBlockLocG(a);
                saveLocS[a]=nowBlock.getBlockLocS(a);
            }
            nowBlock.moveBlockDown(1);

            PrintMoveBlock PMB = new PrintMoveBlock(context);
            ((InGameActivity) context).addContentView(PMB,paramlinear);
        }
    }

    public void buttonSpaceEvent(){
        int offset=30;
        int temp=1;
        spaceEvent=true;

        while(true){
            if(blockState[nowBlock.getBlockLocS(0)+temp][nowBlock.getBlockLocG(0)]==0)
                temp++;
            else
                break;
        }
        if(offset>temp)
            offset=temp;
        temp=1;
        while(true){
            if(blockState[nowBlock.getBlockLocS(1)+temp][nowBlock.getBlockLocG(1)]==0) temp++;
            else break;
        }
        if(offset>temp) offset=temp;
        temp=1;
        while(true){
            if(blockState[nowBlock.getBlockLocS(2)+temp][nowBlock.getBlockLocG(2)]==0) temp++;
            else break;
        }
        if(offset>temp) offset=temp;
        temp=1;
        while(true){
            if(blockState[nowBlock.getBlockLocS(3)+temp][nowBlock.getBlockLocG(3)]==0) temp++;
            else break;
        }
        if(offset>temp) offset=temp;

        offset--;

        for(int a=0;a<4;a++){
            saveLocG[a]=nowBlock.getBlockLocG(a);
            saveLocS[a]=nowBlock.getBlockLocS(a);
        }
        nowBlock.moveBlockDown(offset);

        int fixNum = 10+nowBlock.getBlockKind();
        for(int a=0;a<4;a++){
            blockState[nowBlock.getBlockLocS(a)][nowBlock.getBlockLocG(a)]=fixNum;
        }

        PrintMoveBlock PMB = new PrintMoveBlock(context);
        ((InGameActivity) context).addContentView(PMB,paramlinear);
    }

    public void buttonSaveEvent(){
        if(savePress){
            for(int a=0;a<4;a++){
                saveLocG[a]=nowBlock.getBlockLocG(a);
                saveLocS[a]=nowBlock.getBlockLocS(a);
            }

            if(hasSave){
                TetrisBlock blockTemp = saveBlock;
                String strTemp = strColor3;

                saveBlock=nowBlock;
                strColor3=strColor;

                nowBlock=blockTemp;
                strColor=strTemp;

                pnt.setColor(Color.parseColor(strColor));
                pnt3.setColor(Color.parseColor(strColor3));

                PrintNewBlock PNB = new PrintNewBlock(context,2);
                ((InGameActivity) context).addContentView(PNB,paramlinear);

                saveBlock.setBlockBasicLoc();
                savePress=false;
            }
            else {
                saveBlock = nowBlock;
                strColor3=strColor;

                pnt3.setColor(Color.parseColor(strColor3));

                makeBlockMode=1;
                makeNewBlock();
                makeBlockMode=0;

                saveBlock.setBlockBasicLoc();
                hasSave=true;
                savePress=false;
            }
        }
    }

    // 블록 회전 , 스압주의(700줄)
    public boolean checkRotatable(){
        int[] gTemp = new int[4];
        int[] sTemp = new int[4];
        for(int a=0;a<4;a++){
            gTemp[a] = nowBlock.getBlockLocG(a);
            sTemp[a] = nowBlock.getBlockLocS(a);
        }

        switch (nowBlock.getBlockKind()){
            case 0:                                         // 코딩하기 힘드므로, 2,3번 블록 기준 회전만 가능하도록 하자..
                if(nowBlock.getRotateState()==0){
                    if(blockState[sTemp[1]+1][gTemp[1]]!=0 && blockState[sTemp[2]+1][gTemp[2]]!=0){     //바로밑이 꽉차있음!
                        if(blockState[sTemp[1]-3][gTemp[1]]==0 && blockState[sTemp[1]-2][gTemp[1]]==0 && blockState[sTemp[1]-1][gTemp[1]]==0){
                            for(int a=0;a<4;a++){
                                rotateLocG[a]=gTemp[1];
                                rotateLocS[a]=sTemp[1]-3+a;
                            }
                            return true;
                        }
                        if(blockState[sTemp[2]-3][gTemp[2]]==0 && blockState[sTemp[2]-2][gTemp[2]]==0 && blockState[sTemp[2]-1][gTemp[2]]==0){
                            for(int a=0;a<4;a++){
                                rotateLocG[a]=gTemp[2];
                                rotateLocS[a]=sTemp[2]-3+a;
                            }
                            return true;
                        }
                        return false;
                    }
                    if(blockState[sTemp[1]-1][gTemp[1]]!=0 && blockState[sTemp[2]-1][gTemp[2]]!=0){     //바로위가 꽉차있음!
                        if(blockState[sTemp[1]+3][gTemp[1]]==0 && blockState[sTemp[1]+2][gTemp[1]]==0 && blockState[sTemp[1]+1][gTemp[1]]==0){
                            for(int a=0;a<4;a++){
                                rotateLocG[a]=gTemp[1];
                                rotateLocS[a]=sTemp[1]+a;
                            }
                            return true;
                        }
                        if(blockState[sTemp[2]+3][gTemp[2]]==0 && blockState[sTemp[2]+2][gTemp[2]]==0 && blockState[sTemp[2]+1][gTemp[2]]==0){
                            for(int a=0;a<4;a++){
                                rotateLocG[a]=gTemp[2];
                                rotateLocS[a]=sTemp[2]+a;
                            }
                            return true;
                        }
                        return false;
                    }
                    if(blockState[sTemp[1]+2][gTemp[1]]!=0 && blockState[sTemp[2]+2][gTemp[2]]!=0){
                        if(blockState[sTemp[1]-2][gTemp[1]]==0 && blockState[sTemp[1]-1][gTemp[1]]==0 && blockState[sTemp[1]+1][gTemp[1]]==0){
                            for(int a=0;a<4;a++){
                                rotateLocG[a]=gTemp[1];
                                rotateLocS[a]=sTemp[1]-2+a;
                            }
                            return true;
                        }
                        if(blockState[sTemp[2]-2][gTemp[2]]==0 && blockState[sTemp[2]-1][gTemp[2]]==0 && blockState[sTemp[2]+1][gTemp[2]]==0){
                            for(int a=0;a<4;a++){
                                rotateLocG[a]=gTemp[2];
                                rotateLocS[a]=sTemp[2]-2+a;
                            }
                            return true;
                        }
                        return false;
                    }
                    if(blockState[sTemp[1]-1][gTemp[1]]==0 && blockState[sTemp[1]+1][gTemp[1]]==0 && blockState[sTemp[1]+2][gTemp[1]]==0){
                        for(int a=0;a<4;a++){
                            rotateLocG[a]=gTemp[1];
                            rotateLocS[a]=sTemp[1]-1+a;
                        }
                        return true;
                    }
                    if(blockState[sTemp[2]-1][gTemp[2]]==0 && blockState[sTemp[2]+1][gTemp[2]]==0 && blockState[sTemp[2]+2][gTemp[2]]==0){
                        for(int a=0;a<4;a++){
                            rotateLocG[a]=gTemp[2];
                            rotateLocS[a]=sTemp[2]-1+a;
                        }
                        return true;
                    }
                    return false;
                }
                else{
                    if(blockState[sTemp[1]][gTemp[1]+1]!=0 && blockState[sTemp[2]][gTemp[2]+1]!=0){     //바로 오른쪽이 꽉차있음!
                        if(blockState[sTemp[1]][gTemp[1]-3]==0 && blockState[sTemp[1]][gTemp[1]-2]==0 && blockState[sTemp[1]][gTemp[1]-1]==0){
                            for(int a=0;a<4;a++){
                                rotateLocG[a]=gTemp[1]-3+a;
                                rotateLocS[a]=sTemp[1];
                            }
                            return true;
                        }
                        if(blockState[sTemp[2]][gTemp[2]-3]==0 && blockState[sTemp[2]][gTemp[2]-2]==0 && blockState[sTemp[2]][gTemp[2]-1]==0){
                            for(int a=0;a<4;a++){
                                rotateLocG[a]=gTemp[2]-3+a;
                                rotateLocS[a]=sTemp[2];
                            }
                            return true;
                        }
                        return false;
                    }
                    if(blockState[sTemp[1]][gTemp[1]-1]!=0 && blockState[sTemp[2]][gTemp[2]-1]!=0){     //바로 왼쪽이 꽉차있음!
                        if(blockState[sTemp[1]][gTemp[1]+3]==0 && blockState[sTemp[1]][gTemp[1]+2]==0 && blockState[sTemp[1]][gTemp[1]+1]==0){
                            for(int a=0;a<4;a++){
                                rotateLocG[a]=gTemp[1]+a;
                                rotateLocS[a]=sTemp[1];
                            }
                            return true;
                        }
                        if(blockState[sTemp[2]][gTemp[2]+3]==0 && blockState[sTemp[2]][gTemp[2]+2]==0 && blockState[sTemp[2]][gTemp[2]+1]==0){
                            for(int a=0;a<4;a++){
                                rotateLocG[a]=gTemp[2]+a;
                                rotateLocS[a]=sTemp[2];
                            }
                            return true;
                        }
                        return false;
                    }
                    if(blockState[sTemp[1]][gTemp[1]+2]!=0 && blockState[sTemp[2]][gTemp[2]+2]!=0){
                        if(blockState[sTemp[1]][gTemp[1]-2]==0 && blockState[sTemp[1]][gTemp[1]-1]==0 && blockState[sTemp[1]][gTemp[1]+1]==0){
                            for(int a=0;a<4;a++){
                                rotateLocG[a]=gTemp[1]-2+a;
                                rotateLocS[a]=sTemp[1];
                            }
                            return true;
                        }
                        if(blockState[sTemp[2]][gTemp[2]-2]==0 && blockState[sTemp[2]][gTemp[2]-1]==0 && blockState[sTemp[2]][gTemp[2]+1]==0){
                            for(int a=0;a<4;a++){
                                rotateLocG[a]=gTemp[2]-2+a;
                                rotateLocS[a]=sTemp[2];
                            }
                            return true;
                        }
                    }
                    if(blockState[sTemp[1]][gTemp[1]-1]==0 && blockState[sTemp[1]][gTemp[1]+1]==0 && blockState[sTemp[1]][gTemp[1]+2]==0){
                        for(int a=0;a<4;a++){
                            rotateLocG[a]=gTemp[1]-1+a;
                            rotateLocS[a]=sTemp[1];
                        }
                        return true;
                    }
                    if(blockState[sTemp[2]][gTemp[2]-1]==0 && blockState[sTemp[2]][gTemp[2]+1]==0 && blockState[sTemp[2]][gTemp[2]+2]==0){
                        for(int a=0;a<4;a++){
                            rotateLocG[a]=gTemp[2]-1+a;
                            rotateLocS[a]=sTemp[2];
                        }
                        return true;
                    }
                    return false;
                }
            case 1:
                return false;
            case 2:
                if(nowBlock.getRotateState()==0){
                    if(blockState[sTemp[2]-1][gTemp[2]]==0 && blockState[sTemp[2]+1][gTemp[2]]==0 && blockState[sTemp[3]-1][gTemp[3]]==0){      // 정상 회전 가능
                        rotateLocG[0]=gTemp[3];
                        rotateLocS[0]=sTemp[3]-1;
                        for(int a=1;a<4;a++){
                            rotateLocG[a]=gTemp[2];
                            rotateLocS[a]=sTemp[2]-2+a;
                        }
                        return true;
                    }
                    if(blockState[sTemp[2]-1][gTemp[2]]!=0 && blockState[sTemp[2]+1][gTemp[2]]==0 && blockState[sTemp[2]+2][gTemp[2]]==0){
                        rotateLocG[0]=gTemp[3];
                        rotateLocS[0]=sTemp[3];
                        for(int a=1;a<4;a++){
                            rotateLocG[a]=gTemp[2];
                            rotateLocS[a]=sTemp[2]-1+a;
                        }
                        return true;
                    }
                    if(blockState[sTemp[2]+1][gTemp[2]]!=0){
                        if(blockState[sTemp[2]-1][gTemp[2]]==0 && blockState[sTemp[2]-2][gTemp[2]]==0 && blockState[sTemp[3]-2][gTemp[3]]==0){
                            rotateLocG[0]=gTemp[3];
                            rotateLocS[0]=sTemp[3]-2;
                            for(int a=1;a<4;a++){
                                rotateLocG[a]=gTemp[2];
                                rotateLocS[a]=sTemp[2]-3+a;
                            }
                            return true;
                        }
                        if(blockState[sTemp[1]+1][gTemp[1]]==0 && blockState[sTemp[2]-1][gTemp[2]]==0){
                            rotateLocG[0]=gTemp[2];
                            rotateLocS[0]=sTemp[2]-1;
                            for(int a=1;a<4;a++){
                                rotateLocG[a]=gTemp[1];
                                rotateLocS[a]=sTemp[1]-2+a;
                            }
                            return true;
                        }
                        if(blockState[sTemp[1]+1][gTemp[1]]==0 && blockState[sTemp[1]+2][gTemp[1]]==0){
                            rotateLocG[0]=gTemp[2];
                            rotateLocS[0]=sTemp[2];
                            for(int a=1;a<4;a++){
                                rotateLocG[a]=gTemp[1];
                                rotateLocS[a]=sTemp[1]-1+a;
                            }
                            return true;
                        }
                    }
                    return false;
                }
                else if(nowBlock.getRotateState()==1){
                    if(blockState[sTemp[2]][gTemp[2]-1]==0 && blockState[sTemp[2]][gTemp[2]+1]==0 && blockState[sTemp[3]][gTemp[3]+1]==0){      // 정상 회전 가능
                        rotateLocG[0]=gTemp[3]+1;
                        rotateLocS[0]=sTemp[3];
                        for(int a=1;a<4;a++){
                            rotateLocG[a]=gTemp[2]+2-a;
                            rotateLocS[a]=sTemp[2];
                        }
                        return true;
                    }
                    if(blockState[sTemp[2]][gTemp[2]+1]!=0 && blockState[sTemp[2]][gTemp[2]-1]==0 && blockState[sTemp[2]][gTemp[2]-2]==0){
                        rotateLocG[0]=gTemp[3];
                        rotateLocS[0]=sTemp[3];
                        for(int a=1;a<4;a++){
                            rotateLocG[a]=gTemp[2]+1-a;
                            rotateLocS[a]=sTemp[2];
                        }
                        return true;
                    }
                    if(blockState[sTemp[2]][gTemp[2]-1]!=0){
                        if(blockState[sTemp[2]][gTemp[2]+1]==0 && blockState[sTemp[2]][gTemp[2]+2]==0 && blockState[sTemp[3]][gTemp[3]+2]==0){
                            rotateLocG[0]=gTemp[3]+2;
                            rotateLocS[0]=sTemp[3];
                            for(int a=1;a<4;a++){
                                rotateLocG[a]=gTemp[2]+3-a;
                                rotateLocS[a]=sTemp[2];
                            }
                            return true;
                        }
                        if(blockState[sTemp[1]][gTemp[1]-1]==0 && blockState[sTemp[2]][gTemp[2]+1]==0){
                            rotateLocG[0]=gTemp[2]+1;
                            rotateLocS[0]=sTemp[2];
                            for(int a=1;a<4;a++){
                                rotateLocG[a]=gTemp[1]+2-a;
                                rotateLocS[a]=sTemp[1];
                            }
                            return true;
                        }
                        if(blockState[sTemp[1]][gTemp[1]-1]==0 && blockState[sTemp[1]][gTemp[1]-2]==0){
                            rotateLocG[0]=gTemp[2];
                            rotateLocS[0]=sTemp[2];
                            for(int a=1;a<4;a++){
                                rotateLocG[a]=gTemp[1]+1-a;
                                rotateLocS[a]=sTemp[1];
                            }
                            return true;
                        }
                    }
                    return false;
                }
                else if(nowBlock.getRotateState()==2){
                    if(blockState[sTemp[2]-1][gTemp[2]]==0 && blockState[sTemp[2]+1][gTemp[2]]==0 && blockState[sTemp[3]+1][gTemp[3]]==0){      // 정상 회전 가능
                        rotateLocG[0]=gTemp[3];
                        rotateLocS[0]=sTemp[3]+1;
                        for(int a=1;a<4;a++){
                            rotateLocG[a]=gTemp[2];
                            rotateLocS[a]=sTemp[2]+2-a;
                        }
                        return true;
                    }
                    if(blockState[sTemp[2]+1][gTemp[2]]!=0 && blockState[sTemp[2]-1][gTemp[2]]==0 && blockState[sTemp[2]-2][gTemp[2]]==0){
                        rotateLocG[0]=gTemp[3];
                        rotateLocS[0]=sTemp[3];
                        for(int a=1;a<4;a++){
                            rotateLocG[a]=gTemp[2];
                            rotateLocS[a]=sTemp[2]+1-a;
                        }
                        return true;
                    }
                    if(blockState[sTemp[2]-1][gTemp[2]]!=0){
                        if(blockState[sTemp[2]+1][gTemp[2]]==0 && blockState[sTemp[2]+2][gTemp[2]]==0 && blockState[sTemp[3]+2][gTemp[3]]==0){
                            rotateLocG[0]=gTemp[3];
                            rotateLocS[0]=sTemp[3]+2;
                            for(int a=1;a<4;a++){
                                rotateLocG[a]=gTemp[2];
                                rotateLocS[a]=sTemp[2]+3-a;
                            }
                            return true;
                        }
                        if(blockState[sTemp[1]-1][gTemp[1]]==0 && blockState[sTemp[2]+1][gTemp[2]]==0){
                            rotateLocG[0]=gTemp[2];
                            rotateLocS[0]=sTemp[2]+1;
                            for(int a=1;a<4;a++){
                                rotateLocG[a]=gTemp[1];
                                rotateLocS[a]=sTemp[1]+2-a;
                            }
                            return true;
                        }
                        if(blockState[sTemp[1]-1][gTemp[1]]==0 && blockState[sTemp[1]-2][gTemp[1]]==0){
                            rotateLocG[0]=gTemp[2];
                            rotateLocS[0]=sTemp[2];
                            for(int a=1;a<4;a++){
                                rotateLocG[a]=gTemp[1];
                                rotateLocS[a]=sTemp[1]+1-a;
                            }
                            return true;
                        }
                    }
                    return false;
                }
                else{
                    if(blockState[sTemp[2]][gTemp[2]+1]==0 && blockState[sTemp[2]][gTemp[2]-1]==0 && blockState[sTemp[3]][gTemp[3]-1]==0){      // 정상 회전 가능
                        rotateLocG[0]=gTemp[3]-1;
                        rotateLocS[0]=sTemp[3];
                        for(int a=1;a<4;a++){
                            rotateLocG[a]=gTemp[2]-2+a;
                            rotateLocS[a]=sTemp[2];
                        }
                        return true;
                    }
                    if(blockState[sTemp[2]][gTemp[2]-1]!=0 && blockState[sTemp[2]][gTemp[2]+1]==0 && blockState[sTemp[2]][gTemp[2]+2]==0){
                        rotateLocG[0]=gTemp[3];
                        rotateLocS[0]=sTemp[3];
                        for(int a=1;a<4;a++){
                            rotateLocG[a]=gTemp[2]-1+a;
                            rotateLocS[a]=sTemp[2];
                        }
                        return true;
                    }
                    if(blockState[sTemp[2]][gTemp[2]+1]!=0){
                        if(blockState[sTemp[2]][gTemp[2]-1]==0 && blockState[sTemp[2]][gTemp[2]-2]==0 && blockState[sTemp[3]][gTemp[3]-2]==0){
                            rotateLocG[0]=gTemp[3]-2;
                            rotateLocS[0]=sTemp[3];
                            for(int a=1;a<4;a++){
                                rotateLocG[a]=gTemp[2]-3+a;
                                rotateLocS[a]=sTemp[2];
                            }
                            return true;
                        }
                        if(blockState[sTemp[1]][gTemp[1]+1]==0 && blockState[sTemp[2]][gTemp[2]-1]==0){
                            rotateLocG[0]=gTemp[2]-1;
                            rotateLocS[0]=sTemp[2];
                            for(int a=1;a<4;a++){
                                rotateLocG[a]=gTemp[1]-2+a;
                                rotateLocS[a]=sTemp[1];
                            }
                            return true;
                        }
                        if(blockState[sTemp[1]][gTemp[1]+1]==0 && blockState[sTemp[1]][gTemp[1]+2]==0){
                            rotateLocG[0]=gTemp[2];
                            rotateLocS[0]=sTemp[2];
                            for(int a=1;a<4;a++){
                                rotateLocG[a]=gTemp[1]-1+a;
                                rotateLocS[a]=sTemp[1];
                            }
                            return true;
                        }
                    }
                    return false;
                }
            case 3:
                if(nowBlock.getRotateState()==0){
                    if(blockState[sTemp[2]-1][gTemp[2]]==0 && blockState[sTemp[2]+1][gTemp[2]]==0 && blockState[sTemp[1]+1][gTemp[1]]==0){      // 정상 회전 가능
                        rotateLocG[0]=gTemp[1];
                        rotateLocS[0]=sTemp[1]+1;
                        for(int a=1;a<4;a++){
                            rotateLocG[a]=gTemp[2];
                            rotateLocS[a]=sTemp[2]+2-a;
                        }
                        return true;
                    }
                    if(blockState[sTemp[2]-1][gTemp[2]]==0 && blockState[sTemp[2]-2][gTemp[2]]==0){
                        rotateLocG[0]=gTemp[1];
                        rotateLocS[0]=sTemp[1];
                        for(int a=1;a<4;a++){
                            rotateLocG[a]=gTemp[2];
                            rotateLocS[a]=sTemp[2]+1-a;
                        }
                        return true;
                    }
                    return false;
                }
                else if(nowBlock.getRotateState()==1){
                    if(blockState[sTemp[2]][gTemp[2]+1]==0 && blockState[sTemp[2]][gTemp[2]-1]==0 && blockState[sTemp[1]][gTemp[1]-1]==0){      // 정상 회전 가능
                        rotateLocG[0]=gTemp[1]-1;
                        rotateLocS[0]=sTemp[1];
                        for(int a=1;a<4;a++){
                            rotateLocG[a]=gTemp[2]-2+a;
                            rotateLocS[a]=sTemp[2];
                        }
                        return true;
                    }
                    if(blockState[sTemp[2]][gTemp[2]+1]==0 && blockState[sTemp[2]][gTemp[2]+2]==0){
                        rotateLocG[0]=gTemp[1];
                        rotateLocS[0]=sTemp[1];
                        for(int a=1;a<4;a++){
                            rotateLocG[a]=gTemp[2]-1+a;
                            rotateLocS[a]=sTemp[2];
                        }
                        return true;
                    }
                    return false;
                }
                else if(nowBlock.getRotateState()==2){
                    if(blockState[sTemp[2]+1][gTemp[2]]==0 && blockState[sTemp[2]-1][gTemp[2]]==0 && blockState[sTemp[1]-1][gTemp[1]]==0){      // 정상 회전 가능
                        rotateLocG[0]=gTemp[1];
                        rotateLocS[0]=sTemp[1]-1;
                        for(int a=1;a<4;a++){
                            rotateLocG[a]=gTemp[2];
                            rotateLocS[a]=sTemp[2]-2+a;
                        }
                        return true;
                    }
                    if(blockState[sTemp[2]+1][gTemp[2]]==0 && blockState[sTemp[2]+2][gTemp[2]]==0){
                        rotateLocG[0]=gTemp[1];
                        rotateLocS[0]=sTemp[1];
                        for(int a=1;a<4;a++){
                            rotateLocG[a]=gTemp[2];
                            rotateLocS[a]=sTemp[2]-1+a;
                        }
                        return true;
                    }
                    return false;
                }
                else{
                    if(blockState[sTemp[2]][gTemp[2]-1]==0 && blockState[sTemp[2]][gTemp[2]+1]==0 && blockState[sTemp[1]][gTemp[1]+1]==0){      // 정상 회전 가능
                        rotateLocG[0]=gTemp[1]+1;
                        rotateLocS[0]=sTemp[1];
                        for(int a=1;a<4;a++){
                            rotateLocG[a]=gTemp[2]+2-a;
                            rotateLocS[a]=sTemp[2];
                        }
                        return true;
                    }
                    if(blockState[sTemp[2]][gTemp[2]-1]==0 && blockState[sTemp[2]][gTemp[2]-2]==0){
                        rotateLocG[0]=gTemp[1];
                        rotateLocS[0]=sTemp[1];
                        for(int a=1;a<4;a++){
                            rotateLocG[a]=gTemp[2]+1-a;
                            rotateLocS[a]=sTemp[2];
                        }
                        return true;
                    }
                    return false;
                }
            case 4:
                if(nowBlock.getRotateState()==0){
                    if(blockState[sTemp[1]][gTemp[1]+1]==0 && blockState[sTemp[1]-1][gTemp[1]+1]==0){           // 정상 회전
                        rotateLocG[0]=gTemp[1]+1;
                        rotateLocS[0]=sTemp[1]-1;
                        rotateLocG[1]=gTemp[1]+1;
                        rotateLocS[1]=sTemp[1];
                        rotateLocG[2]=gTemp[1];
                        rotateLocS[2]=sTemp[1];
                        rotateLocG[3]=gTemp[2];
                        rotateLocS[3]=sTemp[2];
                        return true;
                    }
                    if(blockState[sTemp[1]-1][gTemp[1]]==0 && blockState[sTemp[2]][gTemp[2]-1]==0){
                        rotateLocG[0]=gTemp[1];
                        rotateLocS[0]=sTemp[1]-1;
                        rotateLocG[1]=gTemp[1];
                        rotateLocS[1]=sTemp[1];
                        rotateLocG[2]=gTemp[0];
                        rotateLocS[2]=sTemp[0];
                        rotateLocG[3]=gTemp[0];
                        rotateLocS[3]=sTemp[0]+1;
                        return true;
                    }
                    if(blockState[sTemp[2]][gTemp[2]-1]==0 && blockState[sTemp[2]+1][gTemp[2]-1]==0){
                        rotateLocG[0]=gTemp[1];
                        rotateLocS[0]=sTemp[1];
                        rotateLocG[1]=gTemp[2];
                        rotateLocS[1]=sTemp[2];
                        rotateLocG[2]=gTemp[2]-1;
                        rotateLocS[2]=sTemp[2];
                        rotateLocG[3]=gTemp[2]-1;
                        rotateLocS[3]=sTemp[2]+1;
                        return true;
                    }
                    return false;
                }
                else{
                    if(blockState[sTemp[2]][gTemp[2]-1]==0 && blockState[sTemp[3]][gTemp[3]+1]==0){           // 정상 회전
                        rotateLocG[0]=gTemp[2]-1;
                        rotateLocS[0]=sTemp[2];
                        rotateLocG[1]=gTemp[2];
                        rotateLocS[1]=sTemp[2];
                        rotateLocG[2]=gTemp[3];
                        rotateLocS[2]=sTemp[3];
                        rotateLocG[3]=gTemp[3]+1;
                        rotateLocS[3]=sTemp[3];
                        return true;
                    }
                    if(blockState[sTemp[3]][gTemp[3]+1]==0 && blockState[sTemp[3]][gTemp[3]+2]==0){
                        rotateLocG[0]=gTemp[2];
                        rotateLocS[0]=sTemp[2];
                        rotateLocG[1]=gTemp[1];
                        rotateLocS[1]=sTemp[1];
                        rotateLocG[2]=gTemp[3]+1;
                        rotateLocS[2]=sTemp[3];
                        rotateLocG[3]=gTemp[3]+2;
                        rotateLocS[3]=sTemp[3];
                        return true;
                    }
                    if(blockState[sTemp[1]][gTemp[1]+1]==0 && blockState[sTemp[0]][gTemp[0]-1]==0){
                        rotateLocG[0]=gTemp[0]-1;
                        rotateLocS[0]=sTemp[0];
                        rotateLocG[1]=gTemp[0];
                        rotateLocS[1]=sTemp[0];
                        rotateLocG[2]=gTemp[1];
                        rotateLocS[2]=sTemp[1];
                        rotateLocG[3]=gTemp[1]+1;
                        rotateLocS[3]=sTemp[1];
                        return true;
                    }
                    return false;
                }
            case 5:
                if(nowBlock.getRotateState()==0){
                    if(blockState[sTemp[1]][gTemp[1]-1]==0 && blockState[sTemp[1]-1][gTemp[1]-1]==0){           // 정상 회전
                        rotateLocG[0]=gTemp[1]-1;
                        rotateLocS[0]=sTemp[1]-1;
                        rotateLocG[1]=gTemp[1]-1;
                        rotateLocS[1]=sTemp[1];
                        rotateLocG[2]=gTemp[1];
                        rotateLocS[2]=sTemp[1];
                        rotateLocG[3]=gTemp[2];
                        rotateLocS[3]=sTemp[2];
                        return true;
                    }
                    if(blockState[sTemp[1]-1][gTemp[1]]==0 && blockState[sTemp[2]][gTemp[2]+1]==0){
                        rotateLocG[0]=gTemp[1];
                        rotateLocS[0]=sTemp[1]-1;
                        rotateLocG[1]=gTemp[1];
                        rotateLocS[1]=sTemp[1];
                        rotateLocG[2]=gTemp[0];
                        rotateLocS[2]=sTemp[0];
                        rotateLocG[3]=gTemp[0];
                        rotateLocS[3]=sTemp[0]+1;
                        return true;
                    }
                    if(blockState[sTemp[2]][gTemp[2]+1]==0 && blockState[sTemp[2]+1][gTemp[2]+1]==0){
                        rotateLocG[0]=gTemp[1];
                        rotateLocS[0]=sTemp[1];
                        rotateLocG[1]=gTemp[2];
                        rotateLocS[1]=sTemp[2];
                        rotateLocG[2]=gTemp[2]+1;
                        rotateLocS[2]=sTemp[2];
                        rotateLocG[3]=gTemp[2]+1;
                        rotateLocS[3]=sTemp[2]+1;
                        return true;
                    }
                    return false;
                }
                else{
                    if(blockState[sTemp[2]][gTemp[2]+1]==0 && blockState[sTemp[3]][gTemp[3]-1]==0){           // 정상 회전
                        rotateLocG[0]=gTemp[2]+1;
                        rotateLocS[0]=sTemp[2];
                        rotateLocG[1]=gTemp[2];
                        rotateLocS[1]=sTemp[2];
                        rotateLocG[2]=gTemp[3];
                        rotateLocS[2]=sTemp[3];
                        rotateLocG[3]=gTemp[3]-1;
                        rotateLocS[3]=sTemp[3];
                        return true;
                    }
                    if(blockState[sTemp[3]][gTemp[3]-1]==0 && blockState[sTemp[3]][gTemp[3]-2]==0){
                        rotateLocG[0]=gTemp[2];
                        rotateLocS[0]=sTemp[2];
                        rotateLocG[1]=gTemp[1];
                        rotateLocS[1]=sTemp[1];
                        rotateLocG[2]=gTemp[3]-1;
                        rotateLocS[2]=sTemp[3];
                        rotateLocG[3]=gTemp[3]-2;
                        rotateLocS[3]=sTemp[3];
                        return true;
                    }
                    if(blockState[sTemp[1]][gTemp[1]-1]==0 && blockState[sTemp[0]][gTemp[0]+1]==0){
                        rotateLocG[0]=gTemp[0]+1;
                        rotateLocS[0]=sTemp[0];
                        rotateLocG[1]=gTemp[0];
                        rotateLocS[1]=sTemp[0];
                        rotateLocG[2]=gTemp[1];
                        rotateLocS[2]=sTemp[1];
                        rotateLocG[3]=gTemp[1]-1;
                        rotateLocS[3]=sTemp[1];
                        return true;
                    }
                    return false;
                }
            case 6:
                if(nowBlock.getRotateState()==0){
                    if(blockState[sTemp[2]+1][gTemp[2]]==0){            //정상 회전
                        rotateLocG[0]=gTemp[3];
                        rotateLocS[0]=sTemp[3];
                        for(int a=1;a<4;a++){
                            rotateLocG[a]=gTemp[0];
                            rotateLocS[a]=sTemp[0]-1+a;
                        }
                        return true;
                    }
                    if(blockState[sTemp[0]][gTemp[0]+1]==0 && blockState[sTemp[0]-1][gTemp[0]]==0){
                        rotateLocG[0]=gTemp[0]+1;
                        rotateLocS[0]=sTemp[0];
                        for(int a=1;a<4;a++){
                            rotateLocG[a]=gTemp[0];
                            rotateLocS[a]=sTemp[0]-2+a;
                        }
                        return true;
                    }
                    return false;
                }
                else if(nowBlock.getRotateState()==1){
                    if(blockState[sTemp[2]][gTemp[2]-1]==0){            //정상 회전
                        rotateLocG[0]=gTemp[3];
                        rotateLocS[0]=sTemp[3];
                        for(int a=1;a<4;a++){
                            rotateLocG[a]=gTemp[0]+1-a;
                            rotateLocS[a]=sTemp[0];
                        }
                        return true;
                    }
                    if(blockState[sTemp[0]+1][gTemp[0]]==0 && blockState[sTemp[0]][gTemp[0]+1]==0){
                        rotateLocG[0]=gTemp[0];
                        rotateLocS[0]=sTemp[0]+1;
                        for(int a=1;a<4;a++){
                            rotateLocG[a]=gTemp[0]+2-a;
                            rotateLocS[a]=sTemp[0];
                        }
                        return true;
                    }
                    return false;
                }
                else if(nowBlock.getRotateState()==2){
                    if(blockState[sTemp[2]-1][gTemp[2]]==0){            //정상 회전
                        rotateLocG[0]=gTemp[3];
                        rotateLocS[0]=sTemp[3];
                        for(int a=1;a<4;a++){
                            rotateLocG[a]=gTemp[0];
                            rotateLocS[a]=sTemp[0]+1-a;
                        }
                        return true;
                    }
                    if(blockState[sTemp[0]][gTemp[0]-1]==0 && blockState[sTemp[0]+1][gTemp[0]]==0){
                        rotateLocG[0]=gTemp[0]-1;
                        rotateLocS[0]=sTemp[0];
                        for(int a=1;a<4;a++){
                            rotateLocG[a]=gTemp[0];
                            rotateLocS[a]=sTemp[0]+2-a;
                        }
                        return true;
                    }
                    return false;
                }
                else{
                    if(blockState[sTemp[2]][gTemp[2]+1]==0){            //정상 회전
                        rotateLocG[0]=gTemp[3];
                        rotateLocS[0]=sTemp[3];
                        for(int a=1;a<4;a++){
                            rotateLocG[a]=gTemp[0]-1+a;
                            rotateLocS[a]=sTemp[0];
                        }
                        return true;
                    }
                    if(blockState[sTemp[0]-1][gTemp[0]]==0 && blockState[sTemp[0]][gTemp[0]-1]==0){
                        rotateLocG[0]=gTemp[0];
                        rotateLocS[0]=sTemp[0]-1;
                        for(int a=1;a<4;a++){
                            rotateLocG[a]=gTemp[0]-2+a;
                            rotateLocS[a]=sTemp[0];
                        }
                        return true;
                    }
                    return false;
                }
            default:
                break;
        }
        return false;
    }

    //true 이면 Fix 해야함, false면 Fix 안 해도 됨
    public boolean checkNeedFix(){
        switch (nowBlock.getBlockKind()){
            case 0:
                if(nowBlock.getRotateState()==0){
                    if(blockState[nowBlock.getBlockLocS(0)+1][nowBlock.getBlockLocG(0)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(1)+1][nowBlock.getBlockLocG(1)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(2)+1][nowBlock.getBlockLocG(2)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(3)+1][nowBlock.getBlockLocG(3)]!=0) return true;
                    return false;
                }
                else{
                    if(blockState[nowBlock.getBlockLocS(3)+1][nowBlock.getBlockLocG(3)]!=0) return true;
                    return false;
                }
            case 1:
                if(blockState[nowBlock.getBlockLocS(2)+1][nowBlock.getBlockLocG(2)]!=0) return true;
                if(blockState[nowBlock.getBlockLocS(3)+1][nowBlock.getBlockLocG(3)]!=0) return true;
                return false;
            case 2:
                if(nowBlock.getRotateState()==0){
                    if(blockState[nowBlock.getBlockLocS(1)+1][nowBlock.getBlockLocG(1)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(2)+1][nowBlock.getBlockLocG(2)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(3)+1][nowBlock.getBlockLocG(3)]!=0) return true;
                    return false;
                }
                else if(nowBlock.getRotateState()==1){
                    if(blockState[nowBlock.getBlockLocS(0)+1][nowBlock.getBlockLocG(0)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(3)+1][nowBlock.getBlockLocG(3)]!=0) return true;
                    return false;
                }
                else if(nowBlock.getRotateState()==2){
                    if(blockState[nowBlock.getBlockLocS(0)+1][nowBlock.getBlockLocG(0)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(2)+1][nowBlock.getBlockLocG(2)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(3)+1][nowBlock.getBlockLocG(3)]!=0) return true;
                    return false;
                }
                else{
                    if(blockState[nowBlock.getBlockLocS(0)+1][nowBlock.getBlockLocG(0)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(1)+1][nowBlock.getBlockLocG(1)]!=0) return true;
                    return false;
                }
            case 3:
                if(nowBlock.getRotateState()==0){
                    if(blockState[nowBlock.getBlockLocS(1)+1][nowBlock.getBlockLocG(1)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(2)+1][nowBlock.getBlockLocG(2)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(3)+1][nowBlock.getBlockLocG(3)]!=0) return true;
                    return false;
                }
                else if(nowBlock.getRotateState()==1){
                    if(blockState[nowBlock.getBlockLocS(0)+1][nowBlock.getBlockLocG(0)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(1)+1][nowBlock.getBlockLocG(1)]!=0) return true;
                    return false;
                }
                else if(nowBlock.getRotateState()==2){
                    if(blockState[nowBlock.getBlockLocS(0)+1][nowBlock.getBlockLocG(0)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(2)+1][nowBlock.getBlockLocG(2)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(3)+1][nowBlock.getBlockLocG(3)]!=0) return true;
                    return false;
                }
                else{
                    if(blockState[nowBlock.getBlockLocS(0)+1][nowBlock.getBlockLocG(0)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(3)+1][nowBlock.getBlockLocG(3)]!=0) return true;
                    return false;
                }
            case 4:
                if(nowBlock.getRotateState()==0){
                    if(blockState[nowBlock.getBlockLocS(0)+1][nowBlock.getBlockLocG(0)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(2)+1][nowBlock.getBlockLocG(2)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(3)+1][nowBlock.getBlockLocG(3)]!=0) return true;
                    return false;
                }
                else{
                    if(blockState[nowBlock.getBlockLocS(1)+1][nowBlock.getBlockLocG(1)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(3)+1][nowBlock.getBlockLocG(3)]!=0) return true;
                    return false;
                }
            case 5:
                if(nowBlock.getRotateState()==0){
                    if(blockState[nowBlock.getBlockLocS(0)+1][nowBlock.getBlockLocG(0)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(2)+1][nowBlock.getBlockLocG(2)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(3)+1][nowBlock.getBlockLocG(3)]!=0) return true;
                    return false;
                }
                else{
                    if(blockState[nowBlock.getBlockLocS(0)+1][nowBlock.getBlockLocG(0)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(2)+1][nowBlock.getBlockLocG(2)]!=0) return true;
                    return false;
                }
            case 6:
                if(nowBlock.getRotateState()==0){
                    if(blockState[nowBlock.getBlockLocS(1)+1][nowBlock.getBlockLocG(1)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(2)+1][nowBlock.getBlockLocG(2)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(3)+1][nowBlock.getBlockLocG(3)]!=0) return true;
                    return false;
                }
                else if(nowBlock.getRotateState()==1){
                    if(blockState[nowBlock.getBlockLocS(0)+1][nowBlock.getBlockLocG(0)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(3)+1][nowBlock.getBlockLocG(3)]!=0) return true;
                    return false;
                }
                else if(nowBlock.getRotateState()==2){
                    if(blockState[nowBlock.getBlockLocS(0)+1][nowBlock.getBlockLocG(0)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(1)+1][nowBlock.getBlockLocG(1)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(3)+1][nowBlock.getBlockLocG(3)]!=0) return true;
                    return false;
                }
                else{
                    if(blockState[nowBlock.getBlockLocS(0)+1][nowBlock.getBlockLocG(0)]!=0) return true;
                    if(blockState[nowBlock.getBlockLocS(1)+1][nowBlock.getBlockLocG(1)]!=0) return true;
                    return false;
                }
            default:
                return false;
        }
    }

    public void completeRow(){
        int checkNum=0;
        int[] completeLoc = new int[20];
        int completeCount=0;

        for(int a=1;a<21;a++){
            for(int b=1;b<11;b++){
                if(blockState[a][b]!=0) checkNum++;
            }
            if(checkNum==10){
                completeLoc[completeCount]=a;
                completeCount++;
            }
            checkNum=0;
        }
        if(completeCount!=0){
            giveScore(completeCount);
            levelUp();
            deleteRow(completeLoc, completeCount);
        }
        else{
            combo=0;
        }
    }

    public void deleteRow(int[] completeLoc, int count){
        int num=0;
        while(count>0){
            for(int a=completeLoc[num];a>1;a--){
                for(int b=1;b<11;b++){
                    blockState[a][b]=blockState[a-1][b];
                }
            }
            for(int b=1;b<11;b++){
                blockState[1][b]=0;
            }

            count--;
            num++;
        }

        PrintReNew PRN = new PrintReNew(context);
        ((InGameActivity) context).addContentView(PRN,paramlinear);
    }

    public void giveScore(int count){       // 1줄 당 100점 지급 , 콤보 당 0.5배씩 추가비율 증가
        int temp = (100*count)*(1+combo/2);
        gameScore+=temp;
        scoreForLevel+=temp;
        combo++;

        nowScore.setText("점수 : " + gameScore);
    }

    public void levelUp(){
        if(scoreForLevel>=1000){
            gameLevel++;
            scoreForLevel-=1000;

            if(gameLevel<=16){      // 1레벨부터 시작이니, 주기가 0.5초까지 짧아짐
                blockDownTime-=100;
            }
            else if (gameLevel<=31){         // 17레벨부터 이곳에 들어오니, 주기가 0.2초까지 짧아짐
                blockDownTime-=20;
            }
            else ;
            nowLevel.setText("Lv : "+gameLevel);
        }
    }

    public void gameOverCheck(){
        boolean check=true;
        switch (nowBlock.getBlockKind()){
            case 0:
                if(blockState[1][4]!=0 || blockState[1][5]!=0||blockState[1][6]!=0 || blockState[1][7]!=0) check=false;
                break;
            case 1:
                if(blockState[1][5]!=0 || blockState[1][6]!=0||blockState[2][5]!=0 || blockState[2][6]!=0) check=false;
                break;
            case 2:
                if(blockState[1][5]!=0 || blockState[2][5]!=0||blockState[2][6]!=0 || blockState[2][7]!=0) check=false;
                break;
            case 3:
                if(blockState[1][7]!=0 || blockState[2][5]!=0||blockState[2][6]!=0 || blockState[2][7]!=0) check=false;
                break;
            case 4:
                if(blockState[1][5] !=0 || blockState[1][6]!=0||blockState[2][6]!=0 || blockState[2][7]!=0) check=false;
                break;
            case 5:
                if(blockState[1][6]!=0 || blockState[1][7]!=0||blockState[2][5]!=0 || blockState[2][6]!=0) check=false;
                break;
            case 6:
                if(blockState[1][6]!=0 || blockState[2][5]!=0||blockState[2][6]!=0 || blockState[2][7]!=0) check=false;
                break;
            default:
                break;
        }

        if(!check){
            blockDownTime=10000000;
            TextView gameOver = new TextView(context);
            gameOver.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
            gameOver.setTextColor(Color.BLACK);
            gameOver.setPadding(70,750,0,0);
            gameOver.setTextSize(40);
            gameOver.setText("Image.GameOver");

            ((InGameActivity)context).addContentView(gameOver,paramlinear);
            gameState=1;
        }
    }

    class PrintReNew extends View {
        public PrintReNew(Context context) {
            super(context);
        }

        protected void onDraw(Canvas canvas) {
            float garo = (float)50;
            float sero = (float)190;
            float width = (float)67.6;
            float width2 = (float)66.6;
            float g;
            float s;
            Paint pntTemp = new Paint();
            String strColorTemp = new String();

            super.onDraw(canvas);

            for(int a=1;a<21;a++){
                s=sero+(a-1)*width;
                for(int b=1;b<11;b++){
                    g=garo+(b-1)*width;
                    if(blockState[a][b]==0){
                        pntTemp.setColor(Color.DKGRAY);
                    }
                    else{
                        switch(blockState[a][b]){
                            case 10:
                                strColorTemp="#00FFFF";
                                break;
                            case 11:
                                strColorTemp="#FFFF00";
                                break;
                            case 12:
                                strColorTemp="#0000FF";
                                break;
                            case 13:
                                strColorTemp="#FF8C00";
                                break;
                            case 14:
                                strColorTemp="#FF0000";
                                break;
                            case 15:
                                strColorTemp="#7CFC00";
                                break;
                            case 16:
                                strColorTemp="#8A2BE2";
                                break;
                            default:
                                break;
                        }
                        pntTemp.setColor(Color.parseColor(strColorTemp));
                    }
                    canvas.drawRect(g*garoR,s*seroR,(g+width2)*garoR,(s+width2)*seroR,pntTemp);
                }
            }
        }
    }

    public void fixBlock(){
        int fixNum = 10+nowBlock.getBlockKind();
        for(int a=0;a<4;a++){
            blockState[nowBlock.getBlockLocS(a)][nowBlock.getBlockLocG(a)]=fixNum;
        }
        PrintFixBlock PFB = new PrintFixBlock(context);
        ((InGameActivity) context).addContentView(PFB,paramlinear);
    }
}

