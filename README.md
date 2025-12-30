# SPK Pemilihan Pupuk Kelapa Sawit (AHP & TOPSIS)

Aplikasi Android Sistem Pendukung Keputusan (SPK) untuk membantu petani kelapa sawit memilih rekomendasi pupuk terbaik berdasarkan kondisi lahan dan kebutuhan nutrisi.

Aplikasi ini menggabungkan metode **AHP (Analytic Hierarchy Process)** untuk pembobotan kriteria dan **TOPSIS (Technique for Order of Preference by Similarity to Ideal Solution)** untuk perankingan alternatif.

---

## Screenshots

| Dashboard Utama | Input Bobot AHP | Hasil Perangkingan | Riwayat Analisis |
|:---:|:---:|:---:|:---:|
| <img width="240" alt="Screenshot 1" src="https://github.com/user-attachments/assets/df7edb8e-a8af-4db2-87df-2d6e926d4798" /> | <img width="240" alt="Screenshot 2" src="https://github.com/user-attachments/assets/01f3c7aa-4ce2-4aff-8d74-da9e15c9ea01" /> | <img width="240" alt="Screenshot 3" src="https://github.com/user-attachments/assets/df723d5a-1471-4685-8325-6fb750f652c3" /> | <img width="240" alt="Screenshot 4" src="https://github.com/user-attachments/assets/7f280609-1219-47c6-82df-fff34f27a1e9" /> |

---

## Fitur Utama

### 1. Analisis AHP Cerdas (Penentuan Bobot)
* **Pairwise Comparison:** Input perbandingan antar kriteria menggunakan *SeekBar* yang intuitif.
* **Live Consistency Check:** Menghitung *Consistency Ratio (CR)* secara *real-time* saat user menggeser slider.
* **Auto-Repair Logic:** Fitur unggulan yang dapat **memperbaiki otomatis** input user yang tidak konsisten (Logika *Intransitive*) agar CR < 0.10.

### 2. Analisis TOPSIS (Perankingan)
* **Wizard Style:** Proses analisis dibagi menjadi langkah-langkah yang rapi (Pilih Bobot -> Pilih Kandidat Pupuk).
* **Dynamic Calculation:** Menghitung jarak solusi ideal positif/negatif secara presisi.
* **Cost vs Benefit:** Membedakan logika kriteria *Benefit* (Kandungan Hara, dll) dan *Cost* (Harga).

### 3. Manajemen Data (CRUD)
* **Data Pupuk:** Tambah, Edit, Hapus data pupuk (Nama, Harga, Kandungan Makro/Mikro, dll).
* **Profil Bobot:** Simpan hasil hitungan AHP sebagai "Profil" (misal: "Prioritas Harga Murah" atau "Prioritas Kualitas").
* **Riwayat Analisis:** Menyimpan hasil rekomendasi (Snapshot) agar bisa dilihat kembali di kemudian hari.

---

## Teknologi yang Digunakan

* **Bahasa:** Java (Native Android)
* **Database:** Room Database (SQLite Object Mapping)
* **Arsitektur:** MVC (Model-View-Controller) pattern
* **UI Components:** Material Design 3, CardView, RecyclerView,MPAndroidChart
* **Minimum SDK:** Android 7.0 (Nougat)

---

## Kriteria Keputusan
Aplikasi ini menggunakan 5 kriteria utama:
1.  **C1 - Kandungan Hara Makro** (Benefit)
2.  **C2 - Kandungan Hara Mikro** (Benefit)
3.  **C3 - Kecepatan Lepas / Release** (Benefit)
4.  **C4 - Kecocokan Jenis Tanah** (Benefit - *Input Dinamis*)
5.  **C5 - Harga** (Cost)

---

## Cara Instalasi (Untuk Developer)

1.  Clone repository ini:
    ```bash
    git clone [https://github.com/username-anda/spk-pupuk-sawit.git](https://github.com/username-anda/spk-pupuk-sawit.git)
    ```
2.  Buka project menggunakan **Android Studio Jellyfish** (atau versi terbaru).
3.  Biarkan Gradle melakukan sinkronisasi (*Sync Project*).
4.  Sambungkan HP Android atau jalankan Emulator.
5.  Tekan tombol **Run**.

---

## Author
**[Tawarikh Anggia Cristopher]**
*Mahasiswa Sistem Informasi - Unika Atma Jaya*

Dikembangkan sebagai tugas, untuk membantu optimalisasi pertanian kelapa sawit melalui teknologi.

---

*Dibuat dengan menggunakan Android Studio.*
