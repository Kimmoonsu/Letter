package onepercent.mobile.com.onepercent;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class MyInformation extends BaseActivity {

    TextView alarmTV, Alarmtext;
    TextView installtime, closetime, accesstime;
    ImageView BACK1;
    String install_time, close_time, access_time;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myinformation);
        getTime();
        BACK1=(ImageView) findViewById(R.id.alarmback); // alarmback은 뒤로가기 이미지

        installtime = (TextView)findViewById(R.id.installtime);
        accesstime = (TextView)findViewById(R.id.accesstime);
        closetime = (TextView)findViewById(R.id.closetime);

        installtime.setText(""+install_time);
        accesstime.setText(""+access_time);
        closetime.setText(""+close_time);

        BACK1.setOnClickListener(new View.OnClickListener() { // 알림창에서 뒤로가기 버튼 눌렀을때
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // User time getter
    private void getTime(){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        install_time = pref.getString("install_time", "");
        access_time = pref.getString("access_time", "");
        close_time = pref.getString("close_time", "");
    }
}