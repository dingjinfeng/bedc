package acquire.database.service.impl;


import android.text.TextUtils;

import androidx.room.Entity;
import androidx.sqlite.db.SimpleSQLiteQuery;

import java.util.List;
import java.util.Locale;

import acquire.base.utils.LoggerUtils;
import acquire.database.AcquireDatabase;
import acquire.database.dao.RecordDao;
import acquire.database.model.Record;
import acquire.database.service.RecordService;

/**
 * The implemented class of{@link RecordService}.
 * <p>It is used to access the transaction record table according to business needs
 *
 * @author Janson
 * @date 2021/1/5 17:18
 */
public class RecordServiceImpl implements RecordService {
    /**
     * Transaction record table. see {@link Record}'s {@link Entity}
     */
    private final static String TABLE_NAME = "T_RECORD";

    private final RecordDao mRecordDao;

    public RecordServiceImpl() {
        mRecordDao = AcquireDatabase.getInstance().recordDao();
    }

    @Override
    public boolean add(Record record) {
        if (record == null) {
            LoggerUtils.e("Add record failed: Record is null");
            return false;
        }
        if (record.getTraceNo() == null) {
            LoggerUtils.e("Add record failed: TraceNo is null");
            return false;
        }
        // determine if it is a duplicate trace
        if (findByTrace(record.getTraceNo()) != null) {
            LoggerUtils.e("Add record failed: Duplicate trace,trace is " + record.getTraceNo());
            return false;
        }
        return mRecordDao.insert(record) > 0;
    }

    @Override
    public boolean delete(long id) {
        return mRecordDao.deleteById(id) >= 0;
    }

    @Override
    public boolean delete(String traceNo) {
        Record record = findByTrace(traceNo);
        if (record == null) {
            LoggerUtils.e("Delete record failed: No such record with trace[" + traceNo + "]");
            return true;
        }
        return delete(record.getId());
    }

    @Override
    public boolean deleteAll() {
        return mRecordDao.deleteAll() >= 0;
    }

    @Override
    public boolean deleteAll(String mid, String tid) {
        return mRecordDao.deleteAll(mid, tid) >= 0;
    }

    @Override
    public Record findByTrace(String trace) {
        if (trace == null) {
            return null;
        }
        List<Record> list = mRecordDao.findByTraceNo(trace);
        return list.size() > 0 ? list.get(0) : null;
    }

    @Override
    public Record findByReferNum(String referNum) {
        if (referNum == null) {
            return null;
        }
        List<Record> list = mRecordDao.findByReferNum(referNum);
        return list.size() > 0 ? list.get(0) : null;
    }

    @Override
    public Record findByAuthCode(String transType, String authCode) {
        if (authCode == null) {
            return null;
        }
        List<Record> records = mRecordDao.findByAuthCode(authCode);
        for (Record record : records) {
            if (record.getTransType().equals(transType)) {
                return record;
            }
        }
        return null;
    }


    @Override
    public Record findByIndex(int index) {
        List<Record> records = mRecordDao.findByRange(index, 1);
        if (records == null || records.size() == 0) {
            return null;
        }
        return records.get(0);
    }

    @Override
    public Record findByIndex(String mid, String tid, int index) {
        if (mid == null || tid == null) {
            return null;
        }
        List<Record> records = mRecordDao.findByRange(index, 1);
        if (records == null || records.size() == 0) {
            return null;
        }
        return records.get(0);
    }

    @Override
    public Record findByOutOrderNo(String outOrderNo) {
        if (outOrderNo == null) {
            return null;
        }
        List<Record> list = mRecordDao.findByOutOrderNo(outOrderNo);
        return list.size() > 0 ? list.get(0) : null;
    }


    @Override
    public List<Record> findByTransType(String oldTransType) {
        return mRecordDao.findByTransType(oldTransType);
    }

    @Override
    public Record findByQrOrder(String qrOrder) {
        if (qrOrder == null) {
            return null;
        }
        List<Record> list = mRecordDao.findByQrOrder(qrOrder);
        return list.size() > 0 ? list.get(0) : null;
    }

    @Override
    public Record findLast() {
        List<Record> records = mRecordDao.findByRangeDesc(0, 1);
        if (records == null || records.size() == 0) {
            return null;
        }
        return records.get(0);
    }

    @Override
    public int getCount() {
        return mRecordDao.getCount();
    }

    @Override
    public int getCount(String mid, String tid) {
        return mRecordDao.getCount(mid, tid);
    }

    @Override
    public boolean update(Record record) {
        if (record == null) {
            LoggerUtils.e("Update record failed: Params is null.");
            return false;
        }
        if (record.getId() <= 0) {
            Record tmp = findByTrace(record.getTraceNo());
            if (tmp == null) {
                LoggerUtils.e("Update record failed: No such record with trace " + record.getTraceNo());
                return false;
            }
            record.setId(tmp.getId());
        }
        return mRecordDao.update(record) >= 0;
    }

