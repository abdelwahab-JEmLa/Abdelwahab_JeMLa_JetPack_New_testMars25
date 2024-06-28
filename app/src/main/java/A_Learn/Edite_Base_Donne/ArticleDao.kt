package A_Learn.Edite_Base_Donne

import a_RoomDB.BaseDonne
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY idCategorie, classementCate")
    suspend fun getAllArticlesOrder(): List<BaseDonne>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<BaseDonne>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(article: BaseDonne)

    @Query("DELETE FROM articles")
    suspend fun deleteAll()

    @Query("DELETE FROM articles WHERE idArticle = :idArticle")
    suspend fun delete(idArticle: Int)
}
//SELECT * FROM BaseDonne ORDER BY idCategorie, classementCate