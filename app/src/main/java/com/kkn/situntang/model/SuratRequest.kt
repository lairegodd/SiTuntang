package com.kkn.situntang.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class SuratRequest(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val nik: String = "",
    val nama: String = "",
    val tanggalLahir: String = "",
    val alamat: String = "",
    val jenisSurat: String = "",
    val keperluan: String = "",
    val catatan: String? = null,
    val metodePengambilan: String = "", // ANTAR / AMBIL_SENDIRI
    val status: String = "PENDING", // PENDING / DITERIMA / DITOLAK
    val tanggalPengajuan: String = "",
    val rejectReason: String? = null,
    val createdAt: Timestamp? = null,
    val verifiedAt: Timestamp? = null,
    val verifiedBy: String? = null
)
