package acquire.core.fragment.print;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import acquire.base.BaseApplication;
import acquire.base.utils.BitmapUtils;
import acquire.base.utils.BytesUtils;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.TlvUtils;
import acquire.base.utils.emv.EmvTag;
import acquire.base.utils.emv.EmvTlv;
import acquire.base.utils.file.FileUtils;
import acquire.base.utils.qrcode.QRCodeUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.R;
import acquire.core.constant.FileConst;
import acquire.core.constant.FileDir;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.PrintSize;
import acquire.core.constant.SettleAttr;
import acquire.core.constant.TransType;
import acquire.core.esc.EscPrinter;
import acquire.core.tools.CurrencyCodeProvider;
import acquire.core.tools.StatisticsUtils;
import acquire.core.tools.TransUtils;
import acquire.database.model.Merchant;
import acquire.database.model.Record;
import acquire.database.service.RecordService;
import acquire.database.service.impl.MerchantServiceImpl;
import acquire.database.service.impl.RecordServiceImpl;
import acquire.sdk.device.BDevice;
import acquire.sdk.emv.constant.EntryMode;
import acquire.sdk.printer.BPrinter;
import acquire.sdk.printer.BitmapDraw;
import acquire.sdk.printer.IPrinter;

/**
 * Print ViewModel
 *
 * @author Janson
 * @date 2023/4/28 10:05
 */
public class PrintViewModel extends ViewModel {
    public final static int STATUS_OUT_OF_PAPER = -2,STATUS_ERROR = -1, STATUS_READY = 0, STATUS_SUCCESS = 1, STATUS_NEXT_RECEIPT = 2;

    private int index = 0;
    private IPrinter printer = new BPrinter();
    private boolean supportCut;

    private final MutableLiveData<Bitmap> receipt = new MutableLiveData<>();
    private final MutableLiveData<PrtStatus> status = new MutableLiveData<>();

    private final MutableLiveData<Integer> prompt = new MutableLiveData<>();

    public MutableLiveData<Bitmap> getReceipt() {
        return receipt;
    }

    public MutableLiveData<PrtStatus> getStatus() {
        return status;
    }

    public MutableLiveData<Integer> getPrompt() {
        return prompt;
    }

