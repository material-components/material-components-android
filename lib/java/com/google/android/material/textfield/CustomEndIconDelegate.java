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

package com.google.android.material.textfield;

import androidx.annotation.NonNull;

/**
 * Default initialization of a custom {@link TextInputLayout.EndIconMode}.
 *
 * <p>It will initialize with an empty icon drawable unless one was specified in xml via the {@code
 * app:endIconDrawable} attribute. You can also specify the drawable by calling {@link
 * TextInputLayout#setEndIconDrawable(int)} after calling {@link
 * TextInputLayout#setEndIconMode(int)}.
 */
class CustomEndIconDelegate extends EndIconDelegate {
  CustomEndIconDelegate(@NonNull EndCompoundLayout endLayout) {
    super(endLayout);
  }

  @Override
  void setUp() {
    endLayout.setEndIconOnLongClickListener(null);
  }
}
