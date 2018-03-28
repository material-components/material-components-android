/*
 * Copyright (C) 2016 The Android Open Source Project
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

package android.support.design.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * @deprecated This version of the TextInputLayout is deprecated use {@link
 *     android.support.design.textfield.TextInputLayout} instead.
 */
@Deprecated
public class TextInputLayout extends android.support.design.textfield.TextInputLayout {

  public TextInputLayout(Context context) {
    super(context);
  }

  public TextInputLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public TextInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  // Below are methods for curvular since they have to be methods set on this class

  @Override
  public void setCounterEnabled(boolean enabled) {
    super.setCounterEnabled(enabled);
  }

  @Override
  public void setCounterMaxLength(int maxLength) {
    super.setCounterMaxLength(maxLength);
  }

  @Override
  public void setErrorEnabled(boolean enabled) {
    super.setErrorEnabled(enabled);
  }

  @Override
  public void setError(@Nullable CharSequence errorText) {
    super.setError(errorText);
  }

  @Override
  public void setHint(@Nullable CharSequence hint) {
    super.setHint(hint);
  }
}
