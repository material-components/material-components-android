/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.progressindicator;

import com.google.android.material.R;

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * This class contains the common functions shared between ProgressIndicator in different types.
 * This is an abstract class which is meant for directly use.
 *
 * <p>With the default style {@link R.style#Widget_MaterialComponents_ProgressIndicator}, 4dp
 * indicator/track size and no animation is used for visibility change. Without customization,
 * primaryColor will be used as the indicator color; the indicator color applying disabledAlpha will
 * be used as the track color. The following attributes can be used to customize the
 * ProgressIndicator's appearance:
 *
 * <ul>
 *   <li>{@code indicatorSize}: the stroke width of the indicator and track.
 *   <li>{@code indicatorColor}: the color of the indicator.
 *   <li>{@code trackColor}: the color of the track.
 *   <li>{@code indicatorCornerRadius}: the radius of the rounded corner of the indicator stroke.
 * </ul>
 */
public class BaseProgressIndicator extends ProgressBar {
  protected static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_ProgressIndicator;

  protected static final float DEFAULT_OPACITY = 0.2f;
  protected static final int MAX_ALPHA = 255;

  /** A place to hold all the attributes. */
  protected final BaseProgressIndicatorSpec baseSpec;

  // **************** Constructors ****************

  protected BaseProgressIndicator(@NonNull Context context) {
    this(context, null);
  }

  protected BaseProgressIndicator(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  protected BaseProgressIndicator(
      @NonNull Context context, @Nullable AttributeSet attrs, final int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);

    // Ensures that we are using the correctly themed context rather than the context that was
    // passed in.
    context = getContext();

    baseSpec = new BaseProgressIndicatorSpec(context, attrs, defStyleAttr);
  }
}
