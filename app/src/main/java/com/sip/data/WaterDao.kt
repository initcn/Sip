package com.sip.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {

    /*
    ---------------------------------------------------
    MUTATIONS
    ---------------------------------------------------
    */

    @Insert
    suspend fun insertEntry(entry: WaterEntry)

    @Delete
    suspend fun deleteEntry(entry: WaterEntry)

    /*
    ---------------------------------------------------
    QUERIES
    ---------------------------------------------------
    */

    @Query("SELECT * FROM water_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<WaterEntry>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM water_entries WHERE timestamp BETWEEN :start AND :end")
    fun getTotalBetween(start: Long, end: Long): Flow<Int>

    /*
    ---------------------------------------------------
    INDIE STATS EXTENSIONS (COMPUTED ON SQLITE THREAD)
    ---------------------------------------------------
    */

    /**
     * Groups entries into 24-hour calendar days based on timestamps,
     * sums each day's amount, and returns the highest single daily total.
     */
    @Query(
        """
        SELECT COALESCE(MAX(daily_sum), 0) 
        FROM (
            SELECT SUM(amount) AS daily_sum 
            FROM water_entries 
            WHERE timestamp BETWEEN :start AND :end 
            GROUP BY (timestamp / 86400000)
        )
        """
    )
    fun getBestDayBetween(start: Long, end: Long): Flow<Int>
}