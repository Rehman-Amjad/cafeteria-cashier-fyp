package com.technogenis.cafeteriacashier;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.technogenis.cafeteriacashier.util.EdgeToEdgeHelper;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_TIME_OUT = 1500L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable navigate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        View root = findViewById(R.id.main);
        TextView textView = findViewById(R.id.textView);

        EdgeToEdgeHelper.applySystemBarsPadding(root, true, true);

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.myanimation);
        textView.startAnimation(anim);

        final MyPreferenceManager prefs = MyPreferenceManager.getInstance(this);
        navigate = () -> {
            String rfid = prefs.getString("rfid");
            Intent next = (rfid != null && !rfid.isEmpty())
                    ? new Intent(SplashActivity.this, DashboardActivity.class)
                    : new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(next);
            finish();
        };
        handler.postDelayed(navigate, SPLASH_TIME_OUT);
    }

    @Override
    protected void onDestroy() {
        if (navigate != null) handler.removeCallbacks(navigate);
        super.onDestroy();
    }
}
