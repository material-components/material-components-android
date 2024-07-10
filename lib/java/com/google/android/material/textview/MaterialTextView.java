/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.google.android.material.textview;

import com.google.android.material.R;

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StyleableRes;
import com.google.android.material.resources.MaterialAttributes;
import com.google.android.material.resources.MaterialResources;

/**
 * A MaterialTextView is a derivative of {@link AppCompatTextView} that displays text to the user.
 * To provide user-editable text, see {@link android.widget.EditText}.
 *
 * <p>MaterialTextView supports the ability to read and apply {@code android:lineHeight} value from
 * a {@code TextAppearance} style.
 *
 * <p>The following code sample shows a typical use, with an XML layout and code to modify the
 * contents of the material text view:
 *
 * <pre>
 * &lt;LinearLayout
 * xmlns:android="http://schemas.android.com/apk/res/android"
 * android:layout_width="match_parent"
 * android:layout_height="match_parent"&gt;
 *    &lt;MaterialTextView
 *        android:id="@+id/text_view_id"
 *        android:layout_height="wrap_content"
 *        android:layout_width="wrap_content"
 *        android:text="@string/hello" /&gt;
 * &lt;/LinearLayout&gt;
 * </pre>
 *
 * <p>This code sample demonstrates how to modify the contents of the material text view defined in
 * the previous XML layout:
 *
 * <pre>
 * public class MainActivity extends Activity {
 *
 *    protected void onCreate(Bundle savedInstanceState) {
 *         super.onCreate(savedInstanceState);
 *         setContentView(R.layout.activity_main);
 *         final MaterialTextView textView = (MaterialTextView) findViewById(R.id.text_view_id);
 *         textView.setText(R.string.user_greeting);
 *     }
 * }
 * </pre>
 *
 * <p>To customize the appearance of MaterialTextView, see <a
 * href="https://developer.android.com/guide/topics/ui/themes.html">Styles and Themes</a>.
 *
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/MaterialTextView.md">component
 * developer guidance</a>.
 */
public class MaterialTextView extends AppCompatTextView {

  public MaterialTextView(@NonNull Context context) {
    this(context, null /* attrs */);
  }

