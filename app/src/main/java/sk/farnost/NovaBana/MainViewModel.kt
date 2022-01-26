package sk.farnost.NovaBana

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import sk.farnost.NovaBana.massInformation.MassInfoDownloader
import sk.farnost.NovaBana.massInformation.MassInformationViewModel
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.*

open class MainViewModel  : ViewModel() {

    private lateinit var newsDatabase: DatabaseReference
    private lateinit var calendarDatabase: DatabaseReference
    private lateinit var dayThoughtDatabase: DatabaseReference

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
        val massInfoFile = File("${path}/oznamy-${currentWeek}.pdf")
        if(!massInfoFile.exists()){
            val downloader = MassInfoDownloader()
            viewModelScope.launch {
                val massInfoFilePath = downloader.runDownload(context, currentWeek)
                val editor = context.getSharedPreferences("MassInfo", Context.MODE_PRIVATE).edit()
                editor.putString("filePath", massInfoFilePath)
                editor.apply()
            }
        }
        val massInfoFilePrevious = File("${path}/oznamy-${previousWeek}.pdf")
        if(massInfoFilePrevious.exists())
            massInfoFilePrevious.delete()
    }

}