package acquire.app.fragment.main.menu;


import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import acquire.app.R;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.TransType;
import acquire.core.tools.TransUtils;
import acquire.sdk.device.BDevice;

/**
 * Main Menu Configuration
 *
 * @author Janson
 * @date 2020/6/8 10:19
 */
public class MainMenu {
    /**
     * menu items
     */
    private List<MenuItem> menu = new ArrayList<>();
    /**
     * {@link ParamsConst} that indicates whether to support this item.
     */
    private final Map<String, Boolean> mLastParamsMap = new ArrayMap<>();
    private boolean lastSupportPrinter;
    private static volatile MainMenu instance;

    private MainMenu() {
    }

    public static MainMenu getInstance() {
        if (instance == null) {
            synchronized (MainMenu.class) {
                if (instance == null) {
                    instance = new MainMenu();
                }
            }
        }
        return instance;
    }

    /**
     * get items
     */
    private List<MenuItem> getItems() {
        List<MenuItem> items = new ArrayList<>();
        items.add(new MenuItem(TransType.TRANS_SALE, R.drawable.app_menu_sale, R.color.app_menu_light_pink_background));
        items.add(new MenuItem(TransType.TRANS_PRE_AUTH, R.drawable.app_menu_pre_auth, R.color.app_menu_light_blue_background));
        items.add(new MenuItem(TransType.TRANS_AUTH_COMPLETE, R.drawable.app_menu_auth_complete, R.color.app_menu_light_pink_background));
        items.add(new MenuItem(TransType.TRANS_VOID_SALE, R.drawable.app_menu_void_sale, R.color.app_menu_light_green_background));
        items.add(new MenuItem(TransType.TRANS_REFUND, R.drawable.app_menu_refund, R.color.app_menu_light_blue_background));
        items.add(new MenuItem(TransType.TRANS_VOID_PRE_AUTH, R.drawable.app_menu_void_pre_auth, R.color.app_menu_light_purple_background));
        items.add(new MenuItem(TransType.TRANS_VOID_AUTH_COMPLETE, R.drawable.app_menu_void_auth_complete, R.color.app_menu_light_pink_background));

        if (BDevice.supportPrint() || ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL)) {
            List<MenuItem> folder = new ArrayList<>();
            folder.add(new MenuItem(TransType.TRANS_REPRINT_LAST_RECEIPT, R.drawable.app_menu_reprint_last_receipt, R.color.app_menu_light_pink_background));
            folder.add(new MenuItem(TransType.TRANS_REPRINT_RECEIPT, R.drawable.app_menu_reprint_receipt, R.color.app_menu_light_green_background));
            folder.add(new MenuItem(TransType.TRANS_REPRINT_SETTLE, R.drawable.app_menu_reprint_settle, R.color.app_menu_light_purple_background));
            folder.add(new MenuItem(TransType.TRANS_PRINT_DETAIL, R.drawable.app_menu_print_detail, R.color.app_menu_light_pink_background));
            items.add(new MenuItem(folder, R.string.app_menu_print_folder_title, R.drawable.app_menu_print_folder, R.color.app_menu_light_purple_background));
        }else{
            items.add(new MenuItem(TransType.TRANS_REPRINT_RECEIPT, R.drawable.app_menu_reprint_receipt, R.color.app_menu_light_green_background));
        }
        items.add(new MenuItem(TransType.TRANS_SETTLE, R.drawable.app_menu_settle, R.color.app_menu_light_green_background));
        items.add(new MenuItem(TransType.TRANS_BALANCE, R.drawable.app_menu_balance, R.color.app_menu_light_blue_background));
        items.add(new MenuItem(TransType.TRANS_SETTINGS, R.drawable.app_menu_settings, R.color.app_menu_light_purple_background));
        items.add(new MenuItem(TransType.TRANS_INSTALLMENT, R.drawable.app_menu_installment, R.color.app_menu_light_green_background));
        items.add(new MenuItem(TransType.TRANS_VOID_INSTALLMENT, R.drawable.app_menu_void_installment, R.color.app_menu_light_blue_background));
        items.add(new MenuItem(TransType.TRANS_SCAN_PAY, R.drawable.app_menu_scan_pay, R.color.app_menu_light_blue_background));
        items.add(new MenuItem(TransType.TRANS_QR_CODE, R.drawable.app_menu_qr_code, R.color.app_menu_light_blue_background));
        items.add(new MenuItem(TransType.TRANS_QR_REFUND, R.drawable.app_menu_qr_refund, R.color.app_menu_light_pink_background));
        items.add(new MenuItem(TransType.TRANS_ABOUT, R.drawable.app_menu_about, R.color.app_menu_light_purple_background));
        items.add(new MenuItem(TransType.TRANS_LOGIN, R.drawable.app_menu_login, R.color.app_menu_light_blue_background));
        return items;
    }
    /**
     * get the main menu
     */
    public List<MenuItem> getMenu() {
        if (menu.size() == 0 || isChanged()) {
            menu = getItems();
            checkSupport(menu);
            LoggerUtils.d("init main menu");
            for (MenuItem item : menu) {
                LoggerUtils.d(item.toString());
            }
        }
        return menu;
    }

    /**
     * If the Transaction support status was changed, return true.
     */
    public boolean isChanged() {
        boolean supportPrinter = BDevice.supportPrint() || ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL);
        if (lastSupportPrinter != supportPrinter ){
            return true;
        }
        for (Map.Entry<String, Boolean> entry : mLastParamsMap.entrySet()) {
            String key = entry.getKey();
            Boolean lastParamValue = mLastParamsMap.get(key);
            if (lastParamValue == null || lastParamValue != ParamsUtils.getBoolean(key, true)) {
                //Trans support status was changed
                return true;
            }
        }
        return false;
    }

    /**
     * Check item support
     */
    private void checkSupport(List<MenuItem> items) {
        if (items == null || items.size() == 0) {
            return;
        }
        Iterator<MenuItem> iterator = items.iterator();
        while (iterator.hasNext()) {
            MenuItem item = iterator.next();
            List<MenuItem> subItems = item.getSubItems();
            if (subItems != null) {
                checkSupport(subItems);
                if (subItems.isEmpty()) {
                    iterator.remove();
                }
                continue;
            }
            //Check whether to support this item.
            //Save item ParamsConst key.
            String paramsKey = TransUtils.getParamsKey(item.getTransType());
            boolean support = ParamsUtils.getBoolean(paramsKey, true);
            mLastParamsMap.put(paramsKey, support);
            if (!support) {
                iterator.remove();
            }
        }
        lastSupportPrinter = BDevice.supportPrint() || ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL);
    }

}
