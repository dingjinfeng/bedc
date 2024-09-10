package acquire.core.tools;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

import acquire.base.utils.LoggerUtils;
import acquire.core.BindTag;
import acquire.core.bean.PubBean;
import acquire.core.constant.TransStatus;
import acquire.core.constant.TransTag;
import acquire.database.model.Record;
import acquire.database.model.ReversalData;

/**
 * Data Converter Utils
 *
 * @author Janson
 * @date 2020/2/18 10:33
 */
public class DataConverter {

    /**
     * convert record to pubBean
     *
     * @param record  transaction records
     * @param pubBean parameters required for transaction execution
     */
    public static void recordToPubBean(@NonNull Record record, @NonNull PubBean pubBean) {
        try {
            pubBean.setMid(record.getMid());
            pubBean.setTid(record.getTid());
            pubBean.setProcessCode(record.getProcessCode());
            pubBean.setCardNo(record.getCardNo());
            pubBean.setAmount(record.getAmount());
            pubBean.setTipAmount(record.getTipAmount());
            pubBean.setBatchNo(record.getBatchNo());
            pubBean.setBillAmount(record.getBillAmount());
            pubBean.setTraceNo(record.getTraceNo());
            pubBean.setTime(record.getTime());
            pubBean.setDate(record.getDate());
            pubBean.setExpDate(record.getExpDate());
            pubBean.setField22(record.getField22());
            pubBean.setEntryMode(record.getEntryMode());
            pubBean.setCardSn(record.getCardSerialNo());
            if (null != record.getTrack2()) {
                pubBean.setTrack2(record.getTrack2());
            }
            if (null != record.getTrack3()) {
                pubBean.setTrack3(record.getTrack3());
            }
            pubBean.setReferNo(record.getReferNo());
            pubBean.setAuthCode(record.getAuthCode());
            pubBean.setOrigAuthCode(record.getOrigAuthCode());
            pubBean.setOrigReferNo(record.getOrigReferNo());
            pubBean.setCardOrg(record.getCardOrg());
            pubBean.setField55(record.getField55());
            pubBean.setOrigDate(record.getOrigDate());
            pubBean.setCurrencyCode(record.getCurrencyCode());
            pubBean.setBizOrderNo(record.getBizOrderNo());
        } catch (Exception e) {
            LoggerUtils.e("Record to PubBean error！", e);
        }
    }

    /**
     * convert record to intent
     *
     * @param record transaction records
     * @param bundle out bundle
     */
    public static void recordToBundle(@NonNull Record record, @NonNull Bundle bundle) {
        bundle.putString(TransTag.TRANS_TYPE, record.getTransType());
        bundle.putString(TransTag.RESULT_CODE, record.getResponseCode());
        bundle.putString(TransTag.MID, record.getMid());
        bundle.putString(TransTag.TID, record.getTid());
        bundle.putString(TransTag.CARD_NO, record.getCardNo());
        bundle.putString(TransTag.ORGANIZATION, record.getCardOrg());
        bundle.putInt(TransTag.ENTRY_MODE, record.getEntryMode());
        bundle.putLong(TransTag.AMOUNT, record.getAmount());
        bundle.putLong(TransTag.TIP, record.getTipAmount());
        bundle.putLong(TransTag.BILL_AMOUNT, record.getBillAmount());
        bundle.putString(TransTag.BATCH_NO, record.getBatchNo());
        bundle.putString(TransTag.TRACE_NO, record.getTraceNo());
        bundle.putString(TransTag.TIME, record.getTime());
        bundle.putString(TransTag.DATE, record.getDate());
        bundle.putString(TransTag.REFERENCE_NO, record.getReferNo());
        bundle.putString(TransTag.AUTH_CODE, record.getAuthCode());
        bundle.putString(TransTag.ORIG_AUTH_CODE, record.getOrigAuthCode());
        bundle.putString(TransTag.ORIG_REFERENCE_NO, record.getOrigReferNo());
        bundle.putString(TransTag.ORIG_TRACE_NO, record.getOrigTraceNo());
        bundle.putString(TransTag.ORIG_DATE, record.getOrigDate());
        bundle.putString(TransTag.CURRENCY_CODE, record.getCurrencyCode());
        bundle.putString(TransTag.BIZ_ORDER_NO, record.getBizOrderNo());
    }

