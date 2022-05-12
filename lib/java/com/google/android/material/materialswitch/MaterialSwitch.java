/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.google.android.material.materialswitch;

import com.google.android.material.R;

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.content.Context;
import androidx.appcompat.widget.SwitchCompat;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A class that creates a Material Themed Switch. This class is intended to provide a brand new
 * Switch design and replace the obsolete
 * {@link com.google.android.material.switchmaterial.SwitchMaterial} class.
 */
public class MaterialSwitch extends SwitchCompat {
  private static final int DEF_STYLE_RES = R.style.Widget_Material3_CompoundButton_MaterialSwitch;

  public MaterialSwitch(@NonNull Context context) {
    this(context, null);
  }

  public MaterialSwitch(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.materialSwitchStyle);
  }

  public MaterialSwitch(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
  }
}
