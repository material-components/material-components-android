/*
 * Copyright 2019 The Android Open Source Project
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

package com.google.android.material.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import com.google.android.material.resources.TextAppearance;
import com.google.android.material.resources.TextAppearanceFontCallback;
import java.lang.ref.WeakReference;

/**
 * Class that helps to support drawing text in drawables. It can be used by any drawable that draws
 * text.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class TextDrawableHelper {

  private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

  private final TextAppearanceFontCallback fontCallback =
      new TextAppearanceFontCallback() {
        @Override
        public void onFontRetrieved(@NonNull Typeface typeface, boolean fontResolvedSynchronously) {
          if (fontResolvedSynchronously) {
            return;
          }
          textWidthDirty = true;
          TextDrawableDelegate textDrawableDelegate = delegate.get();
          if (textDrawableDelegate != null) {
            textDrawableDelegate.onTextSizeChange();
          }
        }

        @Override
        public void onFontRetrievalFailed(int reason) {
          textWidthDirty = true;
          // Use fallback font.
          TextDrawableDelegate textDrawableDelegate = delegate.get();
          if (textDrawableDelegate != null) {
            textDrawableDelegate.onTextSizeChange();
          }
        }
      };

  private float textWidth;
  private boolean textWidthDirty = true;
  @Nullable private WeakReference<TextDrawableDelegate> delegate = new WeakReference<>(null);
  @Nullable private TextAppearance textAppearance;

  /**
   * Please provide a delegate if your text font may load asynchronously.
   */
  public TextDrawableHelper(@Nullable TextDrawableDelegate delegate) {
    setDelegate(delegate);
  }

  /** Sets the delegate that owns this TextDrawableHelper. */
  public void setDelegate(@Nullable TextDrawableDelegate delegate) {
    this.delegate = new WeakReference<>(delegate);
  }

  @NonNull
  public TextPaint getTextPaint() {
    return textPaint;
  }

  public void setTextWidthDirty(boolean dirty) {
    textWidthDirty = dirty;
  }

  public boolean isTextWidthDirty() {
    return textWidthDirty;
  }

  /** Returns the visual width of the {@code text} based on its current text appearance. */
  public float getTextWidth(String text) {
    if (!textWidthDirty) {
      return textWidth;
    }

    textWidth = calculateTextWidth(text);
    textWidthDirty = false;
    return textWidth;
  }

  private float calculateTextWidth(@Nullable CharSequence charSequence) {
    if (charSequence == null) {
      return 0f;
    }
    return textPaint.measureText(charSequence, 0, charSequence.length());
  }

  /**
   * Returns the text appearance.
   *
   * @see #setTextAppearance(TextAppearance, Context)
   */
  @Nullable
  public TextAppearance getTextAppearance() {
    return textAppearance;
  }

  /**
   * Sets the delegate drawable's text appearance. If the {@code textAppearance} is {@code null},
   * text appearance will be cleared.
   *
   * @param textAppearance The delegate drawable's text appearance or null to clear it.
   * @see #getTextAppearance()
   */
  public void setTextAppearance(@Nullable TextAppearance textAppearance, Context context) {
    if (this.textAppearance != textAppearance) {
      this.textAppearance = textAppearance;
      if (textAppearance != null) {
        textAppearance.updateMeasureState(context, textPaint, fontCallback);

        TextDrawableDelegate textDrawableDelegate = delegate.get();
        if (textDrawableDelegate != null) {
          textPaint.drawableState = textDrawableDelegate.getState();
        }
        textAppearance.updateDrawState(context, textPaint, fontCallback);
        textWidthDirty = true;
      }

      TextDrawableDelegate textDrawableDelegate = delegate.get();
      if (textDrawableDelegate != null) {
        textDrawableDelegate.onTextSizeChange();
        textDrawableDelegate.onStateChange(textDrawableDelegate.getState());
      }
    }
  }

  public void updateTextPaintDrawState(Context context) {
    textAppearance.updateDrawState(context, textPaint, fontCallback);
  }

  /** Delegate interface to be implemented by Drawables that own a TextDrawableHelper. */
  public interface TextDrawableDelegate {
    // See Drawable#getState()
    @NonNull
    int[] getState();

    /** Handles a change in the text's size. */
    void onTextSizeChange();

    // See Drawable#onStateChange();
    boolean onStateChange(int[] state);
  }
}
