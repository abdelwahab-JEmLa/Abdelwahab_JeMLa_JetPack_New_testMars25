package b_Edite_Base_Donne

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert

@Dao
interface DataBaseDonneDao {
    @Query("SELECT * FROM articles ORDER BY idCategorie, classementCate")
    suspend fun getAllArticlesOrder(): List<DataBaseDonne>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<DataBaseDonne>)

    @Upsert
    suspend fun upsert(articles: List<DataBaseDonne>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: DataBaseDonne)

    @Update
    suspend fun update(article: DataBaseDonne)

    @Update
    suspend fun updateFromeDataBaseDonne(article: DataBaseDonne)

    @Query("DELETE FROM articles")
    suspend fun deleteAll()

    @Query("DELETE FROM articles WHERE idArticle = :idArticle")
    suspend fun delete(idArticle: Int)
}
