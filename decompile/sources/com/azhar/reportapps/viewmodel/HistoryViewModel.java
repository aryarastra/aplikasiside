package com.azhar.reportapps.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.azhar.reportapps.dao.DatabaseDao;
import com.azhar.reportapps.database.DatabaseClient;
import com.azhar.reportapps.model.ModelDatabase;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.List;

/* loaded from: classes9.dex */
public class HistoryViewModel extends AndroidViewModel {
    DatabaseDao databaseDao;
    LiveData<List<ModelDatabase>> modelLaundry;

    public HistoryViewModel(Application application) {
        super(application);
        DatabaseDao databaseDao = DatabaseClient.getInstance(application).getAppDatabase().databaseDao();
        this.databaseDao = databaseDao;
        this.modelLaundry = databaseDao.getAllReport();
    }

    public LiveData<List<ModelDatabase>> getDataLaporan() {
        return this.modelLaundry;
    }

    public void deleteDataById(final int uid) {
        Completable.fromAction(new Action() { // from class: com.azhar.reportapps.viewmodel.HistoryViewModel$$ExternalSyntheticLambda0
            @Override // io.reactivex.rxjava3.functions.Action
            public final void run() {
                HistoryViewModel.this.m44xc0d0fd33(uid);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    /* renamed from: lambda$deleteDataById$0$com-azhar-reportapps-viewmodel-HistoryViewModel  reason: not valid java name */
    public /* synthetic */ void m44xc0d0fd33(int uid) throws Throwable {
        this.databaseDao.deleteSingleReport(uid);
    }
}
