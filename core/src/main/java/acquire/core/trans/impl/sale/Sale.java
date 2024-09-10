package acquire.core.trans.impl.sale;

import acquire.core.TransResultListener;
import acquire.core.trans.AbstractTrans;
import acquire.core.trans.steps.AddRecordStep;
import acquire.core.trans.steps.FlyReceiptStep;
import acquire.core.trans.steps.InputAmountStep;
import acquire.core.trans.steps.InputPinStep;
import acquire.core.trans.steps.PreCheckStep;
import acquire.core.trans.steps.PrintReceiptStep;
import acquire.core.trans.steps.ReadCardStep;
import acquire.core.trans.steps.SignatureStep;
import acquire.core.trans.steps.TipAmountStep;
import acquire.sdk.emv.constant.EntryMode;

/**
 * Sale
 *
 * @author Janson
 * @date 2019/4/24 10:04
 */
public class Sale extends AbstractTrans {

    @Override
    public void transact(TransResultListener listener) {
        chain.next(new PreCheckStep(true,  false))
                .next(new InputAmountStep())
                .next(new TipAmountStep())
                .next(new ReadCardStep(new InputPinStep(), new PackSaleStep(),
                        EntryMode.MAG | EntryMode.INSERT | EntryMode.TAP | EntryMode.MANUAL))
                .next(new AddRecordStep())
                .next(new SignatureStep())
                .next(new PrintReceiptStep())
                .next(new FlyReceiptStep())
                .proceed(isSucc -> showResult(isSucc,listener));
    }
}
