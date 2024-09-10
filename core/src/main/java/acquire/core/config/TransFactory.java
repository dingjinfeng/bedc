package acquire.core.config;


import acquire.core.R;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.SettleAttr;
import acquire.core.constant.TransType;
import acquire.core.trans.impl.about.About;
import acquire.core.trans.impl.authcomplete.AuthComplete;
import acquire.core.trans.impl.balance.Balance;
import acquire.core.trans.impl.installment.Installment;
import acquire.core.trans.impl.login.Login;
import acquire.core.trans.impl.preauth.PreAuth;
import acquire.core.trans.impl.printdetail.PrintDetail;
import acquire.core.trans.impl.qrcode.QrCode;
import acquire.core.trans.impl.qrrefund.QrRefund;
import acquire.core.trans.impl.refund.Refund;
import acquire.core.trans.impl.reprintlastreceipt.ReprintLastReceipt;
import acquire.core.trans.impl.reprintreceipt.ReprintReceipt;
import acquire.core.trans.impl.reprintsettle.ReprintSettle;
import acquire.core.trans.impl.reversal.Reversal;
import acquire.core.trans.impl.sale.Sale;
import acquire.core.trans.impl.scanpay.ScanPay;
import acquire.core.trans.impl.settings.Settings;
import acquire.core.trans.impl.settle.Settle;
import acquire.core.trans.impl.voidauthcomplete.VoidAuthComplete;
import acquire.core.trans.impl.voidinstallment.VoidInstallment;
import acquire.core.trans.impl.voidpreauth.VoidPreAuth;
import acquire.core.trans.impl.voidsale.VoidSale;

/**
 * A factory that configrues transaction parameters.
 *
 * @author Janson
 * @date 2019/1/16 16:22
 */
public class TransFactory {

