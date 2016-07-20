package onepercent.mobile.com.onepercent.Custom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import onepercent.mobile.com.onepercent.R;


public class FaceFriendAdapter extends BaseAdapter {

    private ArrayList<FaceFriendData> itemDatas = null;
    private LayoutInflater layoutInflater = null;
    private Context contexts=null;
    public FaceFriendAdapter(ArrayList<FaceFriendData> itemDatas, Context ctx){
        contexts = ctx;
        this.itemDatas = itemDatas;
        layoutInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setItemData(ArrayList<FaceFriendData> itemDatas){
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

        FaceFriendHolder viewHolder = new FaceFriendHolder();

        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.list_item, parent ,false);

            viewHolder.id = (TextView)convertView.findViewById(R.id.id_item);
            viewHolder.name = (TextView)convertView.findViewById(R.id.nickname_item);

            convertView.setTag(viewHolder);
        }
        else{
            viewHolder=(FaceFriendHolder)convertView.getTag();
        }

        FaceFriendData itemData = itemDatas.get(position);


        viewHolder.id.setText(itemData.id);
        viewHolder.name.setText(itemData.name);

        return convertView;
    }

    public void addListItem(int id, String name)
    {

        FaceFriendData addinfo = null;
        addinfo = new FaceFriendData();

        addinfo.id = id;
        addinfo.name = name;
        itemDatas.add(addinfo);
    }
    public void removeAlls()
    {
        itemDatas.clear();
    }
}
