package b2_Edite_Base_Donne_With_Creat_New_Articls

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoriesTabelleECB)
}

interface CategoriesRepository {
    fun getAllCategories(): Flow<List<CategoriesTabelleECB>>
    suspend fun moveCategory(fromCategoryId: Long, toCategoryId: Long)
    suspend fun reorderCategories(fromCategoryId: Long, toCategoryId: Long)
    suspend fun importCategoriesFromFirebase()
}

class CategoriesRepositoryImpl(
    private val categoriesDao: CategoriesTabelleECBDao,
    firebaseDatabase: FirebaseDatabase
) : CategoriesRepository {
    private val refCategorieTabelee = firebaseDatabase.getReference("H_CategorieTabele")

    override fun getAllCategories(): Flow<List<CategoriesTabelleECB>> {
        return categoriesDao.getAllCategories()
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
                // Handle any errors, such as network issues
                e.printStackTrace()
            }
        }
    }

    override suspend fun moveCategory(fromCategoryId: Long, toCategoryId: Long) {
        withContext(Dispatchers.IO) {
            val fromPosition = categoriesDao.getCategoryPosition(fromCategoryId)
            val toPosition = categoriesDao.getCategoryPosition(toCategoryId)

            if (fromPosition == toPosition) return@withContext

            val start = minOf(fromPosition, toPosition)
            val end = maxOf(fromPosition, toPosition)

            val affectedCategoriesTabelleECB = categoriesDao.getCategoriesTabelleECBBetweenPositions(start, end)

            when {
                fromPosition < toPosition -> {
                    // Moving down: Shift affected categories up
                    affectedCategoriesTabelleECB.forEach { category ->
                        when (category.idCategorieInCategoriesTabele) {
                            fromCategoryId -> updateCategoryPosition(category.idCategorieInCategoriesTabele, toPosition)
                            else -> updateCategoryPosition(
                                category.idCategorieInCategoriesTabele,
                                (category.idClassementCategorieInCategoriesTabele - 1)
                            )
                        }
                    }
                }
                else -> {
                    // Moving up: Shift affected categories down
                    affectedCategoriesTabelleECB.forEach { category ->
                        when (category.idCategorieInCategoriesTabele) {
                            fromCategoryId -> updateCategoryPosition(category.idCategorieInCategoriesTabele, toPosition)
                            else -> updateCategoryPosition(
                                category.idCategorieInCategoriesTabele,
                                (category.idClassementCategorieInCategoriesTabele + 1)
                            )
                        }
                    }
                }
            }
        }
    }

    override suspend fun reorderCategories(fromCategoryId: Long, toCategoryId: Long) {
        moveCategory(fromCategoryId, toCategoryId)
    }

    private suspend fun updateCategoryPosition(categoryId: Long, newPosition: Int) {
        categoriesDao.updateCategoryPosition(categoryId, newPosition)
        refCategorieTabelee.child(categoryId.toString())
            .child("idClassementCategorieInCategoriesTabele")
            .setValue(newPosition)
            .await()
    }
}

@Entity(tableName = "CategoriesTabelleECB")
data class CategoriesTabelleECB(
    @PrimaryKey(autoGenerate = true)
    val idCategorieInCategoriesTabele: Long = 0,
    val nomCategorieInCategoriesTabele: String = "",
    var idClassementCategorieInCategoriesTabele: Int = 0
) {
    // Add a no-argument constructor
    constructor() : this(0, "", 0)
}
