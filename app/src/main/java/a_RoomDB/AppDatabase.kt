package a_RoomDB

import a_MainAppCompnents.ArticlesAcheteModele
import a_MainAppCompnents.CategoriesTabelleECB
import a_MainAppCompnents.CategoriesTabelleECBDao
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import b_Edite_Base_Donne.ArticleDao
import com.example.abdelwahabjemlajetpack.c_ManageBonsClients.ArticlesAcheteModeleDao

@Database(
    entities = [
        BaseDonne::class,
        ArticlesAcheteModele::class,
        CategoriesTabelleECB::class  // Add the new entity
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun articlesAcheteModeleDao(): ArticlesAcheteModeleDao
    abstract fun categoriesTabelleECBDao(): CategoriesTabelleECBDao  // Add the new DAO

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
