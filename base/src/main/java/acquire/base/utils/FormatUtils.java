package acquire.base.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Format data utils
 *
 * @author tao
 */
public class FormatUtils {


    /**
     * format amount as a string with two decimal and ",".
     * <p>e.g. 123,333.02</p>
     */
    public static String formatAmount(long amount) {
        return formatAmount(amount, 2);
    }

    /**
     * format amount as a string with decimal and ",".
     * @param format decimal number
     */
    public static String formatAmount(long amount, int format) {
        return formatAmount(amount, format, ",");
    }

    /**
     * format amount as a string with decimal and segmenter.
     * <p>e.g. 123,333.02</p>
     *
     * @param format    decimal number
     * @param segmenter segmenter flag per 3
     */
    public static String formatAmount(long amount, int format, String segmenter) {
        String strInteger = "0";
        String strDecimal;
        StringBuilder tmp = new StringBuilder();
        String flag = "";
        if (amount < 0) {
            flag = "-";
            amount = -amount;
        }
        String strValue = amount + "";
        if (strValue.length() > format) {
            strInteger = Long.valueOf(strValue.substring(0, strValue.length() - format)).toString();
            if (strInteger.length() > 3) {
                int end = strInteger.length() % 3;
                int len = strInteger.length();
                for (int start = 0; end <= len; start = end, end += 3) {
                    tmp.append(strInteger.subSequence(start, end));
                    if (end < len && start != end) {
                        tmp.append(segmenter);
                    }
                }
                strInteger = tmp.toString();
            }
            strDecimal = strValue.substring(strValue.length() - format);
        } else {
            strDecimal = String.format("%0" + format + "d", amount);
        }
        if (strDecimal.length() == 0) {
            return flag + strInteger;
        } else {
            return flag + strInteger + "." + strDecimal;
        }
    }

    /**
     * format card as a string with " "
     * <p>e.g. "1234567890123456789"=>"1234 5678 9012 3456"</p>
     */
    public static String formatCardNoWithSpace(String card) {
        return formatCardNo(card, " ");
    }

    /**
     * format card as a string with separator
     * <p>e.g. separator = '-', "1234567890123456789"=>"1234-5678-9012-3456"</p>
     */
    public static String formatCardNo(String card, String separator) {
        if (card == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        char[] cardChs = card.toCharArray();
        for (int i = 0; i < cardChs.length; i++) {
            if (i != 0 && i % 4 == 0) {
                builder.append(separator);
            }
            builder.append(cardChs[i]);
        }
        return builder.toString();
    }

    /**
     * mask card no with '*'.
     * <p>e.g. "6666666666666666"=>"************6666"</p>
     */
    public static String maskCardNo(String card) {
        if (card == null) {
            return "";
        }
        int len = card.length();
        if (len <= 4) {
            return card;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len - 4; i++) {
            builder.append("*");
        }
        builder.append(card.substring(len - 4));
        return builder.toString();
    }


    /**
     * format phone number
     * <p>e.g. "1234567890"=>"123 456 7890"</p>
     */
    public static String formatPhoneNumber(String phone) {
        if (phone == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        char[] phoneChs = phone.toCharArray();
        for (int i = 0; i < phoneChs.length; i++) {
            if (i == 3 || i == 6) {
                builder.append(' ');
            }
            builder.append(phoneChs[i]);
        }
        return builder.toString();
    }

    /**
     * format data&time
     * <pre>
     *     String resultDateTime = FormatUtils.formatDateTiem("20210327120320");
     *     //==> resultDateTime: 2021/03/27 12:03：20
     * </pre>
     *
     * @param dateTime the date&time(yyyyMMddHHmmss) value
     * @return return "" if failed; else, return formatted time.such as 2015/06/08 22:12:09.
     */
    public static String formatTimeStamp(String dateTime) {
        return FormatUtils.formatTimeStamp(dateTime, "yyyyMMddHHmmss", "yyyy/MM/dd HH:mm:ss");
    }

    /**
     * format data&time.
     * <pre>
     *     String resultDateTime = FormatUtils.formatDateTiem("20210327","yyyyMMdd","yyyy/MM/dd");
     *     //==> resultDateTime: 2021/03/27
     * </pre>
     *
     * @param dateTime  the date&time value
     * @param inFormat  dateTime format. such as yyyyMMddHHmmss, yyyyMMdd, HHmmss
     * @param outFormat out date time format. such as yyyy/MM/dd HH:mm:ss, yyyy/MM/dd, HH:mm:ss
     * @return return "" if failed; else, return formatted time.such as 2015/06/08 22:12:09.
     */
    public static String formatTimeStamp(String dateTime, String inFormat, String outFormat) {
        try {
            SimpleDateFormat parseFormat = new SimpleDateFormat(inFormat, Locale.getDefault());
            Date date2 = parseFormat.parse(dateTime);
            if (date2 == null) {
                return "";
            }
            SimpleDateFormat resultFormat = new SimpleDateFormat(outFormat, Locale.getDefault());
            return resultFormat.format(date2);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * format time stamp
     *
     * @param timeStamp time satmp
     * @param outFormat out date time format. such as yyyy/MM/dd HH:mm:ss, yyyy/MM/dd, HH:mm:ss
     * @return return formatted time.such as 2015/06/08 22:12:09.
     */
    public static String formatTimeStamp(long timeStamp, String outFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(outFormat, Locale.getDefault());
        return sdf.format(new Date(timeStamp));
    }


}
