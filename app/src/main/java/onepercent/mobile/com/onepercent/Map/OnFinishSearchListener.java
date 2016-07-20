package onepercent.mobile.com.onepercent.Map;

import java.util.List;

public interface OnFinishSearchListener {
	
	public void onSuccess(List<Item> itemList);
	public void onFail();
}
