package onepercent.mobile.com.onepercent;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;


public class Main extends Activity {
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        btn = (Button)findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String response_msg = post_jsp("http://117.17.142.139:8080/letter/json.do", "Hello Android & Maven!!", "MoonSu");
                jsonParserList(response_msg);
                Log.d("letter", " msg: " + response_msg);
            }
        });
    }
    /************************* http 통신 메소드***********************/
    public String post_jsp(String strurl, String msg, String name)
    {
        String response_msg =  null;
        try {
            String data = URLEncoder.encode("msg", "EUC-KR") + "=" + URLEncoder.encode(""+msg, "EUC-KR");
            data += "&" + URLEncoder.encode("name", "EUC-KR") + "=" + URLEncoder.encode(""+name, "EUC-KR");

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


    public String[][] jsonParserList(String pRecvServerPage) {

        Log.d("서버에서 받은 전체 내용 : ", pRecvServerPage);

        try {
            JSONObject json = null;
            try {
                String s = URLDecoder.decode(pRecvServerPage);
                json = new JSONObject(s);
                Log.i("original json " , ""+json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONArray jArr = json.getJSONArray("List");

            // 받아온 pRecvServerPage를 분석하는 부분
            String[] jsonName = {"name", "id"};
            String[][] parseredData = new String[jArr.length()][jsonName.length];
//            json = jArr.getJSONObject(0);
////            String name = json.getString("name");
////            String msg = json.getString("msg");
//            Log.i("json " , ""+json);
//            Log.i("name " , json.getString(jsonName[0]));
//            json = jArr.getJSONObject(1);
//            Log.i("msg : " , json.getString(jsonName[1]));

//
            Log.i("size : ", "" + jArr.length());
            for (int i = 0; i < jArr.length(); i++) {
                json = jArr.getJSONObject(i);
                if(json != null) {
                    for (int j = 0; j < json.length(); j++)
                        parseredData[i][j] = json.getString(jsonName[j]);
                }
            }
//
//
//            // 분해 된 데이터를 확인하기 위한 부분
            for (int i = 0 ; i < jArr.length(); i++)
                for (int j = 0 ; j < 2; j++)
                    Log.d("JSON을 분석한 데이터 " + " : ", parseredData[i][j]);

            return parseredData;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
