package b_Edite_Base_Donne

import a_RoomDB.BaseDonne
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY idCategorie, classementCate")
    suspend fun getAllArticlesOrder(): List<BaseDonne>

    @Query("SELECT * FROM articles ORDER BY dateCreationCategorie")
    suspend fun getAllArticlesOrderDate(): List<BaseDonne>

    @Query("UPDATE articles SET nomArab = :newArabName WHERE idArticle = :idArticle")
    suspend fun updateArticleArabName(idArticle: Long, newArabName: String)

    // Méthode pour insérer un article
    @Insert
    suspend fun insert(article: BaseDonne)

    @Update
    suspend fun updateArticle(article: BaseDonne)

    @Query("SELECT * FROM articles WHERE idArticle = :id")
    suspend fun getArticleById(id: Long): BaseDonne?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<BaseDonne>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: BaseDonne)

    @Delete
    suspend fun delete(article: BaseDonne)

    @Upsert
    suspend fun upsert(articles: List<BaseDonne>)

    @Update
    suspend fun update(article: BaseDonne)
    @Update
    suspend fun updateFromeDataBaseDonne(article: BaseDonne)

    @Query("DELETE FROM articles")
    suspend fun deleteAll()

    @Query("DELETE FROM articles WHERE idArticle = :idArticle")
    suspend fun delete(idArticle: Int)

}
//SELECT * FROM BaseDonne ORDER BY idCategorie, classementCate
