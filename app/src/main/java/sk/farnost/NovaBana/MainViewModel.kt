package sk.farnost.NovaBana

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import sk.farnost.NovaBana.download.MassInfoDownloader
import java.io.File
import java.util.*

open class MainViewModel  : ViewModel() {

    private lateinit var newsDatabase: DatabaseReference
    private lateinit var calendarDatabase: DatabaseReference
    private lateinit var dayThoughtDatabase: DatabaseReference
    var status = MutableLiveData<String>()
    var filePath = MutableLiveData<String>()

    internal fun initiateFirebase(url: String) {
        //when outside of america-central, need to use url -> should hide + hide auth
        val database = FirebaseDatabase.getInstance(url)
        database.setPersistenceEnabled(true)
        newsDatabase = database.getReference("Aktuality")
        newsDatabase.keepSynced(true)
        calendarDatabase = database.getReference("Kalendar")
        calendarDatabase.keepSynced(true)
        dayThoughtDatabase = database.getReference("Myslienka")
        dayThoughtDatabase.keepSynced(true)

        //write
        //databaseReference.child("try").setValue("trying")
        //read
        /* myRef.addValueEventListener(object: ValueEventListener {
             override fun onDataChange(snapshot: DataSnapshot) {
                 // This method is called once with the initial value and again
                 // whenever data at this location is updated.
                 val value = snapshot.getValue<String>()
                 Log.d("mValue", "Value is: " + value)
             }
             override fun onCancelled(error: DatabaseError) {
                 Log.w("mValue", "Failed to read value.", error.toException())
             }
         })*/
    }

    fun getAvailableMassInformation(context: Context) {
        val calendar : Calendar = Calendar.getInstance(Locale.ITALY)
        calendar.firstDayOfWeek = 1
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val previousWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val path = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        saveCurrentWeek(path, "oznamy-${currentWeek}.pdf", context)
        deletePreviousWeek("${path}/oznamy-${previousWeek}.pdf")
    }

    private fun saveCurrentWeek(path: File?, filename: String, context: Context) {
        val massInfoFile = File("${path}/${filename}")
        if(!massInfoFile.exists()){
            if(isConnectedToInternet(context)) {
                status.value = "S??ahuje sa..."
                val downloader = MassInfoDownloader()
                viewModelScope.async {
                    val massInfoFilePath = downloader.runDownload(context, filename)
                    status.value = "K dispoz??ci??."
                    filePath.value = massInfoFilePath
                }
            } else
                filePath.value = ""
        } else {
            filePath.value = "${path}/${filename}"
        }
    }

    private fun deletePreviousWeek(path: String) {
        val massInfoFilePrevious = File(path)
        if(massInfoFilePrevious.exists())
            massInfoFilePrevious.delete()
    }


    fun isConnectedToInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION") val networkInfo =
                    connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }
}