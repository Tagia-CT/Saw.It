package com.tac.sawit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tac.sawit.model.Transaksi;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PengeluaranAdapter extends RecyclerView.Adapter<PengeluaranAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Transaksi transaksi);
    }

    private List<Transaksi> data;
    private final OnItemClickListener listener;
    private final NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));

    public PengeluaranAdapter(List<Transaksi> data, OnItemClickListener listener) {
        this.data = data;
        this.listener = listener;
    }

    public void setData(List<Transaksi> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PengeluaranAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaksi_pengeluaran, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PengeluaranAdapter.ViewHolder holder, int position) {
        Transaksi t = data.get(position);
        holder.bind(t, listener, nf);
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textKategori, textTotalAmount, textTanggal, textKeterangan;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textKategori = itemView.findViewById(R.id.textKategori);
            textTotalAmount = itemView.findViewById(R.id.textTotalAmount);
            textTanggal = itemView.findViewById(R.id.textTanggal);
            textKeterangan = itemView.findViewById(R.id.textKeterangan);
        }

        void bind(Transaksi t, OnItemClickListener listener, NumberFormat nf) {

            textKategori.setText(t.kategori);

            textTotalAmount.setText(nf.format(t.jumlah));

            textTanggal.setText(t.tanggal);
            textTanggal.setVisibility(View.VISIBLE);

            if (t.keterangan != null && !t.keterangan.isEmpty()) {
                textKeterangan.setText("Keterangan: " + t.keterangan);
                textKeterangan.setVisibility(View.VISIBLE);
            } else {
                textKeterangan.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onItemClick(t));
        }
    }
}
