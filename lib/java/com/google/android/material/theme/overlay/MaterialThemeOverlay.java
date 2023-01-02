/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.theme.overlay;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.view.ContextThemeWrapper;

/**
 * Utility to apply a theme overlay to any {@link android.content.Context}. The theme overlay is
 * read from an attribute in the style. This is useful to override theme attributes only for the
 * specific view.
 *
 * <p>The intended use is in a custom view constructor.
 *
 * <pre>{@code
 * public MyCustomView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
 *     super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
 * }
 * }</pre>
 */
public class MaterialThemeOverlay {

  private MaterialThemeOverlay() {
  }

  private static final int[] ANDROID_THEME_OVERLAY_ATTRS =
      new int[] {android.R.attr.theme, R.attr.theme};

  private static final int[] MATERIAL_THEME_OVERLAY_ATTR = new int[] {R.attr.materialThemeOverlay};

  /**
   * Uses the materialThemeOverlay attribute to create a themed context. This allows us to use
   * MaterialThemeOverlay with a default style, and gives us some protection against losing our
   * ThemeOverlay by clients who set android:theme or app:theme. If android:theme or app:theme is
   * specified by the client, any attributes defined there will take precedence over attributes
   * defined in materialThemeOverlay.
   */
  @NonNull
  public static Context wrap(
      @NonNull Context context,
      @Nullable AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    int materialThemeOverlayId =
        obtainMaterialThemeOverlayId(context, attrs, defStyleAttr, defStyleRes);
    boolean contextHasOverlay = context instanceof ContextThemeWrapper
        && ((ContextThemeWrapper) context).getThemeResId() == materialThemeOverlayId;

    if (materialThemeOverlayId == 0 || contextHasOverlay) {
      return context;
    }

    Context contextThemeWrapper = new ContextThemeWrapper(context, materialThemeOverlayId);

    // We want values set in android:theme or app:theme to always override values supplied by
    // materialThemeOverlay, so we'll wrap the context again if either of those are set.
    int androidThemeOverlayId = obtainAndroidThemeOverlayId(context, attrs);
    if (androidThemeOverlayId != 0) {
      contextThemeWrapper.getTheme().applyStyle(androidThemeOverlayId, true);
    }

    return contextThemeWrapper;
  }

  /**
   * Retrieves the value of {@code android:theme} or {@code app:theme}, not taking into account
   * {@code defStyleAttr} and {@code defStyleRes} because the Android theme overlays shouldn't work
   * from default styles.
   */
  @StyleRes
  private static int obtainAndroidThemeOverlayId(@NonNull Context context, AttributeSet attrs) {
    TypedArray a = context.obtainStyledAttributes(attrs, ANDROID_THEME_OVERLAY_ATTRS);
    int androidThemeId = a.getResourceId(0 /* index */, 0 /* defaultVal */);
    int appThemeId = a.getResourceId(1 /* index */, 0 /* defaultVal */);
    a.recycle();

    return androidThemeId != 0 ? androidThemeId : appThemeId;
  }

  /**
   * Retrieves the value of {@code materialThemeOverlay}, taking into account {@code defStyleAttr}
   * and {@code defStyleRes} because the Material theme overlay should work from default styles.
   */
  @StyleRes
  private static int obtainMaterialThemeOverlayId(
      @NonNull Context context,
      @Nullable AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    TypedArray a =
        context.obtainStyledAttributes(
            attrs, MATERIAL_THEME_OVERLAY_ATTR, defStyleAttr, defStyleRes);
    int materialThemeOverlayId = a.getResourceId(0 /* index */, 0 /* defaultVal */);
    a.recycle();

    return materialThemeOverlayId;
  }
}

