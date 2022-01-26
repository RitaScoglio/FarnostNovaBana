package sk.farnost.NovaBana.massInformation

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import java.io.*

class MassInformationViewModel : ViewModel() {

    var filePath = MutableLiveData<String>()

    fun retrieveFilePath(context: Context) {
        val sharedPref = context.getSharedPreferences("MassInfo", Context.MODE_PRIVATE)
        filePath.value = sharedPref.getString("filePath", "")
    }

}