package com.azhar.reportapps.model;

import java.io.Serializable;

/* loaded from: classes10.dex */
public class ModelDatabase implements Serializable {
    public String image;
    public String isi_laporan;
    public String kategori;
    public String lokasi;
    public String nama;
    public String tanggal;
    public String telepon;
    public int uid;

    public int getUid() {
        return this.uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getKategori() {
        return this.kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNama() {
        return this.nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getLokasi() {
        return this.lokasi;
    }

    public void setLokasi(String lokasi) {
        this.lokasi = lokasi;
    }

    public String getTanggal() {
        return this.tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public String getIsiLaporan() {
        return this.isi_laporan;
    }

    public void setIsiLaporan(String isi_laporan) {
        this.isi_laporan = isi_laporan;
    }

    public String getTelepon() {
        return this.telepon;
    }

    public void setTelepon(String telepon) {
        this.telepon = telepon;
    }
}
