package acquire.core.tools;


import android.util.ArrayMap;

import java.util.Locale;
import java.util.Map;

import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.core.constant.ParamsConst;
import acquire.database.model.Merchant;
import acquire.database.model.Record;
import acquire.database.service.MerchantService;
import acquire.database.service.RecordService;
import acquire.database.service.impl.MerchantServiceImpl;
import acquire.database.service.impl.RecordServiceImpl;

/**
 * Statistics utils
 *
 * @author Janson
 * @date 2018/12/13 9:50
 */
public class StatisticsUtils {

    /**
     * Trace number +1
     */
    public static synchronized void increaseTraceNo() {
        String traceNo = ParamsUtils.getString(ParamsConst.PARAMS_KEY_BASE_TRACE_NO);
        int currenNo = 1;
        try {
            currenNo = Integer.parseInt(traceNo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int nextNo = (currenNo + 1) % 1000000;
        if (nextNo == 0){
            nextNo = 1;
        }
        ParamsUtils.setString(ParamsConst.PARAMS_KEY_BASE_TRACE_NO, String.format(Locale.getDefault(), "%06d", nextNo));
    }

    /**
     * Batch number +1
     */
    public static synchronized void increaseBatchNo(String mid,String tid) {
        MerchantService merchantService = new MerchantServiceImpl();
        Merchant merchant = merchantService.find(mid,tid);
        if (merchant == null){
            LoggerUtils.e("No such merchant[mid = "+mid+",tid = "+tid+"].");
            return;
        }
        String batchNo = merchant.getBatchNo();
        int currenNo = 1;
        try {
            currenNo = Integer.parseInt(batchNo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int nextNo = (currenNo + 1) % 1000000;
        if (nextNo == 0){
            nextNo = 1;
        }
        merchant.setBatchNo(String.format(Locale.getDefault(), "%06d", nextNo));
        merchantService.update(merchant);
    }

    /**
     * Get transaction total data by mid and tid
     * @return TransType- long[]{amount，count}
     */
    public static Map<String,long[]> getTotalAmtNum(String mid,String tid){
        RecordService recordService = new RecordServiceImpl();
        int count = recordService.getCount(mid,tid);
        Map<String,long[]> total = new ArrayMap<>();
        for (int i = 0; i < count; i++) {
            Record record = recordService.findByIndex(mid,tid,i);
            String transType = record.getTransType();
            long[] amtNum = total.computeIfAbsent(transType, k -> new long[]{0, 0});
            amtNum[0] += record.getAmount();
            amtNum[1] ++;
        }
        return total;
    }

}
