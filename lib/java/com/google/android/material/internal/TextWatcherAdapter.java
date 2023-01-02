/*
 * Copyright 2020 The Android Open Source Project
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

package com.google.android.material.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.text.Editable;
import android.text.TextWatcher;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * Base class for scenarios where user wants to implement only one method of
 * {@link TextWatcher}.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class TextWatcherAdapter implements TextWatcher {

  @Override
  public void beforeTextChanged(@NonNull CharSequence s, int start, int count, int after) {}

  @Override
  public void onTextChanged(@NonNull CharSequence s, int start, int before, int count) {}

  @Override
  public void afterTextChanged(@NonNull Editable s) {}
}
