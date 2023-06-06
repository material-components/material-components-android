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

package com.google.android.material.color;

import android.content.Context;
import android.content.res.loader.ResourcesLoader;
import android.content.res.loader.ResourcesProvider;
import android.os.Build.VERSION_CODES;
import android.os.ParcelFileDescriptor;
import android.system.Os;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;

/** This class creates a Resources Table at runtime and helps replace color Resources on the fly. */
@RequiresApi(VERSION_CODES.R)
final class ColorResourcesLoaderCreator {

  private ColorResourcesLoaderCreator() {}

  private static final String TAG = "ColorResLoaderCreator";

  @Nullable
  static ResourcesLoader create(
      @NonNull Context context, @NonNull Map<Integer, Integer> colorMapping) {
    try {
      byte[] contentBytes = ColorResourcesTableCreator.create(context, colorMapping);
      Log.i(TAG, "Table created, length: " + contentBytes.length);
      if (contentBytes.length == 0) {
        return null;
      }
      FileDescriptor arscFile = null;
      try {
        arscFile = Os.memfd_create("temp.arsc", /* flags= */ 0);
        if (arscFile == null) {
          // For robolectric tests, memfd_create will return null without ErrnoException.
          Log.w(TAG, "Cannot create memory file descriptor.");
          return null;
        }
        // Note: This must not be closed through the OutputStream.
        try (OutputStream pipeWriter = new FileOutputStream(arscFile)) {
          pipeWriter.write(contentBytes);

          try (ParcelFileDescriptor pfd = ParcelFileDescriptor.dup(arscFile)) {
            ResourcesLoader colorsLoader = new ResourcesLoader();
            colorsLoader.addProvider(
                ResourcesProvider.loadFromTable(pfd, /* assetsProvider= */ null));
            return colorsLoader;
          }
        }
      } finally {
        if (arscFile != null) {
          Os.close(arscFile);
        }
      }
    } catch (Exception e) {
      Log.e(TAG, "Failed to create the ColorResourcesTableCreator.", e);
    }
    return null;
  }
}
