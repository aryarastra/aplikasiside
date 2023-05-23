package com.azhar.reportapps.dao;

import androidx.lifecycle.LiveData;
import com.azhar.reportapps.model.ModelDatabase;
import java.util.List;

/* loaded from: classes8.dex */
public interface DatabaseDao {
    void deleteAllReport();

    void deleteSingleReport(int uid);

    LiveData<List<ModelDatabase>> getAllReport();

    void insertData(ModelDatabase... modelDatabases);
}
