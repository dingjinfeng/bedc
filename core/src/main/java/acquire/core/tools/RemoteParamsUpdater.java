package acquire.core.tools;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.WindowManager;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acquire.base.BaseApplication;
import acquire.base.utils.AppUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.base.widget.dialog.progress.ProgressDialog;
import acquire.core.R;
import acquire.core.constant.ParamsConst;
import acquire.database.model.Merchant;
import acquire.database.service.MerchantService;
import acquire.database.service.impl.MerchantServiceImpl;
import acquire.sdk.ExtServiceHelper;
import acquire.sdk.FlyParameterHelper;
import acquire.sdk.ServiceHelper;
import acquire.sdk.emv.BEmvParamLoader;
import acquire.sdk.emv.BExtEmvParamLoader;
import acquire.sdk.emv.IEmvParamLoader;

/**
 * A remote params updater based on TOMS FlyParameters.
 *
 * @author Janson
 * @date 2022/7/26 15:27
 */
public class RemoteParamsUpdater {
    private ProgressDialog progressDialog;

    /**
     * receuve Fly Parameters data and parse it.
     */
    public void updateParams(Context context) {
        ThreadPool.postOnMain(() -> {
            Dialog dlg =  new AlertDialog.Builder(BaseApplication.getAppContext())
                    .setTitle(AppUtils.getAppName(context))
                    .setMessage(R.string.core_fly_parameter_dialog_updating_title)
                    .setPositiveButton(R.string.base_ok, (dialog, which) -> update(context))
                    .setNegativeButton(R.string.base_cancel, (dialog, which) -> {})
                    .create();
//            MessageDialog dlg = new MessageDialog.Builder(context)
//                    .setMessage(R.string.core_fly_parameter_dialog_updating_title)
//                    .setConfirmButton(dialog -> update(context))
//                    .setCancelButton(dialog -> {})
//                    .create();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            } else {
                dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }
            dlg.show();
        });


    }

    private void update(Context context) {
        LoggerUtils.d("FlyParameter request");
        progressDialog = new ProgressDialog.Builder(context)
                .setContent(R.string.core_fly_parameter_updating)
                .setTimeout(3*60*1000,dialog->{})
                .create();
        if (!(context instanceof Activity)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                progressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            } else {
                progressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }
        }
        progressDialog.show();

        FlyParameterHelper.getInstance().fetchParameters(new FlyParameterHelper.FlyParameterCallback() {
            @Override
            public void onReceive(Map<String, Object> map) {
                if (map != null){
                    for (String key : map.keySet()) {
                        Object value = map.get(key);
                        try {
                            switch (key){
                                case "Merchants":
                                    //Merchants
                                    if (value instanceof List){
                                        setMerchant((List) value);
                                    }else{
                                        LoggerUtils.e("Merchant value format error.");
                                    }
                                    break;
                                case "Newland_L3_configuration":
                                    //EMV config file
                                    LoggerUtils.d("Found Newland_L3_configuration");
                                    if (value instanceof InputStream){
                                        InputStream inputStream = (InputStream) value;
                                        if (!parseEmv(inputStream)) {
                                            ToastUtils.showToast(context.getString(R.string.core_fly_parameter_parse_emv_failed_format, key));
                                        }
                                        inputStream.close();
                                    }else{
                                        LoggerUtils.e("Newland_L3_configuration value format error.");
                                    }
                                    break;
                                default:
                                    //Settings
                                    if (isValidSettingKey(key)) {
                                        ParamsUtils.setObject(key, value);
                                    }else{
                                        LoggerUtils.e("invalid key: "+key);
                                    }
                                    break;
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                LoggerUtils.d("FlyParameter success");
                ToastUtils.showToast(R.string.core_fly_parameter_update_success);
                ThreadPool.postOnMain(() -> progressDialog.dismiss());
            }

            @Override
            public void onError(int errorCode, String message) {
                LoggerUtils.e("Receive FlyParameter failed.Error code = " + errorCode + ",message = " + message);
                ToastUtils.showToast(message);
                ThreadPool.postOnMain(() -> progressDialog.dismiss());
            }
        });
    }

    private static List<String> settingsKeys;
    private boolean isValidSettingKey(String key){
        if (settingsKeys == null){
            settingsKeys = new ArrayList<>();
            for (Field field : ParamsConst.class.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (field.getType() == String.class
                        && (Modifier.PUBLIC & modifiers) != 0
                        && (Modifier.FINAL & modifiers) != 0
                        && (Modifier.STATIC & modifiers) != 0) {
                    try {
                        String filedValue = (String) field.get(ParamsConst.class);
                        settingsKeys.add(filedValue);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return settingsKeys.contains(key);
    }
    private void setMerchant(List merchantList){
        MerchantService merchantService = new MerchantServiceImpl();
        Map<String,Object> merchants = (Map<String,Object>) merchantList.get(0);

        for (String org : merchants.keySet()) {
            List tmp = (List) merchants.get(org);
            Map<String,Object> map = (Map<String, Object>) tmp.get(0);
            String mid = (String) map.get(AppParamsImporter.PARAM_CONFIG_MID);
            String tid = (String) map.get(AppParamsImporter.PARAM_CONFIG_TID);
            String batchNo = (String) map.get(AppParamsImporter.PARAM_CONFIG_BATCH);
            Merchant merchant = merchantService.find(org);
            boolean exit = merchant != null;
            if (!exit){
                merchant = new Merchant();
            }
            merchant.setMid(mid);
            merchant.setTid(tid);
            merchant.setBatchNo(batchNo);
            if (exit){
                LoggerUtils.d("FlyParameter update merchant:"+merchant);
                merchantService.update(merchant);
            }else{
                LoggerUtils.d("add merchant:"+merchant);
                merchantService.add(merchant);
            }
        }
    }
    /**
     * Parse EMV AID and CAPKS
     */
    private boolean parseEmv(InputStream inputStream) {
        boolean external = ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_EXTERNAL_PINPAD);
        IEmvParamLoader loader;
        if (external) {
            loader = new BExtEmvParamLoader();
            if (!ExtServiceHelper.getInstance().isInit()) {
                return false;
            }
        } else {
            loader = new BEmvParamLoader();
            if (!ServiceHelper.getInstance().isInit()) {
                return false;
            }
        }
        boolean result = EmvConfigXmlParser.parseXml(inputStream, loader);
        if (result) {
            ParamsUtils.setBoolean(ParamsConst.PARAMS_KEY_EMV_AID_CAPK, true);
        }
        return result;
    }
} 
