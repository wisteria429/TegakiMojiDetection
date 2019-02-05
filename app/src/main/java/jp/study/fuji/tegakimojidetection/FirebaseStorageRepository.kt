package jp.study.fuji.tegakimojidetection

import android.graphics.Bitmap
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.ByteArrayOutputStream


class FirebaseStorageRepository {
    private val storage: FirebaseStorage
    private val rootRef: StorageReference


    init {
        storage = FirebaseStorage.getInstance()
        rootRef = storage.getReference()
    }

    fun post(bitmap:Bitmap, label:String):Completable {
        return convertBitmapToByteArray(bitmap)
            .subscribeOn(Schedulers.io()).flatMapCompletable {
                upload(it)
            }

    }

    fun upload(uploadData: ByteArray): Completable {
        return Completable.create {
            val uploadTask = rootRef.putBytes(uploadData)
            //TODO ここもっと良い書き方無い？
            val emiter = it
            uploadTask.addOnFailureListener(emiter::onError)
            uploadTask.addOnSuccessListener {emiter.onComplete()}
        }

    }

    fun convertBitmapToByteArray(bitmap: Bitmap): Single<ByteArray> {
        return Single.create {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            it.onSuccess(baos.toByteArray())
        }
    }
}