package acquire.core.tools;

import android.util.ArrayMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acquire.base.BaseApplication;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.core.constant.FileConst;
import acquire.core.constant.FileDir;
import acquire.database.model.Merchant;
import acquire.database.service.MerchantService;
import acquire.database.service.impl.MerchantServiceImpl;

/**
 * Import default params
 *
 * @author Janson
 * @date 2021/3/15 9:25
 */
public class AppParamsImporter {
    private final static String SP_GROUP = "PARAMS";

    /**
     * load defaultparams.ini from assets.
     */
    public static void initDefaultAppParams() {
        InputStream inputStream = null;
        try {
            //The file downloaded from the PC downloader will be placed in FileDir.SHARE_PATH
            File importFile = new File(FileDir.SHARE_PATH + FileConst.PARAMS);
            if (importFile.exists()) {
                LoggerUtils.e("There is an external defaultparams.properties !");
                inputStream = new FileInputStream(importFile);
                if (inputStream.available() < 0) {
                    LoggerUtils.e("External defaultparams.properties is empty!!!");
                    inputStream = null;
                }
            }
            if (inputStream == null) {
                inputStream = BaseApplication.getAppContext().getAssets().open(FileConst.PARAMS);
            }
            //import file data
            importAppParams(inputStream);
            if (importFile.exists()) {
                importFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * import params
     *
     * @param inputStream params data stream.  File format reference core>src>main>asset>defaultparams.ini
     */
    public static void importAppParams(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        Map<String, String> map = new ArrayMap<>();
        String line;
        String group = "";
        while ((line = bufferedReader.readLine()) != null) {
            line = line.trim();
            if (line.matches("\\[.*]")) {
                //e.g. [BASE] , group = BASE
                group = line.replaceFirst("\\[(.*)]", "$1");
            } else if (line.matches(".*=.*")) {
                int i = line.indexOf('=');
                //e.g. BASE_TRACE_NO=000001 , key = BASE_TRACE_NO, value = 000001
                String key = line.substring(0, i).trim();
                String value = line.substring(i + 1).trim();
                if (SP_GROUP.equals(group)) {
                    map.put(key, value);
                    LoggerUtils.d("Import params->key:" + key + ", value:" + value);
                }
            }
        }
        ParamsUtils.save(map);
        bufferedReader.close();
    }


    /**
     * load defaultmerchants.ini from assets.
     */
    public static void initDefaultMerchants() {
        InputStream inputStream = null;
        try {
            //The file downloaded from the PC downloader will be placed in FileDir.SHARE_PATH
            File importFile = new File(FileDir.SHARE_PATH + FileConst.MERCHANTS);
            if (importFile.exists()) {
                LoggerUtils.e("There is an external defaultmerchants.properties !");
                inputStream = new FileInputStream(importFile);
                if (inputStream.available() < 0) {
                    LoggerUtils.e("External defaultmerchants.properties is empty!!!");
                    inputStream = null;
                }
            }
            if (inputStream == null) {
                inputStream = BaseApplication.getAppContext().getAssets().open(FileConst.MERCHANTS);
            }
            //import merchant file data
            importMerchants(inputStream);
            if (importFile.exists()) {
                importFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public final static String PARAM_CONFIG_MID = "MERCHANT_ID",PARAM_CONFIG_TID = "TERMINAL_ID",PARAM_CONFIG_BATCH = "BATCH_NO";
    /**
     * import merchant
     *
     * @param inputStream merchant data stream. File format reference core>src>main>asset>defaultmerchants.ini
     */
    public static void importMerchants(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        List<Merchant> merchants = new ArrayList<>();
        Merchant merchant = null;
        while ((line = bufferedReader.readLine()) != null) {
            line = line.trim();
            if (line.matches("\\[.*]")) {
                //e.g. [DEFAULT] , group = DEFAULT
                String group = line.replaceFirst("\\[(.*)]", "$1");
                merchant = new Merchant();
                merchant.setCardOrg(group);
                merchants.add(merchant);
            } else if (line.matches(".*=.*")) {
                if (merchant == null) {
                    continue;
                }
                int i = line.indexOf('=');
                //e.g. MERCHANT_ID=123456789012345 , key = MERCHANT_ID, value = 123456789012345
                String key = line.substring(0, i).trim();
                String value = line.substring(i + 1).trim();
                switch (key) {
                    case PARAM_CONFIG_MID:
                        merchant.setMid(value);
                        break;
                    case PARAM_CONFIG_TID:
                        merchant.setTid(value);
                        break;
                    case PARAM_CONFIG_BATCH:
                        merchant.setBatchNo(value);
                        break;
                    default:
                        break;
                }
            }
        }
        bufferedReader.close();
        if (!merchants.isEmpty()) {
            MerchantService merchantService = new MerchantServiceImpl();
            merchantService.deleteAll();
            for (Merchant mch : merchants) {
                LoggerUtils.d("Import merchant->" + mch.toString());
                merchantService.add(mch);
            }
        }
    }

}
