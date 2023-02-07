package com.google.android.material.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.Display;

import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** @hide */
@RestrictTo(LIBRARY_GROUP)
public class WindowUtils {

  private static final String TAG = WindowUtils.class.getSimpleName();

  private WindowUtils() {}

  public static Rect getCurrentWindowBounds(Activity activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      return Api30Impl.getCurrentWindowBounds(activity);
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      return Api17Impl.getCurrentWindowBounds(activity);
    } else {
      return Api14Impl.getCurrentWindowBounds(activity);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.R)
  private static class Api30Impl {

    static Rect getCurrentWindowBounds(Activity activity) {
      return activity.getWindowManager().getCurrentWindowMetrics().getBounds();
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
  private static class Api17Impl {

    static Rect getCurrentWindowBounds(Activity activity) {
      Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();

      Point defaultDisplaySize = new Point();
      defaultDisplay.getRealSize(defaultDisplaySize);

      Rect bounds = new Rect();
      bounds.right = defaultDisplaySize.x;
      bounds.bottom = defaultDisplaySize.y;

      return bounds;
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
  private static class Api14Impl {

    static Rect getCurrentWindowBounds(Activity activity) {
      Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();
      Point defaultDisplaySize = getRealSizeForDisplay(defaultDisplay);

      Rect bounds = new Rect();
      if (defaultDisplaySize.x == 0 || defaultDisplaySize.y == 0) {
        defaultDisplay.getRectSize(bounds);
      } else {
        bounds.right = defaultDisplaySize.x;
        bounds.bottom = defaultDisplaySize.y;
      }

      return bounds;
    }

    private static Point getRealSizeForDisplay(Display display) {
      Point size = new Point();
      try {
        Method getRealSizeMethod = Display.class.getDeclaredMethod("getRealSize", Point.class);
        getRealSizeMethod.setAccessible(true);
        getRealSizeMethod.invoke(display, size);
      } catch (NoSuchMethodException e) {
        Log.w(TAG, e);
      } catch (IllegalAccessException e) {
        Log.w(TAG, e);
      } catch (InvocationTargetException e) {
        Log.w(TAG, e);
      }

      return size;
    }
  }
}
