package a_MainAppCompnents

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface CategoriesTabelleECBDao {
    @Transaction
    suspend fun transaction(block: suspend CategoriesTabelleECBDao.() -> Unit) {
        block()
    }

    @Query("SELECT * FROM CategoriesTabelleECB ORDER BY idClassementCategorieInCategoriesTabele")
    suspend fun getAllCategoriesList(): MutableList<CategoriesTabelleECB>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoriesTabelleECB)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoriesTabelleECB>)

    @Query("DELETE FROM CategoriesTabelleECB")
    suspend fun deleteAll()

    @Update
    suspend fun updateAll(categories: List<CategoriesTabelleECB>)
}

