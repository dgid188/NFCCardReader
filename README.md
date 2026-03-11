# 📱 NFC Card Reader - Aplikasi Membaca Kartu Akses NFC

Aplikasi Android untuk membaca berbagai jenis kartu akses NFC, mirip dengan fitur NFC Multifunctions pada vivo X50 Series.

## ✨ Fitur Utama

- **Membaca Kartu Akses** - MIFARE Classic (kartu kantor/apartemen)
- **MIFARE Ultralight** - Kartu tiket sederhana  
- **ISO 14443-4 (ISO-DEP)** - Kartu e-money / kartu pintar
- **NFC-A, NFC-B, NFC-F (FeliCa), NFC-V** - Semua standar NFC utama
- **Riwayat Kartu** - Semua kartu yang dibaca tersimpan otomatis
- **Detail Lengkap** - UID, teknologi, produsen, data mentah
- **Salin ke Clipboard** - Salin UID atau semua info kartu

## 📋 Persyaratan

- Android 6.0 (API 23) atau lebih tinggi
- Ponsel dengan chip NFC
- NFC dalam kondisi aktif

## 🚀 Cara Build & Install

### Menggunakan Android Studio:

1. Buka **Android Studio**
2. Pilih **File → Open** → pilih folder `NFCCardReader`
3. Tunggu Gradle sync selesai
4. Sambungkan ponsel Android (aktifkan USB Debugging)
5. Klik tombol **Run ▶**

### Cara Generate APK:
1. Di Android Studio: **Build → Generate Signed Bundle/APK**
2. Pilih **APK** → Next
3. Buat/pilih keystore
4. Pilih **release** → Finish

## 📖 Cara Penggunaan

1. Buka aplikasi
2. Pastikan NFC aktif di ponsel
3. Layar utama akan menampilkan area scan
4. **Tempelkan kartu akses** ke bagian belakang ponsel (dekat logo NFC)
5. Informasi kartu akan muncul secara otomatis
6. Tap item di riwayat untuk melihat detail lengkap
7. Gunakan tombol **Salin UID** atau **Salin Semua** di halaman detail

## 🎯 Jenis Kartu yang Didukung

| Teknologi | Contoh Penggunaan |
|-----------|------------------|
| MIFARE Classic 1K/4K | Kartu akses kantor, apartemen |
| MIFARE Ultralight | Kartu tiket, e-ticket |
| ISO-DEP / ISO 14443-4 | Kartu e-money, kartu bank |
| NFC-A | Kartu NFC umum |
| NFC-B | Kartu identitas |
| NFC-F (FeliCa) | Kartu transportasi Jepang |
| NFC-V / ISO 15693 | Kartu jarak jauh |

## ⚠️ Catatan Penting

- Kartu yang terenkripsi tidak dapat dibaca datanya, tetapi UID tetap bisa dibaca
- Enkripsi default (Key A/B default) akan dicoba untuk MIFARE Classic
- Aplikasi ini hanya **membaca** kartu, tidak dapat menulis atau menduplikasi
- Beberapa kartu akses menggunakan enkripsi kustom yang tidak bisa dibaca

## 🏗️ Struktur Proyek

```
NFCCardReader/
├── app/src/main/
│   ├── AndroidManifest.xml          # Permission NFC
│   ├── java/com/nfcreader/
│   │   ├── MainActivity.java         # Layar utama + dispatch NFC
│   │   ├── CardDetailActivity.java   # Detail kartu lengkap
│   │   ├── NfcTagProcessor.java      # Engine pembaca NFC
│   │   ├── CardInfo.java             # Model data kartu
│   │   └── CardHistoryAdapter.java   # RecyclerView adapter
│   └── res/
│       ├── layout/                   # XML layouts
│       ├── drawable/                 # Icons & shapes
│       ├── values/                   # Colors, strings, themes
│       └── xml/nfc_tech_filter.xml  # Filter teknologi NFC
```

## 📜 Lisensi

MIT License - Bebas digunakan dan dimodifikasi