    /**
     * convert pubBean to record
     *
     * @param pubBean parameters required for transaction execution
     * @param record  transaction records
     */
    public static void pubBeanToRecord(@NonNull PubBean pubBean, @NonNull Record record) {
        record.setMid(pubBean.getMid());
        record.setTid(pubBean.getTid());
        record.setProcessCode(pubBean.getProcessCode());
        record.setTransType(pubBean.getTransType());
        record.setStatus(TransStatus.SUCCESS);
        record.setCardNo(pubBean.getCardNo());
        record.setAmount(pubBean.getAmount());
        record.setTipAmount(pubBean.getTipAmount());
        record.setBillAmount(pubBean.getBillAmount());
        record.setTraceNo(pubBean.getTraceNo());
        record.setBatchNo(pubBean.getBatchNo());
        record.setDate(pubBean.getDate());
        record.setTime(pubBean.getTime());
        record.setExpDate(pubBean.getExpDate());
        record.setField22(pubBean.getField22());
        record.setEntryMode(pubBean.getEntryMode());
        record.setCardSerialNo(pubBean.getCardSn());
        if (null != pubBean.getTrack2()) {
            record.setTrack2(pubBean.getTrack2());
        }
        if (null != pubBean.getTrack3()) {
            record.setTrack3(pubBean.getTrack3());
        }
        record.setReferNo(pubBean.getReferNo());
        record.setAuthCode(pubBean.getAuthCode());
        record.setResponseCode(pubBean.getResultCode());
        record.setOrigTraceNo(pubBean.getOrigTraceNo());
        //yyyyMMdd
        record.setOrigDate(pubBean.getOrigDate());
        record.setOrigAuthCode(pubBean.getOrigAuthCode());
        record.setOrigReferNo(pubBean.getOrigReferNo());
        record.setCardOrg(pubBean.getCardOrg());
        record.setField55(pubBean.getField55());
        record.setEmvPrintData(pubBean.getEmvPrintData());
        record.setCurrencyCode(pubBean.getCurrencyCode());
        if (pubBean.getOutOrderNo() != null) {
            record.setOutOrderNo(pubBean.getOutOrderNo());
        }
        record.setCardSn(pubBean.getCardSn());
        record.setQrPayCode(pubBean.getQrPayCode());
        record.setBizOrderNo(pubBean.getBizOrderNo());
        record.setRemarks(pubBean.getRemarks());
        record.setFreePin(TextUtils.isEmpty(pubBean.getPinBlock()) && TextUtils.isEmpty(pubBean.getOfflinePinBlock()));
        record.setFreeSign(pubBean.isFreeSign());
        record.setSignPath(pubBean.getSignPath());
    }

    /**
     * convert reversal record to pubBean
     *
     * @param reversalData reversal data
     * @param pubBean      parameters required for transaction execution
     */
    public static void reversalToPubBean(@NonNull ReversalData reversalData, @NonNull PubBean pubBean) {
        pubBean.setMid(reversalData.getMid());
        pubBean.setTid(reversalData.getTid());
        pubBean.setCardNo(reversalData.getCardNo());
        pubBean.setProcessCode(reversalData.getProcessCode());
        pubBean.setAmount(reversalData.getAmount());
        pubBean.setTraceNo(reversalData.getTraceNo());
        pubBean.setExpDate(reversalData.getExpDate());
        pubBean.setField22(reversalData.getField22());
        pubBean.setEntryMode(reversalData.getEntryMode());
        pubBean.setCardSn(reversalData.getCardSerialNo());
        pubBean.setServerCode(reversalData.getServerCode());
        pubBean.setOrigAuthCode(reversalData.getOrigAuthCode());
        pubBean.setCurrencyCode(reversalData.getCurrencyCode());
        pubBean.setField55(reversalData.getField55());
        pubBean.setNii(reversalData.getNii());
    }

    /**
     * convert pubBean to reversal record
     *
     * @param pubBean      parameters required for transaction execution
     * @param reversalData reversal data
     */
    public static void pubBeanToReversal(@NonNull PubBean pubBean, @NonNull ReversalData reversalData) {
        reversalData.setMid(pubBean.getMid());
        reversalData.setTid(pubBean.getTid());
        reversalData.setTransType(pubBean.getTransType());
        reversalData.setCardNo(pubBean.getCardNo());
        reversalData.setProcessCode(pubBean.getProcessCode());
        reversalData.setAmount(pubBean.getAmount());
        reversalData.setTraceNo(pubBean.getTraceNo());
        reversalData.setExpDate(pubBean.getExpDate());
        reversalData.setEntryMode(pubBean.getEntryMode());
        reversalData.setField22(pubBean.getField22());
        reversalData.setCardSerialNo(pubBean.getCardSn());
        reversalData.setServerCode(pubBean.getServerCode());
        reversalData.setOrigAuthCode(pubBean.getOrigAuthCode());
        reversalData.setCurrencyCode(pubBean.getCurrencyCode());
        reversalData.setNii(pubBean.getNii());
    }

