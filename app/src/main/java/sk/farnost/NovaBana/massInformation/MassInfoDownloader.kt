package sk.farnost.NovaBana.massInformation

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
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.ArrayList

class MassInfoDownloader {
    private lateinit var filePath: String

    suspend fun runDownload(context: Context, currentWeek: Int): String {
        checkAndRequestPermissions(context)
        val fileURL = doInBackground()
        onPostExecute(context, fileURL, currentWeek)
        return filePath
    }

    private suspend fun doInBackground(): String = withContext(Dispatchers.IO) { // to run code in Background Thread
        try {
            val document: Document = Jsoup.connect("https://novabana.fara.sk/oznamy-2/").get()
            val element: Element = document.select("a.wp-block-file__button").first()
            val url = element.attr("href")
            return@withContext url
        } catch (ignored: Exception) {
            return@withContext ""
        }
    }

    private fun checkAndRequestPermissions(context : Context): Boolean {
        val permissionACCESS_NETWORK_STATE = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_NETWORK_STATE
        )
        val permissionINTERNET = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.INTERNET
        )
        val permissionWRITE_EXTERNAL_STORAGE = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val permissionREAD_EXTERNAL_STORAGE = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        val listPermissionsNeeded: MutableList<String> = ArrayList()
        if (permissionACCESS_NETWORK_STATE != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_NETWORK_STATE)
        }
        if (permissionWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionREAD_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionINTERNET != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.INTERNET)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            for (item in listPermissionsNeeded)
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(item), 1)
            return false
        }
        return true
    }

    @SuppressLint("Range")
    private fun onPostExecute(context: Context, fileURL: String, currentWeek: Int) {
        val fileName = "oznamy-${currentWeek}.pdf"
        val extStorageDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

        val file = File("${extStorageDirectory}/${fileName}")

        /*try {
            file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }*/

        val request = DownloadManager.Request(Uri.parse(fileURL))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationUri(Uri.fromFile(file))
            .setTitle(fileName)
            .setDescription("Downloading")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadID =
            downloadManager.enqueue(request)

        var finishDownload = false
        while (!finishDownload) {
            val cursor: Cursor =
                downloadManager.query(DownloadManager.Query().setFilterById(downloadID))
            if (cursor.moveToFirst()) {
                val status: Int =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                when (status) {
                    DownloadManager.STATUS_FAILED -> {
                        finishDownload = true
                    }
                    DownloadManager.STATUS_PAUSED -> {
                    }
                    DownloadManager.STATUS_PENDING -> {
                    }
                    DownloadManager.STATUS_RUNNING -> {
                        //to publish progress
                        /*val total: Long =
                            cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        if (total >= 0) {
                            val downloaded: Long = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                              publishProgress((int) ((downloaded * 100L) / total));
                        }*/
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        // publishProgress(progress)
                        finishDownload = true
                        filePath = file.path
                        //Toast.makeText(context, "Download Completed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

}