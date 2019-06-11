/*
 * Copyright (C) 2015 The Android Open Source Project
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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import androidx.annotation.RestrictTo;
import androidx.annotation.XmlRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import java.io.IOException;
import java.lang.reflect.Method;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Utility class for functionality relating to {@link Drawable}s.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class DrawableUtils {

  private static final String LOG_TAG = "DrawableUtils";

  private static Method setConstantStateMethod;
  private static boolean setConstantStateMethodFetched;

  private DrawableUtils() {}

  public static boolean setContainerConstantState(
      DrawableContainer drawable, Drawable.ConstantState constantState) {
    // We can use getDeclaredMethod() on v9+
    return setContainerConstantStateV9(drawable, constantState);
  }

  private static boolean setContainerConstantStateV9(
      DrawableContainer drawable, Drawable.ConstantState constantState) {
    if (!setConstantStateMethodFetched) {
      try {
        setConstantStateMethod =
            DrawableContainer.class.getDeclaredMethod(
                "setConstantState", DrawableContainer.DrawableContainerState.class);
        setConstantStateMethod.setAccessible(true);
      } catch (NoSuchMethodException e) {
        Log.e(LOG_TAG, "Could not fetch setConstantState(). Oh well.");
      }
      setConstantStateMethodFetched = true;
    }
    if (setConstantStateMethod != null) {
      try {
        setConstantStateMethod.invoke(drawable, constantState);
        return true;
      } catch (Exception e) {
        Log.e(LOG_TAG, "Could not invoke setConstantState(). Oh well.");
      }
    }
    return false;
  }

  public static AttributeSet parseDrawableXml(
      final Context context, @XmlRes int id, CharSequence startTag) {
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
}
