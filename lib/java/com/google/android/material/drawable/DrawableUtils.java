/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.drawable;

import com.google.android.material.R;

import static java.lang.Math.max;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ColorStateListDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.Gravity;
import androidx.annotation.ColorInt;
import androidx.annotation.DoNotInline;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.XmlRes;
import androidx.core.graphics.drawable.DrawableCompat;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Utils class for Drawables.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public final class DrawableUtils {

  /**
   * Indicates to use the intrinsic size of the {@link Drawable}.
   *
   * <p>Used in {@link #compositeTwoLayeredDrawable(Drawable, Drawable, int, int)}.
   */
  public static final int INTRINSIC_SIZE = -1;

  // The value that the Drawable#getIntrinsicWidth() method returns when the drawable has no
  // intrinsic width.
  private static final int UNSPECIFIED_WIDTH = -1;

  // The value that the Drawable#getIntrinsicHeight() method returns when the drawable has no
  // intrinsic height.
  private static final int UNSPECIFIED_HEIGHT = -1;

  private DrawableUtils() {}

  /**
   * Tints the given {@link Drawable} with the given color. If the color is transparent, this
   * method will remove any set tints on the drawable.
   */
  public static void setTint(@NonNull Drawable drawable, @ColorInt int color) {
    boolean hasTint = color != Color.TRANSPARENT;
    if (VERSION.SDK_INT == VERSION_CODES.LOLLIPOP) {
      // On API 21, AppCompat's WrappedDrawableApi21 class only supports tinting certain types of
      // drawables. Replicates the logic here to support all types of drawables.
      if (hasTint) {
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
      } else {
        drawable.setColorFilter(null);
      }
    } else {
      if (hasTint) {
        drawable.setTint(color);
      } else {
        drawable.setTintList(null);
      }
    }
  }

  /** Returns a tint filter for the given tint and mode. */
  @Nullable
  public static PorterDuffColorFilter updateTintFilter(
      @NonNull Drawable drawable,
      @Nullable ColorStateList tint,
      @Nullable PorterDuff.Mode tintMode) {
    if (tint == null || tintMode == null) {
      return null;
    }

    final int color = tint.getColorForState(drawable.getState(), Color.TRANSPARENT);
    return new PorterDuffColorFilter(color, tintMode);
  }

  @NonNull
  public static AttributeSet parseDrawableXml(
      @NonNull Context context, @XmlRes int id, @NonNull CharSequence startTag) {
    try {
      XmlPullParser parser = context.getResources().getXml(id);

      int type;
      do {
        type = parser.next();
      } while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT);
      if (type != XmlPullParser.START_TAG) {
        throw new XmlPullParserException("No start tag found");
      }

      if (!TextUtils.equals(parser.getName(), startTag)) {
        throw new XmlPullParserException("Must have a <" + startTag + "> start tag");
      }

      AttributeSet attrs = Xml.asAttributeSet(parser);

      return attrs;
    } catch (XmlPullParserException | IOException e) {
      NotFoundException exception =
          new NotFoundException("Can't load badge resource ID #0x" + Integer.toHexString(id));
      exception.initCause(e);
      throw exception;
    }
  }

  public static void setRippleDrawableRadius(@Nullable RippleDrawable drawable, int radius) {
    if (VERSION.SDK_INT >= VERSION_CODES.M) {
      drawable.setRadius(radius);
    } else {
      try {
        @SuppressLint("PrivateApi")
        Method setMaxRadiusMethod =
            RippleDrawable.class.getDeclaredMethod("setMaxRadius", int.class);
        setMaxRadiusMethod.invoke(drawable, radius);
      } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
        throw new IllegalStateException("Couldn't set RippleDrawable radius", e);
      }
    }
  }

  /**
   * Wraps and mutates the passed in drawable so that it may be used for tinting if a tintList is
   * present. Also applies the tintMode if present.
   */
  @Nullable
  public static Drawable createTintableDrawableIfNeeded(
      @Nullable Drawable drawable, @Nullable ColorStateList tintList, @Nullable Mode tintMode) {
    return createTintableMutatedDrawableIfNeeded(
        drawable, tintList, tintMode, /* forceMutate= */ false);
  }

  /**
   * Wraps and mutates the passed in drawable so that it may be used for tinting if a tintList is
   * present. Also applies the tintMode if present. If there's not a tintList and the API level is <
   * 21, it'll still mutate the drawable.
   *
   * <p>Use this method instead of the above if the passed in drawable will be a child of a {@link
   * LayerDrawable} in APIs < 23, its tintList may be null, and it may be mutated, in order to
   * prevent issue where the drawable may not have its constant state set up properly.
   */
  @Nullable
  public static Drawable createTintableMutatedDrawableIfNeeded(
      @Nullable Drawable drawable, @Nullable ColorStateList tintList, @Nullable Mode tintMode) {
    return createTintableMutatedDrawableIfNeeded(
        drawable, tintList, tintMode, VERSION.SDK_INT < VERSION_CODES.M);
  }

  @Nullable
  private static Drawable createTintableMutatedDrawableIfNeeded(
      @Nullable Drawable drawable,
      @Nullable ColorStateList tintList,
      @Nullable Mode tintMode,
      boolean forceMutate) {
    if (drawable == null) {
      return null;
    }
    if (tintList != null) {
      drawable = DrawableCompat.wrap(drawable).mutate();
      if (tintMode != null) {
        drawable.setTintMode(tintMode);
      }
    } else if (forceMutate) {
      drawable.mutate();
    }
    return drawable;
  }

  /**
   * Composites two drawables, returning a drawable instance of {@link LayerDrawable},
   * with the top layer centered.
   *
   * <p>If any of the drawables is null, this method will return the other.
   *
   * @param bottomLayerDrawable the drawable to be on the bottom layer
   * @param topLayerDrawable the drawable to be on the top layer
   */
  @Nullable
  public static Drawable compositeTwoLayeredDrawable(
      @Nullable Drawable bottomLayerDrawable,
      @Nullable Drawable topLayerDrawable) {
    return compositeTwoLayeredDrawable(
        bottomLayerDrawable, topLayerDrawable, INTRINSIC_SIZE, INTRINSIC_SIZE);
  }

  /**
   * Composites two drawables, returning a drawable instance of {@link LayerDrawable},
   * with the top layer centered to the bottom layer. The top layer will be scaled according to the
   * provided desired width/height and the size of the bottom layer so the top layer can fit in the
   * bottom layer and preserve its desired aspect ratio.
   *
   * <p>If any of the drawables is null, this method will return the other.
   *
   * @param bottomLayerDrawable the drawable to be on the bottom layer
   * @param topLayerDrawable the drawable to be on the top layer
   * @param topLayerDesiredWidth top layer desired width in pixels, or {@link #INTRINSIC_SIZE} to
   *     use the intrinsic width.
   * @param topLayerDesiredHeight top layer desired height in pixels, or {@link #INTRINSIC_SIZE} to
   *     use the intrinsic height.
   */
  @Nullable
  public static Drawable compositeTwoLayeredDrawable(
      @Nullable Drawable bottomLayerDrawable,
      @Nullable Drawable topLayerDrawable,
      @Px int topLayerDesiredWidth,
      @Px int topLayerDesiredHeight) {
    if (bottomLayerDrawable == null) {
      return topLayerDrawable;
    }
    if (topLayerDrawable == null) {
      return bottomLayerDrawable;
    }

    boolean shouldScaleTopLayer =
        topLayerDesiredWidth != INTRINSIC_SIZE && topLayerDesiredHeight != INTRINSIC_SIZE;
    if (topLayerDesiredWidth == INTRINSIC_SIZE) {
      topLayerDesiredWidth = getTopLayerIntrinsicWidth(bottomLayerDrawable, topLayerDrawable);
    }
    if (topLayerDesiredHeight == INTRINSIC_SIZE) {
      topLayerDesiredHeight = getTopLayerIntrinsicHeight(bottomLayerDrawable, topLayerDrawable);
    }

    final int topLayerNewWidth;
    final int topLayerNewHeight;
    if (topLayerDesiredWidth <= bottomLayerDrawable.getIntrinsicWidth()
        && topLayerDesiredHeight <= bottomLayerDrawable.getIntrinsicHeight()) {
      // If the top layer's desired size is smaller than the bottom layer's size in both its width
      // and height, keep top layer's desired size.
      topLayerNewWidth = topLayerDesiredWidth;
      topLayerNewHeight = topLayerDesiredHeight;
    } else {
      float topLayerRatio = (float) topLayerDesiredWidth / topLayerDesiredHeight;
      float bottomLayerRatio =
          (float) bottomLayerDrawable.getIntrinsicWidth()
              / bottomLayerDrawable.getIntrinsicHeight();
      if (topLayerRatio >= bottomLayerRatio) {
        // If the top layer is wider in ratio than the bottom layer, shrink it according to its
        // width.
        topLayerNewWidth = bottomLayerDrawable.getIntrinsicWidth();
        topLayerNewHeight = (int) (topLayerNewWidth / topLayerRatio);
      } else {
        // If the top layer is taller in ratio than the bottom layer, shrink it according to its
        // height.
        topLayerNewHeight = bottomLayerDrawable.getIntrinsicHeight();
        topLayerNewWidth = (int) (topLayerRatio * topLayerNewHeight);
      }
    }

    LayerDrawable drawable;
    if (VERSION.SDK_INT >= VERSION_CODES.M) {
      drawable = new LayerDrawable(new Drawable[] {bottomLayerDrawable, topLayerDrawable});

      drawable.setLayerSize(1, topLayerNewWidth, topLayerNewHeight);
      drawable.setLayerGravity(1, Gravity.CENTER);
    } else {
      if (shouldScaleTopLayer) {
        topLayerDrawable =
            new ScaledDrawableWrapper(topLayerDrawable, topLayerNewWidth, topLayerNewHeight);
      }
      drawable = new LayerDrawable(new Drawable[] {bottomLayerDrawable, topLayerDrawable});

      final int horizontalInset =
          max((bottomLayerDrawable.getIntrinsicWidth() - topLayerNewWidth) / 2, 0);
      final int verticalInset =
          max((bottomLayerDrawable.getIntrinsicHeight() - topLayerNewHeight) / 2, 0);
      drawable.setLayerInset(1, horizontalInset, verticalInset, horizontalInset, verticalInset);
    }

    return drawable;
  }

  private static int getTopLayerIntrinsicWidth(
      @NonNull Drawable bottomLayerDrawable, @NonNull Drawable topLayerDrawable) {
    int topLayerIntrinsicWidth = topLayerDrawable.getIntrinsicWidth();
    return topLayerIntrinsicWidth != UNSPECIFIED_WIDTH
        ? topLayerIntrinsicWidth : bottomLayerDrawable.getIntrinsicWidth();
  }

  private static int getTopLayerIntrinsicHeight(
      @NonNull Drawable bottomLayerDrawable, @NonNull Drawable topLayerDrawable) {
    int topLayerIntrinsicHeight = topLayerDrawable.getIntrinsicHeight();
    return topLayerIntrinsicHeight != UNSPECIFIED_HEIGHT
        ? topLayerIntrinsicHeight : bottomLayerDrawable.getIntrinsicHeight();
  }

  /** Returns a new state that adds the checked state to the input state. */
  @NonNull
  public static int[] getCheckedState(@NonNull int[] state) {
    for (int i = 0; i < state.length; i++) {
      if (state[i] == android.R.attr.state_checked) {
        return state;
      } else if (state[i] == 0) {
        int[] newState = state.clone();
        newState[i] = android.R.attr.state_checked;
        return newState;
      }
    }
    int[] newState = Arrays.copyOf(state, state.length + 1);
    newState[state.length] = android.R.attr.state_checked;
    return newState;
  }

  /** Returns a new state that removes the checked state from the input state. */
  @NonNull
  public static int[] getUncheckedState(@NonNull int[] state) {
    int[] newState = new int[state.length];
    int i = 0;
    for (int subState : state) {
      if (subState != android.R.attr.state_checked) {
        newState[i++] = subState;
      }
    }
    return newState;
  }

  /** Sets the Outline to a {@link android.graphics.Path path}, if possible. */
  public static void setOutlineToPath(@NonNull final Outline outline, @NonNull final Path path) {
    if (VERSION.SDK_INT >= VERSION_CODES.R) {
      OutlineCompatR.setPath(outline, path);
    } else if (VERSION.SDK_INT >= VERSION_CODES.Q) {
      try {
        // As of Android Q, the restriction that the path must be convex is removed, but the API is
        // misnamed until the introduction of setPath() in R, so we have to use setConvexPath for Q.
        OutlineCompatL.setConvexPath(outline, path);
      } catch (IllegalArgumentException ignored) {
        // The change to support concave paths was done late in the release cycle. People
        // using pre-releases of Q would experience a crash here.
      }
    } else if (path.isConvex()) {
      OutlineCompatL.setConvexPath(outline, path);
    }
  }

  /**
   * Returns the {@link ColorStateList} if it can be retrieved from the {@code drawable}, or null
   * otherwise.
   *
   * <p>In particular:
   *
   * <ul>
   *   <li>If the {@code drawable} is a {@link ColorStateListDrawable}, the method will return the
   *       {@code drawable}'s {@link ColorStateList}.
   *   <li>If the {@code drawable} is a {@link ColorDrawable}, the method will return a {@link
   *       ColorStateList} containing the {@code drawable}'s color.
   * </ul>
   */
  @Nullable
  public static ColorStateList getColorStateListOrNull(@Nullable final Drawable drawable) {
    if (drawable instanceof ColorDrawable) {
      return ColorStateList.valueOf(((ColorDrawable) drawable).getColor());
    }

    if (VERSION.SDK_INT >= VERSION_CODES.Q) {
      if (drawable instanceof ColorStateListDrawable) {
        return ((ColorStateListDrawable) drawable).getColorStateList();
      }
    }

    return null;
  }

  @RequiresApi(VERSION_CODES.R)
  private static class OutlineCompatR {
    // Avoid class verification failures on older Android versions.
    @DoNotInline
    static void setPath(@NonNull Outline outline, @NonNull Path path) {
      outline.setPath(path);
    }
  }

  private static class OutlineCompatL {
    // Avoid class verification failures on older Android versions.
    @DoNotInline
    static void setConvexPath(@NonNull Outline outline, @NonNull Path path) {
      outline.setConvexPath(path);
    }
  }
}
