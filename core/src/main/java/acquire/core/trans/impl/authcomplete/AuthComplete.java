package acquire.core.trans.impl.authcomplete;

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
 * Auth Complete
 *
 * @author Janson
 * @date 2019/5/21 14:34
 */
public class AuthComplete extends AbstractTrans {

    @Override
    public void transact(TransResultListener listener) {
        chain.next(new PreCheckStep(true,false))
                .next(new InputAmountStep())
                .next(new InputOrigAuthCodeStep())
                .next(new InputOrigDateStep())
                .next(new ReadCardStep(new InputPinStep(), new PackAuthCompleteStep(),
                        EntryMode.MAG|EntryMode.INSERT|EntryMode.TAP))
                .next(new AddRecordStep(TransStatus.COMPLETED))
                .next(new PrintReceiptStep())
                .proceed(isSucc -> showResult(isSucc, listener));
    }
}
