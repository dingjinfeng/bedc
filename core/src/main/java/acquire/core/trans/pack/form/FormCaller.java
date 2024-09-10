package acquire.core.trans.pack.form;

import android.text.TextUtils;
import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import acquire.base.BaseApplication;
import acquire.base.activity.BaseActivity;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.network.HttpOkHelper;
import acquire.base.utils.network.exception.NetConnectException;
import acquire.base.utils.network.exception.NetHttpCodeException;
import acquire.base.utils.network.exception.NetReceiveException;
import acquire.core.R;
import acquire.core.constant.CallerResult;
import acquire.core.constant.FileConst;
import acquire.core.constant.ParamsConst;
import acquire.core.tools.StatisticsUtils;
import acquire.core.trans.pack.BaseCaller;
import acquire.core.trans.pack.CallerException;
import okhttp3.ResponseBody;

/**
 * A tool for FORM data request.
 * <p><hr><b>Form Data Format Example</b></p>
 * <pre>
 *    a=2&b=3&c=4
 * </pre>
 * <p><hr><b>e.g.</b>
 * <pre>
 *   Map<String, String> request = new ArrayMap<>();
 *   ...
 *   try {
 *       Map<String, String> response = new FormCaller.Builder(mActivity)
 *           .withPrompts("Connecting...","Receiving...")
 *           .packComm(request);
 *   } catch (CallerException e) {
 *       int callResult = e.getCallerResult();
 *       String message = e.getMessage();
 *   }
 * </pre>
 *
 * @author Janson
 * @date 2020/12/23 9:42
 */
public class FormCaller extends BaseCaller {
    private BaseActivity activity;
    /**
     * Communication prompts
     */
    private String[] commPrompts;

    private HttpOkHelper httpOkHelper;

    private FormCaller() {
    }

    /**
     * Stop communication
     */
    public void abort() {
        if (httpOkHelper != null) {
            httpOkHelper.abort();
        }
    }

    /**
     * Pack and send data. Data format：a=2&b=3&c=4
     *
     * @param request request form data
     * @return response data map
     * @throws CallerException Communication Exception
     */
    public Map<String, String> execute(Map<String, String> request) throws CallerException {
        //trace num +1
        StatisticsUtils.increaseTraceNo();
        String response;
        try {
            if (commPrompts != null && commPrompts.length > 0) {
                showProgress(activity, commPrompts[0]);
            }
            //send data
            response = send(request);
        } catch (NetConnectException | NetHttpCodeException e) {
            //net error
            e.printStackTrace();
            if (e instanceof NetHttpCodeException) {
                throw new CallerException(CallerResult.FAIL_NET_CONNECT,e.getMessage());
            } else {
                throw new CallerException(CallerResult.FAIL_NET_CONNECT,BaseApplication.getAppString(R.string.core_comm_connect_fail));
            }
        } catch (NetReceiveException e) {
            //receive failed
            e.printStackTrace();
            throw new CallerException(CallerResult.FAIL_NET_RECV,BaseApplication.getAppString(R.string.core_comm_recv_fail));
        } finally {
            hideProgress();
        }
        //parse response to map
        Map<String, String> responseMap = new ArrayMap<>();
        String[] items = response.split("&");
        for (String item : items) {
            String[] kv = item.split("=");
            if (kv.length != 2) {
                responseMap.put(kv[0], kv[1]);
            }
        }
        return responseMap;
    }


    /**
     * Send data
     */
    private String send(Map<String, String> request) throws NetReceiveException, NetConnectException, NetHttpCodeException {
        String ip = ParamsUtils.getString(ParamsConst.PARAMS_KEY_COMM_SERVER_ADDRESS);
        int port = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_COMM_PORT, 8080);
        int timeout = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_COMM_TIMEOUT, 30);
        String url = "https://" + ip + ":" + port;
        //create a ok http
        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_COMM_USE_SSL, false)) {
            //ssl certificate
            try (InputStream cerIs = BaseApplication.getAppContext().getAssets().open(FileConst.PEM_CERT)){
                httpOkHelper = new HttpOkHelper(url, timeout, cerIs);
            } catch (IOException e) {
                throw new NetConnectException(BaseApplication.getAppString(R.string.core_comm_ssl_cert_not_found));
            }
        }else{
            httpOkHelper = new HttpOkHelper(url, timeout);
        }
        //UI
        if (commPrompts != null && commPrompts.length > 1) {
            //send success,so update progress
            httpOkHelper.setProcessListener(() -> showProgress(activity, commPrompts[1]));
        }
        //set cancel button
        setProgressCancelListener(activity, () -> httpOkHelper.abort());
        //send form data
        ResponseBody responseBody = httpOkHelper.httpPostForm(request);
        if (responseBody == null) {
            throw new NetReceiveException(BaseApplication.getAppString(R.string.core_comm_recv_fail));
        }
        String responseString = responseBody.toString();
        if (TextUtils.isEmpty(responseString)) {
            throw new NetReceiveException(BaseApplication.getAppString(R.string.core_comm_recv_fail));
        }
        return responseString;
    }

    /**
     * A {@link FormCaller} builder
     *
     * @author Janson
     * @date 2020/12/23 10:31
     */
    public static class Builder {
        private final BaseActivity activity;
        private String[] commPrompts;

        public Builder(BaseActivity activity) {
            this.activity = activity;
            if (activity != null){
                this.commPrompts = new String[]{BaseApplication.getAppString(R.string.core_comm_connecting),
                        BaseApplication.getAppString(R.string.core_comm_recving)};
            }
        }

        /**
         * No ui
         */
        public Builder withoutPrompts() {
            this.commPrompts = null;
            return this;
        }

        /**
         * Set 1 or 2 progress prompts
         */
        public Builder withPrompts(String... prompts) {
            this.commPrompts = prompts;
            return this;
        }

        /**
         * Set 1 or 2 progress prompts
         */
        public Builder withPrompts(@NonNull @StringRes int... resIds) {
            this.commPrompts = new String[resIds.length];
            for (int i = 0; i < resIds.length; i++) {
                this.commPrompts[i] = BaseApplication.getAppString(resIds[i]);
            }
            return this;
        }

        /**
         * Create a {@link FormCaller}
         */
        public FormCaller create() {
            FormCaller caller = new FormCaller();
            caller.activity = activity;
            caller.commPrompts = this.commPrompts;
            return caller;
        }

        /**
         * Create a {@link FormCaller} and pack and send data.
         * <p><hr><b>Form Data Format Example</b>
         * <pre>
         *     a=2&b=3&c=4
         * </pre>.
         *
         * @param request form request data
         * @return response
         */
        public Map<String, String> packComm(Map<String, String> request) throws CallerException {
            FormCaller caller = create();
            return caller.execute(request);
        }
    }
} 