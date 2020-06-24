/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.drawable;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Xml;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.XmlRes;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Utils class for Drawables.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public final class DrawableUtils {

  private DrawableUtils() {}

  /** Returns a tint filter for the given tint and mode. */
  @Nullable
  public static PorterDuffColorFilter updateTintFilter(
      @NonNull Drawable drawable,
      @Nullable ColorStateList tint,
      @Nullable PorterDuff.Mode tintMode) {
    if (tint == null || tintMode == null) {
      return null;
    }

    final int color = tint.getColorForState(drawable.getState(), Color.TRANSPARENT);
    return new PorterDuffColorFilter(color, tintMode);
  }

  @NonNull
  public static AttributeSet parseDrawableXml(
      @NonNull Context context, @XmlRes int id, @NonNull CharSequence startTag) {
    try {
      XmlPullParser parser = context.getResources().getXml(id);

      int type;
      do {
        type = parser.next();
      } while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT);
      if (type != XmlPullParser.START_TAG) {
        throw new XmlPullParserException("No start tag found");
      }

      if (!TextUtils.equals(parser.getName(), startTag)) {
        throw new XmlPullParserException("Must have a <" + startTag + "> start tag");
      }

      AttributeSet attrs = Xml.asAttributeSet(parser);

      return attrs;
    } catch (XmlPullParserException | IOException e) {
      NotFoundException exception =
          new NotFoundException("Can't load badge resource ID #0x" + Integer.toHexString(id));
      exception.initCause(e);
      throw exception;
    }
  }

  @TargetApi(VERSION_CODES.LOLLIPOP)
  public static void setRippleDrawableRadius(@Nullable RippleDrawable drawable, int radius) {
    if (VERSION.SDK_INT >= VERSION_CODES.M) {
      drawable.setRadius(radius);
    } else {
      try {
        @SuppressLint("PrivateApi")
        Method setMaxRadiusMethod =
            RippleDrawable.class.getDeclaredMethod("setMaxRadius", int.class);
        setMaxRadiusMethod.invoke(drawable, radius);
      } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
        throw new IllegalStateException("Couldn't set RippleDrawable radius", e);
      }
    }
  }
}
