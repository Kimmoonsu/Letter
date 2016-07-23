package onepercent.mobile.com.onepercent;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import onepercent.mobile.com.onepercent.Custom.CustomAdapter;
import onepercent.mobile.com.onepercent.Custom.ItemData;
import onepercent.mobile.com.onepercent.Map.Item;
import onepercent.mobile.com.onepercent.Map.MapApiConst;
import onepercent.mobile.com.onepercent.Map.OnFinishSearchListener;
import onepercent.mobile.com.onepercent.Map.Searcher;
import onepercent.mobile.com.onepercent.Model.ActivityModel;
import onepercent.mobile.com.onepercent.SQLite.DBManager;
import onepercent.mobile.com.onepercent.SQLite.LetterInfo;

import static net.daum.mf.map.api.MapReverseGeoCoder.ReverseGeoCodingResultListener;

public class MapActivity extends BaseActivity implements MapView.MapViewEventListener, MapView.POIItemEventListener, View.OnClickListener, TextView.OnEditorActionListener, ReverseGeoCodingResultListener {
    private static final String LOG_TAG = "SearchDemoActivity";
    Typeface mTypeface = null;


    public static Context ctx;
    // Map
    private MapView mMapView;
    ViewGroup mapViewContainer;
    private EditText mEditTextQuery;
    private ImageButton mButtonSearch;
    private HashMap<Integer, Item> mTagItemMap = new HashMap<Integer, Item>();

    // CustomList
    public ListView listView;

    public final ArrayList<ItemData> itemDatas  = new ArrayList<ItemData>();
    public CustomAdapter adapter;

    // writer page
    LinearLayout writerLayout,searchLayout,titleLayout;
    Button sendBtn;
    EditText toEt, contextEt;
    TextView titleTv;

    // gps
    private GpsInfo gps;
    ImageButton  synchBtn;

    // gps 카운트
    Boolean GPS_CLICK = false; // 검색마다 내위치 한번만 추가되도록
    Boolean LOCATION_SELECT = false;

    // 수신자 정보
    String toNickname, toId, toContext, from_id, from_name, inAddress, date;
    Double inLongitude, inLatitude;

    // SQLite
    ArrayList<LetterInfo> arrayList = new ArrayList<LetterInfo>();
    DBManager manager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Intent data = getIntent();
        toId = data.getStringExtra("id");
        toNickname = data.getStringExtra("nickname");
        getUser();


        // font
        if (mTypeface == null) {
            mTypeface = Typeface.createFromAsset(this.getAssets(), "fonts.ttf"); // 외부폰트 사용
            // mTypeface = Typeface.MONOSPACE; // 내장 폰트 사용
        }
        ctx = this;
        manager = new DBManager(this);
        writerLayout = (LinearLayout)findViewById(R.id.writerLayout);
        searchLayout = (LinearLayout)findViewById(R.id.searchLayout);
        titleLayout = (LinearLayout)findViewById(R.id.titleLayout);
        sendBtn = (Button)findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(this);
        synchBtn = (ImageButton) findViewById(R.id.synchBtn);
        synchBtn.setOnClickListener(this);
        toEt = (EditText) findViewById(R.id.to);
        contextEt = (EditText) findViewById(R.id.contextEt);
        titleTv= (TextView) findViewById(R.id.titleTv);

        // 리스트뷰
        listView = (ListView) findViewById(R.id.listView);
        adapter = new CustomAdapter(itemDatas, getApplicationContext());
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        mEditTextQuery = (EditText) findViewById(R.id.editTextQuery); // 검색창
        mEditTextQuery.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mEditTextQuery.setInputType(InputType.TYPE_CLASS_TEXT);
        mButtonSearch = (ImageButton) findViewById(R.id.buttonSearch); // 검색버튼
        mButtonSearch.setOnClickListener(this);

        mEditTextQuery.setOnEditorActionListener(this);
        // 검색 결과 리스트
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ItemData itemData_temp = (ItemData) adapter.getItem(position);
                Double longi = itemData_temp.longitude;
                Double lati = itemData_temp.latitude;

                mMapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(lati,longi), 2, true);
                MapPOIItem[] poiItems = mMapView.getPOIItems();
                mMapView.selectPOIItem(poiItems[position],true);
            }
        });
        mEditTextQuery.setSelection(mEditTextQuery.length());


    }
    @Override
    protected void onResume() {
        super.onResume();
        // 지도
        mMapView =  new MapView(this);
        mMapView.setDaumMapApiKey(MapApiConst.DAUM_MAPS_ANDROID_APP_API_KEY);
        mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mMapView);
        mMapView.setMapViewEventListener(this);
        mMapView.setPOIItemEventListener(this);
        mMapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapViewContainer.removeAllViews();
    }

    /* 클릭 */
    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.buttonSearch: // 검색 버튼 클릭
                searchQuery();
                break;

            case R.id.synchBtn: // 이부분이 위도경도
                GPSGPS();
                break;
            case R.id.sendBtn: // 서버로 편지내용 전송
                // 날짜
                Date d = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = sdf.format(d).toString();
                Log.d("letter",  "date : "+date);
                toContext  = contextEt.getText().toString();
                Log.d("letter", "sendLetter : " + from_id + " , " + from_name + " . " + inAddress + "!!");
                manager.insertData2(new LetterInfo(0, toId, toNickname, toContext, inAddress, inLatitude, inLongitude, 0, date), ctx);
                String url = "http://52.78.88.51:8080/letter/insertLetter.do";
                sendLetter(url, toId, toNickname, from_id, from_name, inAddress, toContext, inLatitude, inLongitude, date);
                Toast.makeText(this, "소중한 편지 전달 완료~!", Toast.LENGTH_SHORT).show();
                for (int i = 0; i < ActivityModel.actList.size(); i++)
                    ActivityModel.actList.get(i).finish();
                ActivityModel.actList.clear();
                finish();
                break;

        }
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch(v.getId())
        {
            case R.id.editTextQuery:
            {
                searchQuery();
                break;
            }
        }
        return false;
    }

    /*********검색 버튼 event method *********/
    public void searchQuery() {
        GPS_CLICK = false;
        itemDatas.clear();// 리스트 초기화

        String query = mEditTextQuery.getText().toString();
        if (query == null || query.length() == 0) {
            showToast("검색어를 입력하세요.");
            return;
        }
        //hideSoftKeyboard(); // 키보드 숨김
        MapPoint.GeoCoordinate geoCoordinate = mMapView.getMapCenterPoint().getMapPointGeoCoord();
        double latitude = geoCoordinate.latitude; // 위도
        double longitude = geoCoordinate.longitude; // 경도
        int radius = 10000; // 중심 좌표부터의 반경거리. 특정 지역을 중심으로 검색하려고 할 경우 사용. meter 단위 (0 ~ 10000)
        int page = 1; // 페이지 번호 (1 ~ 3). 한페이지에 15개
        String apikey = MapApiConst.DAUM_MAPS_ANDROID_APP_API_KEY;

        Searcher searcher = new Searcher(); // net.daum.android.map.openapi.search.Searcher
        searcher.searchKeyword(getApplicationContext(), query, latitude, longitude, radius, page, apikey, new OnFinishSearchListener() {
            @Override
            public void onSuccess(List<Item> itemList) {

                mMapView.removeAllPOIItems(); // 기존 검색 결과 삭제
                showResult(itemList); // 검색 결과 보여줌

            }

            @Override
            public void onFail() {
                showToast("API_KEY의 제한 트래픽이 초과되었습니다.");
            }
        });
        adapter.notifyDataSetChanged();

    }
    /****************************************/

    public  void GPSGPS(){
        gps= new GpsInfo(MapActivity.this);
        //GPS 사용유무 가져오기다
        if(gps.isGetLocation()){
            if(GPS_CLICK == false) { // 검색후 한번만 실행되서 추가 시키기
                double latitude = gps.getLatitude(); //위도다
                double longitude = gps.getLongitude(); //경도다

                // 내위치 지도 리스트에 추가
                MapPOIItem[] poiItems = mMapView.getPOIItems();
                int last = poiItems.length + 1;
                Item item = new Item();
                item.title = "Your location";
                item.latitude = latitude;
                item.longitude = longitude;
                item.address = "";

                MapPOIItem poiItem = new MapPOIItem();
                poiItem.setItemName(item.title);
                poiItem.setTag(last);
                MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(item.latitude, item.longitude);
                poiItem.setMapPoint(mapPoint);
                poiItem.setMarkerType(MapPOIItem.MarkerType.YellowPin);
                mMapView.addPOIItem(poiItem);


                mMapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(item.latitude, item.longitude), 2, true);
                mTagItemMap.put(poiItem.getTag(), item);
                GPS_CLICK = true;
//                MapReverseGeoCoder mReverseGeoCoder = new MapReverseGeoCoder( MapApiConst.DAUM_MAPS_ANDROID_APP_API_KEY, mMapView.getMapCenterPoint(), MapActivity.this, MapActivity.this);
//                mReverseGeoCoder.startFindingAddress();

            }
            else { // 이미 마커가 추가됬으므로 중심 만 바꿔주기
                double latitude = gps.getLatitude(); //위도다
                double longitude = gps.getLongitude(); //경도다
                mMapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(latitude, longitude), 2, true);
            }
        }else {
            //gps 사용할수 없으므로
            gps.showSettingsAlert();
        }
    }


    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {

        private final View mCalloutBalloon;

        public CustomCalloutBalloonAdapter() {
            mCalloutBalloon = getLayoutInflater().inflate(R.layout.custom_callout_balloon, null);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem poiItem) {
            if (poiItem == null) return null;
            Item item = mTagItemMap.get(poiItem.getTag());
            Log.d("letter","Balloon Tag() : "+poiItem.getTag());
            if (item == null) return null;
            ImageView imageViewBadge = (ImageView) mCalloutBalloon.findViewById(R.id.badge);
            TextView textViewTitle = (TextView) mCalloutBalloon.findViewById(R.id.title);
            textViewTitle.setText(item.title);
            TextView textViewDesc = (TextView) mCalloutBalloon.findViewById(R.id.desc);
            textViewDesc.setText(item.address);
            imageViewBadge.setImageDrawable(createDrawableFromUrl(item.imageUrl));
            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return null;
        }

    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditTextQuery.getWindowToken(), 0);
    }

    public void onMapViewInitialized(MapView mapView) { // 맵 처음 초기
        Log.i(LOG_TAG, "MapView had loaded. Now, MapView APIs could be called safely");

        mMapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(37.537229,127.005515), 2, true);

        Searcher searcher = new Searcher();
        String query = mEditTextQuery.getText().toString();
        double latitude = 37.537229;
        double longitude = 127.005515;
        int radius = 10000; // 중심 좌표부터의 반경거리. 특정 지역을 중심으로 검색하려고 할 경우 사용. meter 단위 (0 ~ 10000)
        int page = 1;
        String apikey = MapApiConst.DAUM_MAPS_ANDROID_APP_API_KEY;

        searcher.searchKeyword(getApplicationContext(), query, latitude, longitude, radius, page, apikey, new OnFinishSearchListener() {
            @Override
            public void onSuccess(final List<Item> itemList) {
                showResult(itemList);
            }

            @Override
            public void onFail() {
                showToast("API_KEY의 제한 트래픽이 초과되었습니다.");
            }
        });
    }

    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MapActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showResult(List<Item> itemList) { // 검색 결과 반환
        //StringBuilder sb = new StringBuilder();
        MapPointBounds mapPointBounds = new MapPointBounds();

        for (int i = 0; i < itemList.size(); i++) {
            Item item = itemList.get(i);

            MapPOIItem poiItem = new MapPOIItem();
            poiItem.setItemName(item.title);
            poiItem.setTag(i);
            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(item.latitude, item.longitude);
            poiItem.setMapPoint(mapPoint);
            mapPointBounds.add(mapPoint);
            poiItem.setMarkerType(MapPOIItem.MarkerType.CustomImage);

            poiItem.setCustomImageResourceId(R.drawable.map_pin_blue);
            poiItem.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
            poiItem.setCustomSelectedImageResourceId(R.drawable.map_pin_red);
            poiItem.setCustomImageAutoscale(false);
            poiItem.setCustomImageAnchor(0.5f, 1.0f);

            mMapView.addPOIItem(poiItem);
            mTagItemMap.put(poiItem.getTag(), item);


            // 검색 리스트
            adapter.addListItem(getResources().getDrawable(R.drawable.map_pin_blue), item.title, item.address, item.longitude, item.latitude);
        }

        mMapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds));

        MapPOIItem[] poiItems = mMapView.getPOIItems();
        if (poiItems.length > 0) {
            mMapView.selectPOIItem(poiItems[0], false);
        }
    }



    private Drawable createDrawableFromUrl(String url) {
        try {
            InputStream is = (InputStream) this.fetch(url);
            Drawable d = Drawable.createFromStream(is, "src");
            return d;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Object fetch(String address) throws MalformedURLException,IOException {
        URL url = new URL(address);
        Object content = url.getContent();
        return content;
    }

    /* 말풍선 */
    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, final MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
        final Item item = mTagItemMap.get(mapPOIItem.getTag());

        if(LOCATION_SELECT==false) {
            LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflate.inflate(R.layout.dialog_popop, null);
            setGlobalFont(layout);

            AlertDialog.Builder aDialog = new AlertDialog.Builder(ctx);
            aDialog.setView(layout);
            final AlertDialog ad = aDialog.create();
            ad.show();

            Button dialog_cancle = (Button) layout.findViewById(R.id.dialog_cancle);
            Button dialog_ok = (Button)layout.findViewById(R.id.dialog_ok);

            dialog_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mMapView.removeAllPOIItems(); // 기존 검색 결과 삭제
                    Bitmap letter = BitmapFactory.decodeResource(getResources(), R.drawable.letter);
                    letter = Bitmap.createScaledBitmap(letter, 96, 120, true);
                    MapPOIItem poiItem = new MapPOIItem();
                    poiItem.setItemName("send letter");
                    poiItem.setTag(mapPOIItem.getTag());
                    MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(item.latitude, item.longitude);
                    poiItem.setMapPoint(mapPoint);
                    poiItem.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                    poiItem.setCustomImageBitmap(letter);
                    poiItem.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                    poiItem.setCustomSelectedImageBitmap(letter);
                    poiItem.setCustomImageAutoscale(false);
                    poiItem.setCustomImageAnchor(0.5f, 1.0f);

                    mMapView.addPOIItem(poiItem);

                    mMapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(item.latitude, item.longitude), 2, true);

                    listView.setVisibility(View.GONE);
                    searchLayout.setVisibility(View.GONE);
                    writerLayout.setVisibility(View.VISIBLE);
                    titleLayout.setVisibility(View.VISIBLE);
                    //hideSoftKeyboard();
                    if (item.address.equals("")) {
                        MapReverseGeoCoder mReverseGeoCoder = new MapReverseGeoCoder(MapApiConst.DAUM_MAPS_ANDROID_APP_API_KEY, mapPoint, MapActivity.this, MapActivity.this);
                        mReverseGeoCoder.startFindingAddress();
                    } else
                        inAddress = item.address;
                    inLatitude = item.latitude;
                    inLongitude = item.longitude;
                    toEt.setText(toNickname);
                    titleTv.setText("To." + toNickname);
                    Log.d("letter", item.latitude + " " + item.longitude);
                    LOCATION_SELECT = true;
                }
            });

            dialog_cancle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ad.cancel();
                }
            });

        }
    }

    private void setGlobalFont(View view) {
        if (view != null) {
            if(view instanceof ViewGroup){
                ViewGroup vg = (ViewGroup)view;
                int vgCnt = vg.getChildCount();
                for(int i=0; i < vgCnt; i++){
                    View v = vg.getChildAt(i);
                    if(v instanceof TextView){
                        ((TextView) v).setTypeface(mTypeface);
                    }
                    setGlobalFont(v);
                }
            }
        }
    }

    /************************* http 통신 메소드***********************/
    public String sendLetter(String strurl, String to_id, String to_name, String from_id, String from_name, String address, String content, double latitude, double longitude, String date)
    {
        String response_msg =  null;
        try {
            String data = URLEncoder.encode("to_id", "EUC-KR") + "=" + URLEncoder.encode(""+to_id, "EUC-KR");
            data += "&" + URLEncoder.encode("to_name", "EUC-KR") + "=" + URLEncoder.encode(""+to_name, "EUC-KR");
            data += "&" + URLEncoder.encode("from_id", "EUC-KR") + "=" + URLEncoder.encode(""+from_id, "EUC-KR");
            data += "&" + URLEncoder.encode("from_name", "EUC-KR") + "=" + URLEncoder.encode(""+from_name, "EUC-KR");
            data += "&" + URLEncoder.encode("address", "EUC-KR") + "=" + URLEncoder.encode(""+address, "EUC-KR");
            data += "&" + URLEncoder.encode("content", "EUC-KR") + "=" + URLEncoder.encode(""+content, "EUC-KR");
            data += "&" + URLEncoder.encode("latitude", "EUC-KR") + "=" + URLEncoder.encode(""+latitude, "EUC-KR");
            data += "&" + URLEncoder.encode("longitude", "EUC-KR") + "=" + URLEncoder.encode(""+longitude, "EUC-KR");
            data += "&" + URLEncoder.encode("date", "EUC-KR") + "=" + URLEncoder.encode(""+date, "EUC-KR");

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

    @Override
    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String result) {
        inAddress = result;
        Log.d("SUN","ADDRESS : "+result);
    }

    @Override
    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
        inAddress = " ";
        Log.d("SUN","ADDRESS : fail");
    }

    // User name, id 불러오기
    private void getUser(){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        from_name = pref.getString("user_name", "");
        from_id = pref.getString("user_id", "");
    }

    @Override
    @Deprecated
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {
    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapCenterPoint) {
    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int zoomLevel) {
    }



}
