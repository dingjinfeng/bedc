package acquire.base.utils;

import android.content.Context;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import acquire.base.R;


/**
 * Android display utils
 *
 * @author wader
 */
public class DisplayUtils {
    /**
     * Pixel to dip or dp
     */
    public static float px2dip(@NonNull Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return pxValue / scale;
    }

    /**
     * Dip or dp tp pixel
     */
    public static float dip2px(@NonNull Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return dipValue * scale;
    }


    /**
     * Get display metrics
     *
     * @return the screen width, height, density and other related information
     */
    public static DisplayMetrics getDisplayMetrics(@NonNull Context context) {
        return context.getResources().getDisplayMetrics();
    }

    /**
     * Get screen real size
     *
     * @return screen width and height
     */
    @Nullable
    public static int[] getDisplaySize(@NonNull Context context) {
        WindowManager wm = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
        if (wm == null) {
            return null;
        }
        Point point = new Point();
        wm.getDefaultDisplay().getRealSize(point);
        return new int[]{point.x, point.y};
    }

    /**
     * set whether full screen
     */
    public static void setFullScreen(@NonNull Window window, boolean fullScreen) {
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());
        if (fullScreen) {
            WindowCompat.setDecorFitsSystemWindows(window, false);
            controller.hide(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        } else {
            WindowCompat.setDecorFitsSystemWindows(window, true);
            controller.show(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());
        }
    }

    /**
     * show status bar
     */
    public static void showStatusBars(@NonNull Window window, boolean show) {
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());
        if (show) {
            controller.show(WindowInsetsCompat.Type.statusBars());
        } else {
            controller.hide(WindowInsetsCompat.Type.statusBars());
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    /**
     * show bottom navigation bar
     */
    public static void showNavigationBar(@NonNull Window window, boolean show) {
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());
        if (show) {
            controller.show(WindowInsetsCompat.Type.navigationBars());
        } else {
            controller.hide(WindowInsetsCompat.Type.navigationBars());
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    /**
     * set immersive system status bar.
     */
    public static void immersedStatusBar(@NonNull Window window) {
        ViewGroup decorView = (ViewGroup) window.getDecorView();
        //expand to status and navigation bar
        WindowCompat.setDecorFitsSystemWindows(window, false);
        //fit padding
        ViewCompat.setOnApplyWindowInsetsListener(decorView, (v, insets) -> {
            Insets navigationInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //set a navigation padding
                decorView.setPadding(0, 0, navigationInsets.right, navigationInsets.bottom);
                //reverse navigation icon
                WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, decorView);
                controller.setAppearanceLightNavigationBars(true);
            } else {
                //add a navigation padding view
                final String tag = "CUSTOM_NAV_TAG";
                if (decorView.findViewWithTag(tag) == null) {
                    if (navigationInsets.bottom > 0) {
                        View navBarView = new View(window.getContext());
                        LinearLayout.LayoutParams params =
                                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, navigationInsets.bottom);
                        navBarView.setLayoutParams(params);
                        navBarView.setBackgroundResource(R.color.base_colorPrimary);
                        FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(navBarView.getLayoutParams());
                        params2.gravity = Gravity.BOTTOM;
                        navBarView.setLayoutParams(params2);
                        navBarView.setTag(tag);
                        decorView.addView(navBarView);
                        ViewGroup dChild = ((ViewGroup) decorView.getChildAt(0));
                        dChild.getChildAt(dChild.getChildCount()-1).setPadding(0, 0, 0, navigationInsets.bottom);
                    }
                }
            }
            return insets;
        });
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }

    /**
     * set immersive system navigation bar.
     */
    public static void immersedStatusAndNavigationBar(@NonNull Window window) {
        View decorView = window.getDecorView();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        ViewCompat.setOnApplyWindowInsetsListener(decorView, (v, insets) -> {
            decorView.setPadding(0, 0, 0, 0);
            return insets;
        });
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }

    /**
     * add a top padding to the height of the status bar. it works with {@link #immersedStatusBar}
     */
    public static void fitsWindowStatus(@NonNull View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets statusInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            view.setPadding(0, statusInsets.top, 0, 0);
            return insets;
        });
    }

    /**
     * add a bottom padding to the height of the navigation bar. it works with {@link #immersedStatusBar}
     */
    public static void fitsWindowNavigation(@NonNull ViewGroup view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets statusInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            view.setPadding(0, 0, 0, statusInsets.bottom);
            return insets;
        });
    }


    /**
     * set navigation bar's foreground color to light
     */
    private static void setNavigationBarLight(@NonNull Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());
            controller.setAppearanceLightNavigationBars(true);
        } else {
            ViewGroup decorView = (ViewGroup) window.getDecorView();
            final String tag = "CUSTOM_NAV_TAG";
            ViewCompat.setOnApplyWindowInsetsListener(decorView, (v, insets) -> {
                if (decorView.findViewWithTag(tag) == null) {
                    Insets navigationInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
                    if (navigationInsets.bottom > 0) {
                        View navBarView = new View(window.getContext());
                        LinearLayout.LayoutParams params =
                                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, navigationInsets.bottom);
                        navBarView.setLayoutParams(params);
                        navBarView.setBackgroundResource(R.color.base_translucent);
                        FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(navBarView.getLayoutParams());
                        params2.gravity = Gravity.BOTTOM;
                        navBarView.setLayoutParams(params2);
                        navBarView.setTag(tag);
                        decorView.addView(navBarView);
                        decorView.setPadding(0, 0, 0, navigationInsets.bottom);
                    }
                }
                return insets;
            });
        }
    }


    public static Display getDisplay2(@NonNull Context context) {
        DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = displayManager.getDisplays();
        if (displays.length > 1) {
            return displays[1];
        } else {
            return null;
        }
    }

    public static boolean isLandScreen(Window window) {
        WindowManager windowManager = window.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();
        return screenHeight < screenWidth;
    }

}
