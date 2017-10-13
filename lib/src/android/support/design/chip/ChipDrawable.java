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

package android.support.design.chip;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;

/**
 * ChipDrawable contains all the layout and draw logic for {@link Chip}.
 *
 * <p>Use ChipDrawable directly in contexts that require a Drawable. For example, an auto-complete
 * enabled EditText can replace snippets of text with a ChipDrawable to represent it as a semantic
 * entity.
 *
 * @see Chip
 */
public class ChipDrawable extends Drawable {

  private final Context context;

  public static ChipDrawable createFromAttributes(
      Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
    ChipDrawable chip = new ChipDrawable(context);
    chip.loadFromAttributes(attrs, defStyleAttr, defStyleRes);
    return chip;
  }

  private ChipDrawable(Context context) {
    this.context = context;
  }

  private void loadFromAttributes(
      AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
    TypedArray a =
        context.obtainStyledAttributes(attrs, R.styleable.ChipDrawable, defStyleAttr, defStyleRes);

    // TODO

    a.recycle();
  }

  @Override
  public void draw(@NonNull Canvas canvas) {}

  @Override
  public void setAlpha(int alpha) {}

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {}

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }
}
