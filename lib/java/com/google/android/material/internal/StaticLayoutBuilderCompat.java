/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.google.android.material.internal;

import static androidx.core.util.Preconditions.checkNotNull;

import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristic;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import android.text.TextUtils;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import java.lang.reflect.Constructor;

/**
 * Class to create StaticLayout using StaticLayout.Builder on API23+ and a hidden StaticLayout
 * constructor before that.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * StaticLayout staticLayout =
 *   StaticLayoutBuilderCompat.obtain("Lorem Ipsum", new TextPaint(), 100)
 *     .setAlignment(Alignment.ALIGN_NORMAL)
 *     .build();
 * }</pre>
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
final class StaticLayoutBuilderCompat {

  private static final String TEXT_DIR_CLASS = "android.text.TextDirectionHeuristic";
  private static final String TEXT_DIRS_CLASS = "android.text.TextDirectionHeuristics";
  private static final String TEXT_DIR_CLASS_LTR = "LTR";
  private static final String TEXT_DIR_CLASS_RTL = "RTL";

  private static boolean initialized;

  @Nullable private static Constructor<StaticLayout> constructor;
  @Nullable private static Object textDirection;

  private CharSequence source;
  private final TextPaint paint;
  private final int width;
  private int start;
  private int end;

  private Alignment alignment;
  private int maxLines;
  private boolean includePad;
  private boolean isRtl;
  @Nullable private TextUtils.TruncateAt ellipsize;

  private StaticLayoutBuilderCompat(CharSequence source, TextPaint paint, int width) {
    this.source = source;
    this.paint = paint;
    this.width = width;
    this.start = 0;
    this.end = source.length();
    this.alignment = Alignment.ALIGN_NORMAL;
    this.maxLines = Integer.MAX_VALUE;
    this.includePad = true;
    this.ellipsize = null;
  }

  /**
   * Obtain a builder for constructing StaticLayout objects.
   *
   * @param source The text to be laid out, optionally with spans
   * @param paint The base paint used for layout
   * @param width The width in pixels
   * @return a builder object used for constructing the StaticLayout
   */
  @NonNull
  public static StaticLayoutBuilderCompat obtain(
      @NonNull CharSequence source, @NonNull TextPaint paint, @IntRange(from = 0) int width) {
    return new StaticLayoutBuilderCompat(source, paint, width);
  }

  /**
   * Set the alignment. The default is {@link Layout.Alignment#ALIGN_NORMAL}.
   *
   * @param alignment Alignment for the resulting {@link StaticLayout}
   * @return this builder, useful for chaining
   */
  @NonNull
  public StaticLayoutBuilderCompat setAlignment(@NonNull Alignment alignment) {
    this.alignment = alignment;
    return this;
  }

  /**
   * Set whether to include extra space beyond font ascent and descent (which is needed to avoid
   * clipping in some languages, such as Arabic and Kannada). The default is {@code true}.
   *
   * @param includePad whether to include padding
   * @return this builder, useful for chaining
   * @see android.widget.TextView#setIncludeFontPadding
   */
  @NonNull
  public StaticLayoutBuilderCompat setIncludePad(boolean includePad) {
    this.includePad = includePad;
    return this;
  }

  /**
   * Set the index of the start of the text
   *
   * @return this builder, useful for chaining
   */
  @NonNull
  public StaticLayoutBuilderCompat setStart(@IntRange(from = 0) int start) {
    this.start = start;
    return this;
  }

  /**
   * Set the index + 1 of the end of the text
   *
   * @return this builder, useful for chaining
   * @see android.widget.TextView#setIncludeFontPadding
   */
  @NonNull
  public StaticLayoutBuilderCompat setEnd(@IntRange(from = 0) int end) {
    this.end = end;
    return this;
  }

  /**
   * Set maximum number of lines. This is particularly useful in the case of ellipsizing, where it
   * changes the layout of the last line. The default is unlimited.
   *
   * @param maxLines maximum number of lines in the layout
   * @return this builder, useful for chaining
   * @see android.widget.TextView#setMaxLines
   */
  @NonNull
  public StaticLayoutBuilderCompat setMaxLines(@IntRange(from = 0) int maxLines) {
    this.maxLines = maxLines;
    return this;
  }

  /**
   * Set ellipsizing on the layout. Causes words that are longer than the view is wide, or exceeding
   * the number of lines (see #setMaxLines).
   *
   * @param ellipsize type of ellipsis behavior
   * @return this builder, useful for chaining
   * @see android.widget.TextView#setEllipsize
   */
  @NonNull
  public StaticLayoutBuilderCompat setEllipsize(@Nullable TextUtils.TruncateAt ellipsize) {
    this.ellipsize = ellipsize;
    return this;
  }

  /** A method that allows to create a StaticLayout with maxLines on all supported API levels. */
  public StaticLayout build() throws StaticLayoutBuilderCompatException {
    if (source == null) {
      source = "";
    }


    int availableWidth = Math.max(0, width);
    CharSequence textToDraw = source;
    if (maxLines == 1) {
      textToDraw = TextUtils.ellipsize(source, paint, availableWidth, ellipsize);
    }

    end = Math.min(textToDraw.length(), end);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (isRtl) {
        alignment = Alignment.ALIGN_OPPOSITE;
      }
      // Marshmallow introduced StaticLayout.Builder which allows us not to use
      // the hidden constructor.
      StaticLayout.Builder builder =
          StaticLayout.Builder.obtain(
              textToDraw, start, end, paint, availableWidth);
      builder.setAlignment(alignment);
      builder.setIncludePad(includePad);
      TextDirectionHeuristic textDirectionHeuristic = isRtl
          ? TextDirectionHeuristics.RTL
          : TextDirectionHeuristics.LTR;
      builder.setTextDirection(textDirectionHeuristic);
      if (ellipsize != null) {
        builder.setEllipsize(ellipsize);
      }
      builder.setMaxLines(maxLines);
      return builder.build();
    }

    createConstructorWithReflection();
    // Use the hidden constructor on older API levels.
    try {
      return checkNotNull(constructor)
          .newInstance(
              textToDraw,
              start,
              end,
              paint,
              availableWidth,
              alignment,
              checkNotNull(textDirection),
              1.0f,
              0.0f,
              includePad,
              null,
              availableWidth,
              maxLines);
    } catch (Exception cause) {
      throw new StaticLayoutBuilderCompatException(cause);
    }
  }

  /**
   * set constructor to this hidden {@link StaticLayout constructor.}
   *
   * <pre>{@code
   * StaticLayout(
   *   CharSequence source,
   *   int bufstart,
   *   int bufend,
   *   TextPaint paint,
   *   int outerwidth,
   *   Alignment align,
   *   TextDirectionHeuristic textDir,
   *   float spacingmult,
   *   float spacingadd,
   *   boolean includepad,
   *   TextUtils.TruncateAt ellipsize,
   *   int ellipsizedWidth,
   *   int maxLines)
   * }</pre>
   */
  private void createConstructorWithReflection() throws StaticLayoutBuilderCompatException {
    if (initialized) {
      return;
    }

    try {
      final Class<?> textDirClass;
      boolean useRtl = isRtl && Build.VERSION.SDK_INT >= VERSION_CODES.M;
      if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2) {
        textDirClass = TextDirectionHeuristic.class;
        textDirection = useRtl ? TextDirectionHeuristics.RTL : TextDirectionHeuristics.LTR;
      } else {
        ClassLoader loader = StaticLayoutBuilderCompat.class.getClassLoader();
        String textDirClassName = isRtl ? TEXT_DIR_CLASS_RTL : TEXT_DIR_CLASS_LTR;
        textDirClass = loader.loadClass(TEXT_DIR_CLASS);
        Class<?> textDirsClass = loader.loadClass(TEXT_DIRS_CLASS);
        textDirection = textDirsClass.getField(textDirClassName).get(textDirsClass);
      }

      final Class<?>[] signature =
          new Class<?>[] {
            CharSequence.class,
            int.class,
            int.class,
            TextPaint.class,
            int.class,
            Alignment.class,
            textDirClass,
            float.class,
            float.class,
            boolean.class,
            TextUtils.TruncateAt.class,
            int.class,
            int.class
          };

      constructor = StaticLayout.class.getDeclaredConstructor(signature);
      constructor.setAccessible(true);
      initialized = true;
    } catch (Exception cause) {
      throw new StaticLayoutBuilderCompatException(cause);
    }
  }

  public StaticLayoutBuilderCompat setIsRtl(boolean isRtl) {
    this.isRtl = isRtl;
    return this;
  }

  static class StaticLayoutBuilderCompatException extends Exception {

    StaticLayoutBuilderCompatException(Throwable cause) {
      super("Error thrown initializing StaticLayout " + cause.getMessage(), cause);
    }
  }
}
