package onepercent.mobile.com.onepercent;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;

import onepercent.mobile.com.onepercent.Map.Item;
import onepercent.mobile.com.onepercent.Map.MapApiConst;
import onepercent.mobile.com.onepercent.Model.User;
import onepercent.mobile.com.onepercent.SQLite.DBManager;
import onepercent.mobile.com.onepercent.SQLite.LetterInfo;


public class CardActivity extends Activity implements View.OnClickListener, POIItemEventListener {
    //user
    String user_id, user_name;

    // letter
    int letter_id;
    String to_id, to_name, content, address, from_id, from_name;
    double latitude, longitude;

    Handler handler = new Handler();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        User user = User.getInstance();
        user_id = user.getUser_id();
        user_name = user.getUser_name();
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


//        manager.insertData1(new LetterInfo(0, "보내는 id", "보내는사람 이름", "내용", "도봉산역", 37.6896072, 127.0441583, 0), ctx);
//        manager.insertData1(new LetterInfo(1,  "보내는 id", "보내는사람 이름", "내용", "도봉역", 37.6794452,127.0433323, 0), ctx);
//        manager.insertData1(new LetterInfo(2, "보내는 id", "보내는사람 이름", "내용", "성신여대역", 37.5927242, 127.0143553, 0), ctx);
//        manager.insertData1(new LetterInfo(3,   "보내는 id",   "보내는사람 이름",  "내용","성신여대" ,37.5913145,127.0199425,  0),ctx);
//        manager.insertData1(new LetterInfo(4, "보내는 id", "보내는사람 이름", "내용", "상지초등학교", 37.493405, 126.763322, 0), ctx);
//        manager.insertData1(new LetterInfo(5,   "보내는 id",   "보내는사람 이름",  "내용","우리집" ,   37.4915496,126.7542664,  0),ctx);


