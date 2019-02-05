package jp.study.fuji.tegakimojidetection

import android.graphics.Bitmap
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.util.*


class FirebaseStorageRepository {
    private val storage: FirebaseStorage
    private val rootRef: StorageReference


    init {
        storage = FirebaseStorage.getInstance()
        rootRef = storage.getReference()
    }

    fun upload(bitmap: Bitmap, label: String): Completable {
        return convertBitmapToByteArray(bitmap)
            .subscribeOn(Schedulers.io()).flatMapCompletable {
                val ref = createStorageReference(label)
                upload(it, ref)
            }

    }

    private fun createStorageReference(label: String): StorageReference {
        val filename = createFileName()
        return rootRef.child("images/$label/$filename")
    }

    private fun createFileName(): String {
        return Date().time.toString() + ".jpeg"
    }

    private fun upload(uploadData: ByteArray, ref: StorageReference): Completable {
        return Completable.create {
            val uploadTask = ref.putBytes(uploadData)
            //TODO ここもっと良い書き方無い？
            val emiter = it
            uploadTask.addOnFailureListener(emiter::onError)
            uploadTask.addOnSuccessListener { emiter.onComplete() }
        }

    }

    private fun convertBitmapToByteArray(bitmap: Bitmap): Single<ByteArray> {
        return Single.create {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            it.onSuccess(baos.toByteArray())
        }
    }
}