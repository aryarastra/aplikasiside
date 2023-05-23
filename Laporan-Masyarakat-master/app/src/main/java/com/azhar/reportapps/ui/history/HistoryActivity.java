package com.azhar.reportapps.ui.history;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.azhar.reportapps.R;
import com.azhar.reportapps.model.ModelDatabase;
import com.azhar.reportapps.ui.history.HistoryAdapter;
import com.azhar.reportapps.viewmodel.HistoryViewModel;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes4.dex */
public class HistoryActivity extends AppCompatActivity implements HistoryAdapter.HistoryAdapterCallback {
    HistoryAdapter historyAdapter;
    HistoryViewModel historyViewModel;
    List<ModelDatabase> modelDatabaseList = new ArrayList();
    RecyclerView rvHistory;
    Toolbar toolbar;
    TextView tvNotFound;

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        setStatusBar();
        setToolbar();
        setInitLayout();
        setViewModel();
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.toolbar = toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setInitLayout() {
        this.rvHistory = (RecyclerView) findViewById(R.id.rvHistory);
        TextView textView = (TextView) findViewById(R.id.tvNotFound);
        this.tvNotFound = textView;
        textView.setVisibility(8);
        this.historyAdapter = new HistoryAdapter(this, this.modelDatabaseList, this);
        this.rvHistory.setHasFixedSize(true);
        this.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        this.rvHistory.setAdapter(this.historyAdapter);
    }

    private void setViewModel() {
        HistoryViewModel historyViewModel = (HistoryViewModel) ViewModelProviders.of(this).get(HistoryViewModel.class);
        this.historyViewModel = historyViewModel;
        historyViewModel.getDataLaporan().observe(this, new Observer() { // from class: com.azhar.reportapps.ui.history.HistoryActivity$$ExternalSyntheticLambda2
            @Override // androidx.lifecycle.Observer
            public final void onChanged(Object obj) {
                HistoryActivity.this.m34xe633a746((List) obj);
            }
        });
    }

    /* renamed from: lambda$setViewModel$0$com-azhar-reportapps-ui-history-HistoryActivity */
    public /* synthetic */ void m34xe633a746(List modelDatabases) {
        if (modelDatabases.isEmpty()) {
            this.tvNotFound.setVisibility(0);
            this.rvHistory.setVisibility(8);
        } else {
            this.tvNotFound.setVisibility(8);
            this.rvHistory.setVisibility(0);
        }
        this.historyAdapter.setDataAdapter(modelDatabases);
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

    @Override // com.azhar.reportapps.ui.history.HistoryAdapter.HistoryAdapterCallback
    public void onDelete(final ModelDatabase modelDatabase) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Hapus riwayat ini?");
        alertDialogBuilder.setPositiveButton("Ya, Hapus", new DialogInterface.OnClickListener() { // from class: com.azhar.reportapps.ui.history.HistoryActivity$$ExternalSyntheticLambda0
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                HistoryActivity.this.m33x8b3b68ef(modelDatabase, dialogInterface, i);
            }
        });
        alertDialogBuilder.setNegativeButton("Batal", new DialogInterface.OnClickListener() { // from class: com.azhar.reportapps.ui.history.HistoryActivity$$ExternalSyntheticLambda1
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /* renamed from: lambda$onDelete$1$com-azhar-reportapps-ui-history-HistoryActivity */
    public /* synthetic */ void m33x8b3b68ef(ModelDatabase modelDatabase, DialogInterface dialogInterface, int i) {
        int uid = modelDatabase.uid;
        this.historyViewModel.deleteDataById(uid);
        Toast.makeText(this, "Yeay! Data yang dipilih sudah dihapus", 0).show();
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 16908332) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
