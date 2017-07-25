package com.example.myproject1_tetris;

/**
 * Created by 장건희 on 2017-07-12.
 */

public class TetrisBlock {
    private int blockKind;
    private int[] blockLocG = new int[4];
    private int[] blockLocS = new int[4];
    private int rotateAvailable;
    private int rotateState;

    public TetrisBlock(int blockKind){
        this.blockKind=blockKind;
        setBlockBasicLoc();
    }

    public void setBlockBasicLoc(){
        switch(blockKind){
            case 0:
                rotateState=0;
                rotateAvailable=2;
                blockLocG[0]=4;
                blockLocG[1]=5;
                blockLocG[2]=6;
                blockLocG[3]=7;
                blockLocS[0]=1;
                blockLocS[1]=1;
                blockLocS[2]=1;
                blockLocS[3]=1;
                break;
            case 1:
                rotateState=0;
                rotateAvailable=1;
                blockLocG[0]=5;
                blockLocG[1]=6;
                blockLocG[2]=5;
                blockLocG[3]=6;
                blockLocS[0]=1;
                blockLocS[1]=1;
                blockLocS[2]=2;
                blockLocS[3]=2;
                break;
            case 2:
                rotateState=0;
                rotateAvailable=4;
                blockLocG[0]=5;
                blockLocG[1]=5;
                blockLocG[2]=6;
                blockLocG[3]=7;
                blockLocS[0]=1;
                blockLocS[1]=2;
                blockLocS[2]=2;
                blockLocS[3]=2;
                break;
            case 3:
                rotateState=0;
                rotateAvailable=4;
                blockLocG[0]=7;
                blockLocG[1]=7;
                blockLocG[2]=6;
                blockLocG[3]=5;
                blockLocS[0]=1;
                blockLocS[1]=2;
                blockLocS[2]=2;
                blockLocS[3]=2;
                break;
            case 4:
                rotateState=0;
                rotateAvailable=2;
                blockLocG[0]=5;
                blockLocG[1]=6;
                blockLocG[2]=6;
                blockLocG[3]=7;
                blockLocS[0]=1;
                blockLocS[1]=1;
                blockLocS[2]=2;
                blockLocS[3]=2;
                break;
            case 5:
                rotateState=0;
                rotateAvailable=2;
                blockLocG[0]=7;
                blockLocG[1]=6;
                blockLocG[2]=6;
                blockLocG[3]=5;
                blockLocS[0]=1;
                blockLocS[1]=1;
                blockLocS[2]=2;
                blockLocS[3]=2;
                break;
            case 6:
                rotateState=0;
                rotateAvailable=4;
                blockLocG[0]=6;
                blockLocG[1]=5;
                blockLocG[2]=6;
                blockLocG[3]=7;
                blockLocS[0]=1;
                blockLocS[1]=2;
                blockLocS[2]=2;
                blockLocS[3]=2;
                break;
            default:
                break;
        }
    }

    public int getBlockKind(){
        return blockKind;
    }
    public int getBlockLocG(int index){
        return blockLocG[index];
    }
    public int getBlockLocS(int index){
        return blockLocS[index];
    }
    public int getRotateState(){
        return rotateState;
    }

    public void moveBlockSide(int direct){
        for(int a=0;a<4;a++){
            blockLocG[a]+=direct;
        }
    }
    public void moveBlockDown(int direct){
        for(int a=0;a<4;a++){
            blockLocS[a]+=direct;
        }
    }
    public void rotateBlock(int[] garo, int[] sero){
        for(int a=0;a<4;a++){
            blockLocG[a]=garo[a];
            blockLocS[a]=sero[a];
        }
        rotateState++;
        switch (blockKind){
            case 0:
                rotateState=rotateState%2;
                break;
            case 1:
                rotateState=rotateState%1;
                break;
            case 2:
                rotateState=rotateState%4;
                break;
            case 3:
                rotateState=rotateState%4;
                break;
            case 4:
                rotateState=rotateState%2;
                break;
            case 5:
                rotateState=rotateState%2;
                break;
            case 6:
                rotateState=rotateState%4;
                break;
            default:
                break;
        }
    }
}
