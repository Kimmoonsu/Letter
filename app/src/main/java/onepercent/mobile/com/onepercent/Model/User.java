package onepercent.mobile.com.onepercent.Model;

/**
 * Created by moonsu on 2016-07-20.
 */

// 사용자  ID, Name Data Model
public class User {
    private static User instance = new User();

    public static User getInstance() { return instance; }
    public User() {}

    private String user_id;
    private String user_name;
    public void setUser_id(String user_id) { this.user_id = user_id; }
    public void setUser_name(String user_name) { this.user_name = user_name; }
    public String getUser_id() { return user_id; }
    public String getUser_name() { return user_name; }

}
