package onepercent.mobile.com.onepercent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import onepercent.mobile.com.onepercent.Model.ActivityModel;
import onepercent.mobile.com.onepercent.SQLite.DBManager;
import onepercent.mobile.com.onepercent.SQLite.FriendAdapter;
import onepercent.mobile.com.onepercent.SQLite.FriendInfo;

public class FriendActivity extends Activity implements  View.OnClickListener{

    // CustomList
    public ListView listView;
    public final ArrayList<FriendInfo> itemDatas  = new ArrayList<FriendInfo>();
    public FriendAdapter adapter;
    Context ctx = null;

    // SQLite
    ArrayList<FriendInfo> arrayList = new   ArrayList<FriendInfo>();
    DBManager manager;

    // d위젯
    EditText wordEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        ctx = this;

        // 리스트뷰
        listView = (ListView) findViewById(R.id.friendlistView);
        adapter = new FriendAdapter(itemDatas, getApplicationContext());
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        /* 위젯 */
        wordEt = (EditText)findViewById(R.id.wordEt);
        wordEt.setText("");
        /* DB */
        manager = new DBManager(this);

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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long ids) {
                FriendInfo itemData_temp = (FriendInfo) adapter.getItem(position);
                final String id = itemData_temp.id;
                final String nickname = itemData_temp.nickname;

                Intent intent = new Intent(FriendActivity.this, MapActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("nickname", nickname);
                startActivity(intent);
                ActivityModel.actList.add(FriendActivity.this);
            }
        });
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
        }
    }
}
