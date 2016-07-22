package onepercent.mobile.com.onepercent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.internal.Utility;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import onepercent.mobile.com.onepercent.Model.User;

public class FaceLogin extends Activity {

    private CallbackManager callbackManager                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            ;
    Context mContext;
    String id="";  //나중에 받아올 것들
    String name="";
    String email="";
    String gender="";
    String check;
    // Register_id data
    String SENDER_ID = "469522385717";
    String regid = null;

    GoogleCloudMessaging gcm;
    SharedPreferences prefs;
    Context context;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    // SharedPreferences에 저장할 때 key 값으로 사용됨.
    public static final String PROPERTY_REG_ID = "";
    public static final String PROPERTY_ID = "";

    // SharedPreferences에 저장할 때 key 값으로 사용됨.
    private static final String PROPERTY_APP_VERSION = "2";
    private static final String TAG = "Sumus";
    Intent intent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();
        context = getApplicationContext();
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
            Log.i("letter", "regid:" + regid);
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        {
            FacebookSdk.sdkInitialize(this.getApplicationContext()); //sdk 초기내용 설정
            setContentView(R.layout.activity_facebook);

            callbackManager = CallbackManager.Factory.create();
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends"));

            LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
            loginButton.setReadPermissions("email");
            loginButton.setReadPermissions("user_friends");
                    loginButton.registerCallback(callbackManager,
                            new FacebookCallback<LoginResult>() {
                                @Override
                                public void onSuccess(LoginResult loginResult) {//로그인이 성공되었을때 호출
                                    GraphRequest request = GraphRequest.newMeRequest(
                                            loginResult.getAccessToken(),
                                            new GraphRequest.GraphJSONObjectCallback() {
                                                @Override
                                                public void onCompleted(
                                                        JSONObject object,
                                                        GraphResponse response) {
                                                    // Application code
                                                    try {
                                                        id = (String) response.getJSONObject().get("id");//페이스북 아이디값
                                                        name = (String) response.getJSONObject().get("name");//페이스북 이름
                                                        //  email = (String) response.getJSONObject().get("email");//이메일
                                                        gender = (String) response.getJSONObject().get("gender");
                                                        Log.d("letter", "id : " + id);
                                                        Log.d("letter", "name : " + name);
                                                        Log.d("letter", "email : " + email);
                                                        Log.d("letter", "gender : " + gender);
                                                        Log.d("letter", "regid : " + regid);
                                                        Date d = new Date();
                                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                        String date = sdf.format(d).toString();
                                                        User user = User.getInstance();
                                                        String install_date = user.getInstall_date();
                                                        user.setInstall_date(install_date.equals("") || install_date.equals(null) ? date : install_date);
                                                        user.setUser_id(id);
                                                        user.setUser_name(name);
                                                        user.setUser_date(date);
                                                        String url = "http://52.78.88.51:8080/letter/insertUser.do";
                                                        post_profile(url, id, name, date);
                                                        boolean tutorial_state = user.getTutorial_State();
                                                        if (tutorial_state) {
                                                            Intent intent = new Intent(FaceLogin.this, CardActivity.class);
                                                            intent.putExtra("letter_id", "0");
                                                            intent.putExtra("from_id", "");
                                                            intent.putExtra("from_name", "");
                                                            intent.putExtra("to_id", "");
                                                            intent.putExtra("to_name", "");
                                                            intent.putExtra("content", "");
                                                            intent.putExtra("latitude", "0.0");
                                                            intent.putExtra("longitude", "0.0");
                                                            intent.putExtra("address", "");
                                                            intent.putExtra("date", "");
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                        else {
                                                            user.setTutorial_state(true);
                                                            Intent intent = new Intent(FaceLogin.this, TutorialActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }

                                                    } catch (JSONException e) {
                                                        // TODO Auto-generated catch block
                                                        e.printStackTrace();
                                                    }
                                                    // new joinTask().execute(); //자신의 서버에서 로그인 처리를 해줍니다

                                                }
                                            });
                                    Bundle parameters = new Bundle();
                                    parameters.putString("fields", "id,name,email,gender, birthday");
                                    request.setParameters(parameters);
                                    request.executeAsync();
                                }

                                @Override
                                public void onCancel() {
                                    Toast.makeText(FaceLogin.this, "로그인을 취소 하였습니다!", Toast.LENGTH_SHORT).show();
                                    // App code
                                }

                                @Override
                                public void onError(FacebookException exception) {
                                    Toast.makeText(FaceLogin.this, "에러가 발생하였습니다", Toast.LENGTH_SHORT).show();
                                    // App code
                                }
                            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        Log.d("myLog", "requestCode : " + requestCode);
        Log.d("myLog", "resultCode :" + resultCode);
        Log.d("myLog", "data : " + data.toString());
        Log.d("myLog", "email :   " + email.toString());
        Log.d("myLog", "id : " + id.toString());
        Log.d("myLog", "name : " + name.toString());
        Log.d("myLog", "gender : " + gender.toString());
    }



    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // 서버에 발급받은 등록 아이디를 전송한다.
                    // 등록 아이디는 서버에서 앱에 푸쉬 메시지를 전송할 때 사용된다.
                    // sendRegistrationIdToBackend();

                    // 등록 아이디를 저장해 등록 아이디를 매번 받지 않도록 한다.
                    storeRegistrationId(context, regid);

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                //mDisplay.append(msg + "\n");
                Log.i("letter", msg);
            }

        }.execute(null, null, null);
    }

    private void storeRegistrationId(Context context, String regid) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);

        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regid);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private void sendRegistrationIdToBackend() {

    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        // 앱이 업데이트 되었는지 확인하고, 업데이트 되었다면 기존 등록 아이디를 제거한다.
        // 새로운 버전에서도 기존 등록 아이디가 정상적으로 동작하는지를 보장할 수 없기 때문이다.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);

        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");

            return "";
        }
        else {}
        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(FaceLogin.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("letter", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /*********************************************/

    /************************* http 통신 메소드***********************/
    public String post_profile(String strurl, String id, String name, String date)
    {
        String response_msg =  null;
        try {
            String data = URLEncoder.encode("id", "EUC-KR") + "=" + URLEncoder.encode(""+id, "EUC-KR");
            data += "&" + URLEncoder.encode("name", "EUC-KR") + "=" + URLEncoder.encode(""+name, "EUC-KR");
            data += "&" + URLEncoder.encode("date", "EUC-KR") + "=" + URLEncoder.encode(""+date, "EUC-KR");
            data += "&" + URLEncoder.encode("register_id", "EUC-KR") + "=" + URLEncoder.encode(""+regid, "EUC-KR");

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

}
    /*****************************************************************/



