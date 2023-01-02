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

package com.google.android.material.internal;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

class ViewGroupOverlayApi14 extends ViewOverlayApi14 implements ViewGroupOverlayImpl {

  ViewGroupOverlayApi14(Context context, ViewGroup hostView, View requestingView) {
    super(context, hostView, requestingView);
  }

  static ViewGroupOverlayApi14 createFrom(ViewGroup viewGroup) {
    return (ViewGroupOverlayApi14) ViewOverlayApi14.createFrom(viewGroup);
  }

  @Override
  public void add(@NonNull View view) {
    overlayViewGroup.add(view);
  }

  @Override
  public void remove(@NonNull View view) {
    overlayViewGroup.remove(view);
  }
}
