package com.azhar.reportapps.ui.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.azhar.reportapps.R;
import com.azhar.reportapps.model.ModelDatabase;
import com.azhar.reportapps.ui.history.HistoryAdapter;
import java.util.List;

/* loaded from: classes4.dex */
public class HistoryAdapter extends RecyclerView.Adapter<ViewHolder> {
    HistoryAdapterCallback mAdapterCallback;
    Context mContext;
    List<ModelDatabase> modelDatabase;

    /* loaded from: classes4.dex */
    public interface HistoryAdapterCallback {
        void onDelete(ModelDatabase modelDatabase);
    }

    public HistoryAdapter(Context context, List<ModelDatabase> modelDatabaseList, HistoryAdapterCallback adapterCallback) {
        this.mContext = context;
        this.modelDatabase = modelDatabaseList;
        this.mAdapterCallback = adapterCallback;
    }

    public void setDataAdapter(List<ModelDatabase> items) {
        this.modelDatabase.clear();
        this.modelDatabase.addAll(items);
        notifyDataSetChanged();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_history, parent, false);
        return new ViewHolder(view);
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(ViewHolder holder, int position) {
        char c;
        ModelDatabase data = this.modelDatabase.get(position);
        holder.tvKategori.setText(data.getKategori());
        holder.tvNama.setText(data.getKategori());
        holder.tvDate.setText(data.getTanggal());
        holder.tvLokasi.setText(data.getLokasi());
        String kategori = data.getKategori();
        switch (kategori.hashCode()) {
            case 63123519:
                if (kategori.equals("Aduan")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 1614684491:
                if (kategori.equals("Laporan")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                holder.layoutHeader.setBackgroundResource(R.color.blue);
                return;
            case 1:
                holder.layoutHeader.setBackgroundResource(R.color.green);
                return;
            default:
                return;
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.modelDatabase.size();
    }

    /* loaded from: classes4.dex */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView cvHistory;
        public LinearLayout layoutHeader;
        public TextView tvDate;
        public TextView tvKategori;
        public TextView tvLokasi;
        public TextView tvNama;

        public ViewHolder(View itemView) {
            super(itemView);
            this.tvKategori = (TextView) itemView.findViewById(R.id.tvKategori);
            this.tvNama = (TextView) itemView.findViewById(R.id.tvNama);
            this.tvDate = (TextView) itemView.findViewById(R.id.tvDate);
            this.tvLokasi = (TextView) itemView.findViewById(R.id.tvLokasi);
            this.cvHistory = (CardView) itemView.findViewById(R.id.cvHistory);
            this.layoutHeader = (LinearLayout) itemView.findViewById(R.id.layoutHeader);
            this.cvHistory.setOnClickListener(new View.OnClickListener() { // from class: com.azhar.reportapps.ui.history.HistoryAdapter$ViewHolder$$ExternalSyntheticLambda0
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    HistoryAdapter.ViewHolder.this.m35x92daf301(view);
                }
            });
        }

        /* renamed from: lambda$new$0$com-azhar-reportapps-ui-history-HistoryAdapter$ViewHolder  reason: not valid java name */
        public /* synthetic */ void m35x92daf301(View view) {
            ModelDatabase modelLaundry = HistoryAdapter.this.modelDatabase.get(getAdapterPosition());
            HistoryAdapter.this.mAdapterCallback.onDelete(modelLaundry);
        }
    }
}
