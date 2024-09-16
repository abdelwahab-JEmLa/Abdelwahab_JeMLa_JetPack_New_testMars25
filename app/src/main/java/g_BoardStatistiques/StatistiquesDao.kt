package g_BoardStatistiques

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface StatistiquesDao {
    @Query("SELECT * FROM Statistiques")
    suspend fun getAll(): List<Statistiques>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(Statistiques: List<Statistiques>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: Statistiques)

    @Delete
    suspend fun delete(article: Statistiques)


    @Update
    suspend fun update(article: Statistiques)


    @Query("DELETE FROM Statistiques")
    suspend fun deleteAll()


}