    public Param getParam(String transType) {
        if (transType == null){
            return null;
        }
        switch (transType) {
            //Sale
            case TransType.TRANS_SALE:
                return new Param.Builder(Sale.class)
                        .switcher(ParamsConst.PARAMS_KEY_TRANS_SALE)
                        .name(R.string.core_transaction_name_sale)
                        .settleAttr(SettleAttr.PLUS)
                        .create();
            //Query Balance
            case TransType.TRANS_BALANCE:
                return new Param.Builder(Balance.class)
                        .switcher(ParamsConst.PARAMS_KEY_TRANS_BALANCE)
                        .name(R.string.core_transaction_name_balance)
                        .create();
            //Void Sale
            case TransType.TRANS_VOID_SALE:
                return new Param.Builder(VoidSale.class)
                        .switcher(ParamsConst.PARAMS_KEY_TRANS_VOID)
                        .name(R.string.core_transaction_name_void_sale)
                        .settleAttr(SettleAttr.REDUCE)
                        .create();
            //Refund
            case TransType.TRANS_REFUND:
                return new Param.Builder(Refund.class)
                        .switcher(ParamsConst.PARAMS_KEY_TRANS_REFUND)
                        .name(R.string.core_transaction_name_refund)
                        .settleAttr(SettleAttr.REDUCE)
                        .create();
            //Auth Complete
            case TransType.TRANS_AUTH_COMPLETE:
                return new Param.Builder(AuthComplete.class)
                        .switcher(ParamsConst.PARAMS_KEY_TRANS_PREAUTH)
                        .name(R.string.core_transaction_name_auth_complete)
                        .settleAttr(SettleAttr.PLUS)
                        .create();
            //Ppre-Auth
            case TransType.TRANS_PRE_AUTH:
                return new Param.Builder(PreAuth.class)
                        .switcher(ParamsConst.PARAMS_KEY_TRANS_PREAUTH)
                        .name(R.string.core_transaction_name_pre_auth)
                        .create();
            //Void Auth Complete
            case TransType.TRANS_VOID_AUTH_COMPLETE:
                return new Param.Builder(VoidAuthComplete.class)
                        .switcher(ParamsConst.PARAMS_KEY_TRANS_PREAUTH)
                        .name(R.string.core_transaction_name_void_auth_complete)
                        .settleAttr(SettleAttr.REDUCE)
                        .create();
            //Void Pre-Auth
            case TransType.TRANS_VOID_PRE_AUTH:
                return new Param.Builder(VoidPreAuth.class)
                        .switcher(ParamsConst.PARAMS_KEY_TRANS_PREAUTH)
                        .name(R.string.core_transaction_name_void_pre_auth)
                        .create();
            //Settle
            case TransType.TRANS_SETTLE:
                return new Param.Builder(Settle.class)
                        .name(R.string.core_transaction_name_settle)
                        .create();
            //Reprint last record
            case TransType.TRANS_REPRINT_LAST_RECEIPT:
                return new Param.Builder(ReprintLastReceipt.class)
                        .name(R.string.core_transaction_name_reprint_last_receipt)
                        .create();
            //Reprint any record
            case TransType.TRANS_REPRINT_RECEIPT:
                return new Param.Builder(ReprintReceipt.class)
                        .name(R.string.core_transaction_name_reprint_receipt)
                        .create();
            //Reprint settle
            case TransType.TRANS_REPRINT_SETTLE:
                return new Param.Builder(ReprintSettle.class)
                        .name(R.string.core_transaction_name_reprint_settle)
                        .create();
            //Print detail
            case TransType.TRANS_PRINT_DETAIL:
                return new Param.Builder(PrintDetail.class)
                        .name(R.string.core_transaction_name_print_detail)
                        .create();
            //Instalment
            case TransType.TRANS_INSTALLMENT:
                return new Param.Builder(Installment.class)
                        .switcher(ParamsConst.PARAMS_KEY_TRANS_INSTALLMENT)
                        .name(R.string.core_transaction_name_installment)
                        .settleAttr(SettleAttr.PLUS)
                        .create();
            //Void Instalment
            case TransType.TRANS_VOID_INSTALLMENT:
                return new Param.Builder(VoidInstallment.class)
                        .switcher(ParamsConst.PARAMS_KEY_TRANS_INSTALLMENT)
                        .name(R.string.core_transaction_name_void_installment)
                        .settleAttr(SettleAttr.REDUCE)
                        .create();
            //Reversal
            case TransType.TRANS_REVERSAL:
                return new Param.Builder(Reversal.class)
                        .name(R.string.core_transaction_name_reversal)
                        .create();
            //Settings
            case TransType.TRANS_SETTINGS:
                return new Param.Builder(Settings.class)
                        .name(R.string.core_transaction_name_settings)
                        .create();
            //Version information
            case TransType.TRANS_ABOUT:
                return new Param.Builder(About.class)
                        .name(R.string.core_transaction_name_about)
                        .create();
            //Scan pay
            case TransType.TRANS_SCAN_PAY:
                return new Param.Builder(ScanPay.class)
                        .switcher(ParamsConst.PARAMS_KEY_TRANS_MOBILE_PAY)
                        .name(R.string.core_transaction_name_scan_pay)
                        .settleAttr(SettleAttr.PLUS)
                        .create();
            //Qr code
            case TransType.TRANS_QR_CODE:
                return new Param.Builder(QrCode.class)
                        .switcher(ParamsConst.PARAMS_KEY_TRANS_MOBILE_PAY)
                        .name(R.string.core_transaction_name_qr_code)
                        .settleAttr(SettleAttr.PLUS)
                        .create();
            //Qr refund
            case TransType.TRANS_QR_REFUND:
                return new Param.Builder(QrRefund.class)
                        .switcher(ParamsConst.PARAMS_KEY_TRANS_MOBILE_PAY)
                        .name(R.string.core_transaction_name_qr_refund)
                        .settleAttr(SettleAttr.REDUCE)
                        .create();
            //Login
            case TransType.TRANS_LOGIN:
                return new Param.Builder(Login.class)
                        .name(R.string.core_transaction_name_login)
                        .create();
            default:
                return null;
        }
    }

}
