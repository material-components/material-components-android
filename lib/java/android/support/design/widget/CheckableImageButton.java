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

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.support.annotation.RestrictTo;
import android.util.AttributeSet;

/**
 * @hide
 * @deprecated This version of the ClickableImageButton is deprecated use {@link
 *     android.support.design.internal.CheckableImageButton} instead.
 */
@Deprecated
@RestrictTo(LIBRARY_GROUP)
public class CheckableImageButton extends android.support.design.internal.CheckableImageButton {

  public CheckableImageButton(Context context) {
    super(context);
  }

  public CheckableImageButton(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public CheckableImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }
}
