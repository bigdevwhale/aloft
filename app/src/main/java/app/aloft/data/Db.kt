package app.aloft.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * One training. [at] is when it ended; [sport] a built-in key or "own_<id>", with its [name] and
 * [element] kept as they were that day. [wx] is the weather outside then (see Wx), [guest] whoever
 * flew in for it.
 */
@Entity
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val at: Long,
    val sport: String,
    val name: String,
    val element: String,
    val minutes: Int,
    val rpe: Int,
    val wx: String = "",
    val temp: Double? = null,
    val guest: String? = null,
    val note: String = "",
)

/** A day marked as rest: calm over the island. */
@Entity
data class Calm(@PrimaryKey val day: Long, val at: Long)

/** A kind of training the user added themselves. */
@Entity
data class OwnSport(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val element: String,
    val hidden: Boolean = false,
) {
    val key get() = "own_$id"
}

/** Ill or injured from [from] until [to] (null while it lasts): the island rides at anchor and nothing fades. */
@Entity
data class Anchor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val from: Long,
    val to: Long? = null,
)

/** The island reached stop [stop] of season [season]'s route. */
@Entity
data class Arrival(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val season: Int,
    val stop: Int,
    val at: Long,
    val read: Boolean = false,
)

/** A guest from the weather, first seen at [first], [count] visits since. */
@Entity
data class Meeting(@PrimaryKey val guest: String, val first: Long, val count: Int = 1, val read: Boolean = false)

/** An islander's request for the week starting [week] (epoch day of its Monday) was done. */
@Entity
data class Thanks(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val week: Long,
    val req: String,
    val at: Long,
    val read: Boolean = false,
)

@Dao
interface IslandDao {
    @Query("SELECT * FROM Workout ORDER BY at DESC") fun workouts(): Flow<List<Workout>>
    @Query("SELECT * FROM Calm ORDER BY day DESC") fun calms(): Flow<List<Calm>>
    @Query("SELECT * FROM OwnSport ORDER BY id") fun own(): Flow<List<OwnSport>>
    @Query("SELECT * FROM Anchor ORDER BY `from`") fun anchors(): Flow<List<Anchor>>
    @Query("SELECT * FROM Arrival ORDER BY season, stop") fun arrivals(): Flow<List<Arrival>>
    @Query("SELECT * FROM Meeting ORDER BY first") fun meetings(): Flow<List<Meeting>>
    @Query("SELECT * FROM Thanks ORDER BY at") fun thanks(): Flow<List<Thanks>>

    @Query("SELECT * FROM Workout") suspend fun allWorkouts(): List<Workout>
    @Query("SELECT * FROM Anchor") suspend fun allAnchors(): List<Anchor>
    @Query("SELECT * FROM Arrival") suspend fun allArrivals(): List<Arrival>
    @Query("SELECT * FROM Meeting WHERE guest = :g") suspend fun meeting(g: String): Meeting?
    @Query("SELECT * FROM Meeting") suspend fun allMeetingsList(): List<Meeting>
    @Query("SELECT * FROM Thanks") suspend fun allThanks(): List<Thanks>
    @Query("SELECT * FROM Calm") suspend fun allCalms(): List<Calm>
    @Query("SELECT * FROM OwnSport") suspend fun allOwn(): List<OwnSport>

    @Insert suspend fun insert(w: Workout): Long
    @Insert suspend fun insert(o: OwnSport): Long
    @Insert suspend fun insert(a: Anchor): Long
    @Insert suspend fun insert(a: Arrival): Long
    @Insert suspend fun insert(t: Thanks): Long
    @Upsert suspend fun upsert(c: Calm)
    @Upsert suspend fun upsert(m: Meeting)

    @Update suspend fun update(w: Workout)
    @Update suspend fun update(o: OwnSport)
    @Update suspend fun update(a: Anchor)
    @Update suspend fun update(a: Arrival)
    @Update suspend fun update(t: Thanks)

    @Query("DELETE FROM Workout WHERE id = :id") suspend fun deleteWorkout(id: Long)
    @Query("DELETE FROM Calm WHERE day = :day") suspend fun deleteCalm(day: Long)
}

@Database(entities = [Workout::class, Calm::class, OwnSport::class, Anchor::class, Arrival::class, Meeting::class, Thanks::class], version = 1, exportSchema = true)
abstract class IslandDb : RoomDatabase() {
    abstract fun dao(): IslandDao

    companion object {
        fun create(ctx: Context): IslandDb = Room.databaseBuilder(ctx, IslandDb::class.java, "island.db").build()
    }
}
