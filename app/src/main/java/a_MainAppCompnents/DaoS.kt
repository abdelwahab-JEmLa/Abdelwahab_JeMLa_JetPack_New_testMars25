package a_MainAppCompnents

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update

@Dao
interface CategoriesTabelleECBDao {

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


@Entity(tableName = "CategoriesTabelleECB")
data class CategoriesTabelleECB(
    @PrimaryKey(autoGenerate = true)
    val idCategorieInCategoriesTabele: Long = 0,
    val nomCategorieInCategoriesTabele: String = "",
    var idClassementCategorieInCategoriesTabele: Int = 0
) {
    constructor() : this(0, "", 0)
}
