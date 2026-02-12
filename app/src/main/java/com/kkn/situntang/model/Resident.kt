package com.kkn.situntang.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Resident(
    @DocumentId
    val id: String = "",
    
    // TANGGAL PELAPORAN
    val tanggal_pindah_masuk: String = "",
    val tanggal_lapor: String = "",

    // DATA DIRI
    val nama: String = "",
    val nik: String = "",
    val tanggal_lahir: String = "",
    val jenis_kelamin: String = "Jenis Kelamin",
    val agama: String = "Pilih Agama",
    val alamat: String = "",
    val foto_url: String = "",
    
    // STATUS & METADATA
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val created_at: Timestamp? = null,
    val verified_at: Timestamp? = null,
    val verified_by: String? = null,
    val reject_reason: String? = null,
    
    // STATUS KEPEMILIKAN IDENTITAS
    val wajib_identitas: String = "BELUM WAJIB",
    val identitas_elektronik: String = "Pilih Identitas-EL",
    val status_rekam: String = "Pilih Status Rekam",
    val tag_id_card: String = "",
    val nomor_kk_sebelumnya: String = "",
    val hubungan_keluarga: String = "Pilih Hubungan Keluarga",
    val status_penduduk: String = "Pilih Status Penduduk",
    
    // DATA KELAHIRAN
    val nomor_akta_kelahiran: String = "",
    val tempat_lahir: String = "",
    val waktu_kelahiran: String = "",
    val tempat_dilahirkan: String = "Pilih Tempat Dilahirkan",
    val jenis_kelahiran: String = "Pilih Jenis Kelahiran",
    val anak_ke: String = "",
    val penolong_kelahiran: String = "Pilih Penolong Kelahiran",
    val berat_lahir: String = "",
    val panjang_lahir: String = "",
    
    // PENDIDIKAN DAN PEKERJAAN
    val pendidikan_kk: String = "Pilih Pendidikan (Dalam KK)",
    val pendidikan_tempuh: String = "Pilih Pendidikan",
    val pekerjaan: String = "Pilih Pekerjaan",
    
    // DATA KEWARGANEGARAAN
    val suku_etnis: String = "Pilih Suku/Etnis",
    val status_warga_negara: String = "Pilih Warga Negara",
    val nomor_paspor: String = "",
    val tgl_berakhir_paspor: String = "",

    // DATA ORANG TUA
    val nik_ayah: String = "",
    val nama_ayah: String = "",
    val nik_ibu: String = "",
    val nama_ibu: String = "",
    
    // ALAMAT
    val dusun: String = "Pilih Dusun",
    val rw: String = "Pilih RW",
    val rt: String = "Pilih RT",
    val alamat_sebelumnya: String = "",
    val no_telepon: String = "",
    val email: String = "",
    val telegram: String = "",
    val cara_hubung: String = "Pilih Cara Hubungi",
    
    // STATUS PERKAWINAN
    val status_perkawinan: String = "Pilih Status Perkawinan",
    val no_akta_nikah: String = "",
    val tanggal_perkawinan: String = "",
    val akta_perceraian: String = "",
    val tanggal_perceraian: String = "",
    
    // DATA KESEHATAN
    val golongan_darah: String = "Pilih Golongan Darah",
    val cacat: String = "Pilih Jenis Cacat",
    val sakit_menahun: String = "Pilih Sakit Menahun",
    val akseptor_kb: String = "Pilih Cara KB Saat Ini",
    val asuransi_kesehatan: String = "Pilih Asuransi",
    val no_bpjs_ketenagakerjaan: String = "",
    
    // LAINNYA
    val dapat_membaca_huruf: String = "Pilih Isian",
    val keterangan: String = "",
    
    // AUTH METADATA
    val userId: String = ""
)
