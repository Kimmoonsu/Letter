package onepercent.mobile.com.onepercent;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import onepercent.mobile.com.onepercent.Custom.LetterAdapter;
import onepercent.mobile.com.onepercent.SQLite.DBManager;
import onepercent.mobile.com.onepercent.SQLite.LetterInfo;

public class LetterListActivity extends Activity {

    TextView letterTitle;

    // SQLite
    ArrayList<LetterInfo> arrayList = new ArrayList<LetterInfo>();
    DBManager manager;

    // CustomList
    public ListView listView;
   // public  ArrayList<LetterInfo> itemDatas  = new ArrayList<LetterInfo>();
    public LetterAdapter adapter;
    Context ctx = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_letter_list);
        ctx = this;

        // 리스트뷰
        listView = (ListView) findViewById(R.id.letterlistView);
        adapter = new LetterAdapter(arrayList, ctx);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        manager = new DBManager(this);


        letterTitle = (TextView)findViewById(R.id.letterTitle);

          letterTitle.setText("편지 발신함");
            arrayList = manager.selectAll2();

            for(int i=0; i<arrayList.size(); i++)
            {
                Log.d("letter"," Letterlist : " + arrayList.get(i).send_name+" "+arrayList.get(i).context);
                adapter.addListItem(arrayList.get(i).letter_id,arrayList.get(i).send_id,arrayList.get(i).send_name,arrayList.get(i).context,arrayList.get(i).address,arrayList.get(i).latitude,arrayList.get(i).longitude,arrayList.get(i).state,arrayList.get(i).date);
                adapter.notifyDataSetChanged();
            }



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long ids) {
                LetterInfo itemData_temp = (LetterInfo) adapter.getItem(position);
                Toast.makeText(ctx, itemData_temp.latitude + "", Toast.LENGTH_SHORT).show();

            }
        });

    }
}
