package acquire.core.trans.impl.voidsale;

import acquire.core.TransResultListener;
import acquire.core.constant.TransStatus;
import acquire.core.constant.TransType;
import acquire.core.trans.AbstractTrans;
import acquire.core.trans.steps.AddRecordStep;
import acquire.core.trans.steps.FindOrigTraceStep;
import acquire.core.trans.steps.InputPinStep;
import acquire.core.trans.steps.PreCheckStep;
import acquire.core.trans.steps.PrintReceiptStep;
import acquire.core.trans.steps.ReadCardStep;
import acquire.sdk.emv.constant.EntryMode;

/**
 * Void Sale
 *
 * @author Janson
 * @date 2019/7/25 9:29
 */
public class VoidSale extends AbstractTrans {

    @Override
    public void transact(TransResultListener listener) {
        chain.next(new PreCheckStep(true,true))
                .next(new FindOrigTraceStep(new String[]{TransType.TRANS_SALE},new int[]{TransStatus.SUCCESS}))
                .next(new ReadCardStep(null,null, EntryMode.MAG|EntryMode.INSERT|EntryMode.TAP))
                .next(new InputPinStep())
                .next(new PackVoidSaleStep())
                .next(new AddRecordStep(TransStatus.CANCELLED))
                .next(new PrintReceiptStep())
                .proceed(isSucc -> showResult(isSucc, listener));
    }
}
