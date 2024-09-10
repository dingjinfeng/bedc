package acquire.core.trans.pack.xml;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.Xpp3Driver;

import java.io.IOException;
import java.io.InputStream;

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

/**
 * A tool for XML data request.
 * <p><hr><b>e.g.</b></p>
 * <pre>
 *   RequestBean request = new RequestBean();
 *   ...
 *   try {
 *       ResponseBean response = new XmlCaller.Builder(mActivity)
 *           .withPrompts("Connecting...","Receiving...")
 *           .packComm(request,ResponseBean.class);
 *   } catch (CallerException e) {
 *       int callResult = e.getCallerResult();
 *       String message = e.getMessage();
 *   }
 * </pre>
 *
 * @author Janson
 * @date 2020/9/7 17:20
 */
public class XmlCaller extends BaseCaller {
    private BaseActivity activity;
    /**
     * Communication prompts
     */
    private String[] commPrompts;

    private HttpOkHelper httpOkHelper;

    private XmlCaller() {
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
     * Pack and send data.
     *
     * @param requestObject       reqeust object
     * @param responseClass response class type
     * @return response data object
     * @throws CallerException Communication Exception
     */
    public <T>T execute(Object requestObject, Class<T> responseClass) throws CallerException {
        //trace +1
        StatisticsUtils.increaseTraceNo();
        XStream xStream = new XStream(new Xpp3Driver());
        //request root node
        final String root = "root";
        xStream.alias(root, requestObject.getClass());
        //bean to xml
        String request = xStream.toXML(requestObject);
        String response;
        try {
            //send data
            if (commPrompts != null && commPrompts.length > 0) {
                showProgress(activity, commPrompts[0]);
            }
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
            throw new CallerException(CallerResult.FAIL_NET_RECV,BaseApplication.getAppString(R.string.core_comm_recv_fail));
        } finally {
            hideProgress();
        }
        //re-new XStream
        xStream = new XStream(new Xpp3Driver());
        //response  root node
        xStream.alias(root, responseClass);
        try {
            //xml to bean
            return (T) xStream.fromXML(response);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CallerException(CallerResult.FAIL_NET_RECV,BaseApplication.getAppString(R.string.core_comm_unpack_response_format_error));
        }
    }


    /**
     * Send data
     */
    private String send(String request) throws NetReceiveException, NetConnectException, NetHttpCodeException {
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
        //send xml data
        String response = httpOkHelper.httpPostXml(request);
        if (TextUtils.isEmpty(response)) {
            throw new NetReceiveException(BaseApplication.getAppString(R.string.core_comm_recv_fail));
        }
        return response;
    }

    /**
     * A {@link XmlCaller} builder
     *
     * @author Janson
     * @date 2020/6/3 9:37
     */
    public static class Builder {
        private final BaseActivity activity;
        private String[] commPrompts;

        public Builder(BaseActivity activity) {
            this.activity = activity;
            if (activity == null){
                this.commPrompts = new String[]{BaseApplication.getAppString(R.string.core_comm_connecting),
                        BaseApplication.getAppString(R.string.core_comm_recving)};
            }
        }

        /**
         * No ui
         */
        public Builder withoutTips() {
            this.commPrompts = null;
            return this;
        }

        /**
         * Set 1 or 2 progress prompts.
         */
        public Builder withPrompts(String... prompts) {
            this.commPrompts = prompts;
            return this;
        }

        /**
         * Set 1 or 2 progress prompts.
         */
        public Builder withPrompts(@NonNull @StringRes int... resIds) {
            this.commPrompts = new String[resIds.length];
            for (int i = 0; i < resIds.length; i++) {
                this.commPrompts[i] = BaseApplication.getAppString(resIds[i]);
            }
            return this;
        }

        /**
         * Create a {@link XmlCaller}
         */
        public XmlCaller create() {
            XmlCaller caller = new XmlCaller();
            caller.activity = activity;
            caller.commPrompts = this.commPrompts;
            return caller;
        }

        /**
         * Create a {@link XmlCaller} and pack and send data.
         *
         * @param request       request object
         * @param responseClass response class type
         * @return response data object
         */
        public <T> T packComm(Object request, Class<T> responseClass) throws CallerException {
            XmlCaller caller = create();
            return caller.execute(request, responseClass);
        }

    }
} 