    public void init() {
        supportCut = BDevice.isCpos();
        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL)) {
            int mode = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL_CONNECT_MODE);
            int baudRata = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL_SERIAL_BAUDRATE);
            printer = new EscPrinter(mode, baudRata);
            supportCut = true;
        }
    }

    /**
     * print receipt. Such as purchase ticket.
     */
    public void printReceipt(Record record, boolean isReprint) {
        status.postValue(new PrtStatus(STATUS_READY));
        int total = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_PRINT_COUNT);
        switch (index) {
            case 0:
                prompt.postValue(R.string.core_print_progress_merchant_copy);
                break;
            case 1:
                prompt.postValue(R.string.core_print_progress_customer_copy);
                break;
            case 2:
            default:
                prompt.postValue(R.string.core_print_progress_bank_copy);
                break;
        }
        Bitmap bitmap = getReceipt(record, isReprint, index);
        receipt.postValue(bitmap);
        bitmap = fitPaper(bitmap);
        printer.print(bitmap, new IPrinter.PrintCallback() {
            @Override
            public void onFinish() {
                //finish
                if (supportCut) {
                    printer.cutPaper();
                }
                index++;
                if (index == total) {
                    status.postValue(new PrtStatus(STATUS_SUCCESS));
                } else {
                    if (index == 1) {
                        status.postValue(new PrtStatus(STATUS_NEXT_RECEIPT, getString(R.string.core_print_dialog_title_customer)));
                    } else {
                        status.postValue(new PrtStatus(STATUS_NEXT_RECEIPT, getString(R.string.core_print_dialog_title_bank)));
                    }
                }
            }

            @Override
            public void onError(String message) {
                status.postValue(new PrtStatus(STATUS_ERROR, message));
            }

            @Override
            public void onOutOfPaper() {
                status.postValue(new PrtStatus(STATUS_OUT_OF_PAPER));
            }
        });
    }

    /**
     * generate receipt bitmap
     */
    public static @NonNull Bitmap getReceipt(Record record, boolean isReprint, @IntRange(from = 0) int index) {
        BitmapDraw bitmapDraw = new BitmapDraw();
        try {
            bitmapDraw.image(BitmapFactory.decodeStream(BaseApplication.getAppContext().getAssets().open(FileConst.LOGO_IMG)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        bitmapDraw.text(getString(R.string.core_receipt_merchant_id_title), record.getMid(), PrintSize.NORMAL, false);
        bitmapDraw.text(getString(R.string.core_receipt_terminal_id_title), record.getTid(), PrintSize.NORMAL, false);
        bitmapDraw.text(getString(R.string.core_receipt_date_time_title), FormatUtils.formatTimeStamp(record.getDate() + record.getTime()), PrintSize.NORMAL, false);
        bitmapDraw.text(getString(R.string.core_receipt_batch_title), record.getBatchNo(), PrintSize.NORMAL, false);
        bitmapDraw.text(getString(R.string.core_receipt_trace_title), record.getTraceNo(), PrintSize.NORMAL, false);
        bitmapDraw.text(getString(R.string.core_receipt_refnum_title), record.getReferNo(), PrintSize.NORMAL, false);
        bitmapDraw.text(getString(R.string.core_receipt_authcode_title), record.getAuthCode(), PrintSize.NORMAL, false);
        if (!TextUtils.isEmpty(record.getOrigTraceNo())) {
            bitmapDraw.text(getString(R.string.core_receipt_orig_trace_title), record.getOrigTraceNo(), PrintSize.NORMAL, false);
        }
        if (!TextUtils.isEmpty(record.getOrigAuthCode())) {
            bitmapDraw.text(getString(R.string.core_receipt_orig_authcode_title), record.getOrigAuthCode(), PrintSize.NORMAL, false);
        }

        if (!TextUtils.isEmpty(record.getBizOrderNo())) {
            Bitmap bitmap = QRCodeUtils.create2dCode(record.getBizOrderNo());
            if (bitmap != null) {
                bitmapDraw.image(bitmap);
                bitmapDraw.text(record.getBizOrderNo(), PrintSize.NORMAL, false, Paint.Align.CENTER);
            }
        }
        bitmapDraw.text(TransUtils.getName(record.getTransType()), PrintSize.TRAN_TYPE, true, Paint.Align.CENTER);
        bitmapDraw.text(record.getCardOrg(), PrintSize.NORMAL, false, Paint.Align.CENTER);
        bitmapDraw.text(EntryMode.getDescription(record.getEntryMode()), PrintSize.NORMAL, false, Paint.Align.CENTER);
        bitmapDraw.text(FormatUtils.maskCardNo(record.getCardNo()), PrintSize.NORMAL, false, Paint.Align.CENTER);
        bitmapDraw.text(CurrencyCodeProvider.getCurrencySymbol(record.getCurrencyCode()) + FormatUtils.formatAmount(record.getAmount()), PrintSize.AMOUNT, true, Paint.Align.CENTER);
        switch (record.getTransType()) {
            case TransType.TRANS_QR_CODE:
            case TransType.TRANS_QR_REFUND:
            case TransType.TRANS_SCAN_PAY:
                break;
            default:
                //signature
                if (index == 0) {
                    //merchant copy
                    if (record.isFreeSign()) {
                        bitmapDraw.text(getString(R.string.core_receipt_no_signature), PrintSize.NORMAL, false, Paint.Align.CENTER);
                    } else {
                        if (!TextUtils.isEmpty(record.getSignPath())) {
                            bitmapDraw.image(BitmapFactory.decodeFile(record.getSignPath()));
                        } else {
                            bitmapDraw.feedPaper(PrintSize.SIGN_FEED);
                        }
                        bitmapDraw.text(getString(R.string.core_receipt_signature_line), PrintSize.NORMAL, false, Paint.Align.CENTER);
                    }
                }
                break;
        }
        bitmapDraw.text(record.getRemarks(), PrintSize.NORMAL, false, Paint.Align.CENTER);
        if (record.getEntryMode() == EntryMode.INSERT || record.getEntryMode() == EntryMode.TAP) {
            EmvTlv[] emvTlvs = TlvUtils.getTlvList(BytesUtils.hexToBytes(record.getEmvPrintData()));
            if (emvTlvs != null) {
                boolean appNamePrinted = false;
                for (EmvTlv emvTlv : emvTlvs) {
                    byte[] value = emvTlv.getValue();
                    switch (emvTlv.getTag()) {
                        case EmvTag.TAG_9F12_IC_APPNAME:
                        case EmvTag.TAG_50_IC_APPLABEL:
                            if (!appNamePrinted) {
                                String emvAppName = new String(value);
                                //check ASCII
                                if (emvAppName.matches("\\A\\p{ASCII}*\\z")) {
                                    bitmapDraw.text(getString(R.string.core_receipt_emv_app_title), emvAppName, PrintSize.SMALL, false);
                                    appNamePrinted = true;
                                }
                            }
                            break;
                        case EmvTag.TAG_4F_IC_AID:
                            bitmapDraw.text(getString(R.string.core_receipt_emv_aid_title), BytesUtils.bcdToString(value), PrintSize.SMALL, false);
                            break;
                        case EmvTag.TAG_95_TM_TVR:
                            bitmapDraw.text(getString(R.string.core_receipt_emv_tvr_title), BytesUtils.bcdToString(value), PrintSize.SMALL, false);
                            break;
                        case EmvTag.TAG_9B_TM_TSI:
                            bitmapDraw.text(getString(R.string.core_receipt_emv_tsi_title), BytesUtils.bcdToString(value), PrintSize.SMALL, false);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        if (isReprint) {
            bitmapDraw.text(getString(R.string.core_receipt_reprint_flag), PrintSize.NORMAL, false, Paint.Align.CENTER);
        }
        switch (index) {
            case 0:
                bitmapDraw.text(getString(R.string.core_receipt_merchant_copy), PrintSize.NORMAL, false, Paint.Align.CENTER);
                break;
            case 1:
                bitmapDraw.text(getString(R.string.core_receipt_customer_copy), PrintSize.NORMAL, false, Paint.Align.CENTER);
                break;
            case 2:
            default:
                bitmapDraw.text(getString(R.string.core_receipt_bank_copy), PrintSize.NORMAL, false, Paint.Align.CENTER);
                break;
        }
        bitmapDraw.text("-------------x----------------x-------------", PrintSize.LINE, false, Paint.Align.CENTER);
        return bitmapDraw.getBitmap();
    }

    /**
     * print settle ticket.
     */
    public void printSettlement(List<Merchant> merchants, boolean isReprint) {
        status.postValue(new PrtStatus(STATUS_READY));
        prompt.postValue(R.string.core_print_progress_settlement);
        Merchant merchant = merchants.get(index);
        //settlement receipt path
        Bitmap bitmap;
        if (isReprint) {
            bitmap = BitmapFactory.decodeFile(merchant.getLastReceipt());
            if (bitmap == null) {
                index++;
                if (index == merchants.size()) {
                    status.postValue(new PrtStatus(STATUS_SUCCESS));
                } else {
                    status.postValue(new PrtStatus(STATUS_NEXT_RECEIPT));
                }
                return;
            }
        } else {
            bitmap = getSettleTicket(merchant);
            //save settlement receipt
            String dir = FileDir.EXTERNAL_ROOT + File.separator + "settleReceipt" + File.separator + merchant.getMid();
            String path = dir + File.separator + merchant.getTid();
            FileUtils.createDir(dir);
            BitmapUtils.saveBmp(bitmap, path);
            merchant.setLastReceipt(path);
            new MerchantServiceImpl().update(merchant);
        }
        receipt.postValue(bitmap);
        bitmap = fitPaper(bitmap);
        printer.print(bitmap, new IPrinter.PrintCallback() {
            @Override
            public void onFinish() {
                //finish
                if (supportCut) {
                    printer.cutPaper();
                }
                index++;
                if (index == merchants.size()) {
                    status.postValue(new PrtStatus(STATUS_SUCCESS));
                } else {
                    status.postValue(new PrtStatus(STATUS_NEXT_RECEIPT));
                }
            }
            @Override
            public void onError(String message) {
                status.postValue(new PrtStatus(STATUS_ERROR, message));
            }

            @Override
            public void onOutOfPaper() {
                status.postValue(new PrtStatus(STATUS_OUT_OF_PAPER));
            }
        });
    }

    /**
     * generate settle ticket bitmap
     */
    public static Bitmap getSettleTicket(Merchant merchant) {
        BitmapDraw bitmapDraw = new BitmapDraw();
        try {
            bitmapDraw.image(BitmapFactory.decodeStream(BaseApplication.getAppContext().getAssets().open(FileConst.LOGO_IMG)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        bitmapDraw.text(getString(R.string.core_receipt_merchant_title), ParamsUtils.getString(ParamsConst.PARAMS_KEY_BASE_MERCHANT_NAME), PrintSize.NORMAL, false);
        bitmapDraw.text(getString(R.string.core_receipt_merchant_id_title), merchant.getMid(), PrintSize.NORMAL, false);
        bitmapDraw.text(getString(R.string.core_receipt_terminal_id_title), merchant.getTid(), PrintSize.NORMAL, false);
        bitmapDraw.text(getString(R.string.core_receipt_date_time_title), FormatUtils.formatTimeStamp(merchant.getSettleDate() + merchant.getSettleTime())
                , PrintSize.NORMAL, false);
        if (merchant.isSettleEqual()) {
            bitmapDraw.text(getString(R.string.core_receipt_settle_balance_title), PrintSize.NORMAL, false, Paint.Align.CENTER);
        } else {
            bitmapDraw.text(getString(R.string.core_receipt_settle_unbalance_title), PrintSize.NORMAL, false, Paint.Align.CENTER);
        }
        bitmapDraw.text(getString(R.string.core_receipt_settle_flag), PrintSize.NORMAL, true, Paint.Align.CENTER);
        bitmapDraw.text(getString(R.string.core_receipt_transaction_column), getString(R.string.core_receipt_count_column), getString(R.string.core_receipt_amount_column), PrintSize.NORMAL, false);
        Map<String, long[]> total = StatisticsUtils.getTotalAmtNum(merchant.getMid(), merchant.getTid());
        long totalAmt = 0;
        int totalNum = 0;
        for (String transType : total.keySet()) {
            long[] amtNum = total.get(transType);
            if (amtNum == null) {
                continue;
            }
            long amt = amtNum[0];
            long num = amtNum[1];
            bitmapDraw.text(TransUtils.getName(transType), num + "", FormatUtils.formatAmount(amt)
                    , PrintSize.NORMAL, false);
            int settleAttr = TransUtils.getSettleAttr(transType);
            if (settleAttr == SettleAttr.PLUS) {
                totalAmt += amt;
            } else if (settleAttr == SettleAttr.REDUCE) {
                totalAmt -= amt;
            }
            totalNum += num;
        }
        bitmapDraw.text(getString(R.string.core_receipt_total), totalNum + "", FormatUtils.formatAmount(totalAmt), PrintSize.NORMAL, false);
        bitmapDraw.text("-------------x----------------x-------------", PrintSize.LINE, false, Paint.Align.CENTER);
        return bitmapDraw.getBitmap();
    }

    private final Bitmap RECEIPT_END = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

    /**
     * print all transaction record detail
     */
    public void printDetail() {
        prompt.postValue(R.string.core_print_progress_detail);
        status.postValue(new PrtStatus(STATUS_READY));
        Queue<Bitmap> queue = new ConcurrentLinkedQueue<>();
        ThreadPool.execute(() -> offerDetailBitmap(queue));
        ThreadPool.execute(() -> printDetailQueue(queue));
    }

    /**
     * detail bitmap
     */
    private void offerDetailBitmap(Queue<Bitmap> queue) {
        queue.clear();
        RecordService recordService = new RecordServiceImpl();
        BitmapDraw bitmapDraw = new BitmapDraw();
        bitmapDraw.text(getString(R.string.core_receipt_detail), PrintSize.NORMAL, false, Paint.Align.CENTER);
        bitmapDraw.text(getString(R.string.core_receipt_transaction_column), getString(R.string.core_receipt_card_column), getString(R.string.core_receipt_amount_column), getString(R.string.core_receipt_trace_column), PrintSize.SMALL, false);
        // print all records
        int count = recordService.getCount();
        for (int index = 0; index < count; index++) {
            if (queue.peek() == RECEIPT_END) {
                //'printQueue()'(maybe failed) close the queue
                return;
            }
            Record record = recordService.findByIndex(index);
            if (record == null) {
                continue;
            }
            //add item
            String transType = TransUtils.getName(record.getTransType());
            String pan = FormatUtils.maskCardNo(record.getCardNo());
            if (pan.length() > 7) {
                pan = pan.substring(pan.length() - 7);
            }
            String amount = CurrencyCodeProvider.getCurrencySymbol(record.getCurrencyCode()) + FormatUtils.formatAmount(record.getAmount());
            String traceNo = record.getTraceNo();
            int[] percents = new int[]{25, 25, 25, 25};
            String[] texts = new String[]{transType, pan, amount, traceNo};
            Paint.Align[] aligns = new Paint.Align[]{Paint.Align.LEFT, Paint.Align.CENTER, Paint.Align.RIGHT, Paint.Align.RIGHT};
            float[] textSizes = new float[]{PrintSize.SMALL, PrintSize.SMALL, PrintSize.SMALL, PrintSize.SMALL};
            boolean[] bolds = new boolean[]{false, false, false, false};
            bitmapDraw.textMulti(percents, texts, aligns, textSizes, bolds);
            if (index > 0 && index % 60 == 0) {
                //print data
                queue.offer(bitmapDraw.getBitmap());
                bitmapDraw = new BitmapDraw();
            }
        }
        bitmapDraw.text("-------------x----------------x-------------", PrintSize.LINE, false, Paint.Align.CENTER);
        queue.offer(bitmapDraw.getBitmap());
        queue.offer(RECEIPT_END);

    }

    /**
     * detail printing queue
     */
    private void printDetailQueue(Queue<Bitmap> queue) {
        Bitmap bitmap;
        while ((bitmap = queue.poll()) == null) {
        }
        if (bitmap == RECEIPT_END) {
            //finish
            if (supportCut) {
                printer.cutPaper();
            }
            status.postValue(new PrtStatus(STATUS_SUCCESS));
            return;
        }
        final Bitmap finalBitmap = bitmap;
        ThreadPool.postOnMain(() -> receipt.setValue(finalBitmap));
        if (queue.peek() == RECEIPT_END) {
            //receipt end
            bitmap = fitPaper(bitmap);
        }
        //print
        printer.print(bitmap, new IPrinter.PrintCallback() {
            @Override
            public void onFinish() {
                printDetailQueue(queue);
            }

            @Override
            public void onError(String message) {
                queue.offer(RECEIPT_END);
                status.postValue(new PrtStatus(STATUS_ERROR, message));
            }

            @Override
            public void onOutOfPaper() {
                queue.offer(RECEIPT_END);
                status.postValue(new PrtStatus(STATUS_OUT_OF_PAPER));
            }
        });
    }

    private Bitmap fitPaper(Bitmap bitmap) {
        if (PrintSize.END_FEED == 0) {
            return bitmap;
        }
        int height = PrintSize.END_FEED;
        if (printer instanceof EscPrinter){
            height = 50;
        }
        Bitmap tailBitmap = Bitmap.createBitmap(bitmap.getWidth(), height, bitmap.getConfig());
        Canvas canvas = new Canvas(tailBitmap);
        canvas.drawColor(Color.WHITE);
        return BitmapUtils.mergeVertical(bitmap, tailBitmap);
    }

    private static String getString(@StringRes int resId, Object... formatArgs) {
        return BaseApplication.getAppString(resId, formatArgs);
    }

    public static class PrtStatus {
        private final int status;
        private String message;

        public PrtStatus(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public PrtStatus(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}