    @Override
    public List<Record> findAll() {
        return mRecordDao.findAll();
    }
    @Override
    public List<Record> findAll(String mid, String tid) {
        if (mid == null || tid == null) {
            return null;
        }
        return mRecordDao.findAll(mid, tid);
    }

    @Override
    public int getCount(String[] transTypes, String startDate, String endDate) {
        String and = " and ";
        StringBuilder builder = new StringBuilder();
        //transaction types
        if (transTypes != null) {
            StringBuilder temp = new StringBuilder();
            for (int i = 0; i < transTypes.length; i++) {
                temp.append("'")
                        .append(transTypes[i])
                        .append("'");
                if (i != transTypes.length - 1) {
                    temp.append(",");
                }
            }
            if (temp.length() > 0) {
                String format = String.format(Locale.getDefault(), "TRANS_TYPE in (%s)", temp);
                builder.append(format).append(and);
            }
        }
        /*
         *sql: start date and end date
         */
        if (!TextUtils.isEmpty(startDate)) {
            String format = String.format(Locale.getDefault(), "(DATE >= '%s')", startDate);
            builder.append(format).append(and);
        }
        if (!TextUtils.isEmpty(endDate)) {
            String format = String.format(Locale.getDefault(), "(DATE <= '%s')", endDate);
            builder.append(format).append(and);
        }
        //sql:where
        String where = builder.toString();
        if (where.endsWith(and)) {
            where = where.substring(0, where.length() - and.length());
        }
        //sql: create
        String sql;
        if (TextUtils.isEmpty(where)) {
            sql = String.format(Locale.getDefault(), "select count (*) from %s ", TABLE_NAME);
        } else {
            sql = String.format(Locale.getDefault(), "select count (*) from %s where %s", TABLE_NAME, where);
        }
        LoggerUtils.d("sql: " + sql);
        return mRecordDao.getCount(new SimpleSQLiteQuery(sql));
    }

    @Override
    public List<Record> findByPageDesc(int pageNoCur, int pageSize) {
        int firstIndex = pageNoCur * pageSize;
        return mRecordDao.findByRangeDesc(firstIndex, pageSize);
    }


    @Override
    public List<Record> findByPageDesc(String[] transTypes, String startDate, String endDate, int pageNoCur, int pageSize) {
        return findByPageDesc(transTypes, null, startDate, endDate, pageNoCur, pageSize);
    }

    @Override
    public List<Record> findByPageDesc(String[] transTypes, int[] status, String startDate, String endDate, int pageNoCur, int pageSize) {
        //sql: where
        String where = getPageSqlWhere(transTypes, status, startDate, endDate);
        //sql:create
        String sql;
        int firstIndex = pageNoCur * pageSize;
        if (TextUtils.isEmpty(where)) {
            sql = String.format(Locale.getDefault(), "select * from %s order by ID desc limit %d,%d", TABLE_NAME, firstIndex, pageSize);
        } else {
            sql = String.format(Locale.getDefault(), "select * from %s where %s order by ID desc limit %d,%d ", TABLE_NAME, where, firstIndex, pageSize);
        }
        LoggerUtils.d("sql: " + sql);
        return mRecordDao.findBySql(new SimpleSQLiteQuery(sql));
    }


    /**
     * Get paging SQL condition statement
     */
    private String getPageSqlWhere(String[] transTypes, int[] status, String startDate, String endDate) {
        String and = " and ";
        StringBuilder builder = new StringBuilder();

        //sql: where
        if (transTypes != null) {
            StringBuilder temp = new StringBuilder();
            for (int i = 0; i < transTypes.length; i++) {
                temp.append("'")
                        .append(transTypes[i])
                        .append("'");
                if (i != transTypes.length - 1) {
                    temp.append(",");
                }
            }
            if (temp.length() > 0) {
                String format = String.format(Locale.getDefault(), "TRANS_TYPE in (%s)", temp);
                builder.append(format);
                builder.append(and);
            }
        }
        if (status != null) {
            StringBuilder temp = new StringBuilder();
            for (int i = 0; i < status.length; i++) {
                temp.append("'")
                        .append(status[i])
                        .append("'");
                if (i != status.length - 1) {
                    temp.append(",");
                }
            }
            if (temp.length() > 0) {
                String format = String.format(Locale.getDefault(), "STATUS in (%s)", temp);
                builder.append(format);
                builder.append(and);
            }
        }
        /*
         *sql: start date and end date
         */
        if (!TextUtils.isEmpty(startDate)) {
            String format = String.format(Locale.getDefault(), "(DATE >= '%s')", startDate);
            builder.append(format);
            builder.append(and);
        }
        if (!TextUtils.isEmpty(endDate)) {
            String format = String.format(Locale.getDefault(), "(DATE <= '%s')", endDate);
            builder.append(format);
            builder.append(and);
        }
        String where = builder.toString();
        if (where.endsWith(and)) {
            where = where.substring(0, where.length() - and.length());
        }
        return where;
    }


}
