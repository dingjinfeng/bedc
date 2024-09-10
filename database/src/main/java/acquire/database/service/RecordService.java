package acquire.database.service;

import java.util.List;

import acquire.database.dao.RecordDao;
import acquire.database.model.Record;

/**
 * The service interface that uses {@link RecordDao} to access to the transaction record table.
 *
 * @author Janson
 * @date 2021/1/5 17:20
 */
public interface RecordService {

    /**
     * Add a transaction record
     */
    boolean add(Record record);

    /**
     * Clear all transaction records
     */
    boolean deleteAll();

    /**
     * Clear all transaction records of a merchant
     *
     * @param mid merhcant id
     * @param tid terminal id
     */
    boolean deleteAll(String mid, String tid);

    /**
     * Delete a transaction record by id
     */
    boolean delete(long id);

    /**
     * Delete a transaction record by trace
     */
    boolean delete(String traceNo);

    /**
     * find all transaction records
     */
    List<Record> findAll();

    /**
     * find all transaction records of a merchant
     *
     * @param mid merchant id
     * @param tid terminal id
     */
    List<Record> findAll(String mid, String tid);

    /**
     * Delete the record index
     */
    Record findByIndex(int index);

    /**
     * find a transaction record by merchant
     */
    Record findByIndex(String mid, String tid, int index);

    /**
     * find a transaction record by trace
     */
    Record findByTrace(String trace);

    /**
     * find a transaction record by reference num
     */
    Record findByReferNum(String referNum);

    /**
     * find a transaction record by auth code
     *
     * @param transType the found transaction type
     * @param authCode  auth code
     */
    Record findByAuthCode(String transType, String authCode);

    /**
     * find a transaction record by outOrderNo
     */
    Record findByOutOrderNo(String outOrderNo);

    /**
     * find a transaction record by transType
     */
    List<Record> findByTransType(String transType);

    /**
     * find a transaction record by qr order
     */
    Record findByQrOrder(String qrOrder);

    /**
     * find the last transaction record
     */
    Record findLast();

    /**
     * Get the count of the transaction records
     */
    int getCount();

    /**
     * find the transaction count record by merchant
     */
    int getCount(String mid, String tid);

    /**
     * Get the count of the transaction records
     *
     * @param transTypes the found transtypes
     * @param startDate  start date
     * @param endDate    end date
     * @return the count
     */
    int getCount(String[] transTypes, String startDate, String endDate);

    /**
     * update a transaction record
     */
    boolean update(Record record);

    /**
     * find the transaction records by pages and desc
     *
     * @param pageNoCur start page index,min is 0
     * @param pageSize  the count every page
     */
    List<Record> findByPageDesc(int pageNoCur, int pageSize);

    /**
     * find the transaction records by pages and desc
     *
     * @param transTypes the found transaction type
     * @param startDate  start date
     * @param endDate    end date
     * @param pageNoCur  start page index,min is 0
     * @param pageSize   the count every page
     */
    List<Record> findByPageDesc(String[] transTypes, String startDate, String endDate, int pageNoCur, int pageSize);

    /**
     * find the transaction records by pages and desc
     *
     * @param transTypes the found transaction type
     * @param status     transaction status
     * @param startDate  start date
     * @param endDate    end date
     * @param pageNoCur  start page index,min is 0
     * @param pageSize   the count every page
     */
    List<Record> findByPageDesc(String[] transTypes, int[] status, String startDate, String endDate, int pageNoCur, int pageSize);
}
