package onepercent.mobile.com.onepercent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class SettingActivity extends Activity implements View.OnClickListener{
    ImageView BACK;
    TextView Alarm,Alarmtext,notice,information1,version1,invite , reciveBox, sendBox;
    int PP =2;//2는 알림받는다는 것을 초기화
    int distance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //타이틀바없애기

        setContentView(R.layout.activity_setting);

        BACK=(ImageView) findViewById(R.id.back); //메인으로

        Alarm=(TextView) findViewById(R.id.alarm);// 알림
        invite=(TextView) findViewById(R.id.invite);// 초대하기
        Alarmtext =(TextView) findViewById(R.id.alarmtext);//
        notice =(TextView) findViewById(R.id.notice);
        information1 =(TextView) findViewById(R.id.information);
        version1=(TextView) findViewById(R.id.version);
        reciveBox=(TextView) findViewById(R.id.reciveBox);
        sendBox=(TextView) findViewById(R.id.sendBox);

        BACK.setOnClickListener(this);
        Alarm.setOnClickListener(this);
        invite.setOnClickListener(this);
        notice.setOnClickListener(this);
        version1.setOnClickListener(this);
        information1.setOnClickListener(this);
        reciveBox.setOnClickListener(this);
        sendBox.setOnClickListener(this);

        Intent intent = new Intent(this.getIntent());
        PP= intent.getIntExtra("P",0); //p는 알람에서 스위치 꺼짐했을때가 p가 1 켜져있을때가 p가 2

        if(PP==2)
        {
            Alarmtext.setText("알림 받음");
        }

        else if(PP==1)
        {
            Alarmtext.setText("받지 않음");
        }


    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch(v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.alarm:

                break;
            case R.id.invite :
                Toast.makeText(getApplication(), "초대하기", Toast.LENGTH_SHORT).show();
                Intent msg = new Intent(Intent.ACTION_SEND);
                msg.setType("text/plain");
                msg.putExtra(Intent.EXTRA_TEXT, "'CATSUP' 확인 - https://play.google.com/store/apps/details?id=sumus.mobile.newcatsup");
                startActivity(Intent.createChooser(msg, " 초대"));
                break;
            case R.id.version :
                break;
            case R.id.information :
                break;
            case R.id.sendBox:
                intent = new Intent(SettingActivity.this, LetterListActivity.class);
                startActivity(intent);
                break;
            case R.id.reciveBox:
//                intent = new Intent(SettingActivity.this, LetterListActivity.class);
//                startActivity(intent);
                break;
        }
    }
}