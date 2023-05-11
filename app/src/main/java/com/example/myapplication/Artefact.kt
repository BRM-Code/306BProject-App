package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import android.content.Context
import android.graphics.Bitmap.CompressFormat
import android.os.Environment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.io.IOException

class Artefact() : Parcelable{
    lateinit var name: String
    lateinit var descriptionShort: String
    lateinit var descriptionLong: String
    private lateinit var imageUrl: String
    lateinit var year: String
    private var image: Bitmap? = null
    private val tag = "Artefact"

    constructor(parcel: Parcel) : this() {
        name = parcel.readString() as String
        descriptionShort = parcel.readString() as String
        descriptionLong = parcel.readString() as String
        imageUrl = parcel.readString() as String
        year = parcel.readString() as String
    }

    constructor(document: DocumentSnapshot): this(){
        this.name = document.data?.get("Name").toString()
        this.descriptionShort = document.data?.get("DescriptionShort").toString()
        this.descriptionLong = document.data?.get("DescriptionLong").toString()
        this.imageUrl = document.data?.get("ImageURL").toString()
        this.year = document.data?.get("Year").toString()
    }

    fun getImage(context: Context): Bitmap? {
        Log.d(tag, "getImage: $imageUrl")
        // If the image has already been downloaded, return it
        if (this.image != null) {
            Log.d(tag, "Found image in cache")
            return this.image
        }
        if (imageUrl.isEmpty()) {
            Log.d(tag, "No image URL")
            return null
        }
        // check if it has been downloaded to a file
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File(storageDir, name)
        if (imageFile.exists()) {
            Log.d(tag, "Found image in file")
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            this.image = bitmap
            return bitmap
        }



        // Load the image in a background thread using a coroutine
        return runBlocking {
            Log.d(tag, "Downloading image")
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    val `in` = java.net.URL(imageUrl).openStream()
                    BitmapFactory.decodeStream(`in`)
                }
                this@Artefact.image = bitmap
                GlobalScope.launch { saveBitmapToFile(context, bitmap, name) }
                bitmap
            } catch (e: Exception) {
                Log.e(tag, "Failed to load image: ${e.message}")
                null
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(descriptionShort)
        parcel.writeString(descriptionLong)
        parcel.writeString(imageUrl)
        parcel.writeString(year)
    }

    override fun describeContents(): Int {
        return 0
    }

    private fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileName: String): Boolean {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File(storageDir, fileName)

        return try {
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    companion object CREATOR : Parcelable.Creator<Artefact> {
        override fun createFromParcel(parcel: Parcel): Artefact {
            return Artefact(parcel)
        }

        override fun newArray(size: Int): Array<Artefact?> {
            return arrayOfNulls(size)
        }
    }
}
