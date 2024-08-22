/*
 * Copyright 2024 The Android Open Source Project
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

package com.google.android.material.shape;

import com.google.android.material.R;

import static android.content.res.Resources.ID_NULL;
import static java.lang.Math.max;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.StateSet;
import android.util.TypedValue;
import android.util.Xml;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.StyleableRes;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * A state list of size values, which are used to define the change per state.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class StateListSizeChange {
  private static final int INITIAL_CAPACITY = 10;
  int stateCount;
  @NonNull private SizeChange defaultSizeChange;
  @NonNull int[][] stateSpecs = new int[INITIAL_CAPACITY][];
  @NonNull SizeChange[] sizeChanges = new SizeChange[INITIAL_CAPACITY];

  /**
   * Creates a {@link StateListSizeChange} from a styleable attribute.
   *
   * <p>If the attribute refers to an xml resource, the resource is parsed and the size values are
   * extracted from the items. If the attribute is not set or refers to a resource that cannot be
   * resolved, {@code null} will be returned.
   *
   * @param context the context
   * @param attributes the typed array in context
   * @param index the index of the styleable attribute
   * @return the {@link StateListSizeChange}
   */
  @Nullable
  public static StateListSizeChange create(
      @NonNull Context context, @NonNull TypedArray attributes, @StyleableRes int index) {
    int resourceId = attributes.getResourceId(index, 0);
    if (resourceId == ID_NULL) {
      return null;
    }
    String typeName = context.getResources().getResourceTypeName(resourceId);
    if (!typeName.equals("xml")) {
      return null;
    }
    try (XmlResourceParser parser = context.getResources().getXml(resourceId)) {
      StateListSizeChange stateListSizeChange = new StateListSizeChange();
      AttributeSet attrs = Xml.asAttributeSet(parser);
      int type;

      while ((type = parser.next()) != XmlPullParser.START_TAG
          && type != XmlPullParser.END_DOCUMENT) {
        // Seek parser to start tag.
      }
      if (type != XmlPullParser.START_TAG) {
        throw new XmlPullParserException("No start tag found");
      }
      final String name = parser.getName();
      if (name.equals("selector")) {
        stateListSizeChange.loadSizeChangeFromItems(context, parser, attrs, context.getTheme());
      }
      return stateListSizeChange;
    } catch (XmlPullParserException | IOException | Resources.NotFoundException e) {
      return null;
    }
  }

  public boolean isStateful() {
    return stateCount > 1;
  }

  @NonNull
  public SizeChange getDefaultSizeChange() {
    return defaultSizeChange;
  }

  @NonNull
  public SizeChange getSizeChangeForState(@NonNull int[] stateSet) {
    int idx = indexOfStateSet(stateSet);
    if (idx < 0) {
      idx = indexOfStateSet(StateSet.WILD_CARD);
    }
    return idx < 0 ? defaultSizeChange : sizeChanges[idx];
  }

  public int getMaxWidthChange(@Px int baseWidth) {
    int maxWidthChange = -baseWidth;
    for (int i = 0; i < stateCount; i++) {
      SizeChange sizeChange = sizeChanges[i];
      if (sizeChange.widthChange.type == SizeChangeType.PIXELS) {
        maxWidthChange = (int) max(maxWidthChange, sizeChange.widthChange.amount);
      } else if (sizeChange.widthChange.type == SizeChangeType.PERCENT) {
        maxWidthChange = (int) max(maxWidthChange, baseWidth * sizeChange.widthChange.amount);
      }
    }
    return maxWidthChange;
  }

  private int indexOfStateSet(int[] stateSet) {
    final int[][] stateSpecs = this.stateSpecs;
    for (int i = 0; i < stateCount; i++) {
      if (StateSet.stateSetMatches(stateSpecs[i], stateSet)) {
        return i;
      }
    }
    return -1;
  }

  private void loadSizeChangeFromItems(
      @NonNull Context context,
      @NonNull XmlPullParser parser,
      @NonNull AttributeSet attrs,
      @Nullable Theme theme)
      throws XmlPullParserException, IOException {
    final int innerDepth = parser.getDepth() + 1;
    int depth;
    int type;

    while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
        && ((depth = parser.getDepth()) >= innerDepth || type != XmlPullParser.END_TAG)) {
      if (type != XmlPullParser.START_TAG
          || depth > innerDepth
          || !parser.getName().equals("item")) {
        continue;
      }

      Resources res = context.getResources();
      final TypedArray a =
          theme == null
              ? res.obtainAttributes(attrs, R.styleable.StateListSizeChange)
              : theme.obtainStyledAttributes(attrs, R.styleable.StateListSizeChange, 0, 0);

      SizeChangeAmount widthChangeAmount =
          getSizeChangeAmount(a, R.styleable.StateListSizeChange_widthChange, null);

      a.recycle();

      // Parse all unrecognized attributes as state specifiers.
      int j = 0;
      final int numAttrs = attrs.getAttributeCount();
      int[] stateSpec = new int[numAttrs];
      for (int i = 0; i < numAttrs; i++) {
        final int stateResId = attrs.getAttributeNameResource(i);
        if (stateResId != R.attr.widthChange) {
          stateSpec[j++] = attrs.getAttributeBooleanValue(i, false) ? stateResId : -stateResId;
        }
      }
      stateSpec = StateSet.trimStateSet(stateSpec, j);
      addStateSizeChange(stateSpec, new SizeChange(widthChangeAmount));
    }
  }

  @Nullable
  private SizeChangeAmount getSizeChangeAmount(
      @NonNull TypedArray a, int index, @Nullable SizeChangeAmount defaultValue) {
    TypedValue value = a.peekValue(index);
    if (value == null) {
      return defaultValue;
    }

    if (value.type == TypedValue.TYPE_DIMENSION) {
      return new SizeChangeAmount(
          SizeChangeType.PIXELS,
          TypedValue.complexToDimensionPixelSize(value.data, a.getResources().getDisplayMetrics()));
    } else if (value.type == TypedValue.TYPE_FRACTION) {
      return new SizeChangeAmount(SizeChangeType.PERCENT, value.getFraction(1.0f, 1.0f));
    } else {
      return defaultValue;
    }
  }

  private void addStateSizeChange(@NonNull int[] stateSpec, @NonNull SizeChange sizeChange) {
    if (stateCount == 0 || stateSpec.length == 0) {
      defaultSizeChange = sizeChange;
    }
    if (stateCount >= stateSpecs.length) {
      growArray(stateCount, stateCount + 10);
    }
    stateSpecs[stateCount] = stateSpec;
    sizeChanges[stateCount] = sizeChange;
    stateCount++;
  }

  private void growArray(int oldSize, int newSize) {
    int[][] newStateSets = new int[newSize][];
    System.arraycopy(stateSpecs, 0, newStateSets, 0, oldSize);
    stateSpecs = newStateSets;
    SizeChange[] newSizeChanges = new SizeChange[newSize];
    System.arraycopy(sizeChanges, 0, newSizeChanges, 0, oldSize);
    sizeChanges = newSizeChanges;
  }

  /** A collection of all values needed in a size change. */
  public static class SizeChange {
    @Nullable public SizeChangeAmount widthChange;

    SizeChange(@Nullable SizeChangeAmount widthChange) {
      this.widthChange = widthChange;
    }

    SizeChange(@NonNull SizeChange other) {
      this.widthChange = new SizeChangeAmount(other.widthChange.type, other.widthChange.amount);
    }
  }

  /** The size change of one dimension, including the type and amount. */
  public static class SizeChangeAmount {
    SizeChangeType type;
    float amount;

    SizeChangeAmount(SizeChangeType type, float amount) {
      this.type = type;
      this.amount = amount;
    }

    public int getChange(@Px int baseSize) {
      if (type == SizeChangeType.PERCENT) {
        return (int) (amount * baseSize);
      }
      if (type == SizeChangeType.PIXELS) {
        return (int) amount;
      }
      return 0;
    }
  }

  /** The type of size change. */
  public enum SizeChangeType {
    PERCENT,
    PIXELS
  }
}
