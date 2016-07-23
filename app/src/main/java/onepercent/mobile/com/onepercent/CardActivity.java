package onepercent.mobile.com.onepercent;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapView;
import net.daum.mf.map.api.MapView.POIItemEventListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import onepercent.mobile.com.onepercent.Map.Item;
import onepercent.mobile.com.onepercent.Map.MapApiConst;
import onepercent.mobile.com.onepercent.Model.ActivityModel;
import onepercent.mobile.com.onepercent.Model.BackPressCloseHandler;
import onepercent.mobile.com.onepercent.SQLite.DBManager;
import onepercent.mobile.com.onepercent.SQLite.LetterInfo;


public class CardActivity extends BaseActivity implements View.OnClickListener, POIItemEventListener {
    //shared memory
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    // font
    Typeface mTypeface = null;

    //user
    String user_id, user_name;

    // letter
    int letter_id;
    String to_id, to_name, content, address, from_id, from_name, date;
    double latitude, longitude;

    Handler handler = new Handler();

    // back button handler
    private BackPressCloseHandler backPressCloseHandler;

    // gps thread
    GpsThread gpsThread;
    Handler mHandler ;
    int GPS_CONTRLL = 0;

    // Main Widget
    Context ctx;
    ImageButton writeBtn, shareBtn, settingBtn, synchBtn, pushBtn;

    // Display size
    float screenWidth;
    float screenHeight;

    public final static int CAMERA_SHOOT = 100;
    public final static int GET_PICTURE = 200;
    public final static int SYNCH = 300;

    private GpsInfo gps;
    Intent intent;
    // 지도
    private MapView mMapView;
    ViewGroup mapViewContainer;

    // 편지 표시 임시 데이터
    HashMap<Integer, Item> mTagItemMap = new HashMap<Integer, Item>();
    int LETTER_SIZE = 0;

    // SQLite
    ArrayList<LetterInfo> arrayList = new ArrayList<LetterInfo>();
    DBManager manager;

    Bitmap letterYellow, letterRed, letterGreen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        // font
        if (mTypeface == null) {
            mTypeface = Typeface.createFromAsset(this.getAssets(), "fonts.ttf"); // 외부폰트 사용
        }

        mHandler   = new Handler();
        gpsThread = new GpsThread(true);
        letterYellow =  BitmapFactory.decodeResource(getResources(), R.drawable.letter);
        letterYellow = Bitmap.createScaledBitmap(letterYellow, 60, 75, true);
        letterGreen=  BitmapFactory.decodeResource(getResources(), R.drawable.letter1);
        letterGreen = Bitmap.createScaledBitmap(letterGreen, 60, 75, true);
        letterRed=  BitmapFactory.decodeResource(getResources(), R.drawable.letter2);
        letterRed = Bitmap.createScaledBitmap(letterRed, 60, 75, true);

        getUser();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        pref = getSharedPreferences("pref", MODE_PRIVATE);
        editor = pref.edit();
        backPressCloseHandler = new BackPressCloseHandler(this, pref, editor);

        Log.d("letter", "user_id : " + user_id + " user_name : " + user_name);
        intent = getIntent();
        // data get
        getLetter();
        if (latitude != 0.0) { comparePush(); }
        //
        ctx=this;


        /* screen size */
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;

