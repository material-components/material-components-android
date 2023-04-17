/*
 * Copyright 2019 The Android Open Source Project
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

import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import java.util.Locale;

/** Utils to determine device manufacturers for special handling. */
@RestrictTo(Scope.LIBRARY_GROUP)
public class ManufacturerUtils {
  private static final String LGE = "lge";
  private static final String SAMSUNG = "samsung";
  private static final String MEIZU = "meizu";

  private ManufacturerUtils() {}

  /** Returns true if the device manufacturer is Meizu. */
  public static boolean isMeizuDevice() {
    return getManufacturer().equals(MEIZU);
  }

  /** Returns true if the device manufacturer is LG. */
  public static boolean isLGEDevice() {
    return getManufacturer().equals(LGE);
  }

  /** Returns true if the device manufacturer is Samsung. */
  public static boolean isSamsungDevice() {
    return getManufacturer().equals(SAMSUNG);
  }

  /**
   * Returns true if the date input keyboard is potentially missing separator characters such as /.
   */
  public static boolean isDateInputKeyboardMissingSeparatorCharacters() {
    return isLGEDevice() || isSamsungDevice();
  }

  @NonNull
  private static String getManufacturer() {
    final String manufacturer = Build.MANUFACTURER;
    if (manufacturer != null) {
      return manufacturer.toLowerCase(Locale.ENGLISH);
    } else {
      return "";
    }
  }
}
