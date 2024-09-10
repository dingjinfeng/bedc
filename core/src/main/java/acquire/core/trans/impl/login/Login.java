package acquire.core.trans.impl.login;

import acquire.core.TransResultListener;
import acquire.core.trans.AbstractTrans;


/**
 * Login
 *
 * @author Janson
 * @date 2022/10/8 14:37
 */
public class Login extends AbstractTrans {

    @Override
    public void transact(TransResultListener listener) {
        chain.next(new PackLoginStep())
                .proceed(listener::onTransResult);
    }
}
