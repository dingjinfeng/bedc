package acquire.core.trans.impl.settle;

import acquire.core.R;
import acquire.core.constant.ResultCode;
import acquire.core.tools.SignatureDirManager;
import acquire.core.tools.StatisticsUtils;
import acquire.core.trans.BaseStep;
import acquire.database.model.Merchant;
import acquire.database.service.MerchantService;
import acquire.database.service.RecordService;
import acquire.database.service.ReversalDataService;
import acquire.database.service.impl.MerchantServiceImpl;
import acquire.database.service.impl.RecordServiceImpl;
import acquire.database.service.impl.ReversalDataServiceImpl;

/**
 * The {@link BaseStep} that clear records.
 *
 * @author Janson
 * @date 2019/7/29 15:19
 */
class ClearSettleStep extends BaseStep {
    private final MerchantService merchantService = new MerchantServiceImpl();
    private final RecordService recordService = new RecordServiceImpl();

    @Override
    public void intercept(Callback callback)  {
        merchantService.clearHalt(pubBean.getSettleMerchants());
        for (Merchant merchant : pubBean.getSettleMerchants()) {
            // clear the records of this merchant.
            recordService.deleteAll(merchant.getMid(),merchant.getTid());
            // batch num +1 of this merchant.
            StatisticsUtils.increaseBatchNo(merchant.getMid(),merchant.getTid());
            //delete signature bmp files of this merchant.
            SignatureDirManager.clearSignatureDir(merchant.getMid(),merchant.getTid());
        }
        //delete reversal data
        ReversalDataService reversalService = new ReversalDataServiceImpl();
        reversalService.delete();
        pubBean.setResultCode(ResultCode.OK);
        pubBean.setMessage(R.string.core_settle_success);
        callback.onResult(true);
    }
}
