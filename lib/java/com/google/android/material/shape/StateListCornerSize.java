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
import static com.google.android.material.shape.ShapeAppearanceModel.getCornerSize;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.StateSet;
import android.util.Xml;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.StyleableRes;
import com.google.android.material.shape.ShapeAppearanceModel.CornerSizeUnaryOperator;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * A state list of {@link CornerSize}s, which are used to define the corner size overrides in a
 * {@link StateListShapeAppearanceModel}.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class StateListCornerSize {
  private static final int INITIAL_CAPACITY = 10;
  int stateCount;
  @NonNull private CornerSize defaultCornerSize;
  @NonNull int[][] stateSpecs = new int[INITIAL_CAPACITY][];
  @NonNull CornerSize[] cornerSizes = new CornerSize[INITIAL_CAPACITY];

  /**
   * Creates a {@link StateListCornerSize} from a styleable attribute.
   *
   * <p>If the attribute refers to an xml resource, the resource is parsed and the corner sizes are
   * extracted from the items. If the attribute refers to a {@code dimen} or {@code fraction}
   * resource, the resource is resolved as a {@link CornerSize} and set as the default corner size.
   * If the attribute is not set or refers to a resource that cannot be resolved, the {@code
   * defaultCornerSize} is set.
   *
   * @param context the context
   * @param attributes the typed array in context
   * @param index the index of the styleable attribute
   * @param defaultCornerSize the default corner size, when attribute is not set or cannot be
   *     resolved.
   * @return the {@link StateListCornerSize}
   */
  @NonNull
  public static StateListCornerSize create(
      @NonNull Context context,
      @NonNull TypedArray attributes,
      @StyleableRes int index,
      @NonNull CornerSize defaultCornerSize) {
    int resourceId = attributes.getResourceId(index, 0);
    if (resourceId == ID_NULL) {
      return create(ShapeAppearanceModel.getCornerSize(attributes, index, defaultCornerSize));
    }
    String typeName = context.getResources().getResourceTypeName(resourceId);
    if (!typeName.equals("xml")) {
      return create(ShapeAppearanceModel.getCornerSize(attributes, index, defaultCornerSize));
    }
    try (XmlResourceParser parser = context.getResources().getXml(resourceId)) {
      StateListCornerSize stateListCornerSize = new StateListCornerSize();
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
        stateListCornerSize.loadCornerSizesFromItems(context, parser, attrs, context.getTheme());
      }
      return stateListCornerSize;
    } catch (XmlPullParserException | IOException | Resources.NotFoundException e) {
      return create(defaultCornerSize);
    }
  }

  @NonNull
  public static StateListCornerSize create(@NonNull CornerSize cornerSize) {
    StateListCornerSize stateListCornerSize = new StateListCornerSize();
    stateListCornerSize.addStateCornerSize(StateSet.WILD_CARD, cornerSize);
    return stateListCornerSize;
  }

  @NonNull
  public StateListCornerSize withTransformedCornerSizes(@NonNull CornerSizeUnaryOperator op) {
    StateListCornerSize newStateListCornerSize = new StateListCornerSize();
    newStateListCornerSize.stateCount = stateCount;
    newStateListCornerSize.stateSpecs = new int[stateSpecs.length][];
    System.arraycopy(stateSpecs, 0, newStateListCornerSize.stateSpecs, 0, stateSpecs.length);
    newStateListCornerSize.cornerSizes = new CornerSize[cornerSizes.length];
    for (int i = 0; i < stateCount; i++) {
      newStateListCornerSize.cornerSizes[i] = op.apply(cornerSizes[i]);
    }
    return newStateListCornerSize;
  }

  public boolean isStateful() {
    return stateCount > 1;
  }

  @NonNull
  public CornerSize getDefaultCornerSize() {
    return defaultCornerSize;
  }

  @NonNull
  public CornerSize getCornerSizeForState(@NonNull int[] stateSet) {
    int idx = indexOfStateSet(stateSet);
    if (idx < 0) {
      idx = indexOfStateSet(StateSet.WILD_CARD);
    }
    return idx < 0 ? defaultCornerSize : cornerSizes[idx];
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

  private void loadCornerSizesFromItems(
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
              ? res.obtainAttributes(attrs, R.styleable.ShapeAppearance)
              : theme.obtainStyledAttributes(attrs, R.styleable.ShapeAppearance, 0, 0);

      CornerSize cornerSize =
          getCornerSize(a, R.styleable.ShapeAppearance_cornerSize, new AbsoluteCornerSize(0));
      a.recycle();

      // Parse all unrecognized attributes as state specifiers.
      int j = 0;
      final int numAttrs = attrs.getAttributeCount();
      int[] stateSpec = new int[numAttrs];
      for (int i = 0; i < numAttrs; i++) {
        final int stateResId = attrs.getAttributeNameResource(i);
        if (stateResId != R.attr.cornerSize) {
          stateSpec[j++] = attrs.getAttributeBooleanValue(i, false) ? stateResId : -stateResId;
        }
      }
      stateSpec = StateSet.trimStateSet(stateSpec, j);
      addStateCornerSize(stateSpec, cornerSize);
    }
  }

  private void addStateCornerSize(@NonNull int[] stateSpec, @NonNull CornerSize cornerSize) {
    if (stateCount == 0 || stateSpec.length == 0) {
      defaultCornerSize = cornerSize;
    }
    if (stateCount >= stateSpecs.length) {
      growArray(stateCount, stateCount + 10);
    }
    stateSpecs[stateCount] = stateSpec;
    cornerSizes[stateCount] = cornerSize;
    stateCount++;
  }

  private void growArray(int oldSize, int newSize) {
    int[][] newStateSets = new int[newSize][];
    System.arraycopy(stateSpecs, 0, newStateSets, 0, oldSize);
    stateSpecs = newStateSets;
    CornerSize[] newCornerSizes = new CornerSize[newSize];
    System.arraycopy(cornerSizes, 0, newCornerSizes, 0, oldSize);
    cornerSizes = newCornerSizes;
  }
}