        /* main widget */
        writeBtn = (ImageButton) findViewById(R.id.writeBtn);
        writeBtn.setOnClickListener(this);
        shareBtn = (ImageButton) findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(this);
        settingBtn = (ImageButton) findViewById(R.id.settingBtn);
        settingBtn.setOnClickListener(this);
        synchBtn = (ImageButton) findViewById(R.id.synchBtn);
        synchBtn.setOnClickListener(this);
        pushBtn = (ImageButton) findViewById(R.id.pushBtn);
        pushBtn.setOnClickListener(this);


//          /* DB  */
        manager = new DBManager(this);
        LETTER_SIZE = manager.letterSize();
        arrayList = manager.selectAll1();
        manager.selectAll2();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapViewContainer.removeAllViews();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        gpsThread.stopThread();
        gpsThread = null;
    }
    @Override
    protected void onStop() {
        super.onStop();
        gpsThread.stopThread();
        GPS_CONTRLL = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LETTER_SIZE = manager.letterSize();
        arrayList = manager.selectAll1();
        // 지도
        mMapView =  new MapView(this);
        mMapView.setDaumMapApiKey(MapApiConst.DAUM_MAPS_ANDROID_APP_API_KEY);
        mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mMapView);
        mMapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(37.5041151, 127.0447707), 1, true);
        mMapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());
        mMapView.setPOIItemEventListener(this);

        MapPointBounds mapPointBounds = new MapPointBounds();
        for (int i = 0; i < LETTER_SIZE; i++) {

            Item item = new Item();
            item.title =  "from."+arrayList.get(i).send_name;
            item.address =  arrayList.get(i).address;
            item.latitude =  arrayList.get(i).latitude;
            item.longitude =  arrayList.get(i).longitude;
            item.letter_id =  arrayList.get(i).letter_id;
            item.send_id =  arrayList.get(i).send_id;
            item.send_name =  arrayList.get(i).send_name;
            item.state =  arrayList.get(i).state;
            item.context =  arrayList.get(i).context;

            MapPOIItem poiItem = new MapPOIItem();
            poiItem.setTag(i);
            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord( item.latitude, item.longitude);
            poiItem.setMapPoint(mapPoint);
            mapPointBounds.add(mapPoint);

            if(arrayList.get(i).state==0) // 안읽은 거
            {
                poiItem.setItemName("yellow");
                poiItem.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                poiItem.setCustomImageBitmap(letterYellow);
                poiItem.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                poiItem.setCustomSelectedImageBitmap(letterYellow);
            }
            else{
                poiItem.setItemName("red"); // 읽은 것
                poiItem.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                poiItem.setCustomImageBitmap(letterRed);
                poiItem.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                poiItem.setCustomSelectedImageBitmap(letterRed);

            }

            poiItem.setCustomImageAutoscale(false);
            poiItem.setCustomImageAnchor(0.5f, 1.0f);


            mMapView.addPOIItem(poiItem);
            mTagItemMap.put(poiItem.getTag(), item);

        }
        mMapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds));
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }
    public  void GPSGPS(){
        gps= new GpsInfo(CardActivity.this);
        //GPS 사용유무 가져오기다
        if(gps.isGetLocation()){
            MapPointBounds mapPointBounds = new MapPointBounds();
            double latitude = gps.getLatitude(); //위도다
            double longitude = gps.getLongitude(); //경도다
            LETTER_SIZE = manager.letterSize();

            mMapView.removeAllPOIItems();
            letterCheck(latitude,longitude); // 내주변 편지 체크

            MapPOIItem[] poiItems = mMapView.getPOIItems();
            if(poiItems.length>LETTER_SIZE)
                mMapView.removePOIItem(poiItems[LETTER_SIZE]);
            int last = LETTER_SIZE;

            MapPOIItem poiItem = new MapPOIItem();
            Item item = new Item();
            item.title = "Your location";
            item.latitude = latitude;
            item.longitude = longitude;
            item.address = last+"";

            poiItem.setItemName("Your location");
            poiItem.setTag(last+1);
            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude);
            poiItem.setMapPoint(mapPoint);
            poiItem.setMarkerType(MapPOIItem.MarkerType.YellowPin);
            mMapView.addPOIItem(poiItem);

            mMapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(latitude, longitude), 2, true);
            mTagItemMap.put(poiItem.getTag(), item);
            mMapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds));


        }else {
            //gps 사용할수 없으므로
            gps.showSettingsAlert();
        }
    }

    // 내주변의 편지 체크하기
    public void letterCheck(double lati, double longi) {
        if (arrayList.size() > 0)
            arrayList.clear();
        arrayList = manager.selectAll1();

        for (int i = 0; i < LETTER_SIZE; i++)
        {

            Item item = new Item();
            item.title = "from." + arrayList.get(i).send_name;
            item.address = arrayList.get(i).address;
            item.latitude = arrayList.get(i).latitude;
            item.longitude = arrayList.get(i).longitude;
            item.longitude = arrayList.get(i).longitude;

            item.letter_id = arrayList.get(i).letter_id;
            item.send_id = arrayList.get(i).send_id;
            item.send_name = arrayList.get(i).send_name;
            item.state = arrayList.get(i).state;
            item.context = arrayList.get(i).context;

            MapPOIItem poiItem = new MapPOIItem();

            if (arrayList.get(i).state == 0) // 안읽은 거
            {
                if (getDistance(lati, longi, item.latitude, item.longitude) <= 500.0) { //읽을 수 있는 것
                    poiItem.setItemName("green");
                    poiItem.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                    poiItem.setCustomImageBitmap(letterGreen);
                    poiItem.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                    poiItem.setCustomSelectedImageBitmap(letterGreen);
                } else {
                    poiItem.setItemName("yellow");
                    poiItem.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                    poiItem.setCustomImageBitmap(letterYellow);
                    poiItem.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                    poiItem.setCustomSelectedImageBitmap(letterYellow);
                }
            } else {
                poiItem.setItemName("red");
                poiItem.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                poiItem.setCustomImageBitmap(letterRed);
                poiItem.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                poiItem.setCustomSelectedImageBitmap(letterRed);
            }
            poiItem.setTag(i);
            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(item.latitude, item.longitude);
            poiItem.setMapPoint(mapPoint);
            poiItem.setCustomImageAutoscale(false);
            poiItem.setCustomImageAnchor(0.5f, 1.0f);

            mMapView.addPOIItem(poiItem);
            mTagItemMap.put(poiItem.getTag(), item);

        } // for
    }


    /******************* get distance method ************************/
    public Double getDistance(Double latitude_1, Double longitude_1, Double latitude_2, Double longitude_2)
    {
        Double distance = calDistance(latitude_1, longitude_1, latitude_2, longitude_2);
        return distance;
    }
    public static double calDistance(double lat1, double lon1, double lat2, double lon2){

        double theta, dist;
        theta = lon1 - lon2;
        dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);

        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;    //  mile to km
        dist = dist * 1000.0;      // km to m

        return dist;
    }

    //
    private static double deg2rad(double deg){
        return (double)(deg * Math.PI / (double)180d);
    }

    //
    private static double rad2deg(double rad){
        return (double)(rad * (double)180d / Math.PI);
    }

    /********************************************************/


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch(v.getId()) {
            case R.id.writeBtn:
                Intent write = new Intent(CardActivity.this, FriendActivity.class);
                startActivity(write);
                break;
            case R.id.shareBtn:
                Intent friend = new Intent(CardActivity.this, AddFriendActivity.class);
                startActivity(friend);
                break;
            case R.id.settingBtn:
                Intent setintent = new Intent(CardActivity.this, SettingActivity.class);
                startActivity(setintent);
                break;
            case R.id.synchBtn: // 이부분이 위도경도
                GPS_CONTRLL = (GPS_CONTRLL+1)%3;
                if(GPS_CONTRLL==1)
                    GPSGPS();
                else if(GPS_CONTRLL==2)
                {
                    gpsThread = new GpsThread(true);
                    gpsThread.start();
                }
                else
                    gpsThread.stopThread();
                break;
            case R.id.pushBtn:
                pushBtn.setVisibility(View.INVISIBLE);
                break;
        }
    }

    // 말풍선 어댑터
    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {

        private final View mCalloutBalloon;

        public CustomCalloutBalloonAdapter() {
            mCalloutBalloon = getLayoutInflater().inflate(R.layout.custom_balloon, null);
            setGlobalFont(mCalloutBalloon);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem poiItem) {
            if (poiItem == null) return null;
            Item item = mTagItemMap.get(poiItem.getTag());
            if (item == null) return null;
            TextView textViewTitle = (TextView) mCalloutBalloon.findViewById(R.id.title);
            textViewTitle.setText(item.title);
            TextView textViewDesc = (TextView) mCalloutBalloon.findViewById(R.id.desc);
            textViewDesc.setText(item.address);
            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return null;
        }

    }


    /* 말풍선 클릭*/
    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
        final Item item = mTagItemMap.get(mapPOIItem.getTag());

        LetterInfo letters = manager.dataGet(item.letter_id);
        LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflate.inflate(R.layout.letter_templete, null);
        setGlobalFont(layout);

        TextView tem_to = (TextView) layout.findViewById(R.id.tem_to);
        TextView tem_context = (TextView) layout.findViewById(R.id.tem_context);
        TextView tem_date = (TextView) layout.findViewById(R.id.tem_date);
        TextView tem_from = (TextView) layout.findViewById(R.id.tem_from);
        Button tem_close = (Button) layout.findViewById(R.id.tem_close);

        AlertDialog.Builder aDialog = new AlertDialog.Builder(ctx);

        aDialog.setView(layout);
        final AlertDialog ad = aDialog.create();

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        WindowManager.LayoutParams params = ad.getWindow().getAttributes();


        // AlertDialog 에서 위치 크기 수정


        if (mapPOIItem.getItemName().equals("green") || mapPOIItem.getItemName().equals("red")) //   (읽을 수 있음)
        {
            tem_to.setText("To. " + user_name);
            tem_context.setText(letters.context);
            tem_date.setText(letters.date);
            tem_from.setText("From. " + item.send_name);

            ad.show();
            params.width = width / 10 * 9;
            params.height = height / 10 * 9;
            ad.getWindow().setAttributes(params);

            tem_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = "http://52.78.88.51:8080/letter/updateLetterState.do";
                    updateLetterState(url, String.valueOf(item.letter_id));
                    manager.updateData1(item.letter_id); // 읽음 상태 갱신
                    ad.cancel();
                }
            });

        } else if (mapPOIItem.getItemName().equals("yellow")) { //   (읽을 수 없음)

            tem_context.setHeight(100);
            tem_to.setVisibility(View.GONE);
            tem_context.setText("\"읽을 수 없습니다\" \n편지 근처라면 동기화를 해주세요!");
            tem_date.setVisibility(View.GONE);
            tem_from.setVisibility(View.GONE);

            ad.show();


            params.width = width / 10 * 9;
            ad.getWindow().setAttributes(params);

            //params.height = height/10*9;

            tem_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ad.cancel();
                }
            });
        }


    }


    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {  }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) { }



    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("letter", "onNewIntent() called.");
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(d).toString();
        editor.putString("access_time", date);
        editor.commit();
        processIntent(intent);

        super.onNewIntent(intent);
    }

    /**
     * 수신자로부터 전달받은 Intent 처리
     *
     * @param intent
     */
    private void processIntent(Intent intent) {
        int letter_id = Integer.parseInt(intent.getStringExtra("letter_id"));
        String from_id = intent.getStringExtra("from_id");
        String from_name = intent.getStringExtra("from_name");
        String content = intent.getStringExtra("content");
        double latitude = Double.parseDouble(intent.getStringExtra("latitude"));
        double longitude = Double.parseDouble(intent.getStringExtra("longitude"));
        String to_id = intent.getStringExtra("to_id");
        String to_name = intent.getStringExtra("to_name");
        String address = intent.getStringExtra("address");
        String date = intent.getStringExtra("date");
        String data = "";
        String msg = intent.getStringExtra("msg");
        try {
            data = URLDecoder.decode(msg, "euc-kr");
            content = URLDecoder.decode(content, "euc-kr");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //pushBtn.setVisibility(View.VISIBLE);
        pushBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(CardActivity.this, FaceLogin.class);
//                //intent.putExtra("id", user_id);
//                startActivity(intent);
//                finish();
            }
        });
        // 전역변수에 선언 된 편지table 내용을 setter
        setLetter(letter_id, to_id, to_name, from_id, from_name, content, latitude, longitude, address, date);
        Log.d("letter", "getter letter table : " + letter_id + " , " + to_id + " , " + to_name + " , " + from_id + ", " + from_name + " , " + content + " , " + latitude + " , " + longitude + ", " + date+", " + address + "!!");
        println("새로운 편지가 도착했습니다 : " + data);
        comparePush();
    }

    private void println(String msg) {
        final String output = msg;
        handler.post(new Runnable() {
            public void run() {
                Log.d("letter", output);
                Toast toast = Toast.makeText(CardActivity.this, output,Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    //letter table attribute setter
    private void setLetter(int letter_id, String to_id, String to_name, String from_id, String from_name, String content, double latitude, double longitude, String address, String date) {
        this.letter_id = letter_id;
        this.to_id = to_id;
        this.to_name = to_name;
        this.from_id = from_id;
        this.from_name = from_name;
        this.content = content;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.date = date;
    }

    // push로 온 data getter
    private void getLetter() {
        int letter_id = Integer.parseInt(intent.getStringExtra("letter_id"));
        String from_id = intent.getStringExtra("from_id");
        String from_name = intent.getStringExtra("from_name");
        String content = intent.getStringExtra("content");
        String address = intent.getStringExtra("address");
        double latitude = Double.parseDouble(intent.getStringExtra("latitude"));
        double longitude = Double.parseDouble(intent.getStringExtra("longitude"));
        String to_id = intent.getStringExtra("to_id");
        String to_name = intent.getStringExtra("to_name");
        String date = intent.getStringExtra("date");
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String access_date = sdf.format(d).toString();
        editor.putString("access_time", access_date);
        editor.commit();
        if (latitude != 0.0) {
            String data = "";

            String msg = intent.getStringExtra("msg");
            try {
                data = URLDecoder.decode(msg, "euc-kr");
                content = URLDecoder.decode(content, "euc-kr");
                address = URLDecoder.decode(address, "euc-kr");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        setLetter(letter_id, to_id, to_name, from_id, from_name, content, latitude, longitude, address, date);
        Log.d("letter", "getter letter table : " + letter_id + " , " + to_id + " , " + to_name + " , " + from_id + ", " + from_name + " , " + content + " , " + latitude + " , " + longitude + ", " + date+", " + address + "!!");
    }

    private void comparePush() {
        /* DB  */
        manager = new DBManager(this);
        // 임시 데이터 삽입
        //manager.insertData1(new LetterInfo(0, "보내는 id", "보내는사람 이름", "내용", "도봉산역", 37.6896072, 127.0441583, 0,"2016-07-21"), ctx);
        manager.insertData1(new LetterInfo(letter_id, from_id, from_name, content, address, latitude , longitude, 0, date),ctx);
        LETTER_SIZE = manager.letterSize();
        arrayList = manager.selectAll1();
        manager.selectAll2();
    }

    /*************************letter read ***********************/
    public String updateLetterState(String strurl, String letter_id)
    {
        String response_msg =  null;
        try {
            String data = URLEncoder.encode("letter_id", "EUC-KR") + "=" + URLEncoder.encode(""+letter_id, "EUC-KR");

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

    // User name, id 불러오기
    private void getUser(){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        user_name = pref.getString("user_name", "");
        user_id = pref.getString("user_id", "");
    }

    // 폰트 적용
    void setGlobalFont(View view) {
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


    // gps Thread class
    class GpsThread  extends Thread {
        private int i = 0;
        private boolean isPlay = false;

        public GpsThread(boolean isPlay){
            this.isPlay = isPlay;
        }

        public void stopThread(){
            isPlay = !isPlay;
        }
        @Override
        public void run() {
            super.run();
            while (isPlay) {
                try { Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        GPSGPS();
                    }
                });
            }
        }
    }
}