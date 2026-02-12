package com.kkn.situntang.repository

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.kkn.situntang.model.Resident
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class ResidentRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun saveResident(resident: Resident, imageUri: Uri?): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Sesi berakhir, silakan login ulang"))
            
            var finalResident = resident.copy(
                userId = user.uid,
                status = "PENDING",
                created_at = Timestamp.now()
            )
            
            // 1. Upload Foto jika ada
            imageUri?.let { uri ->
                val storageRef = storage.reference.child("foto_penduduk/${resident.nik}.jpg")
                storageRef.putFile(uri).await()
                val downloadUrl = storageRef.downloadUrl.await()
                finalResident = finalResident.copy(foto_url = downloadUrl.toString())
            }

            // 2. Simpan ke Firestore
            firestore.collection("penduduk")
                .document(finalResident.nik)
                .set(finalResident)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllResidentsRealtime(): Flow<List<Resident>> = callbackFlow {
        val registration = firestore.collection("penduduk")
            .orderBy("created_at", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val list = snapshot?.toObjects(Resident::class.java) ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    suspend fun updateResidentStatus(nik: String, status: String, rejectReason: String? = null): Result<Unit> {
        return try {
            val adminId = auth.currentUser?.uid ?: "Admin"
            val updateData = mutableMapOf<String, Any>(
                "status" to status,
                "verified_at" to Timestamp.now(),
                "verified_by" to adminId
            )
            if (status == "REJECTED" && rejectReason != null) {
                updateData["reject_reason"] = rejectReason
            }

            firestore.collection("penduduk")
                .document(nik)
                .update(updateData)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteResidents(niks: List<String>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            niks.forEach { nik ->
                val docRef = firestore.collection("penduduk").document(nik)
                batch.delete(docRef)
                
                // Opsional: Hapus foto dari storage jika ada
                try {
                    storage.reference.child("foto_penduduk/$nik.jpg").delete().await()
                } catch (e: Exception) {
                    // Abaikan jika file tidak ada
                }
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMySubmissionRealtime(): Flow<List<Resident>> {
        val user = auth.currentUser ?: return flowOf(emptyList())
        return callbackFlow {
            val registration = firestore.collection("penduduk")
                .whereEqualTo("userId", user.uid)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val list = snapshot?.toObjects(Resident::class.java) ?: emptyList()
                    trySend(list)
                }
            awaitClose { registration.remove() }
        }
    }
}
