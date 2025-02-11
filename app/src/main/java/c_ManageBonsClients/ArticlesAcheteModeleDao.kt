package c_ManageBonsClients

import a_MainAppCompnents.ArticlesAcheteModele
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert

@Dao
interface ArticlesAcheteModeleDao {
    @Query("SELECT * FROM ArticlesAcheteModele")
    suspend fun getAll(): List<ArticlesAcheteModele>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ArticlesAcheteModele: List<ArticlesAcheteModele>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: ArticlesAcheteModele)

    @Delete
    suspend fun delete(article: ArticlesAcheteModele)

    @Upsert
    suspend fun upsert(ArticlesAcheteModele: List<ArticlesAcheteModele>)

    @Update
    suspend fun update(article: ArticlesAcheteModele)
    @Update
    suspend fun updateFromeDataManageBonsClientsModel(article: ArticlesAcheteModele)

    @Query("DELETE FROM ArticlesAcheteModele")
    suspend fun deleteAll()

    @Query("DELETE FROM ArticlesAcheteModele WHERE idArticle = :idArticle")
    suspend fun delete(idArticle: Int)

}
//SELECT * FROM ManageBonsClientsModel ORDER BY idCategorie, classementCate
