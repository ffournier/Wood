package com.tonytangandroid.wood

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Leaf::class], version = 1, exportSchema = false)
internal abstract class WoodDatabase : RoomDatabase() {
    abstract fun leafDao(): LeafDao

    companion object {
        private var WOOD_DATABASE_INSTANCE: WoodDatabase? = null

        fun getInstance(context: Context): WoodDatabase = WOOD_DATABASE_INSTANCE ?: Room.databaseBuilder<WoodDatabase?>(
            context,
            WoodDatabase::class.java,
            "WoodDatabase"
        ).build().also {
            WOOD_DATABASE_INSTANCE = it
        }
    }
}
