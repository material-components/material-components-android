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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
        DrawableCompat.setTint(drawable, color);
      } else {
        DrawableCompat.setTintList(drawable, null);
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

  @TargetApi(VERSION_CODES.LOLLIPOP)
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
        DrawableCompat.setTintMode(drawable, tintMode);
      }
    } else if (forceMutate) {
      drawable.mutate();
    }
    return drawable;
  }

  /**
   * Composites two drawables, returning a drawable instance of {@link LayerDrawable}, with the
   * second on top of the first. If any of the drawables is null, this method will return the other.
   *
   * @param bottomLayerDrawable the drawable to be on the first layer (bottom)
   * @param topLayerDrawable the drawable to be on the second layer (top)
   */
  @Nullable
  public static Drawable compositeTwoLayeredDrawable(
      @Nullable Drawable bottomLayerDrawable, @Nullable Drawable topLayerDrawable) {
    if (bottomLayerDrawable == null) {
      return topLayerDrawable;
    }
    if (topLayerDrawable == null) {
      return bottomLayerDrawable;
    }
    LayerDrawable drawable =
        new LayerDrawable(new Drawable[] {bottomLayerDrawable, topLayerDrawable});
    int topLayerNewWidth;
    int topLayerNewHeight;
    if (topLayerDrawable.getIntrinsicWidth() == -1 || topLayerDrawable.getIntrinsicHeight() == -1) {
      // If there's no intrinsic width or height, keep bottom layer's size.
      topLayerNewWidth = bottomLayerDrawable.getIntrinsicWidth();
      topLayerNewHeight = bottomLayerDrawable.getIntrinsicHeight();
    } else if (topLayerDrawable.getIntrinsicWidth() <= bottomLayerDrawable.getIntrinsicWidth()
        && topLayerDrawable.getIntrinsicHeight() <= bottomLayerDrawable.getIntrinsicHeight()) {
      // If the top layer is smaller than the bottom layer in both its width and height, keep top
      // layer's size.
      topLayerNewWidth = topLayerDrawable.getIntrinsicWidth();
      topLayerNewHeight = topLayerDrawable.getIntrinsicHeight();
    } else {
      float topLayerRatio =
          (float) topLayerDrawable.getIntrinsicWidth() / topLayerDrawable.getIntrinsicHeight();
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
    // Centers the top layer inside the bottom layer. Before M there's no layer gravity support, we
    // need to use layer insets to adjust the top layer position manually.
    if (VERSION.SDK_INT >= VERSION_CODES.M) {
      drawable.setLayerSize(1, topLayerNewWidth, topLayerNewHeight);
      drawable.setLayerGravity(1, Gravity.CENTER);
    } else {
      int horizontalInset = (bottomLayerDrawable.getIntrinsicWidth() - topLayerNewWidth) / 2;
      int verticalInset = (bottomLayerDrawable.getIntrinsicHeight() - topLayerNewHeight) / 2;
      drawable.setLayerInset(1, horizontalInset, verticalInset, horizontalInset, verticalInset);
    }
    return drawable;
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
}
