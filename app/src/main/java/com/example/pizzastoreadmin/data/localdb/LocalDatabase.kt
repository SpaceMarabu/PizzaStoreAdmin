package com.example.pizzastoreadmin.data.localdb

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pizzastoreadmin.data.localdb.entity.orders.ListOrdersDbModel
import com.example.pizzastoreadmin.data.localdb.entity.products.ListProductsDbModel

@Database(entities = [ListOrdersDbModel::class, ListProductsDbModel::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class LocalDatabase : RoomDatabase() {

    abstract fun pizzaDao(): PizzaDao

    companion object {

        private var INSTANCE: LocalDatabase? = null
        private var LOCK = Any()
        private const val DB_NAME = "pizza_store.db"

        fun getInstance(application: Application): LocalDatabase {
            INSTANCE?.let {
                return it
            }
            synchronized(LOCK) {
                INSTANCE?.let {
                    return it
                }
                val db = Room.databaseBuilder(
                    application,
                    LocalDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = db
                return db
            }
        }
    }
}