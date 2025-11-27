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

public class PemasukanAdapter extends RecyclerView.Adapter<PemasukanAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Transaksi transaksi);
    }

    private List<Transaksi> data;
    private final OnItemClickListener listener;
    private final NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));

    public PemasukanAdapter(List<Transaksi> data, OnItemClickListener listener) {
        this.data = data;
        this.listener = listener;
    }

    public void setData(List<Transaksi> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PemasukanAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaksi_pemasukan, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PemasukanAdapter.ViewHolder holder, int position) {
        Transaksi t = data.get(position);
        holder.bind(t, listener, nf);
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textTanggal, textKategori, textTotalAmount, textKeterangan;
        TextView textHargaPerKg, textBeratKg;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textTanggal = itemView.findViewById(R.id.textTanggal);
            textKategori = itemView.findViewById(R.id.textKategori);
            textTotalAmount = itemView.findViewById(R.id.textTotalAmount);
            textKeterangan = itemView.findViewById(R.id.textKeterangan);
            textHargaPerKg = itemView.findViewById(R.id.textHargaPerKg);
            textBeratKg = itemView.findViewById(R.id.textBeratKg);
        }

        void bind(Transaksi t, OnItemClickListener listener, NumberFormat nf) {

            textTanggal.setText("TANGGAL: " + t.tanggal);

            textKategori.setText(t.kategori);

            textTotalAmount.setText(nf.format(t.jumlah));

            textHargaPerKg.setText("Harga/KG (Rp): " + nf.format(t.hargaPerKg));
            textBeratKg.setText("Berat Jual(Kg): " + String.format(Locale.getDefault(), "%.2f Kg", t.beratKg));

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
