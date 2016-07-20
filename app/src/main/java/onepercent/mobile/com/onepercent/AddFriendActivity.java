package onepercent.mobile.com.onepercent;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import onepercent.mobile.com.onepercent.SQLite.DBManager;
import onepercent.mobile.com.onepercent.SQLite.FriendAdapter;
import onepercent.mobile.com.onepercent.SQLite.FriendInfo;

public class AddFriendActivity extends Activity implements  View.OnClickListener, ListView.OnItemClickListener{
    // CustomList
    public ListView listView;
    public final ArrayList<FriendInfo> itemDatas = new ArrayList<FriendInfo>();
    public FriendAdapter adapter;

    // Facebook
    private CallbackManager callbackManager;
    AccessToken accessToken;

    // SQLite
    ArrayList<FriendInfo> arrayList = new ArrayList<FriendInfo>();
    DBManager manager;

    // 기본 위젯
    Context ctx;
    ImageButton refreshBtn,findBtn;
    EditText wordEt;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        ctx = this;

        // 리스트뷰
        listView = (ListView) findViewById(R.id.friendlistView);
        adapter = new FriendAdapter(itemDatas, getApplicationContext());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        adapter.notifyDataSetChanged();


        /* DB 초기값 */
        manager = new DBManager(this);

        refreshBtn = (ImageButton) findViewById(R.id.refreshBtn);
        refreshBtn.setOnClickListener(this);
        findBtn = (ImageButton) findViewById(R.id.findBtn);
        findBtn.setOnClickListener(this);
        wordEt = (EditText)findViewById(R.id.wordEt);
        // 페이스북
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        showList();

        wordEt.addTextChangedListener(new TextWatcher() {
            @Override // 입력되는 텍스트에 변화가 있을 때
            public void onTextChanged(CharSequence s, int start, int before, int count) {      }

            // 입력이 끝났을 때
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0)
                    dataShowList();
                else
                    showList();
            }

            @Override // 입력하기 전에
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {       }
        });

    }

    void showList() { // 리스트 뿌려주기
        if(adapter.getCount()>0)
            adapter.removeAlls();
        arrayList = manager.selectAll();

        if(arrayList.size()>0 ) {
            for (int i = 0; i < arrayList.size(); i++) {
                // 검색 리스트
                adapter.addListItem(arrayList.get(i).id, arrayList.get(i).nickname);
            }
            adapter.notifyDataSetChanged();
        }
        else
            adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.findBtn:
                dataShowList();
                break;

            case R.id.refreshBtn:  // 동기화 버튼
                FacebookSdk.sdkInitialize(getApplicationContext());
                callbackManager = CallbackManager.Factory.create();
                LoginManager.getInstance().logInWithReadPermissions(AddFriendActivity.this, Arrays.asList ("public_profile", "email"));
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

                    @Override
                    public void onSuccess(final LoginResult result) {
                        GraphRequest request;
                        request = GraphRequest.newMeRequest(result.getAccessToken(), new
                                GraphRequest.GraphJSONObjectCallback() {

                                    @Override
                                    public void onCompleted(JSONObject user, GraphResponse response) {
                                        if (response.getError() != null) {

                                        } else {
                                            try {
                                                JSONObject friends = user.getJSONObject("friends");
                                                JSONArray data = friends.getJSONArray("data");

                                                JSONObject summary = friends.getJSONObject("summary");
                                                String total_count = (String) summary.getString("total_count");

                                                Log.d("letter", "total_count : " + total_count);
                                                Log.d("letter", "data : " + data.toString());
                                                Log.d("letter", "data.length() : " + data.length());

                                                if (data.length() <= 0) {
                                                    //adapter.addListItem("0", "이 앱을 설치한 친구가 없습니다.");
                                                } else {
                                                    for (int i = 0; i < data.length(); i++) {
                                                        JSONObject o = data.getJSONObject(i);
                                                        if(!manager.dataCheck(o.getString("id")))
                                                            manager.insertData(new FriendInfo(o.getString("id"), o.getString("name")),ctx);
                                                    }
                                                    showList();
                                                }
                                            } catch (Exception e) {    }
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "friends");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d("letter", "Error: " + error);
                        finish();
                    }
                    @Override
                    public void onCancel() {
                        Log.d("letter", "Cancle");
                        finish();
                    }
                });
                break;
        }
    }
    void dataShowList() // 특정 데이터 검색
    {
        if(adapter.getCount()>0)
            adapter.removeAlls();
        arrayList =  manager.dataSelect(wordEt.getText().toString());
        if(arrayList.size()>0 ) {
            for (int i = 0; i < arrayList.size(); i++) {
                // 검색 리스트
                adapter.addListItem(arrayList.get(i).id, arrayList.get(i).nickname);
            }
            adapter.notifyDataSetChanged();
        }
        else
            adapter.notifyDataSetChanged();
    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        FriendInfo itemData_temp = (FriendInfo) adapter.getItem(position);
        final String id = itemData_temp.id;
        final String nickname = itemData_temp.nickname;
        LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflate.inflate(R.layout.friend_popup, null);

        Button writeLetter = (Button) layout.findViewById(R.id.writeletter);
        Button delLetter = (Button) layout.findViewById(R.id.delLetter);
        Button cancle = (Button) layout.findViewById(R.id.cancle);

        AlertDialog.Builder aDialog = new AlertDialog.Builder(ctx);
        aDialog.setView(layout);
        final AlertDialog ad = aDialog.create();
        ad.show();

        writeLetter.setOnClickListener(new View.OnClickListener() { // 쓰기 버튼
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddFriendActivity.this, MapActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("nickname", nickname);
                startActivity(intent);
                ad.cancel();
            }
        });

        delLetter.setOnClickListener(new View.OnClickListener() { // 삭제 버튼
            @Override
            public void onClick(View view) {
            }
        });

        cancle.setOnClickListener(new View.OnClickListener() {  // 취소 버튼
            @Override
            public void onClick(View view) {
                ad.cancel();
            }
        });
    }

}
