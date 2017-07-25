package com.example.studywithchurl;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button[] mybutton = new Button[12];
    TextView myview;
    String text="0";
    String savetext = "";
    boolean creatable = false;
    int finalvalue=0;
    int savevalue=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mybutton[0] = (Button) findViewById(R.id.button0);
        mybutton[1] = (Button) findViewById(R.id.button1);
        mybutton[2] = (Button) findViewById(R.id.button2);
        mybutton[3] = (Button) findViewById(R.id.button3);
        mybutton[4] = (Button) findViewById(R.id.button4);
        mybutton[5] = (Button) findViewById(R.id.button5);
        mybutton[6] = (Button) findViewById(R.id.button6);
        mybutton[7] = (Button) findViewById(R.id.button7);
        mybutton[8] = (Button) findViewById(R.id.button8);
        mybutton[9] = (Button) findViewById(R.id.button9);
        mybutton[10] = (Button) findViewById(R.id.button10);
        mybutton[11] = (Button) findViewById(R.id.button11);
        myview = (TextView) findViewById(R.id.textView4);

        myview.setText(text);
        for(int a=0;a<12;a++){
            mybutton[a].setOnClickListener(this);
        }
    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.button0 :
                if(!creatable)
                    break;
                else{
                    text+="0";
                    finalvalue *=10;
                    break;
                }
            case R.id.button1:
                if(!creatable)
                    text=savetext;
                text+="1";
                finalvalue*=10;
                finalvalue+=1;
                creatable=true;
                break;
            case R.id.button2:
                if(!creatable)
                    text=savetext;
                text+="2";
                finalvalue*=10;
                finalvalue+=2;
                creatable=true;
                break;
            case R.id.button3:
                if(!creatable)
                    text=savetext;
                text+="3";
                finalvalue*=10;
                finalvalue+=3;
                creatable=true;
                break;
            case R.id.button4:
                if(!creatable)
                    text=savetext;
                text+="4";
                finalvalue*=10;
                finalvalue+=4;
                creatable=true;
                break;
            case R.id.button5:
                if(!creatable)
                    text=savetext;
                text+="5";
                finalvalue*=10;
                finalvalue+=5;
                creatable=true;
                break;
            case R.id.button6:
                if(!creatable)
                    text=savetext;
                text+="6";
                finalvalue*=10;
                finalvalue+=6;
                creatable=true;
                break;
            case R.id.button7:
                if(!creatable)
                    text=savetext;
                text+="7";
                finalvalue*=10;
                finalvalue+=7;
                creatable=true;
                break;
            case R.id.button8:
                if(!creatable)
                    text=savetext;
                text+="8";
                finalvalue*=10;
                finalvalue+=8;
                creatable=true;
                break;
            case R.id.button9:
                if(!creatable)
                    text=savetext;
                text+="9";
                finalvalue*=10;
                finalvalue+=9;
                creatable=true;
                break;
            case R.id.button10:
                if(!creatable)
                    break;
                text+=" + ";
                savevalue+=finalvalue;
                finalvalue=0;
                savetext = text;
                creatable = false;
                break;
            case R.id.button11:
                savevalue += finalvalue;
                finalvalue=0;
                creatable = false;
                text=String.valueOf(savevalue);
                break;
            default:
                break;
        }

        myview.setText(text);
    }
}
