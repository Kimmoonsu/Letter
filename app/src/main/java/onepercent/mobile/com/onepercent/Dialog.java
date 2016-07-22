package onepercent.mobile.com.onepercent;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;


public class Dialog extends Activity {
    DialogThread dialogThread = null;

    private boolean dialogState = true;
    ImageView cardImage = null;
    int index = 0;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_dialog);
        index = 0;
        dialogState = true;
        cardImage = (ImageView)findViewById(R.id.cardImage);
        dialogThread = new DialogThread();
        dialogThread.start();

    }
    public class DialogThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (dialogState)
            {
                try{
                    handle.sendMessage(handle.obtainMessage());
                    sleep(1000);

                } catch (Throwable t){}
            }
            Intent intent = new Intent(Dialog.this, FaceLogin.class);
            startActivity(intent);
            finish();
            Log.d("dialog", "Thread : The end ");
        }

        Handler handle = new Handler()
        {
            @Override
            public void handleMessage(Message msg) {
                dialogState = index < 2 ? true : false;
                index++;
            }
        };
    }
}
