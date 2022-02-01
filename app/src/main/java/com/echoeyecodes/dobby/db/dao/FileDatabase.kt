package com.echoeyecodes.dobby.db.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.echoeyecodes.dobby.db.models.FileDBModel
import com.echoeyecodes.dobby.utils.TypeConverter

@Database(entities = [FileDBModel::class], version = 1)
@TypeConverters(TypeConverter::class)
abstract class FileDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao

    companion object {
        @Volatile
        private var INSTANCE: FileDatabase? = null
        fun getInstance(context: Context) = INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                FileDatabase::class.java,
                "files_db"
            )
                .fallbackToDestructiveMigration().build()
            INSTANCE = instance
            return instance
        }
    }
}