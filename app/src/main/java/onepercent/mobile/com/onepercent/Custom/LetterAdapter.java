package onepercent.mobile.com.onepercent.Custom;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import onepercent.mobile.com.onepercent.R;
import onepercent.mobile.com.onepercent.SQLite.LetterInfo;


public class LetterAdapter extends BaseAdapter {

    private ArrayList<LetterInfo> itemDatas = null;
    private LayoutInflater layoutInflater = null;
    private Context contexts = null;

    public LetterAdapter(ArrayList<LetterInfo> itemDatas, Context ctx) {
        contexts = ctx;
        this.itemDatas = itemDatas;
        layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setItemData(ArrayList<LetterInfo> itemDatas) {
        this.itemDatas = itemDatas;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return (itemDatas != null) ? itemDatas.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return (itemDatas != null && (0 <= position && position < itemDatas.size()) ? itemDatas.get(position) : null);
    }

    @Override
    public long getItemId(int position) {
        return (itemDatas != null && (0 <= position && position < itemDatas.size()) ? position : 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LetterHolder viewHolder = new LetterHolder();

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.letter_item, parent, false);

            viewHolder.send_name = (TextView) convertView.findViewById(R.id.letter_name);
            viewHolder.address = (TextView) convertView.findViewById(R.id.letter_address);
            viewHolder.date = (TextView) convertView.findViewById(R.id.letter_date);
            viewHolder.state = (TextView) convertView.findViewById(R.id.letter_state);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (LetterHolder) convertView.getTag();
        }

        LetterInfo itemData = itemDatas.get(position);

    Log.d("SUN",itemData.send_name+"");
        viewHolder.send_name.setText(itemData.send_name+"");
        viewHolder.address.setText(itemData.address+"");
        viewHolder.date.setText(itemData.date+"");
        viewHolder.state.setText(itemData.state==0?"안읽음":"읽음");

        return convertView;
    }

    public void addListItem(int letter_id, String send_id,  String send_name, String context, String address, Double latitude,   Double longitude, int state, String date) {

        LetterInfo addinfo = null;
        addinfo = new LetterInfo();

        addinfo.letter_id = letter_id;
        addinfo.send_id = send_id;
        addinfo.send_name = send_name;
        addinfo.context = context;
        addinfo.address = address;
        addinfo.longitude = longitude;
        addinfo.latitude = latitude;
        addinfo.state = state;
        addinfo.date = date;
        itemDatas.add(addinfo);

       // itemDatas.add(new LetterInfo(letter_id,send_id,send_name,context,address,latitude,longitude,state,date));

    }

    public void removeAlls() {
        itemDatas.clear();
    }
}