    /**
     * convert intent to pubBean
     *
     * @param intent  activity intent
     * @param pubBean parameters required for transaction execution
     */
    public static void intentToPubBean(Intent intent, PubBean pubBean) {
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }
        for (Field field : pubBean.getClass().getDeclaredFields()) {
            BindTag bindTag = field.getAnnotation(BindTag.class);
            if (bindTag != null) {
                int modifiers = field.getModifiers();
                if ((Modifier.FINAL & modifiers) != 0) {
                    continue;
                }
                String tag = bindTag.value();
                if (!extras.containsKey(tag)) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    field.set(pubBean, extras.get(tag));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * convert pubBean to intent
     *
     * @param pubBean parameters required for transaction execution
     * @param intent  activity intent
     */
    public static void pubBeanToIntent(PubBean pubBean, Intent intent) {
        for (Field field : pubBean.getClass().getDeclaredFields()) {
            BindTag bindTag = field.getAnnotation(BindTag.class);
            if (bindTag != null) {
                int modifiers = field.getModifiers();
                if ((Modifier.FINAL & modifiers) != 0) {
                    continue;
                }
                String tag = bindTag.value();
                try {
                    field.setAccessible(true);
                    Object value = field.get(pubBean);
                    if (value != null) {
                        intent.putExtra(tag, (Serializable) value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * convert uri to pubBean
     *
     * @param uri     activity uri
     * @param pubBean parameters required for transaction execution
     */
    public static void uriToPubBean(Uri uri, PubBean pubBean) {
        for (Field field : pubBean.getClass().getDeclaredFields()) {
            BindTag bindTag = field.getAnnotation(BindTag.class);
            if (bindTag != null) {
                int modifiers = field.getModifiers();
                if ((Modifier.FINAL & modifiers) != 0) {
                    continue;
                }
                String tag = bindTag.value();
                String uriValue = uri.getQueryParameter(tag);
                if (TextUtils.isEmpty(uriValue)) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    Class<?> fieldType = field.getType();
                    if (fieldType == String.class) {
                        field.set(pubBean, uriValue);
                    } else if (fieldType == int.class || fieldType == Integer.class) {
                        field.setInt(pubBean, Integer.parseInt(uriValue));
                    } else if (fieldType == long.class || fieldType == Long.class) {
                        field.setLong(pubBean, Long.parseLong(uriValue));
                    } else if (fieldType == float.class || fieldType == Float.class) {
                        field.setFloat(pubBean, Float.parseFloat(uriValue));
                    } else if (fieldType == double.class || fieldType == Double.class) {
                        field.setDouble(pubBean, Double.parseDouble(uriValue));
                    } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                        field.setBoolean(pubBean, Boolean.parseBoolean(uriValue));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * convert uri to intent
     *
     * @param uri      activity uri
     * @param intent   output result intent.
     * @param template parameters type template class with {@link BindTag}
     */
    public static void uriToIntent(Uri uri, Intent intent, Class<?> template) {
        for (Field field : template.getDeclaredFields()) {
            BindTag bindTag = field.getAnnotation(BindTag.class);
            if (bindTag != null) {
                int modifiers = field.getModifiers();
                if ((Modifier.FINAL & modifiers) != 0) {
                    continue;
                }
                String tag = bindTag.value();
                String uriValue = uri.getQueryParameter(tag);
                if (TextUtils.isEmpty(uriValue)) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    Class<?> fieldType = field.getType();
                    if (fieldType == String.class) {
                        intent.putExtra(tag, uriValue);
                    } else if (fieldType == int.class || fieldType == Integer.class) {
                        intent.putExtra(tag, Integer.parseInt(uriValue));
                    } else if (fieldType == long.class || fieldType == Long.class) {
                        intent.putExtra(tag, Long.parseLong(uriValue));
                    } else if (fieldType == float.class || fieldType == Float.class) {
                        intent.putExtra(tag, Float.parseFloat(uriValue));
                    } else if (fieldType == double.class || fieldType == Double.class) {
                        intent.putExtra(tag, Double.parseDouble(uriValue));
                    } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                        intent.putExtra(tag, Boolean.parseBoolean(uriValue));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * convert JSON to intent
     *
     * @param json     json object
     * @param intent   output result intent.
     * @param template parameters type template class with {@link BindTag}
     */
    public static void jsonToIntent(JSONObject json, Intent intent, Class<?> template) {
        for (Field field : template.getDeclaredFields()) {
            BindTag bindTag = field.getAnnotation(BindTag.class);
            if (bindTag != null) {
                int modifiers = field.getModifiers();
                if ((Modifier.FINAL & modifiers) != 0) {
                    continue;
                }
                String tag = bindTag.value();
                if (!json.has(tag)) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    Class<?> fieldType = field.getType();
                    if (fieldType == String.class) {
                        intent.putExtra(tag, json.getString(tag));
                    } else if (fieldType == int.class || fieldType == Integer.class) {
                        intent.putExtra(tag, json.getInt(tag));
                    } else if (fieldType == long.class || fieldType == Long.class) {
                        intent.putExtra(tag, json.getLong(tag));
                    } else if (fieldType == float.class || fieldType == Float.class || fieldType == double.class || fieldType == Double.class) {
                        intent.putExtra(tag, json.getDouble(tag));
                    } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                        intent.putExtra(tag, json.getBoolean(tag));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * convert intent to JSON
     *
     * @param intent input result intent.
     * @param json   json object
     */
    public static void intentToJson(Intent intent, JSONObject json) {
        Set<String> tagSet = intent.getExtras().keySet();
        for (String tag : tagSet) {
            Object value = intent.getSerializableExtra(tag);
            try {
                json.put(tag, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}

