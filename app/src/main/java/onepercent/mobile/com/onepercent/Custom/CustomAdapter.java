package onepercent.mobile.com.onepercent.Custom;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import onepercent.mobile.com.onepercent.R;

public class CustomAdapter extends BaseAdapter {

    private ArrayList<ItemData> itemDatas = null;
    private LayoutInflater layoutInflater = null;
    private Context  contexts=null;
    public CustomAdapter(ArrayList<ItemData> itemDatas, Context ctx){
        contexts = ctx;
        this.itemDatas = itemDatas;
        layoutInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setItemData(ArrayList<ItemData> itemDatas){
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

        ViewHolder viewHolder = new ViewHolder();

        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.list_item, parent ,false);

            viewHolder.img = (ImageView)convertView.findViewById(R.id.img_item);
            viewHolder.title = (TextView)convertView.findViewById(R.id.txtTitle_item);
            viewHolder.address = (TextView)convertView.findViewById(R.id.txtAddress_item);
            viewHolder.longitude = (TextView)convertView.findViewById(R.id.txtLong_item);
            viewHolder.latitude = (TextView)convertView.findViewById(R.id.txtLati_item);

            convertView.setTag(viewHolder);
        }
        else{
            viewHolder=(ViewHolder)convertView.getTag();
        }

        ItemData itemData = itemDatas.get(position);

        viewHolder.img.setImageDrawable(itemData.img);
        viewHolder.title.setText(itemData.title);
        viewHolder.address.setText(itemData.address);
        viewHolder.longitude.setText(itemData.longitude+"");
        viewHolder.latitude.setText(itemData.latitude + "");

        return convertView;
    }

    public void addListItem(Drawable img,String title,String address,Double longitude, Double lantitude)
    {

        ItemData addinfo = null;
        addinfo = new ItemData();

        addinfo.img = img;
        addinfo.title = title;
        addinfo.address = address;
        addinfo.longitude = longitude;
        addinfo.latitude = lantitude;
        itemDatas.add(addinfo);
    }
    public void removeAlls()
    {
        itemDatas.clear();
    }
}
