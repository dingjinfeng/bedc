package acquire.core.tools;


import android.content.Context;

import java.util.Locale;

import acquire.base.BaseApplication;
import acquire.base.utils.AppUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.iso8583.ISO8583;
import acquire.core.BuildConfig;
import acquire.core.R;
import acquire.core.constant.FileConst;
import acquire.core.constant.ParamsConst;
import acquire.sdk.ExtServiceHelper;
import acquire.sdk.FlyParameterHelper;
import acquire.sdk.ServiceHelper;
import acquire.sdk.device.BDevice;
import acquire.sdk.emv.BEmvParamLoader;
import acquire.sdk.emv.BExtEmvParamLoader;
import acquire.sdk.emv.IEmvParamLoader;

/**
 * Self check. Deal some initial tasks.
 *
 * @author Janson
 * @date 2018/3/26
 */
public class SelfCheckHelper {

    /**
     * init application configuration.
     */
    public static void initAppConfig(Context context) {
        //open log
        LoggerUtils.configPrint(true);
        LoggerUtils.setCustomTagPrefix("bankdemo"+ BuildConfig.TEMPLATE_VERSION);

        LoggerUtils.i("Check application parameters start.");
        //check first run
        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_FIRST_RUN, true)) {
            try {
                LoggerUtils.d("App first run.");
                // init defaultparams.properties
                AppParamsImporter.initDefaultAppParams();
                AppParamsImporter.initDefaultMerchants();
                //set first run false
                ParamsUtils.setBoolean(ParamsConst.PARAMS_KEY_FIRST_RUN, false);
                LoggerUtils.i("App first over.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //load 8583 configuration xml
        LoggerUtils.d("init 8583.xml");
        boolean loadSuccess = ISO8583.getDefault().loadXmlFile(context, FileConst.CUPS8583);
        if (!loadSuccess) {
            LoggerUtils.e("ISO8583 configuration xml file parses failed.");
            ToastUtils.showToast(R.string.core_parse_8583_configration_failed);
            return;
        }
        //init sound
        LoggerUtils.d("init sound resource");
        SoundPlayer.getInstance().init();
        LoggerUtils.i("Check application parameters over.");
    }

    /**
     * init NSDK and EMV.
     */
    public static void initDevice(Context context) {
        LoggerUtils.i("Check device start.");
        LoggerUtils.d("Version name: " + AppUtils.getAppVersionName(context));
        boolean isConnectNsdk;
        //init NSDK
        isConnectNsdk = ServiceHelper.getInstance().init(context);
        if (!isConnectNsdk) {
            ToastUtils.showToast(R.string.core_sdk_init_failed);
            return;
        }
        //check external flag
        boolean external = ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_EXTERNAL_PINPAD);
        if (!external && (!BDevice.isExistSecurityModule() || BDevice.isCpos())) {
            LoggerUtils.d("No Built-in Security Module!");
            ParamsUtils.setBoolean(ParamsConst.PARAMS_KEY_EXTERNAL_PINPAD, true);
            external = true;
        }
        if (external) {
            //external PIN pad
            int connectMode = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_EXTERNAL_PINPAD_CONNECT_MODE);
            isConnectNsdk = ExtServiceHelper.getInstance().init(context,connectMode);
            if (!isConnectNsdk) {
                ToastUtils.showToast(R.string.core_device_ext_pinpad_init_failed);
            }
        }
        if (isConnectNsdk){
            //EMV config
            if (!loadEmvConfig(context, external,false)){
                ToastUtils.showToast(R.string.core_device_load_emv_configurations);
            }
        }

        //init fly parameter service
        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_TOMS_FLY_PARAMETERS)) {
            LoggerUtils.d("register TOMS Fly Parameter service");
            boolean result = FlyParameterHelper.getInstance().bind(context);
            if (result) {
                FlyParameterHelper.getInstance().setParameterWatcher(context, () ->
                    new RemoteParamsUpdater().updateParams(BaseApplication.getAppContext())
                );
            }else{
                ToastUtils.showToast(R.string.core_device_fly_parameter_init_failed);
            }
        }else{
            FlyParameterHelper.getInstance().unbind();
        }

        LoggerUtils.i("Check device over.");
    }


    /**
     * init EMV config
     */
    public static boolean loadEmvConfig(Context context, boolean external,boolean forceLoad) {
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
        boolean loadAidCapk = !ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_EMV_AID_CAPK)
                || forceLoad
                || loader.isCapkLoss()
                || loader.isCtAidLoss()
                || loader.isClessAidLoss();
        if (loadAidCapk) {
            LoggerUtils.d(String.format(Locale.getDefault(),"init %s emv ",external?"external":"built-in"));
            boolean loadSucc = EmvConfigXmlParser.parseXml(context, FileConst.EMV_CONFIG, loader);
            if (loadSucc) {
                ParamsUtils.setBoolean(ParamsConst.PARAMS_KEY_EMV_AID_CAPK, true);
            }
            return loadSucc;
        }
        return true;
    }
}
