package com.example.securepass

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CardDao {
    @Insert
    suspend fun insert(card: CardEntity)

    @Query("SELECT * FROM cards")
    suspend fun getAllCards(): List<CardEntity>
}
