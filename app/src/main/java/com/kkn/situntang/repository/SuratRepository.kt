package com.kkn.situntang.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.kkn.situntang.model.SuratRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SuratRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val suratCollection = firestore.collection("pengajuan_surat")

    // WARGA: Mengajukan Surat (Tanpa Upload)
    suspend fun submitSurat(surat: SuratRequest): Result<Unit> {
        return try {
            suratCollection.add(surat).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // WARGA: Mendapatkan Daftar Pengajuan Milik Sendiri (Realtime)
    fun getMySuratRequests(userId: String): Flow<List<SuratRequest>> = callbackFlow {
        val subscription = suratCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val requests = snapshot?.toObjects(SuratRequest::class.java) ?: emptyList()
                val sortedRequests = requests.sortedByDescending { it.createdAt }
                trySend(sortedRequests)
            }
        awaitClose { subscription.remove() }
    }

    // ADMIN: Mendapatkan Semua Daftar Pengajuan (Realtime)
    fun getAllSuratRequests(): Flow<List<SuratRequest>> = callbackFlow {
        val subscription = suratCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val requests = snapshot?.toObjects(SuratRequest::class.java) ?: emptyList()
                val sortedRequests = requests.sortedByDescending { it.createdAt }
                trySend(sortedRequests)
            }
        awaitClose { subscription.remove() }
    }

    // ADMIN: Approve Surat (Hanya Update Status)
    suspend fun approveSurat(requestId: String, adminId: String): Result<Unit> {
        return try {
            suratCollection.document(requestId).update(
                mapOf(
                    "status" to "DITERIMA",
                    "verifiedAt" to Timestamp.now(),
                    "verifiedBy" to adminId
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ADMIN: Reject Surat
    suspend fun rejectSurat(requestId: String, reason: String, adminId: String): Result<Unit> {
        return try {
            suratCollection.document(requestId).update(
                mapOf(
                    "status" to "DITOLAK",
                    "rejectReason" to reason,
                    "verifiedAt" to Timestamp.now(),
                    "verifiedBy" to adminId
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ADMIN: Hapus Pengajuan Surat
    suspend fun deleteSuratRequests(requestIds: List<String>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            requestIds.forEach { id ->
                val docRef = suratCollection.document(id)
                batch.delete(docRef)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
