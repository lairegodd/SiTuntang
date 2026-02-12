# SiTuntang (Sistem Informasi Desa Tuntang) 📱

![Status Project](https://img.shields.io/badge/Status-Development-orange)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue)
![Firebase](https://img.shields.io/badge/Backend-Firebase-yellow)

**SiTuntang** adalah aplikasi mobile berbasis Android yang dikembangkan untuk mendigitalisasi pelayanan administrasi dan penyebaran informasi di **Desa Tuntang**. Aplikasi ini bertujuan untuk mempermudah interaksi antara warga desa dan perangkat desa melalui fitur-fitur seperti pengajuan surat online, pendataan penduduk, dan portal berita desa.

Proyek ini dikembangkan sebagai bagian dari program pengabdian masyarakat **KKN UNNES GIAT 15 (2026)**.

---

## ✨ Fitur Utama

Aplikasi ini memiliki dua peran pengguna utama: **Warga** dan **Admin**.

### 👤 Warga (Pengguna Umum)
* **Berita Desa Terkini:** Mengakses informasi dan berita terbaru seputar Desa Tuntang secara *real-time*.
* **Pengajuan Surat Online:** Mengajukan berbagai jenis surat administrasi tanpa perlu datang ke kantor desa terlebih dahulu. Jenis surat yang tersedia:
    * Surat Cuti
    * Surat Keterangan Usaha
    * Surat Domisili
    * Surat Keterangan Tidak Mampu
* **Pendataan Mandiri:** Mengisi formulir data diri penduduk secara digital.
* **Riwayat & Status:** Memantau status pengajuan surat dan riwayat pendataan.

### 🛡️ Admin (Perangkat Desa)
* **Dashboard Admin:** Ringkasan aktivitas desa.
* **Manajemen Data Warga:** Melihat dan memverifikasi data penduduk yang masuk.
* **Manajemen Surat:** Menerima, menolak, atau memproses pengajuan surat dari warga.
---

## 🛠️ Teknologi yang Digunakan

Aplikasi ini dibangun menggunakan teknologi *Modern Android Development*:

| Kategori | Teknologi / Library |
| :--- | :--- |
| **Bahasa Pemrograman** | [Kotlin](https://kotlinlang.org/) |
| **UI Framework** | [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3 Expressive UI) |
| **Arsitektur** | MVVM (Model-View-ViewModel) |
| **Backend & Database** | [Firebase Firestore](https://firebase.google.com/docs/firestore) |
| **Autentikasi** | [Firebase Auth](https://firebase.google.com/docs/auth) |
| **Navigasi** | [Navigation Compose](https://developer.android.com/jetpack/compose/navigation) |
| **Build System** | Gradle (Kotlin DSL) |

---

## 📸 Tangkapan Layar (Screenshots)

---

## 🚀 Panduan Instalasi

Ikuti langkah-langkah berikut untuk menjalankan proyek ini di komputer lokal Anda.

### Prasyarat
* [Android Studio Ladybug](https://developer.android.com/studio) atau versi terbaru.
* JDK 11 atau lebih baru (Target Compatibility diset ke versi 11).
* Akun Google untuk konfigurasi Firebase.

### Langkah-langkah

1.  **Clone Repositori**
    ```bash
    git clone [https://github.com/username-anda/SiTuntang.git](https://github.com/username-anda/SiTuntang.git)
    cd SiTuntang
    ```

2.  **Konfigurasi Firebase**
    Proyek ini memerlukan koneksi ke Firebase agar dapat berjalan.
    * Buat proyek baru di [Firebase Console](https://console.firebase.google.com/).
    * Aktifkan layanan **Authentication**, dan **Firestore Database**.
    * Unduh file `google-services.json` dari konsol Firebase Anda.
    * Letakkan file tersebut di dalam direktori aplikasi:
        `app/google-services.json`
    *(Catatan: Tanpa file ini, build akan gagal karena plugin Google Services)*.

3.  **Buka di Android Studio**
    * Buka Android Studio dan pilih "Open an existing project".
    * Arahkan ke folder proyek `SiTuntang`.
    * Biarkan Gradle melakukan sinkronisasi (*sync*) untuk mengunduh semua *dependencies*.

4.  **Jalankan Aplikasi**
    * Sambungkan perangkat Android fisik atau gunakan Emulator.
    * Tekan tombol **Run** (▶️).

---

## 🤝 Kontributor

Aplikasi ini dikembangkan dengan ❤️ oleh mahasiswa **KKN UNNES GIAT 15** di Desa Tuntang.

* **Pengembang Android:** [Nama Anda/GitHub Anda]
* **UI/UX Designer:** [Nama Anggota Tim]
* **Tim KKN UNNES GIAT 15**

---

## 📄 Lisensi

Hak Cipta © 2026 Pemerintah Desa Tuntang & KKN UNNES GIAT 15.
