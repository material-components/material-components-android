/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.google.android.material.testing;

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;
import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Field;

/**
 * Testing utility class for creating a mapping of resource ids to their human-readable names.
 *
 * <p>This should only be used to get the resource name (human-readable String) in a static context
 * (for example: getting the resource name to be used as the name of a parameterized test). If
 * possible, prefer to use {@link android.content.res.Resources#getResourceEntryName}.
 */
public final class ResourceNameLookup {

  private ResourceNameLookup() {}

  @TargetApi(VERSION_CODES.N)
  public static ImmutableMap<Integer, String> createResourceNameMap(Class<?>... resourceClasses) {
    ImmutableMap.Builder<Integer, String> resNameMapBuilder =
        ImmutableMap.<Integer, String>builder();
    for (Class<?> clazz : resourceClasses) {
      for (Field field : clazz.getFields()) {
        if (field.getType().equals(Integer.TYPE)) {
          try {
            int resId = field.getInt(null);
            String resName = field.getName();
            if (resId != 0) {
              resNameMapBuilder.put(resId, resName);
            }
          } catch (IllegalAccessException | IllegalArgumentException e) {
            continue;
          }
        }
      }
    }
    return resNameMapBuilder.build();
  }
}
