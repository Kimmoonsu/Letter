package onepercent.mobile.com.onepercent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class TutorialActivity extends Activity {
    ImageView tutorialView;
    int ids[] = {R.drawable.tutorial0, R.drawable.tutorial1, R.drawable.tutorial2, R.drawable.tutorial3, R.drawable.tutorial4};
    int CLICK_COUTN=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        tutorialView = (ImageView) findViewById(R.id.tutorialView);

        tutorialView.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CLICK_COUTN==5)
                {
                    Intent intent = new Intent(TutorialActivity.this, CardActivity.class);
                    intent.putExtra("letter_id", "0");
                    intent.putExtra("from_id", "");
                    intent.putExtra("from_name", "");
                    intent.putExtra("to_id", "");
                    intent.putExtra("to_name", "");
                    intent.putExtra("content", "");
                    intent.putExtra("latitude", "0.0");
                    intent.putExtra("longitude", "0.0");
                    intent.putExtra("address", "");
                    intent.putExtra("date", "");
                    startActivity(intent);
                    finish();
                } else{
                    tutorialView.setImageDrawable(getResources().getDrawable(ids[CLICK_COUTN]));
                    CLICK_COUTN++;
                }

            }
        });

    }
}
