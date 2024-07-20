package a_RoomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import b_Edite_Base_Donne.ArticleDao
import c_ManageBonsClients.ArticlesAcheteModele
import c_ManageBonsClients.ArticlesAcheteModeleDao

@Database(entities = [BaseDonne::class,
                    ArticlesAcheteModele::class,
                     ], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun articlesAcheteModeleDao(): ArticlesAcheteModeleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