        LETTER_SIZE = manager.nonstateSize();
        arrayList = manager.selectAllstate();
        manager.selectAll2();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapViewContainer.removeAllViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LETTER_SIZE = manager.nonstateSize();
        arrayList = manager.selectAllstate();
        // 지도
        mMapView =  new MapView(this);
        mMapView.setDaumMapApiKey(MapApiConst.DAUM_MAPS_ANDROID_APP_API_KEY);
        mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mMapView);
        mMapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(37.5041151, 127.0447707), 1, true);
        mMapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());
        mMapView.setPOIItemEventListener(this);

        MapPointBounds mapPointBounds = new MapPointBounds();
        Bitmap letter =  BitmapFactory.decodeResource(getResources(), R.drawable.letter);
        letter = Bitmap.createScaledBitmap(letter,60,75, true);
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
            poiItem.setItemName("from."+arrayList.get(i).send_name);
            poiItem.setTag(i);
            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord( item.latitude, item.longitude);
            poiItem.setMapPoint(mapPoint);
            mapPointBounds.add(mapPoint);
            poiItem.setMarkerType(MapPOIItem.MarkerType.CustomImage);
            poiItem.setCustomImageBitmap(letter);
            poiItem.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
            poiItem.setCustomSelectedImageBitmap(letter);
            poiItem.setCustomImageAutoscale(false);
            poiItem.setCustomImageAnchor(0.5f, 1.0f);




            mMapView.addPOIItem(poiItem);
            mTagItemMap.put(poiItem.getTag(), item);

        }
        mMapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds));
    }

    public  void GPSGPS(){
        gps= new GpsInfo(CardActivity.this);
        //GPS 사용유무 가져오기다
        if(gps.isGetLocation()){
            MapPointBounds mapPointBounds = new MapPointBounds();
            double latitude = gps.getLatitude(); //위도다
            double longitude = gps.getLongitude(); //경도다
            LETTER_SIZE = manager.nonstateSize();

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
    public void letterCheck(double lati, double longi)
    {
        if(arrayList.size()>0)
            arrayList.clear();
        arrayList = manager.selectAllstate();
        Bitmap letter =  BitmapFactory.decodeResource(getResources(), R.drawable.letter);
        letter = Bitmap.createScaledBitmap(letter, 60, 75, true);
        Bitmap letter1 =  BitmapFactory.decodeResource(getResources(), R.drawable.letter1);
        letter1 = Bitmap.createScaledBitmap(letter1, 60, 75, true);

        for (int i = 0; i < LETTER_SIZE; i++) {

            Item item = new Item();
            item.title =  "from."+arrayList.get(i).send_name;
            item.address =  arrayList.get(i).address;
            item.latitude =  arrayList.get(i).latitude;
            item.longitude =  arrayList.get(i).longitude;
            item.longitude =  arrayList.get(i).longitude;

            item.letter_id =  arrayList.get(i).letter_id;
            item.send_id =  arrayList.get(i).send_id;
            item.send_name =  arrayList.get(i).send_name;
            item.state =  arrayList.get(i).state;
            item.context =  arrayList.get(i).context;

            MapPOIItem poiItem = new MapPOIItem();


            if(getDistance(lati, longi, item.latitude, item.longitude) <= 500.0) {
                poiItem.setItemName("possible");
                poiItem.setTag(i);
                MapPoint mapPoint = MapPoint.mapPointWithGeoCoord( item.latitude, item.longitude);
                poiItem.setMapPoint(mapPoint);

                poiItem.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                poiItem.setCustomImageBitmap(letter1);
                poiItem.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                poiItem.setCustomSelectedImageBitmap(letter1);
            }
            else
            {
                poiItem.setItemName("impossible");
                        poiItem.setTag(i);
                MapPoint mapPoint = MapPoint.mapPointWithGeoCoord( item.latitude, item.longitude);
                poiItem.setMapPoint(mapPoint);

                poiItem.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                poiItem.setCustomImageBitmap(letter);
                poiItem.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
                poiItem.setCustomSelectedImageBitmap(letter);
            }
            poiItem.setCustomImageAutoscale(false);
            poiItem.setCustomImageAnchor(0.5f, 1.0f);

            mMapView.addPOIItem(poiItem);
            mTagItemMap.put(poiItem.getTag(), item);

        }
    }

    // 비트맵 비교하기
//    public boolean sameAs(Bitmap bitmap)
//    {
//        Bitmap original =  BitmapFactory.decodeResource(getResources(), R.drawable.letter1);
//        original = Biㅁtmap.createScaledBitmap(original, 60, 75, true);
//
//        ByteBuffer bf1 = ByteBuffer.allocate(original.getHeight() * original.getRowBytes());
//        original.copyPixelsToBuffer(bf1);
//
//        ByteBuffer bf2 = ByteBuffer.allocate(bitmap.getHeight() * bitmap.getRowBytes());
//        bitmap.copyPixelsToBuffer(bf2);
//
//        return Arrays.equals(bf1.array(),bf2.array());
//
//
//    }



    /******************* get distance method ************************/
    public Double getDistance(Double latitude_1, Double longitude_1, Double latitude_2, Double longitude_2)
    {
        Double distance = calDistance(latitude_1, longitude_1, latitude_2, longitude_2);
        System.out.println("거리: " + distance);
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
    /************************************/


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
                GPSGPS();

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
        Log.d("letter", "Click");

//        Bitmap bit = mapPOIItem.getCustomImageBitmap();
//        sameAs(bit)
        AlertDialog.Builder ab = new AlertDialog.Builder(ctx);

        if(mapPOIItem.getItemName().equals("possible")) //   (읽을 수 있음)
        {
           ab.setTitle("편지를 읽으시겠습니까?");

            ab.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(CardActivity.this, item.send_id+" "+item.send_name+" "+item.address+" "+item.context+" "+item.latitude+" "+item.longitude, Toast.LENGTH_SHORT).show();
                }
            });

            ab.setNegativeButton("cancle", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {            }
            });
            ab.show();
        }
        else{ //   (읽을 수 없음)
            ab.setTitle("거리가 멀어 읽을 수 없습니다.");
            ab.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Toast.makeText(CardActivity.this, item.send_id+" "+item.send_name+" "+item.address+" "+item.context+" "+item.latitude+" "+item.longitude, Toast.LENGTH_SHORT).show();
                }
            });
            ab.setNegativeButton("cancle", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
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
        setLetter(letter_id, to_id, to_name, from_id, from_name, content, latitude, longitude, address);
        Log.d("letter", "letter table : " + letter_id + " , " + to_id + " , " + to_name + " , " + from_id + ", " + from_name + " , " + content + " , " + latitude + " , " + longitude + " , " + address + "!!");
        println("새로운 메시지가 도착했습니다 : " + data);
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
    private void setLetter(int letter_id, String to_id, String to_name, String from_id, String from_name, String content, double latitude, double longitude, String address) {
        this.letter_id = letter_id;
        this.to_id = to_id;
        this.to_name = to_name;
        this.from_id = from_id;
        this.from_name = from_name;
        this.content = content;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
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
        setLetter(letter_id, to_id, to_name, from_id, from_name, content, latitude, longitude, address);
        Log.d("letter", "getter letter table : " + letter_id + " , " + to_id + " , " + to_name + " , " + from_id + ", " + from_name + " , " + content + " , " + latitude + " , " + longitude + "!!");
    }

    private void comparePush() {
        /* DB  */
        manager = new DBManager(this);
        // 임시 데이터 삽입
        manager.insertData1(new LetterInfo(letter_id,  from_id, from_name, content, address, latitude , longitude,  0),ctx);
        LETTER_SIZE = manager.nonstateSize();
        arrayList = manager.selectAllstate();
        manager.selectAll2();

    }


}