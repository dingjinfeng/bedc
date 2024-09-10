package acquire.core.tools;

import android.content.res.AssetManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import acquire.base.BaseApplication;
import acquire.core.constant.FileConst;

/**
 * Currency code utils
 *
 * @author Janson
 * @date 2021/8/4 16:01
 */
public class CurrencyCodeProvider {
    /**
     * Currency code map
     */
    private final static Map<String, String> CURRENCY_CODES = new HashMap<>();

    /**
     * Init
     */
    private static void init() {
        //load response code file
        AssetManager am = BaseApplication.getAppContext().getAssets();
        try (InputStreamReader inputStream = new InputStreamReader(am.open(FileConst.CURRENCYCODE), StandardCharsets.UTF_8)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            Set<String> names = properties.stringPropertyNames();
            for (String name : names) {
                String symbol = properties.getProperty(name);
                String[] currency = name.split("_");
                if (currency.length > 1 && !TextUtils.isEmpty(currency[0])) {
                    CURRENCY_CODES.put(currency[0], symbol);
                }
                if (currency.length > 2 && !TextUtils.isEmpty(currency[1])) {
                    CURRENCY_CODES.put(currency[1], symbol);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Get currency symbole by currency code.
     *
     * @param currencyCode currency numeric code.
     * @return currency symbole
     */
    @NonNull
    public static String getCurrencySymbol(String currencyCode) {
        if (CURRENCY_CODES.isEmpty()) {
            init();
        }
        String symbol = CURRENCY_CODES.get(currencyCode);
        if (symbol == null) {
            symbol = "";
        }
        return symbol;
    }
}
