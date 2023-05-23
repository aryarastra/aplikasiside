package com.azhar.reportapps.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import com.azhar.reportapps.dao.DatabaseDao;
import com.azhar.reportapps.database.DatabaseClient;
import com.azhar.reportapps.model.ModelDatabase;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.schedulers.Schedulers;

/* loaded from: classes9.dex */
public class InputDataViewModel extends AndroidViewModel {
    DatabaseDao databaseDao;

    public InputDataViewModel(Application application) {
        super(application);
        this.databaseDao = DatabaseClient.getInstance(application).getAppDatabase().databaseDao();
    }

    public void addLaporan(final String kategori, final String image, final String nama, final String lokasi, final String tanggal, final String isi_laporan, final String telepon) {
        Completable.fromAction(new Action() { // from class: com.azhar.reportapps.viewmodel.InputDataViewModel$$ExternalSyntheticLambda0
            @Override // io.reactivex.rxjava3.functions.Action
            public final void run() {
                InputDataViewModel.this.m45x8dafb196(kategori, image, nama, lokasi, tanggal, isi_laporan, telepon);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    /* renamed from: lambda$addLaporan$0$com-azhar-reportapps-viewmodel-InputDataViewModel */
    public /* synthetic */ void m45x8dafb196(String kategori, String image, String nama, String lokasi, String tanggal, String isi_laporan, String telepon) throws Throwable {
        ModelDatabase modelDatabase = new ModelDatabase();
        modelDatabase.kategori = kategori;
        modelDatabase.image = image;
        modelDatabase.nama = nama;
        modelDatabase.lokasi = lokasi;
        modelDatabase.tanggal = tanggal;
        modelDatabase.isi_laporan = isi_laporan;
        modelDatabase.telepon = telepon;
        this.databaseDao.insertData(modelDatabase);
    }
}
