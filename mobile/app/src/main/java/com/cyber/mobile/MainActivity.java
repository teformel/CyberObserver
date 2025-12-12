package com.cyber.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.cyber.mobile.service.CyberAccessibilityService;
import com.cyber.mobile.service.CyberSensorService;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Simple UI constructed programmatically for source compactness
        // In real app, use layout XML
        
        Button btn = new Button(this);
        btn.setText("Enable God Mode (Accessibility)");
        btn.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        });

        Button btnService = new Button(this);
        btnService.setText("Start Sensor Service");
        btnService.setOnClickListener(v -> {
            startService(new Intent(this, CyberSensorService.class));
        });
        
        setContentView(btn);
    }
}
