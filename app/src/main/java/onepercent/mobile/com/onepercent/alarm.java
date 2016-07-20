package onepercent.mobile.com.onepercent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


public class alarm extends SettingActivity {
    @Override
    public void onBackPressed() { //뒤로가는버튼막음
        //super.onBackPressed();
    }


    ImageView BACK1;
    TextView alarmTV,Alarmtext;
    int p ;
    int K;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        alarmTV=(TextView) findViewById(R.id.alarmTV); //알림받기 or 받지않음 text 부분
        Switch sw = (Switch) findViewById(R.id.switch1); // 알림받기 switch버튼을 sw로 정함
        Switch sw2 =  (Switch) findViewById(R.id.switch2); // 무음으로 알림 switch 버튼을 sw2로
        Alarmtext =(TextView) findViewById(R.id.alarmtext);//activity_main의 알람상태 text 부분

        Intent intent2 = new Intent(this.getIntent());
        K= intent2.getIntExtra("PP",0);


       sw.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener(){
           public void onCheckedChanged(CompoundButton cb,  boolean isChecking){
               String str = String.valueOf(isChecking);// boolean-> String 변환

               if(isChecking==true)//스위치가 꺼졌을때
               {
                   Toast.makeText(getApplication(), "OFF", Toast.LENGTH_SHORT).show();
                   alarmTV.setText(" 받지 않음"); //text 받음으로 바뀜
                  // Alarmtext.setText("받지 않음"); // activity_main 즉 설정 메인 창에 알림 설정 상태를 받지않음 으로 바꿈
                   p=1; //1 일때 스위치 켜져있는거

               }
               else if(isChecking==false)//스위치가 켜졌을때
               {
                   Toast.makeText(getApplication(), "ON", Toast.LENGTH_SHORT).show();
                   alarmTV.setText(" 받음"); //text 받음으로 바뀜

                   p=2;//2일때 스위치 꺼져있는거


                   Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE); //진동소스
                   vibe.vibrate(500);

               }
               return ;

           }
       });

        /////////////////////////////////////////////////////////////////////////////////
        BACK1=(ImageView) findViewById(R.id.alarmback); // alarmback은 뒤로가기 이미지


        BACK1.setOnClickListener(new View.OnClickListener() { // 알림창에서 뒤로가기 버튼 눌렀을때
            @Override
            public void onClick(View v) {

                Intent intent1 = new Intent(getApplicationContext(), SettingActivity.class);
                Toast.makeText(getApplication(), "알람에서 뒤로가기", Toast.LENGTH_SHORT).show();
                intent1.putExtra("P", p);
                startActivity(intent1);

            }
        });

        /////////////////////////////////////////////////////////////////////////////////


        sw2.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener(){
            public void onCheckedChanged(CompoundButton cb, boolean isChecking){
                String str = String.valueOf(isChecking);// boolean-> String 변환

                if(isChecking)
                {
                    Toast.makeText(getApplication(), "무음OFF", Toast.LENGTH_SHORT).show();     //스위치가 꺼졌을때
                }
                else
                {
                    Toast.makeText(getApplication(), "무음ON", Toast.LENGTH_SHORT).show();  //스위치가 켜졌을때


                }
            }
        });




        //Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
     //  vibe.vibrate(500);



        ////////////////////////////////////////////////////////////////////////////////




    }


}