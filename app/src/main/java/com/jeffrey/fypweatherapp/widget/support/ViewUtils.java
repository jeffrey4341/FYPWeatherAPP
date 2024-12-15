package com.jeffrey.fypweatherapp.widget.support;

import android.graphics.Rect;
import android.os.Build;
import androidx.core.view.ViewCompat;
import android.util.Log;
import android.view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @hide
 */
public class ViewUtils {
    private static final String TAG = "ViewUtils";

    private static Method sComputeFitSystemWindowsMethod;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            try {
                sComputeFitSystemWindowsMethod = View.class.getDeclaredMethod(
                        "computeFitSystemWindows", Rect.class, Rect.class);
                if (!sComputeFitSystemWindowsMethod.isAccessible()) {
                    sComputeFitSystemWindowsMethod.setAccessible(true);
                }
            } catch (NoSuchMethodException e) {
                Log.d(TAG, "Could not find method computeFitSystemWindows. Oh well.");
            }
        }
    }

    private ViewUtils() {}

    public static boolean isLayoutRtl(View view) {
        return ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    /**
     * Merge two states as returned by {@link ViewCompat#getMeasuredState(View)} ()}.
     * @param curState The current state as returned from a view or the result
     * of combining multiple views.
     * @param newState The new view state to combine.
     * @return Returns a new integer reflecting the combination of the two
     * states.
     */
    public static int combineMeasuredStates(int curState, int newState) {
        return curState | newState;
    }

    /**
     * Allow calling the hidden method {@code computeFitSystemWindows(Rect, Rect)} through
     * reflection on {@code view}.
     */
    public static void computeFitSystemWindows(View view, Rect inoutInsets, Rect outLocalInsets) {
        if (sComputeFitSystemWindowsMethod != null) {
            try {
                sComputeFitSystemWindowsMethod.invoke(view, inoutInsets, outLocalInsets);
            } catch (Exception e) {
                Log.d(TAG, "Could not invoke computeFitSystemWindows", e);
            }
        }
    }

    /**
     * Allow calling the hidden method {@code makeOptionalFitsSystem()} through reflection on
     * {@code view}.
     */
    public static void makeOptionalFitsSystemWindows(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            try {
                // We need to use getMethod() for makeOptionalFitsSystemWindows since both View
                // and ViewGroup implement the method
                Method method = view.getClass().getMethod("makeOptionalFitsSystemWindows");
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                method.invoke(view);
            } catch (NoSuchMethodException e) {
                Log.d(TAG, "Could not find method makeOptionalFitsSystemWindows. Oh well...");
            } catch (InvocationTargetException e) {
                Log.d(TAG, "Could not invoke makeOptionalFitsSystemWindows", e);
            } catch (IllegalAccessException e) {
                Log.d(TAG, "Could not invoke makeOptionalFitsSystemWindows", e);
            }
        }
    }
}
