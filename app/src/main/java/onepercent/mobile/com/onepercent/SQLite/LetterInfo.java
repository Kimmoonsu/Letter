package onepercent.mobile.com.onepercent.SQLite;

public class LetterInfo {
    public int letter_id;
    public String send_id;
    public  String send_name;
    public String context;
    public String address;
    public Double latitude;
    public Double longitude;
    public int state;
    public String date;

    public LetterInfo(int letter_id, String send_id,  String send_name, String context, String address,Double latitude,   Double longitude, int state, String date)
    {
        //letter_id,  send_id,   send_name,  context, latitude,    longitude,  state
        this.letter_id = letter_id;
        this.send_id = send_id;
        this.send_name = send_name;
        this.context = context;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.state = state;
        this.date = date;
    }
}