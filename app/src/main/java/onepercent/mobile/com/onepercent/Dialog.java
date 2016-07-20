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
    //int card[] = {R.drawable.card1, R.drawable.card2, R.drawable.card3, R.drawable.card4, R.drawable.card5, R.drawable.card6, R.drawable.card7, R.drawable.card8, R.drawable.card9, R.drawable.card10, R.drawable.card11, R.drawable.card12, R.drawable.card13, R.drawable.card14, R.drawable.card15, R.drawable.card16, R.drawable.card17, R.drawable.card18, R.drawable.card19, R.drawable.card20, R.drawable.card21, R.drawable.card22, R.drawable.card23, R.drawable.card24, R.drawable.card25, R.drawable.card26, R.drawable.card27, R.drawable.card28, R.drawable.card29, R.drawable.card30, R.drawable.card30, R.drawable.card30, R.drawable.card30, R.drawable.card30, R.drawable.card30,R.drawable.card30,R.drawable.card30,R.drawable.card30,R.drawable.card30,R.drawable.card30,R.drawable.card30,R.drawable.card30,R.drawable.card30,R.drawable.card30,R.drawable.card30,R.drawable.card30,R.drawable.card30,R.drawable.card30,R.drawable.card30,R.drawable.card30,R.drawable.card30,R.drawable.card30,R.drawable.card30 };
    //int card[] = {R.drawable.card1, R.drawable.card2, R.drawable.card3, R.drawable.card4, R.drawable.card5, R.drawable.card6, R.drawable.card7, R.drawable.card8, R.drawable.card9, R.drawable.card10, R.drawable.card11, R.drawable.card12, R.drawable.card12, R.drawable.card12, R.drawable.card12, R.drawable.card12, R.drawable.card12, R.drawable.card12  };
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_dialog);
        cardImage = (ImageView)findViewById(R.id.cardImage);
//
//        model = new Model();
//
//        dialogState = model.getDialogState();
//
//
//        dialogThread = new DialogThread();
//        dialogThread.start();

    }
    public class DialogThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (dialogState)
            {
                try{
                    if (index < 12)
                    {
                        handle.sendMessage(handle.obtainMessage());
                        sleep(60);
                    }
                    if (index >= 12)
                    {
                        sleep(2000);
                        dialogState = false;

                        //Intent intent = new Intent(Dialog.this, LoginActivity.class);

                    }
                } catch (Throwable t){}
            }
            Log.d("dialog", "Thread : The end ");
        }

        Handler handle = new Handler()
        {
            @Override
            public void handleMessage(Message msg) {
                Log.d("dialog", "Thread : " + index);
                //cardImage.setImageResource(card[index]);
                index++;
            }
        };
    }
}
