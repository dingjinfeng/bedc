package acquire.core.trans.impl.reversal;

import acquire.core.TransResultListener;
import acquire.core.trans.AbstractTrans;

/**
 * Reversal
 *
 * @author Janson
 * @date 2021/9/10 9:33
 */
public class Reversal extends AbstractTrans {
    @Override
    public void transact(TransResultListener listener) {
        chain.next(new PackReversalStep())
                .proceed(listener::onTransResult);
    }
}
