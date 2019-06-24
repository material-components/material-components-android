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

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.widget.EditText;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A MaterialTextView is a derivative of {@link AppCompatTextView} that displays text to the user.
 * To provide user-editable text, see {@link EditText}.
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
 */
public class MaterialTextView extends AppCompatTextView {

  /**
   * Flag that will be used to avoid applying line height more than once when {@link
   * #setTextAppearance(int)} method is called, the base implementation of which may call the {@link
   * #setTextAppearance(Context, int)} internally.
   */
  private final AtomicBoolean skipApplyingLineHeight = new AtomicBoolean();

  public MaterialTextView(Context context) {
    this(context, null /* attrs */);
  }

  public MaterialTextView(Context context, AttributeSet attrs) {
    this(context, attrs, android.R.attr.textViewStyle);
  }

  public MaterialTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, 0);
  }

  public MaterialTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr);

    final Resources.Theme theme = context.getTheme();

    if (readLineHeightFromLayout(theme, attrs, defStyleAttr, defStyleRes) < 0) {
      int resId = findViewAppearanceResourceId(theme, attrs, defStyleAttr, defStyleRes);
      if (resId != -1) {
        applyLineHeightFromViewAppearance(theme, resId);
      }
    }
  }

  @Override
  public void setTextAppearance(int resId) {
    skipApplyingLineHeight.set(true);
    super.setTextAppearance(resId);
    skipApplyingLineHeight.set(false);
    applyLineHeightFromViewAppearance(getContext().getTheme(), resId);
  }

  @Override
  public void setTextAppearance(Context context, int resId) {
    super.setTextAppearance(context, resId);
    if (!skipApplyingLineHeight.get()) {
      applyLineHeightFromViewAppearance(context.getTheme(), resId);
    }
  }

  private void applyLineHeightFromViewAppearance(Theme theme, int resId) {
    TypedArray attributes = theme.obtainStyledAttributes(resId, R.styleable.MaterialTextAppearance);
    int lineHeight = readLineHeighAttribute(attributes);
    attributes.recycle();

    if (lineHeight >= 0) {
      setLineHeight(lineHeight);
    }
  }

  private static int readLineHeightFromLayout(
      Theme theme, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    TypedArray attributes =
        theme.obtainStyledAttributes(
            attrs, R.styleable.MaterialTextView, defStyleAttr, defStyleRes);
    int lineHeight = readLineHeighAttribute(attributes);
    attributes.recycle();

    return lineHeight;
  }

  private static int readLineHeighAttribute(TypedArray attributes) {
    int lineHeight =
        attributes.getDimensionPixelSize(R.styleable.MaterialTextView_android_lineHeight, -1);
    if (lineHeight < 0) {
      lineHeight = attributes.getDimensionPixelSize(R.styleable.MaterialTextView_lineHeight, -1);
    }

    return lineHeight;
  }

  private static int findViewAppearanceResourceId(
      Theme theme, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    TypedArray attributes =
        theme.obtainStyledAttributes(
            attrs, R.styleable.MaterialTextView, defStyleAttr, defStyleRes);
    int appearanceAttrId =
        attributes.getResourceId(R.styleable.MaterialTextView_android_textAppearance, -1);
    attributes.recycle();
    return appearanceAttrId;
  }
}
