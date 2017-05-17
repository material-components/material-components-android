/*
 * Copyright 2017 The Android Open Source Project
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

package android.support.design.backlayer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * A ViewGroup that can be expanded to show more content.
 *
 * <p>Since its resting state is hidden (partially exposed), it keeps a copy of its original
 * dimensions.
 *
 * TODO: Actually support expanding and hiding, this is currently in foundational state.
 */
public class BackLayerLayout extends FrameLayout {

  private boolean measured = false;

  public BackLayerLayout(@NonNull Context context) {
    super(context);
  }

  public BackLayerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    measured = true;
    setMinimumHeight(getMeasuredHeight());
    setMinimumWidth(getMeasuredWidth());
  }

  public boolean isMeasured() {
    return measured;
  }

}
