package jp.study.fuji.tegakimojidetection

import android.content.res.AssetManager
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TfAssetLoader(val assets:AssetManager) {
    companion object {
//        private const val MODEL_PATH = "my_model.lite"
        private const val MODEL_PATH = "my_model.lite"
        private const val LABEL_PATH = "my_labels.txt"
    }

    fun loadTfModelFile() : MappedByteBuffer {
        val fileDescriptor = assets.openFd(MODEL_PATH)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel;

        return fileChannel.map(FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    fun loadTfLabelList() : List<String> {
        val labelList = arrayListOf<String>()

        BufferedReader(InputStreamReader(assets.open(LABEL_PATH))).use {
            while(true) {
                val line:String = it.readLine() ?: break
                labelList.add(line)
            }
        }

        return labelList
    }
}