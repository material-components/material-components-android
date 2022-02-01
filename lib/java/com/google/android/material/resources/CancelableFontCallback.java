/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.google.android.material.resources;

import android.graphics.Typeface;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.core.content.res.ResourcesCompat.FontCallback;

/**
 * {@link FontCallback} allowing cancelling of pending async font fetch, in case a different font is
 * set / requested in the meantime. On failed fetch, specified fallback font will be applied.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public final class CancelableFontCallback extends TextAppearanceFontCallback {

  /** Functional interface for method to call when font is retrieved (or fails with fallback). */
  public interface ApplyFont {
    void apply(Typeface font);
  }

  private final Typeface fallbackFont;
  private final ApplyFont applyFont;
  private boolean cancelled;

  public CancelableFontCallback(ApplyFont applyFont, Typeface fallbackFont) {
    this.fallbackFont = fallbackFont;
    this.applyFont = applyFont;
  }

  @Override
  public void onFontRetrieved(Typeface font, boolean fontResolvedSynchronously) {
    updateIfNotCancelled(font);
  }

  @Override
  public void onFontRetrievalFailed(int reason) {
    updateIfNotCancelled(fallbackFont);
  }

  /**
   * Cancels this callback. No async operations will actually be interrupted as a result of this
   * method, but it will ignore any subsequent result of the fetch.
   *
   * <p>Callback cannot be resumed after canceling. New callback has to be created.
   */
  public void cancel() {
    cancelled = true;
  }

  private void updateIfNotCancelled(Typeface updatedFont) {
    if (!cancelled) {
      applyFont.apply(updatedFont);
    }
  }
}
