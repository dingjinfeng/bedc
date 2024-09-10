package acquire.database.service.impl;

import java.util.List;

import acquire.base.utils.LoggerUtils;
import acquire.database.AcquireDatabase;
import acquire.database.dao.ReversalDataDao;
import acquire.database.model.ReversalData;
import acquire.database.service.ReversalDataService;

/**
 * The implemented class of{@link ReversalDataService}.
 * <p>It is used to access the reversal table according to business needs
 *
 * @author Janson
 * @date 2021/1/5 17:15
 */
public class ReversalDataServiceImpl implements ReversalDataService {
    private final ReversalDataDao mReversalRecordDao;

    public ReversalDataServiceImpl() {
        mReversalRecordDao = AcquireDatabase.getInstance().reverseRecordDao();
    }

    @Override
    public ReversalData getReverseRecord() {
        List<ReversalData> reversalData = mReversalRecordDao.findAll();
        if (reversalData == null || reversalData.size() == 0){
            return null;
        }
        //there should be only one reversal data in the reversal table
        return reversalData.get(0);
    }

    @Override
    public boolean add(ReversalData reversalData)  {
        if (getReverseRecord() != null) {
            LoggerUtils.e("Reversal data already exists, cannot add");
            return false;
        }
        return mReversalRecordDao.insert(reversalData) > 0;
    }

    @Override
    public boolean delete() {
        return mReversalRecordDao.deleteAll() >= 0;
    }

    @Override
    public boolean updateField55(String field55) {
        ReversalData rw = getReverseRecord() ;
        if (rw == null) {
            LoggerUtils.e("Update Field55: No reversal data");
            return true;
        }
        rw.setField55(field55);
        return mReversalRecordDao.update(rw) >= 0;
    }


}
