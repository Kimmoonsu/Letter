package com.kakao.sdk.link.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.kakao.sdk.link.sample.kakaolink.KakaoLinkMainActivity;
import com.kakao.sdk.link.sample.storylink.KakaoStoryLinkMainActivity;

public class KakaoServiceListActivity extends Activity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_service_list);

        findViewById(R.id.kakao_link).setOnClickListener(this);
        findViewById(R.id.kakao_story).setOnClickListener(this);

        findViewById(R.id.title_back).setVisibility(View.GONE);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.kakao_link:
                startActivity(new Intent(this, KakaoLinkMainActivity.class));
                break;
            case R.id.kakao_story:
                startActivity(new Intent(this, KakaoStoryLinkMainActivity.class));
                break;
        }
    }
}
