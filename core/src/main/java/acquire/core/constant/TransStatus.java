package acquire.core.constant;

import java.lang.reflect.Field;

import acquire.base.annotations.Description;

/**
 * Transaction status
 *
 * @author Janson
 * @date 2021/7/8 16:16
 */
public class TransStatus {
    /**
     * Normal
     */
    @Description("SUCCESS")
    public static final int SUCCESS = 0;
    /**
     * Cancelled
     */
    @Description("CANCELLED")
    public static final int CANCELLED = 1;
    /**
     * Refunded
     */
    @Description("REFUNDED")
    public static final int REFUNDED = 2;
    /**
     * Auth-completion
     */
    @Description("COMPLETED")
    public static final int COMPLETED = 3;

    public static String getDescription(int status){
        Field[] fields = TransStatus.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                if (field.getInt(TransStatus.class) == status){
                    Description description = field.getAnnotation(Description.class);
                    if (description != null){
                        return description.value();
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}
