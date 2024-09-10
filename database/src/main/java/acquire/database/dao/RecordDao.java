package acquire.database.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;

import acquire.database.model.Record;

/**
 * Access to transaction record
 *
 * @author Janson
 * @date 2021/1/5 17:16
 */
@Dao
public interface RecordDao {
    /**
     * Inserts a transaction record
     *
     * @param record transaction record
     * @return the ID of the new record. If -1,failed.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Record record);
    /**
     * Deletes all transaction records
     *
     * @return the count of deleted records
     */
    @Query("DELETE FROM t_record ")
    int deleteAll();
    /**
     * Deletes all transaction records of a merchant
     *
     * @param mid merchant ID
     * @param tid terminal ID
     * @return the count of deleted records
     */
    @Query("DELETE FROM t_record WHERE MID = (:mid) and TID=(:tid)")
    int deleteAll(String mid, String tid);
    /**
     * Deletes a transaction record by ID
     *
     * @param id record ID
     * @return the count of deleted records
     */
    @Query("DELETE FROM t_record WHERE ID = (:id)")
    int deleteById(long id);
    /**
     * finds all transaction records
     *
     * @return all transaction records
     */
    @Query("SELECT * FROM t_record")
    List<Record> findAll();
    /**
     * finds the transaction records by MID
     *
     * @param mid merchant ID
     * @return the found transaction record
     */
    @Query("SELECT * FROM t_record WHERE MID = :mid and TID =:tid")
    List<Record> findAll(String mid, String tid);
    /**
     * finds a range of transaction records by desc.
     * <p>e.g. <code>findByRangeDesc(3,10)</code>, find 10 records from the bottom 4th</p>
     *
     * @param firstIndex Starting index. The min is 0
     * @param size       the count to find
     * @return the found transaction record
     */
    @Query("SELECT * FROM t_record ORDER BY ID DESC LIMIT :firstIndex, :size ")
    List<Record> findByRangeDesc(int firstIndex, int size);
    /**
     * finds a range of transaction records by acs.
     * <p>e.g. <code>findByRange(3,10)</code>, find 10 records from the 4th</p>
     *
     * @param firstIndex Starting index. The min is 0
     * @param size       the count to find
     * @return the found transaction record
     */
    @Query("SELECT * FROM t_record ORDER BY ID LIMIT :firstIndex, :size ")
    List<Record> findByRange(int firstIndex, int size);
    /**
     * finds a range of a merchant's transaction records by acs.
     */
    @Query("SELECT * FROM t_record WHERE MID = :mid and TID =:tid ORDER BY ID LIMIT :firstIndex,:size ")
    List<Record> findByRange(String mid, String tid,int firstIndex,  int size);
    /**
     * finds the transaction records by trace NO
     *
     * @param traceNo trace
     * @return the found transaction record
     */
    @Query("SELECT * FROM t_record WHERE TRACE_NO = :traceNo")
    List<Record> findByTraceNo(String traceNo);
    @RawQuery()
    List<Record> findBySql(SupportSQLiteQuery sqlQuery);

    /**
     * finds the transaction records by refer number
     *
     * @param referNum efer number
     * @return the found transaction record
     */
    @Query("SELECT * FROM t_record WHERE REFER_NO = :referNum")
    List<Record> findByReferNum(String referNum);

    /**
     * finds the transaction records by auth code
     *
     * @param authCode auth code
     * @return the found transaction record
     */
    @Query("SELECT * FROM t_record WHERE AUTH_CODE = :authCode")
    List<Record> findByAuthCode(String authCode);

    /**
     * finds the transaction records by out order
     *
     * @param outOrderNo out order
     * @return the found transaction record
     */
    @Query("SELECT * FROM t_record WHERE OUT_ORDER_NO = :outOrderNo")
    List<Record> findByOutOrderNo(String outOrderNo);

    /**
     * finds the transaction records by out order
     *
     * @param transType old trans type
     * @return the found transaction record
     */
    @Query("SELECT * FROM t_record WHERE TRANS_TYPE = :transType AND STATUS = 0")
    List<Record> findByTransType(String transType);

    /**
     * finds the transaction records by bizOrder
     *
     * @param bizOrderNo business order
     * @return the found transaction record
     */
    @Query("SELECT * FROM t_record WHERE BIZ_ORDER_NO = :bizOrderNo")
    List<Record> findByQrOrder(String bizOrderNo);

    /**
     * updates a transaction record
     *
     * @param record transaction record
     * @return the count of updated records
     */
    @Update()
    int update(Record record);

    /**
     * Get the records count of a merchant
     *
     * @return records count
     */
    @Query("SELECT COUNT (*) FROM t_record WHERE MID = :mid and TID =:tid")
    int getCount(String mid, String tid);

    /**
     * Get the records count
     *
     * @return records count
     */
    @Query("SELECT COUNT (*) FROM t_record")
    int getCount();

    /**
     * Get the records count with sql statement
     *
     * @param sqlQuery sql statement
     * @return records count
     */
    @RawQuery()
    int getCount(SupportSQLiteQuery sqlQuery);
}