  public MaterialTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, android.R.attr.textViewStyle);
  }

  public MaterialTextView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, /* defStyleRes= */ 0), attrs, defStyleAttr);
    initialize(attrs, defStyleAttr, /* defStyleRes= */ 0);
  }

  /**
   * @deprecated Since {@link AppCompatTextView} does not provide a 4 arg constructor, the
   *     defStyleRes argument will be ignored. Please use the 3 arg constructor instead. see
   *     {@link #MaterialTextView(Context, AttributeSet, int)}
   */
  @Deprecated
  public MaterialTextView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(wrap(context, attrs, defStyleAttr, defStyleRes), attrs, defStyleAttr);
    initialize(attrs, defStyleAttr, defStyleRes);
  }

  @Override
  public void setTextAppearance(@NonNull Context context, int resId) {
    super.setTextAppearance(context, resId);

    boolean canApplyLineHeight = canApplyTextAppearanceLineHeight(context);
    boolean canForceRefreshFontVariationSettings = VERSION.SDK_INT >= VERSION_CODES.O;
    if (!canApplyLineHeight && !canForceRefreshFontVariationSettings) {
      return;
    }

    TypedArray appearance =
        context.getTheme().obtainStyledAttributes(resId, R.styleable.MaterialTextAppearance);
    if (canApplyLineHeight) {
      applyLineHeightFromViewAppearance(appearance);
    }
    if (canForceRefreshFontVariationSettings) {
      maybeForceApplyFontVariationSettingsFromViewAppearance(appearance);
    }
    appearance.recycle();
  }

  private void initialize(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    Context context = getContext();
    final Resources.Theme theme = context.getTheme();
    boolean canApplyLineHeight = canApplyTextAppearanceLineHeight(context)
        && !viewAttrsHasLineHeight(context, theme, attrs, defStyleAttr, defStyleRes);
    boolean canForceRefreshFontVariationSettings = VERSION.SDK_INT >= VERSION_CODES.O;
    if (!canApplyLineHeight && !canForceRefreshFontVariationSettings) {
      return;
    }

    int resId = findViewAppearanceResourceId(theme, attrs, defStyleAttr, defStyleRes);
    if (resId == -1) {
      return;
    }

    TypedArray appearance =
        context.getTheme().obtainStyledAttributes(resId, R.styleable.MaterialTextAppearance);
    if (canApplyLineHeight) {
      applyLineHeightFromViewAppearance(appearance);
    }
    if (canForceRefreshFontVariationSettings) {
      maybeForceApplyFontVariationSettingsFromViewAppearance(appearance);
    }
    appearance.recycle();
  }

  private void applyLineHeightFromViewAppearance(TypedArray appearance) {
    int lineHeight =
        readFirstAvailableDimension(
            getContext(),
            appearance,
            R.styleable.MaterialTextAppearance_android_lineHeight,
            R.styleable.MaterialTextAppearance_lineHeight);
    if (lineHeight >= 0) {
      setLineHeight(lineHeight);
    }
  }

  /**
   * Maybe read and set font variation settings from a TextAppearance.
   *
   * <p>This is a workaround for a bug in appcompat where fontVariationSettings set in a
   * TextAppearance do not take effect.
   *
   * <p>TODO(b/264321145): Remove once AppCompatTextView fixes text appearance font variation
   * support
   */
  @RequiresApi(VERSION_CODES.O)
  private void maybeForceApplyFontVariationSettingsFromViewAppearance(TypedArray appearance) {
    int fontVariationSettingsIndex =
        MaterialResources.getIndexWithValue(
            appearance,
            R.styleable.MaterialTextAppearance_fontVariationSettings,
            R.styleable.MaterialTextAppearance_android_fontVariationSettings);
    String fontVariationSettings = appearance.getString(fontVariationSettingsIndex);
    if (fontVariationSettings != null) {
      // Clear the font variation settings to force TextView to reset the text Paint's
      // settings.
      setFontVariationSettings("");
      setFontVariationSettings(fontVariationSettings);
    }
  }

  private static boolean canApplyTextAppearanceLineHeight(Context context) {
    return MaterialAttributes.resolveBoolean(context, R.attr.textAppearanceLineHeightEnabled, true);
  }

  private static int readFirstAvailableDimension(
      @NonNull Context context,
      @NonNull TypedArray attributes,
      @NonNull @StyleableRes int... indices) {
    int lineHeight = -1;

    for (int index = 0; index < indices.length && lineHeight < 0; ++index) {
      lineHeight = MaterialResources.getDimensionPixelSize(context, attributes, indices[index], -1);
    }

    return lineHeight;
  }

  private static boolean viewAttrsHasLineHeight(
      @NonNull Context context,
      @NonNull Theme theme,
      @Nullable AttributeSet attrs,
      int defStyleAttr,
      int defStyleRes) {
    TypedArray attributes =
        theme.obtainStyledAttributes(
            attrs, R.styleable.MaterialTextView, defStyleAttr, defStyleRes);
    int lineHeight =
        readFirstAvailableDimension(
            context,
            attributes,
            R.styleable.MaterialTextView_android_lineHeight,
            R.styleable.MaterialTextView_lineHeight);
    attributes.recycle();

    return lineHeight != -1;
  }

  private static int findViewAppearanceResourceId(
      @NonNull Theme theme, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    TypedArray attributes =
        theme.obtainStyledAttributes(
            attrs, R.styleable.MaterialTextView, defStyleAttr, defStyleRes);
    int appearanceAttrId =
        attributes.getResourceId(R.styleable.MaterialTextView_android_textAppearance, -1);
    attributes.recycle();
    return appearanceAttrId;
  }
}
