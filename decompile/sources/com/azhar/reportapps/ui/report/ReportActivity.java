package com.azhar.reportapps.ui.report;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import com.azhar.reportapps.R;
import com.azhar.reportapps.utils.BitmapManager;
import com.azhar.reportapps.utils.Constant;
import com.azhar.reportapps.viewmodel.InputDataViewModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/* loaded from: classes5.dex */
public class ReportActivity extends AppCompatActivity {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final String DATA_TITLE = "TITLE";
    public static final int REQUEST_PICK_PHOTO = 1;
    int REQ_CAMERA = 101;
    ExtendedFloatingActionButton fabSend;
    File fileDirectoty;
    File imageFilename;
    ImageView imageLaporan;
    InputDataViewModel inputDataViewModel;
    EditText inputLaporan;
    EditText inputLokasi;
    EditText inputNama;
    EditText inputTanggal;
    EditText inputTelepon;
    LinearLayout layoutImage;
    String strBase64Photo;
    String strFilePath;
    String strImageName;
    String strTimeStamp;
    String strTitle;
    Toolbar toolbar;
    TextView tvTitle;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        setStatusBar();
        setInitLayout();
        setSendLaporan();
    }

    private void setInitLayout() {
        this.toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.tvTitle = (TextView) findViewById(R.id.tvTitle);
        this.imageLaporan = (ImageView) findViewById(R.id.imageLaporan);
        this.layoutImage = (LinearLayout) findViewById(R.id.layoutImage);
        this.fabSend = (ExtendedFloatingActionButton) findViewById(R.id.fabSend);
        this.inputNama = (EditText) findViewById(R.id.inputNama);
        this.inputTelepon = (EditText) findViewById(R.id.inputTelepon);
        this.inputLokasi = (EditText) findViewById(R.id.inputLokasi);
        this.inputTanggal = (EditText) findViewById(R.id.inputTanggal);
        this.inputLaporan = (EditText) findViewById(R.id.inputLaporan);
        String string = getIntent().getExtras().getString(DATA_TITLE);
        this.strTitle = string;
        if (string != null) {
            this.tvTitle.setText(string);
        }
        setSupportActionBar(this.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        this.inputLokasi.setText(Constant.lokasiPengaduan);
        this.inputDataViewModel = (InputDataViewModel) new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(InputDataViewModel.class);
        this.layoutImage.setOnClickListener(new View.OnClickListener() { // from class: com.azhar.reportapps.ui.report.ReportActivity$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ReportActivity.this.m40xbe7ff99(view);
            }
        });
        this.inputTanggal.setOnClickListener(new View.OnClickListener() { // from class: com.azhar.reportapps.ui.report.ReportActivity$$ExternalSyntheticLambda3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ReportActivity.this.m42xf6dc4c1b(view);
            }
        });
    }

    /* renamed from: lambda$setInitLayout$1$com-azhar-reportapps-ui-report-ReportActivity  reason: not valid java name */
    public /* synthetic */ void m40xbe7ff99(View v) {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Upload Foto Bukti Laporan");
        String[] pictureDialogItems = {"Pilih foto dari galeri", "Ambil foto lewat kamera"};
        pictureDialog.setItems(pictureDialogItems, new DialogInterface.OnClickListener() { // from class: com.azhar.reportapps.ui.report.ReportActivity$$ExternalSyntheticLambda1
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                ReportActivity.this.m39x966dd958(dialogInterface, i);
            }
        });
        pictureDialog.show();
    }

    /* renamed from: lambda$setInitLayout$0$com-azhar-reportapps-ui-report-ReportActivity  reason: not valid java name */
    public /* synthetic */ void m39x966dd958(DialogInterface dialog, int which) {
        switch (which) {
            case 0:
                Dexter.withContext(this).withPermissions("android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION").withListener(new MultiplePermissionsListener() { // from class: com.azhar.reportapps.ui.report.ReportActivity.1
                    @Override // com.karumi.dexter.listener.multi.MultiplePermissionsListener
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Intent galleryIntent = new Intent("android.intent.action.PICK", MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            ReportActivity.this.startActivityForResult(galleryIntent, 1);
                        }
                    }

                    @Override // com.karumi.dexter.listener.multi.MultiplePermissionsListener
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
                return;
            case 1:
                Dexter.withContext(this).withPermissions("android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION").withListener(new MultiplePermissionsListener() { // from class: com.azhar.reportapps.ui.report.ReportActivity.2
                    @Override // com.karumi.dexter.listener.multi.MultiplePermissionsListener
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            try {
                                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                                ReportActivity reportActivity = ReportActivity.this;
                                intent.putExtra("output", FileProvider.getUriForFile(reportActivity, "com.azhar.reportapps.provider", reportActivity.createImageFile()));
                                ReportActivity reportActivity2 = ReportActivity.this;
                                reportActivity2.startActivityForResult(intent, reportActivity2.REQ_CAMERA);
                            } catch (IOException e) {
                                Toast.makeText(ReportActivity.this, "Gagal membuka kamera!", 0).show();
                            }
                        }
                    }

                    @Override // com.karumi.dexter.listener.multi.MultiplePermissionsListener
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
                return;
            default:
                return;
        }
    }

    /* renamed from: lambda$setInitLayout$3$com-azhar-reportapps-ui-report-ReportActivity  reason: not valid java name */
    public /* synthetic */ void m42xf6dc4c1b(View view) {
        final Calendar tanggalJemput = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() { // from class: com.azhar.reportapps.ui.report.ReportActivity$$ExternalSyntheticLambda0
            @Override // android.app.DatePickerDialog.OnDateSetListener
            public final void onDateSet(DatePicker datePicker, int i, int i2, int i3) {
                ReportActivity.this.m41x816225da(tanggalJemput, datePicker, i, i2, i3);
            }
        };
        new DatePickerDialog(this, date, tanggalJemput.get(1), tanggalJemput.get(2), tanggalJemput.get(5)).show();
    }

    /* renamed from: lambda$setInitLayout$2$com-azhar-reportapps-ui-report-ReportActivity  reason: not valid java name */
    public /* synthetic */ void m41x816225da(Calendar tanggalJemput, DatePicker view1, int year, int monthOfYear, int dayOfMonth) {
        tanggalJemput.set(1, year);
        tanggalJemput.set(2, monthOfYear);
        tanggalJemput.set(5, dayOfMonth);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
        this.inputTanggal.setText(simpleDateFormat.format(tanggalJemput.getTime()));
    }

    private void setSendLaporan() {
        this.fabSend.setOnClickListener(new View.OnClickListener() { // from class: com.azhar.reportapps.ui.report.ReportActivity$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ReportActivity.this.m43x90e8c59b(view);
            }
        });
    }

    /* renamed from: lambda$setSendLaporan$4$com-azhar-reportapps-ui-report-ReportActivity  reason: not valid java name */
    public /* synthetic */ void m43x90e8c59b(View v) {
        String strNama = this.inputNama.getText().toString();
        String strTelepon = this.inputTelepon.getText().toString();
        String strLokasi = this.inputLokasi.getText().toString();
        String strTanggal = this.inputTanggal.getText().toString();
        String strLaporan = this.inputLaporan.getText().toString();
        if (this.strFilePath == null || strNama.isEmpty() || strTelepon.isEmpty() || strLokasi.isEmpty() || strTanggal.isEmpty() || strLaporan.isEmpty()) {
            Toast.makeText(this, "Data tidak boleh ada yang kosong!", 0).show();
            return;
        }
        this.inputDataViewModel.addLaporan(this.strTitle, this.strBase64Photo, strNama, strLokasi, strTanggal, strLaporan, strTelepon);
        Toast.makeText(this, "Laporan Anda terkirim, tunggu info selanjutnya ya!", 0).show();
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public File createImageFile() throws IOException {
        this.strTimeStamp = new SimpleDateFormat("dd MMMM yyyy HH:mm").format(new Date());
        this.strImageName = "IMG_";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "");
        this.fileDirectoty = file;
        File createTempFile = File.createTempFile(this.strImageName, ".jpg", file);
        this.imageFilename = createTempFile;
        this.strFilePath = createTempFile.getAbsolutePath();
        return this.imageFilename;
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
        if (requestCode == this.REQ_CAMERA && resultCode == -1) {
            convertImage(this.strFilePath);
        } else if (requestCode == 1 && resultCode == -1) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {"_data"};
            if (selectedImage == null) {
                throw new AssertionError();
            }
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            if (cursor == null) {
                throw new AssertionError();
            }
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String mediaPath = cursor.getString(columnIndex);
            cursor.close();
            this.strFilePath = mediaPath;
            convertImage(mediaPath);
        }
    }

    private void convertImage(String imageFilePath) {
        File imageFile = new File(imageFilePath);
        if (imageFile.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap bitmapImage = BitmapFactory.decodeFile(this.strFilePath, options);
            Glide.with((FragmentActivity) this).load(bitmapImage).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ic_image_upload).into(this.imageLaporan);
            this.strBase64Photo = BitmapManager.bitmapToBase64(bitmapImage);
        }
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

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 16908332) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
