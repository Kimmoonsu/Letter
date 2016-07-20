package onepercent.mobile.com.onepercent.SQLite;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import onepercent.mobile.com.onepercent.R;

public class FriendAdapter extends BaseAdapter {

    private ArrayList<FriendInfo> itemDatas = null;
    private LayoutInflater layoutInflater = null;
    private Context contexts=null;
    public FriendAdapter(ArrayList<FriendInfo> itemDatas, Context ctx){
        contexts = ctx;
        this.itemDatas = itemDatas;
        layoutInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setItemData(ArrayList<FriendInfo> itemDatas){
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

        FriendHolder viewHolder = new FriendHolder();

        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.friend_item, parent ,false);

            viewHolder.id = (TextView)convertView.findViewById(R.id.id_item);
            viewHolder.nickname = (TextView)convertView.findViewById(R.id.nickname_item);

            convertView.setTag(viewHolder);
        }
        else{
            viewHolder=(FriendHolder)convertView.getTag();
        }

        FriendInfo itemData = itemDatas.get(position);

        viewHolder.id.setText(itemData.id+"");
        viewHolder.nickname.setText(itemData.nickname);

        return convertView;
    }

    public void addListItem(String id, String name)
    {

        FriendInfo addinfo = null;
        addinfo = new FriendInfo(id,name);

        itemDatas.add(addinfo);
    }

    public void removeAlls()
    {
        itemDatas.clear();
    }
}
