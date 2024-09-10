package acquire.core.trans.steps;


import android.text.TextUtils;

import acquire.base.utils.ParamsUtils;
import acquire.core.R;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ResultCode;
import acquire.core.constant.TransStatus;
import acquire.core.tools.DataConverter;
import acquire.core.trans.BaseStep;
import acquire.database.model.Record;
import acquire.database.model.ReversalData;
import acquire.database.service.RecordService;
import acquire.database.service.ReversalDataService;
import acquire.database.service.impl.RecordServiceImpl;
import acquire.database.service.impl.ReversalDataServiceImpl;

/**
 * The {@link BaseStep} that add the record to database
 *
 * @author Janson
 * @date 2018/11/23 10:42
 */
public class AddRecordStep extends BaseStep {
    /**
     * The status of the original record to be updated .
     *
     * @see TransStatus
     */
    private int newStatus = -1;

    /**
     * @param newStatus Original record status to this new status
     * @see TransStatus
     */
    public AddRecordStep(int newStatus) {
        this.newStatus = newStatus;
    }

    public AddRecordStep() {
    }

    @Override
    public void intercept(Callback callback) {
        RecordService recordService = new RecordServiceImpl();
        if (TextUtils.isEmpty(pubBean.getRemarks())){
            //receipt remarks
            pubBean.setRemarks(ParamsUtils.getString(ParamsConst.PARAMS_KEY_PRINT_REMARKS));
        }
        Record origRecord = getOrigRecord();
        // 1. save the transaction
        Record record = new Record();
        DataConverter.pubBeanToRecord(pubBean, record);
        boolean result = recordService.add(record);
        if (!result) {
            pubBean.setResultCode(ResultCode.FL);
            pubBean.setMessage(R.string.core_add_record_fail);
            callback.onResult(false);
            return;
        }
        //2. delete reversal data that is pre-saved
        ReversalDataService reversalDataService = new ReversalDataServiceImpl();
        ReversalData reversalData = reversalDataService.getReverseRecord();
        if (reversalData != null) {
            reversalDataService.delete();
        }
        //3. set printed record data
        setRecord(record);
        //4. update original record status
        if (origRecord != null && newStatus >= 0) {
            origRecord.setStatus(newStatus);
            recordService.update(origRecord);
        }
        callback.onResult(true);
    }

}
