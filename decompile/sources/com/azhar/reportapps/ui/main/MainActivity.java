package com.azhar.reportapps.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import com.azhar.reportapps.R;
import com.azhar.reportapps.ui.history.HistoryActivity;
import com.azhar.reportapps.ui.report.ReportActivity;
import com.azhar.reportapps.utils.Constant;
import im.delight.android.location.SimpleLocation;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/* loaded from: classes3.dex */
public class MainActivity extends AppCompatActivity {
    int REQ_PERMISSION = 100;
    CardView cvAmbulance;
    CardView cvBencana;
    CardView cvHistory;
    SimpleLocation simpleLocation;
    double strCurrentLatitude;
    String strCurrentLocation;
    double strCurrentLongitude;
    String strTitle;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setStatusBar();
        setPermission();
        setLocation();
        setInitLayout();
        setCurrentLocation();
    }

    private void setLocation() {
        SimpleLocation simpleLocation = new SimpleLocation(this);
        this.simpleLocation = simpleLocation;
        if (!simpleLocation.hasLocationEnabled()) {
            SimpleLocation.openSettings(this);
        }
        this.strCurrentLatitude = this.simpleLocation.getLatitude();
        this.strCurrentLongitude = this.simpleLocation.getLongitude();
        this.strCurrentLocation = this.strCurrentLatitude + "," + this.strCurrentLongitude;
    }

    private void setInitLayout() {
        this.cvAmbulance = (CardView) findViewById(R.id.cvAmbulance);
        this.cvBencana = (CardView) findViewById(R.id.cvBencana);
        this.cvHistory = (CardView) findViewById(R.id.cvHistory);
        this.cvAmbulance.setOnClickListener(new View.OnClickListener() { // from class: com.azhar.reportapps.ui.main.MainActivity$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m36lambda$setInitLayout$0$comazharreportappsuimainMainActivity(view);
            }
        });
        this.cvBencana.setOnClickListener(new View.OnClickListener() { // from class: com.azhar.reportapps.ui.main.MainActivity$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m37lambda$setInitLayout$1$comazharreportappsuimainMainActivity(view);
            }
        });
        this.cvHistory.setOnClickListener(new View.OnClickListener() { // from class: com.azhar.reportapps.ui.main.MainActivity$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m38lambda$setInitLayout$2$comazharreportappsuimainMainActivity(view);
            }
        });
    }

    /* renamed from: lambda$setInitLayout$0$com-azhar-reportapps-ui-main-MainActivity  reason: not valid java name */
    public /* synthetic */ void m36lambda$setInitLayout$0$comazharreportappsuimainMainActivity(View v) {
        this.strTitle = "Laporan";
        Intent intent = new Intent(this, ReportActivity.class);
        intent.putExtra(ReportActivity.DATA_TITLE, this.strTitle);
        startActivity(intent);
    }

    /* renamed from: lambda$setInitLayout$1$com-azhar-reportapps-ui-main-MainActivity  reason: not valid java name */
    public /* synthetic */ void m37lambda$setInitLayout$1$comazharreportappsuimainMainActivity(View v) {
        this.strTitle = "Aduan";
        Intent intent = new Intent(this, ReportActivity.class);
        intent.putExtra(ReportActivity.DATA_TITLE, this.strTitle);
        startActivity(intent);
    }

    /* renamed from: lambda$setInitLayout$2$com-azhar-reportapps-ui-main-MainActivity  reason: not valid java name */
    public /* synthetic */ void m38lambda$setInitLayout$2$comazharreportappsuimainMainActivity(View v) {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }

    private void setCurrentLocation() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(this.strCurrentLatitude, this.strCurrentLongitude, 1);
            if (addressList != null && addressList.size() > 0) {
                Constant.lokasiPengaduan = addressList.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setPermission() {
        if (ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != 0 && ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION"}, this.REQ_PERMISSION);
        }
    }

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int grantResult : grantResults) {
            if (grantResult == 0) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setStatusBar() {
        if (Build.VERSION.SDK_INT >= 23) {
            getWindow().getDecorView().setSystemUiVisibility(9472);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, 67108864, false);
            getWindow().setStatusBarColor(0);
        }
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        if (on) {
            layoutParams.flags |= bits;
        } else {
            layoutParams.flags &= ~bits;
        }
        window.setAttributes(layoutParams);
    }
}
