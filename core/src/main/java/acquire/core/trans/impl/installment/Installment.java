package acquire.core.trans.impl.installment;

import acquire.core.TransResultListener;
import acquire.core.trans.AbstractTrans;
import acquire.core.trans.steps.AddRecordStep;
import acquire.core.trans.steps.InputAmountStep;
import acquire.core.trans.steps.InputInstallmentStep;
import acquire.core.trans.steps.InputPinStep;
import acquire.core.trans.steps.PreCheckStep;
import acquire.core.trans.steps.PrintReceiptStep;
import acquire.core.trans.steps.ReadCardStep;
import acquire.core.trans.steps.SignatureStep;
import acquire.sdk.emv.constant.EntryMode;

/**
 * Installment pay
 *
 * @author Janson
 * @date 2021/8/3 17:42
 */
public class Installment extends AbstractTrans {
    @Override
    public void transact(TransResultListener listener) {
        chain.next(new PreCheckStep(true,  false))
                .next(new InputAmountStep())
                .next(new InputInstallmentStep())
                .next(new ReadCardStep(new InputPinStep(), new PackInstallmentStep(),
                        EntryMode.MAG | EntryMode.INSERT | EntryMode.TAP))
                .next(new AddRecordStep())
                .next(new SignatureStep())
                .next(new PrintReceiptStep())
                .proceed(isSucc -> showResult(isSucc, listener));
    }
}
