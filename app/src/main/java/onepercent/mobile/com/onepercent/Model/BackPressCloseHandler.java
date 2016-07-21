package onepercent.mobile.com.onepercent.Model;

import android.app.Activity;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BackPressCloseHandler {

    private long backKeyPressedTime = 0;
    private Toast toast;

    private Activity activity;

    public BackPressCloseHandler(Activity context) {
        this.activity = context;
    }

    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = sdf.format(d).toString();
            User user = User.getInstance();
            user.setClose_date(date);
            String url = "http://52.78.88.51:8080/letter/closeUser.do";
            sendCloseUser(url, user.getUser_id(), user.getUser_name(), user.getUser_date(), date);
            activity.finish();
            toast.cancel();
        }
    }

    public void showGuide() {
        toast = Toast.makeText(activity,
                "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
        toast.show();
    }

    /************************* 종료 시간 전송***********************/
    public String sendCloseUser(String strurl, String id, String name, String user_date, String close_date)
    {
        String response_msg =  null;
        try {
            String data = URLEncoder.encode("id", "EUC-KR") + "=" + URLEncoder.encode(""+id, "EUC-KR");
            data += "&" + URLEncoder.encode("name", "EUC-KR") + "=" + URLEncoder.encode(""+name, "EUC-KR");
            data += "&" + URLEncoder.encode("access_date", "EUC-KR") + "=" + URLEncoder.encode(""+user_date, "EUC-KR");
            data += "&" + URLEncoder.encode("close_date", "EUC-KR") + "=" + URLEncoder.encode(""+close_date, "EUC-KR");

            URL url = new URL(strurl);
            URLConnection conn = url.openConnection();

            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

// Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
            String line;
            while ((line = rd.readLine()) != null) {
                if (line!="")
                {
                    response_msg = line;
                }

            }

            wr.close();
            rd.close();

        }
        catch (Exception e) {
        }


        return response_msg;
    }
    /******************************************************************/
}