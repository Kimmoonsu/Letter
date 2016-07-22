package onepercent.mobile.com.onepercent.Model;

/**
 * Created by moonsu on 2016-07-20.
 */

// 사용자  ID, Name Data Model
public class User {
    private static User instance = new User();

    public static User getInstance() { return instance; }
    public User() {}

    // tutorial state
    private boolean tutorial_state = false;
    public boolean getTutorial_State() { return tutorial_state; }
    public void setTutorial_state(boolean tutorial_state) { this.tutorial_state = tutorial_state; }

    private String user_id;
    private String user_name;
    private String user_date; // 앱 접속 시각
    private String close_date; // 앱 종료 시각
    private String install_date=""; // 앱 설치 시각
    public void setUser_id(String user_id) { this.user_id = user_id; }
    public void setUser_name(String user_name) { this.user_name = user_name; }
    public void setUser_date(String user_date) { this.user_date = user_date; }
    public void setInstall_date(String install_date) { this.install_date = install_date; }
    public void setClose_date(String close_date) { this.close_date = close_date; }
    public String getUser_id() { return user_id; }
    public String getUser_name() { return user_name; }
    public String getUser_date() { return user_date; }
    public String getInstall_date() { return install_date; }
    public String getClose_date() { return close_date; }

}
