package b2_Edite_Base_Donne_With_Creat_New_Articls

import android.util.Log
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Dao
interface CategoriesTabelleECBDao {
    @Query("SELECT * FROM CategoriesTabelleECB ORDER BY idClassementCategorieInCategoriesTabele")
    fun getAllCategories(): Flow<List<CategoriesTabelleECB>>

    @Query("UPDATE CategoriesTabelleECB SET idClassementCategorieInCategoriesTabele = :newPosition WHERE idCategorieInCategoriesTabele = :categoryId")
    suspend fun updateCategoryPosition(categoryId: Long, newPosition: Int)

    @Query("SELECT idClassementCategorieInCategoriesTabele FROM CategoriesTabelleECB WHERE idCategorieInCategoriesTabele = :categoryId")
    suspend fun getCategoryPosition(categoryId: Long): Int

    @Query("SELECT * FROM CategoriesTabelleECB WHERE idClassementCategorieInCategoriesTabele BETWEEN :start AND :end ORDER BY idClassementCategorieInCategoriesTabele")
    suspend fun getCategoriesTabelleECBBetweenPositions(start: Int, end: Int): List<CategoriesTabelleECB>

    @Query("""
            UPDATE CategoriesTabelleECB 
            SET idClassementCategorieInCategoriesTabele = idClassementCategorieInCategoriesTabele + 1 
            WHERE idClassementCategorieInCategoriesTabele >= :startPosition
        """)
    suspend fun incrementPositionsFromStartPosition(startPosition: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoriesTabelleECB)
}

interface CategoriesRepository {
    fun getAllCategories(): Flow<List<CategoriesTabelleECB>>
    suspend fun moveCategory(fromCategoryId: Long, toCategoryId: Long)
    suspend fun reorderCategories(fromCategoryId: Long, toCategoryId: Long)
    suspend fun importCategoriesFromFirebase()
    suspend fun addNewCategory(categoryName: String): Result<Unit>
    suspend fun getNextCategoryId(): Long
    suspend fun batchUpdateFirebasePositions(categories: List<CategoriesTabelleECB>)
    suspend fun updateCategoryPosition(categoryId: Long, newPosition: Int)
}

class CategoriesRepositoryImpl(
    private val categoriesDao: CategoriesTabelleECBDao,
    firebaseDatabase: FirebaseDatabase
) : CategoriesRepository {
    private val refCategorieTabelee = firebaseDatabase.getReference("H_CategorieTabele")

    override fun getAllCategories(): Flow<List<CategoriesTabelleECB>> {
        return categoriesDao.getAllCategories()
    }

    override suspend fun getNextCategoryId(): Long = withContext(Dispatchers.IO) {
        val categories = categoriesDao.getAllCategories().first()
        (categories.maxOfOrNull { it.idCategorieInCategoriesTabele } ?: 0) + 1
    }

    override suspend fun updateCategoryPosition(categoryId: Long, newPosition: Int) {
        try {
            categoriesDao.updateCategoryPosition(categoryId, newPosition)

            refCategorieTabelee.child(categoryId.toString())
                .child("idClassementCategorieInCategoriesTabele")
                .setValue(newPosition)
                .await()
        } catch (e: Exception) {
            Log.e("CategoriesRepository", "Failed to update category position", e)
            throw e
        }
    }

    override suspend fun importCategoriesFromFirebase() {
        withContext(Dispatchers.IO) {
            try {
                val snapshot = refCategorieTabelee.get().await()
                snapshot.children.forEach { categorySnapshot ->
                    val category = categorySnapshot.getValue(CategoriesTabelleECB::class.java)
                    category?.let {
                        categoriesDao.insert(it)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    override suspend fun reorderCategories(fromCategoryId: Long, toCategoryId: Long) {
        moveCategory(fromCategoryId, toCategoryId)
    }
    override suspend fun addNewCategory(categoryName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val newCategory = CategoriesTabelleECB(
                idCategorieInCategoriesTabele = getNextCategoryId(),
                idClassementCategorieInCategoriesTabele = 1,
                nomCategorieInCategoriesTabele = categoryName
            )

            // Increment all existing positions in one SQL query
            categoriesDao.incrementPositionsFromStartPosition(1)

            categoriesDao.insert(newCategory)

            // Update Firebase with new category
            refCategorieTabelee.child(newCategory.idCategorieInCategoriesTabele.toString())
                .setValue(newCategory)
                .await()

            // Get updated categories and update Firebase
            val updatedCategories = categoriesDao.getAllCategories().first()
                .filter { it.idCategorieInCategoriesTabele != newCategory.idCategorieInCategoriesTabele }

            batchUpdateFirebasePositions(updatedCategories)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CategoriesRepository", "Failed to add new category", e)
            Result.failure(e)
        }
    }

    override suspend fun batchUpdateFirebasePositions(categories: List<CategoriesTabelleECB>) {
        try {
            if (categories.isEmpty()) return

            val updates = categories.associate { category ->
                "/${category.idCategorieInCategoriesTabele}/idClassementCategorieInCategoriesTabele" to
                        category.idClassementCategorieInCategoriesTabele
            }

            refCategorieTabelee.updateChildren(updates).await()
        } catch (e: Exception) {
            Log.e("CategoriesRepository", "Failed to batch update Firebase positions", e)
            throw e
        }
    }

    override suspend fun moveCategory(fromCategoryId: Long, toCategoryId: Long) {
        withContext(Dispatchers.IO) {
            val fromPosition = categoriesDao.getCategoryPosition(fromCategoryId)
            val toPosition = categoriesDao.getCategoryPosition(toCategoryId)

            if (fromPosition == toPosition) return@withContext

            val start = minOf(fromPosition, toPosition)
            val end = maxOf(fromPosition, toPosition)

            val affectedCategories = categoriesDao.getCategoriesTabelleECBBetweenPositions(start, end)

            // Update local database positions
            when {
                fromPosition < toPosition -> {
                    affectedCategories.forEach { category ->
                        when (category.idCategorieInCategoriesTabele) {
                            fromCategoryId -> categoriesDao.updateCategoryPosition(category.idCategorieInCategoriesTabele, toPosition)
                            else -> categoriesDao.updateCategoryPosition(
                                category.idCategorieInCategoriesTabele,
                                category.idClassementCategorieInCategoriesTabele - 1
                            )
                        }
                    }
                }
                else -> {
                    affectedCategories.forEach { category ->
                        when (category.idCategorieInCategoriesTabele) {
                            fromCategoryId -> categoriesDao.updateCategoryPosition(category.idCategorieInCategoriesTabele, toPosition)
                            else -> categoriesDao.updateCategoryPosition(
                                category.idCategorieInCategoriesTabele,
                                category.idClassementCategorieInCategoriesTabele + 1
                            )
                        }
                    }
                }
            }

            batchUpdateFirebasePositions(affectedCategories)
        }
    }
}

@Entity(tableName = "CategoriesTabelleECB")
data class CategoriesTabelleECB(
    @PrimaryKey(autoGenerate = true)
    val idCategorieInCategoriesTabele: Long = 0,
    val nomCategorieInCategoriesTabele: String = "",
    var idClassementCategorieInCategoriesTabele: Int = 0
) {
    constructor() : this(0, "", 0)
}
