package onepercent.mobile.com.onepercent;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;


public class GpsInfo extends Service implements LocationListener {


    private final Context mContext;

    //현재 gps 사용 유무
    boolean isGPSEnabled = false;

    // 네트워크 사용유무
    boolean isNetworkEnabled = false;

    //GSP 상태값
    boolean isGetLocation = false;

    Location location ;

    double lat; // 위도
    double lon; // 경도

    //최소 GPS 정보 업데이트 거리 10미터
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;

    //최소 GPS 정보 업데이트 시간 밀리세컨이므로 1분
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    protected LocationManager locationManager;

    public GpsInfo(Context context)
    {
        this.mContext = context;
        getLocation();
    }

    public Location getLocation() {
        try{
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            //Gps 정보가져오기
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            //현재 네트워크 상태 값 알아오기
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


            if(!isGPSEnabled && !isNetworkEnabled) {
                //GPS 와 네트워크 사용이 가능하지 않을때 소스 구현
            }
            else{
                this.isGetLocation = true;
                //네트워크 정보로 부터 위치값 가져오기
                if(isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES,this);

                    if(locationManager != null)
                    {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if(location != null)
                        {
                            //위도 경도 저장
                            lat = location.getLatitude();
                            lon = location.getLongitude();
                        }
                    }
                }

                if(isGPSEnabled) {
                    if(location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES,this);
                        if(locationManager != null)
                        {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if(location != null){
                                lat = location.getLatitude();
                                lon = location.getLongitude();
                            }
                        }

                    }
                }

            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return location;
    }

    /** GPS 종료 **/
    public void stopUsingGPS(){
        if(locationManager != null)
        {
            locationManager.removeUpdates(GpsInfo.this);

        }
    }

    /** 위도값을 가져온다 **/

    public double getLatitude(){
        if(location!=null)
        {
            lat = location.getLatitude();
        }
        return lat;
    }

    /** 경도값을 가져온다 **/
    public double getLongitude(){
        if(location!=null)
        {
            lon = location.getLongitude();
        }
        return lon;
    }

    /** GPS 나 WIFE 정보가 켜져있는지 확인 **/

    public boolean isGetLocation() {

        return this.isGetLocation;
    }

    /** GPS 정보를 가져오지 못했을때
     *  설정값으로 갈지 물어보는 alert 창
     * **/

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        alertDialog.setTitle("! 위치 서비스 사용 알림 !");
        alertDialog.setMessage("내 위치 정보를 사용하려면, \n설정하기를 눌러주세요. ");

        //OK눌렀을때 설정창 이동

        alertDialog.setPositiveButton("설정하기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        //Cancle 하면 종료합니다.
        alertDialog.setNegativeButton("취소하기",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }




    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
