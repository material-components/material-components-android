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
import static com.google.android.material.shape.ShapeAppearanceModel.CORNER_BOTTOM_LEFT;
import static com.google.android.material.shape.ShapeAppearanceModel.CORNER_BOTTOM_RIGHT;
import static com.google.android.material.shape.ShapeAppearanceModel.CORNER_TOP_LEFT;
import static com.google.android.material.shape.ShapeAppearanceModel.CORNER_TOP_RIGHT;
import static com.google.android.material.shape.ShapeAppearanceModel.containsFlag;

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
import androidx.annotation.XmlRes;
import com.google.android.material.shape.ShapeAppearanceModel.CornerSizeUnaryOperator;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.IOException;
import java.util.Objects;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * A state list of {@link ShapeAppearanceModel}s, which are used to define the shapes for different
 * states of a component or {@link MaterialShapeDrawable}.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class StateListShapeAppearanceModel implements ShapeAppearance {
  private static final int INITIAL_CAPACITY = 10;

  /** Builder for {@link StateListShapeAppearanceModel}. */
  public static final class Builder {
    private int stateCount;
    @NonNull private ShapeAppearanceModel defaultShape;
    @NonNull private int[][] stateSpecs;
    @NonNull private ShapeAppearanceModel[] shapeAppearanceModels;

    @Nullable private StateListCornerSize topLeftCornerSizeOverride;
    @Nullable private StateListCornerSize topRightCornerSizeOverride;
    @Nullable private StateListCornerSize bottomLeftCornerSizeOverride;
    @Nullable private StateListCornerSize bottomRightCornerSizeOverride;

    public Builder(@NonNull StateListShapeAppearanceModel other) {
      this.stateCount = other.stateCount;
      this.defaultShape = other.defaultShape;
      this.stateSpecs = new int[other.stateSpecs.length][];
      this.shapeAppearanceModels = new ShapeAppearanceModel[other.shapeAppearanceModels.length];
      System.arraycopy(other.stateSpecs, 0, this.stateSpecs, 0, stateCount);
      System.arraycopy(other.shapeAppearanceModels, 0, this.shapeAppearanceModels, 0, stateCount);

      this.topLeftCornerSizeOverride = other.topLeftCornerSizeOverride;
      this.topRightCornerSizeOverride = other.topRightCornerSizeOverride;
      this.bottomLeftCornerSizeOverride = other.bottomLeftCornerSizeOverride;
      this.bottomRightCornerSizeOverride = other.bottomRightCornerSizeOverride;
    }

    public Builder(@NonNull ShapeAppearanceModel shapeAppearanceModel) {
      initialize();
      addStateShapeAppearanceModel(StateSet.WILD_CARD, shapeAppearanceModel);
    }

    private Builder(@NonNull Context context, @XmlRes int index) {
      initialize();
      try (XmlResourceParser parser = context.getResources().getXml(index)) {
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
          loadShapeAppearanceModelsFromItems(this, context, parser, attrs, context.getTheme());
        }
      } catch (XmlPullParserException | IOException | Resources.NotFoundException e) {
        // Re-initializes the fields if failed.
        initialize();
      }
    }

    private void initialize() {
      this.defaultShape = new ShapeAppearanceModel();
      this.stateSpecs = new int[INITIAL_CAPACITY][];
      this.shapeAppearanceModels = new ShapeAppearanceModel[INITIAL_CAPACITY];
    }

    @NonNull
    @CanIgnoreReturnValue
    public Builder setCornerSizeOverride(
        @NonNull StateListCornerSize cornerSizeOverride, int cornerPositionSet) {
      if (containsFlag(cornerPositionSet, CORNER_TOP_LEFT)) {
        topLeftCornerSizeOverride = cornerSizeOverride;
      }
      if (containsFlag(cornerPositionSet, CORNER_TOP_RIGHT)) {
        topRightCornerSizeOverride = cornerSizeOverride;
      }
      if (containsFlag(cornerPositionSet, CORNER_BOTTOM_LEFT)) {
        bottomLeftCornerSizeOverride = cornerSizeOverride;
      }
      if (containsFlag(cornerPositionSet, CORNER_BOTTOM_RIGHT)) {
        bottomRightCornerSizeOverride = cornerSizeOverride;
      }
      return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public Builder addStateShapeAppearanceModel(
        @NonNull int[] stateSpec, @NonNull ShapeAppearanceModel shapeAppearanceModel) {
      if (stateCount == 0 || stateSpec.length == 0) {
        defaultShape = shapeAppearanceModel;
      }
      if (stateCount >= stateSpecs.length) {
        growArray(stateCount, stateCount + 10);
      }
      stateSpecs[stateCount] = stateSpec;
      shapeAppearanceModels[stateCount] = shapeAppearanceModel;
      stateCount++;
      return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public Builder withTransformedCornerSizes(@NonNull CornerSizeUnaryOperator op) {
      ShapeAppearanceModel[] newShapeAppearanceModels =
          new ShapeAppearanceModel[shapeAppearanceModels.length];
      for (int i = 0; i < stateCount; i++) {
        newShapeAppearanceModels[i] = shapeAppearanceModels[i].withTransformedCornerSizes(op);
      }
      shapeAppearanceModels = newShapeAppearanceModels;
      if (topLeftCornerSizeOverride != null) {
        topLeftCornerSizeOverride = topLeftCornerSizeOverride.withTransformedCornerSizes(op);
      }
      if (topRightCornerSizeOverride != null) {
        topRightCornerSizeOverride = topRightCornerSizeOverride.withTransformedCornerSizes(op);
      }
      if (bottomLeftCornerSizeOverride != null) {
        bottomLeftCornerSizeOverride = bottomLeftCornerSizeOverride.withTransformedCornerSizes(op);
      }
      if (bottomRightCornerSizeOverride != null) {
        bottomRightCornerSizeOverride =
            bottomRightCornerSizeOverride.withTransformedCornerSizes(op);
      }
      return this;
    }

    private void growArray(int oldSize, int newSize) {
      int[][] newStateSpecs = new int[newSize][];
      System.arraycopy(stateSpecs, 0, newStateSpecs, 0, oldSize);
      stateSpecs = newStateSpecs;
      ShapeAppearanceModel[] newShapeAppearanceModels = new ShapeAppearanceModel[newSize];
      System.arraycopy(shapeAppearanceModels, 0, newShapeAppearanceModels, 0, oldSize);
      shapeAppearanceModels = newShapeAppearanceModels;
    }

    @Nullable
    public StateListShapeAppearanceModel build() {
      return stateCount == 0 ? null : new StateListShapeAppearanceModel(this);
    }
  }

  @Nullable
  public static StateListShapeAppearanceModel create(
      @NonNull Context context, @NonNull TypedArray attributes, @StyleableRes int index) {
    int resourceId = attributes.getResourceId(index, 0);
    if (resourceId == ID_NULL) {
      return null;
    }
    String typeName = context.getResources().getResourceTypeName(resourceId);
    if (!Objects.equals(typeName, "xml")) {
      return null;
    }
    return new Builder(context, resourceId).build();
  }

  private static void loadShapeAppearanceModelsFromItems(
      @NonNull Builder builder,
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
              ? res.obtainAttributes(attrs, R.styleable.MaterialShape)
              : theme.obtainStyledAttributes(attrs, R.styleable.MaterialShape, 0, 0);

      final int shapeAppearanceId = a.getResourceId(R.styleable.MaterialShape_shapeAppearance, 0);
      final int shapeAppearanceOverlayId =
          a.getResourceId(R.styleable.MaterialShape_shapeAppearanceOverlay, 0);
      final ShapeAppearanceModel shapeAppearanceModel =
          ShapeAppearanceModel.builder(context, shapeAppearanceId, shapeAppearanceOverlayId)
              .build();
      a.recycle();

      // Parse all unrecognized attributes as state specifiers.
      int j = 0;
      final int numAttrs = attrs.getAttributeCount();
      int[] stateSpec = new int[numAttrs];
      for (int i = 0; i < numAttrs; i++) {
        final int stateResId = attrs.getAttributeNameResource(i);
        if (stateResId != R.attr.shapeAppearance && stateResId != R.attr.shapeAppearanceOverlay) {
          stateSpec[j++] = attrs.getAttributeBooleanValue(i, false) ? stateResId : -stateResId;
        }
      }
      stateSpec = StateSet.trimStateSet(stateSpec, j);
      builder.addStateShapeAppearanceModel(stateSpec, shapeAppearanceModel);
    }
  }

  final int stateCount;
  @NonNull final ShapeAppearanceModel defaultShape;
  @NonNull final int[][] stateSpecs;

  @NonNull final ShapeAppearanceModel[] shapeAppearanceModels;

  @Nullable final StateListCornerSize topLeftCornerSizeOverride;
  @Nullable final StateListCornerSize topRightCornerSizeOverride;
  @Nullable final StateListCornerSize bottomLeftCornerSizeOverride;
  @Nullable final StateListCornerSize bottomRightCornerSizeOverride;

  private StateListShapeAppearanceModel(@NonNull Builder builder) {
    stateCount = builder.stateCount;
    defaultShape = builder.defaultShape;
    stateSpecs = builder.stateSpecs;
    shapeAppearanceModels = builder.shapeAppearanceModels;

    topLeftCornerSizeOverride = builder.topLeftCornerSizeOverride;
    topRightCornerSizeOverride = builder.topRightCornerSizeOverride;
    bottomLeftCornerSizeOverride = builder.bottomLeftCornerSizeOverride;
    bottomRightCornerSizeOverride = builder.bottomRightCornerSizeOverride;
  }

  public int getStateCount() {
    return stateCount;
  }

  @NonNull
  @Override
  public ShapeAppearanceModel getDefaultShape() {
    return getDefaultShape(/* withCornerSizeOverrides= */ true);
  }

  @NonNull
  public ShapeAppearanceModel getDefaultShape(boolean withCornerSizeOverrides) {
    if (!withCornerSizeOverrides
        || (topLeftCornerSizeOverride == null
            && topRightCornerSizeOverride == null
            && bottomLeftCornerSizeOverride == null
            && bottomRightCornerSizeOverride == null)) {
      return defaultShape;
    }
    ShapeAppearanceModel.Builder builder = defaultShape.toBuilder();
    if (topLeftCornerSizeOverride != null) {
      builder.setTopLeftCornerSize(topLeftCornerSizeOverride.getDefaultCornerSize());
    }
    if (topRightCornerSizeOverride != null) {
      builder.setTopRightCornerSize(topRightCornerSizeOverride.getDefaultCornerSize());
    }
    if (bottomLeftCornerSizeOverride != null) {
      builder.setBottomLeftCornerSize(bottomLeftCornerSizeOverride.getDefaultCornerSize());
    }
    if (bottomRightCornerSizeOverride != null) {
      builder.setBottomRightCornerSize(bottomRightCornerSizeOverride.getDefaultCornerSize());
    }
    return builder.build();
  }

  @NonNull
  @Override
  public ShapeAppearanceModel getShapeForState(@NonNull int[] stateSet) {
    int idx = indexOfStateSet(stateSet);
    if (idx < 0) {
      idx = indexOfStateSet(StateSet.WILD_CARD);
    }
    if (topLeftCornerSizeOverride == null
        && topRightCornerSizeOverride == null
        && bottomLeftCornerSizeOverride == null
        && bottomRightCornerSizeOverride == null) {
      return shapeAppearanceModels[idx];
    }
    ShapeAppearanceModel.Builder builder = shapeAppearanceModels[idx].toBuilder();
    if (topLeftCornerSizeOverride != null) {
      builder.setTopLeftCornerSize(topLeftCornerSizeOverride.getCornerSizeForState(stateSet));
    }
    if (topRightCornerSizeOverride != null) {
      builder.setTopRightCornerSize(topRightCornerSizeOverride.getCornerSizeForState(stateSet));
    }
    if (bottomLeftCornerSizeOverride != null) {
      builder.setBottomLeftCornerSize(bottomLeftCornerSizeOverride.getCornerSizeForState(stateSet));
    }
    if (bottomRightCornerSizeOverride != null) {
      builder.setBottomRightCornerSize(
          bottomRightCornerSizeOverride.getCornerSizeForState(stateSet));
    }
    return builder.build();
  }

  @NonNull
  @Override
  public ShapeAppearanceModel[] getShapeAppearanceModels() {
    return shapeAppearanceModels;
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

  @NonNull
  public StateListShapeAppearanceModel withTransformedCornerSizes(
      @NonNull CornerSizeUnaryOperator op) {
    return toBuilder().withTransformedCornerSizes(op).build();
  }

  @NonNull
  public Builder toBuilder() {
    return new Builder(this);
  }

  @NonNull
  @Override
  public ShapeAppearanceModel withCornerSize(float cornerSize) {
    // If withCornerSize is called on a StateListAppearanceModel, return a stateless
    // ShapeAppearanceModel with the given corner size.
    return getDefaultShape().withCornerSize(cornerSize);
  }

  @NonNull
  @Override
  public ShapeAppearanceModel withCornerSize(@NonNull CornerSize cornerSize) {
    // If withCornerSize is called on a StateListAppearanceModel, return a stateless
    // ShapeAppearanceModel with the given corner size.
    return getDefaultShape().withCornerSize(cornerSize);
  }

  @Override
  public boolean isStateful() {
    return stateCount > 1
        || (topLeftCornerSizeOverride != null && topLeftCornerSizeOverride.isStateful())
        || (topRightCornerSizeOverride != null && topRightCornerSizeOverride.isStateful())
        || (bottomLeftCornerSizeOverride != null && bottomLeftCornerSizeOverride.isStateful())
        || (bottomRightCornerSizeOverride != null && bottomRightCornerSizeOverride.isStateful());
  }

  public static int swapCornerPositionRtl(int flagSet) {
    int leftFlagSet = flagSet & (CORNER_TOP_LEFT | CORNER_BOTTOM_LEFT);
    int rightFlagSet = flagSet & (CORNER_TOP_RIGHT | CORNER_BOTTOM_RIGHT);
    return leftFlagSet << 1 | rightFlagSet >> 1;
  }
}
