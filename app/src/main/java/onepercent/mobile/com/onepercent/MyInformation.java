package onepercent.mobile.com.onepercent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import onepercent.mobile.com.onepercent.Model.User;

public class MyInformation extends Activity {

    TextView alarmTV, Alarmtext;
    TextView installtime, closetime, accesstime;
    ImageView BACK1;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myinformation);
        User user = User.getInstance();
        BACK1=(ImageView) findViewById(R.id.alarmback); // alarmback은 뒤로가기 이미지

        installtime = (TextView)findViewById(R.id.installtime);
        accesstime = (TextView)findViewById(R.id.accesstime);
        closetime = (TextView)findViewById(R.id.closetime);

        installtime.setText(user.getInstall_date());
        accesstime.setText(user.getUser_date());
        closetime.setText(user.getClose_date());

        BACK1.setOnClickListener(new View.OnClickListener() { // 알림창에서 뒤로가기 버튼 눌렀을때
            @Override
            public void onClick(View v) {

                Intent intent1 = new Intent(getApplicationContext(), SettingActivity.class);
                Toast.makeText(getApplication(), "사용자정보 에서 뒤로가기", Toast.LENGTH_SHORT).show();
                startActivity(intent1);
                finish();
            }
        });
    }
}