package acquire.core.trans.impl.voidpreauth;

import acquire.core.TransResultListener;
import acquire.core.constant.TransStatus;
import acquire.core.trans.AbstractTrans;
import acquire.core.trans.steps.AddRecordStep;
import acquire.core.trans.steps.InputAmountStep;
import acquire.core.trans.steps.InputOrigAuthCodeStep;
import acquire.core.trans.steps.InputOrigDateStep;
import acquire.core.trans.steps.InputPinStep;
import acquire.core.trans.steps.PreCheckStep;
import acquire.core.trans.steps.PrintReceiptStep;
import acquire.core.trans.steps.ReadCardStep;
import acquire.sdk.emv.constant.EntryMode;

/**
 * Void pre-auth
 *
 * @author Janson
 * @date 2019/7/31 11:45
 */
public class VoidPreAuth extends AbstractTrans {

    @Override
    public void transact(TransResultListener listener) {
        chain.next(new PreCheckStep(true,true))
                .next(new InputAmountStep())
                .next(new InputOrigAuthCodeStep())
                .next(new InputOrigDateStep())
                .next(new ReadCardStep(null,null, EntryMode.MAG|EntryMode.INSERT|EntryMode.TAP))
                .next(new InputPinStep())
                .next(new PackVoidPreAuthStep())
                .next(new AddRecordStep(TransStatus.CANCELLED))
                .next(new PrintReceiptStep())
                .proceed(isSucc -> showResult(isSucc, listener));
    }
}
