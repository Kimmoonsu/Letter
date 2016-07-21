package onepercent.mobile.com.onepercent.SQLite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

//DB를 총괄관리
public class DBManager {

    private static final String SD_PATH = Environment
            .getExternalStorageDirectory().getAbsolutePath();
    private final String ROOT_DIR = SD_PATH + "/Letter/";

    // DB관련 상수 선언
    private static final String dbName = "Letter.db";
    private static final String tableName = "Friend";
    private static final String tableName1 = "rLetterBox";
    private static final String sendTable = "sLetterBox";
    public static final int dbVersion = 1;

    // DB관련 객체 선언
    private OpenHelper opener; // DB opener
    private SQLiteDatabase db; // DB controller

    // 부가적인 객체들
    private Context context;

    // 생성자
    public DBManager(Context context) {
        this.context = context;
        this.opener = new OpenHelper(context, dbName, null, dbVersion);
        db = opener.getWritableDatabase();
    }

    // Opener of DB and Table
    private class OpenHelper extends SQLiteOpenHelper {

        public OpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, null, version);
            // TODO Auto-generated constructor stub
        }

        // 생성된 DB가 없을 경우에 한번만 호출됨
        @Override
        public void onCreate(SQLiteDatabase arg0) {
            String createSql = "create table " + tableName + " (id text not null, nickname text not null, primary key(id));";
            String createSql1 = "create table " + tableName1 + " (letter_id int not null,send_id text not null, send_name text not null, context text not null, address text not null, latitude double not null, longitude double not null, state int not null ,date text not null ,  primary key(letter_id));";
            String createSql2 = "create table " + sendTable + " (letter_id INTEGER primary key  autoincrement, send_id text not null, send_name text not null, context text not null, address text not null, latitude double not null, longitude double not null, state int not null,date text not null);";

            arg0.execSQL(createSql);
            arg0.execSQL(createSql1);
            arg0.execSQL(createSql2);

            Toast.makeText(context, "DB is opened", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
        }
    }

    /**************************************** Friend Table **********************************************/
    // 데이터 추가
    public void insertData(FriendInfo info,Context context) {
        String sql = "insert into " + tableName + " values("+info.id+",'"+info.nickname+"');";
        try{
            db.execSQL(sql);
        }catch(Exception e) {
        }
    }

    // 데이터 전체 검색
    public ArrayList<FriendInfo> selectAll() {
        String sql = "select * from " + tableName + ";";
        Cursor results = db.rawQuery(sql, null);

        results.moveToFirst();
        ArrayList<FriendInfo> infos = new ArrayList<FriendInfo>();

        while (!results.isAfterLast()) {
            FriendInfo info = new FriendInfo(results.getString(0), results.getString(1));
            infos.add(info);
            results.moveToNext();
        }
        results.close();
        return infos;
    }

    public Boolean dataCheck(String id)
    {
        String sql = "select * from " + tableName + " where id like '" + id   + "';";
        Cursor result = db.rawQuery(sql, null);

        // result(Cursor 객체)가 비어 있으면 false 리턴
        if (result.moveToFirst()) {
            FriendInfo info = new FriendInfo(result.getString(0), result.getString(1));
            result.close();
            return true;
        }
        return false;
    }

    public ArrayList<FriendInfo> dataSelect(String name)
    {
        String sql = "select * from " + tableName + " where nickname like '%"+name+"%';";
        Cursor results = db.rawQuery(sql, null);

        results.moveToFirst();
        ArrayList<FriendInfo> infos = new ArrayList<FriendInfo>();

        while (!results.isAfterLast()) {
            FriendInfo info = new FriendInfo(results.getString(0), results.getString(1));
            infos.add(info);
            results.moveToNext();
        }
        results.close();
        return infos;
    }

    /**************************************** recive LetterBox Table **********************************************/
    // 데이터 추가
    public void insertData1(LetterInfo info,Context context) {
        String sql = "insert into " + tableName1 + " values("+info.letter_id+",'"+info.send_id+"','"+info.send_name+"','"+info.context+"','"+info.address+"',"+info.latitude+","+info.longitude+","+info.state+",'"+info.date+"');";
        try{
            db.execSQL(sql);
        }catch(Exception e){
        }
    }
    // 데이터 갱신
    public void updateData1(int index) {
        String sql = "update " + tableName1 + " set state=1  where letter_id = " + index+";";

        try{
            db.execSQL(sql);
        }catch(Exception e){
            Log.d("database","database 갱신 에러 !!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    public ArrayList<LetterInfo> selectAll1() {
        String sql = "select * from " + tableName1 + ";";
        Cursor results = db.rawQuery(sql, null);

        results.moveToFirst();
        ArrayList<LetterInfo> infos = new ArrayList<LetterInfo>();

        while (!results.isAfterLast()) {
            LetterInfo info = new LetterInfo(results.getInt(0), results.getString(1),results.getString(2),results.getString(3),results.getString(4),results.getDouble(5),results.getDouble(6),results.getInt(7),results.getString(8));
            infos.add(info);
            Log.d("letter",results.getInt(0)+results.getString(1)+results.getString(2)+results.getString(3));
            results.moveToNext();
        }
        results.close();
        return infos;
    }

    public LetterInfo dataGet(int let_id) {
        String sql = "select * from " + tableName1 + " where letter_id ="+let_id+";";
        Cursor results = db.rawQuery(sql, null);

        results.moveToFirst();
        LetterInfo infos = null;

        while (!results.isAfterLast()) {
            infos = new LetterInfo(results.getInt(0), results.getString(1),results.getString(2),results.getString(3),results.getString(4),results.getDouble(5),results.getDouble(6),results.getInt(7),results.getString(8));

            Log.d("letter",results.getInt(0)+results.getString(1)+results.getString(2)+results.getString(3));
            results.moveToNext();
        }
        results.close();
        return infos;
    }


    public int letterSize() {
        String sql = "select * from " + tableName1 + " ;";
        Cursor results = db.rawQuery(sql, null);
        int size =results.getCount();
        results.close();
        return size;
    }

    /**************************************** send LetterBox Table **********************************************/
    // 데이터 추가
    public void insertData2(LetterInfo info,Context context) {
        String sql = "insert into " + sendTable + "(send_id , send_name , context , address , latitude , longitude , state ,date) values('"+info.send_id+"','"+info.send_name+"','"+info.context+"','"+info.address+"',"+info.latitude+","+info.longitude+","+info.state+",'"+info.date+"');";
        Log.d("database", "send sql : " + sql);

        try{
            db.execSQL(sql);
            Log.d("database", "send inset");
        }catch(Exception e){
            Log.d("database", e.getMessage());
        }
    }

    public ArrayList<LetterInfo> selectAll2() {
        String sql = "select * from " + sendTable + ";";
        Cursor results = db.rawQuery(sql, null);
        Log.d("letter","select all");
        results.moveToFirst();
        ArrayList<LetterInfo> infos = new ArrayList<LetterInfo>();

        while (!results.isAfterLast()) {
            LetterInfo info = new LetterInfo(results.getInt(0), results.getString(1),results.getString(2),results.getString(3),results.getString(4),results.getDouble(5),results.getDouble(6),results.getInt(7),results.getString(8));
            infos.add(info);
            Log.d("database",results.getInt(0)+results.getString(1)+results.getString(2)+results.getString(3)+results.getString(4)+results.getDouble(5));
            results.moveToNext();
        }
        results.close();
        return infos;
    }


}