/*
 * Copyright (C) 2026 The Android Open Source Project
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
package com.google.android.material.focus;

import com.google.android.material.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.util.FloatProperty;
import android.util.StateSet;
import android.util.TypedValue;
import android.view.animation.OvershootInterpolator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StyleableRes;
import com.google.android.material.resources.MaterialAttributes;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearance;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.ShapeAppearancePathProvider;
import com.google.android.material.shape.StateListShapeAppearanceModel;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/** Drawable wrapper that provides focus rings. */
public class FocusRingDrawable extends DrawableWrapper {

  private static final boolean DEBUG_COLORS = false;
  private static final Drawable EMPTY_DRAWABLE = new ColorDrawable(Color.TRANSPARENT);
  private static final int[] FOCUSED_STATE_SET = {android.R.attr.state_focused};

  private static final TimeInterpolator INTERPOLATOR = new OvershootInterpolator(4f);
  private static final int ANIMATION_DURATION = 300;

  @RequiresApi(VERSION_CODES.N)
  private static final FloatProperty<FocusRingDrawable> PROPERTY_INTERPOLATION =
      new FloatProperty<FocusRingDrawable>("interpolation") {
        @Override
        public void setValue(FocusRingDrawable drawable, float value) {
          drawable.interpolation = value;
          drawable.invalidateSelf();
        }

        @Override
        public Float get(FocusRingDrawable drawable) {
          return drawable.interpolation;
        }
      };

  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RectF tmpRectF = new RectF();
  private final Rect tmpRect = new Rect();
  private final Path tmpPath = new Path();
  private final Path shapeAppearancePath = new Path();
  private final Matrix matrix = new Matrix();
  private final ShapeAppearancePathProvider pathProvider =
      ShapeAppearancePathProvider.getInstanceOrCreate();

  @Nullable private WeakReference<MaterialShapeDrawable> materialShapeDrawable;
  private float shapeAppearanceCornerSize = -1;
  @Nullable private ObjectAnimator animator;
  private float interpolation = 1f;
  private boolean previousStateSetEmpty;
  private boolean focused = false;
  private boolean mutated = false;

  @NonNull private FocusRingState state;

  /**
   * Wrap the provided drawable with a FocusRingDrawable if focus rings are enabled, otherwise just
   * return the provided drawable.
   *
   * <p>This method of integrating focus rings should be preferred if the drawable is an unbounded
   * ripple drawable (has no layers), because wrapping the ripple will preserve the unbounded-ness.
   * See {@link #layer(Context, LayerDrawable, MaterialShapeDrawable)} for an alternative method
   * where the focus ring can be added as a layer instead of wrapping.
   */
  @Nullable
  public static Drawable wrap(@NonNull Context context, @Nullable Drawable drawable) {
    if (!shouldUseFocusRing(context)) {
      return drawable;
    }
    return new FocusRingDrawable(context, drawable);
  }

  /**
   * Add a FocusRingDrawable as a layer to the provided LayerDrawable if focus rings are enabled,
   * otherwise do nothing and return null.
   *
   * <p>See {@link #layer(Context, LayerDrawable, MaterialShapeDrawable)}.
   */
  @CanIgnoreReturnValue
  @Nullable
  public static FocusRingDrawable layer(
      @NonNull Context context, @NonNull LayerDrawable layerDrawable) {
    return layer(context, layerDrawable, null);
  }

  /**
   * Add a FocusRingDrawable as a layer to the provided LayerDrawable if focus rings are enabled,
   * otherwise do nothing and return null.
   *
   * <p>This method of integrating focus rings should be preferred if the drawable is a bounded
   * ripple drawable (has layers), because we can add the focus ring as a layer without affecting
   * the ripple bounds. See {@link #wrap(Context, Drawable)} for an alternative method where the
   * focus ring can wrap the provided drawable instead of being added as a layer.
   *
   * <p>The MaterialShapeDrawable will be used to draw the focus ring outline shape and bounds.
   */
  @CanIgnoreReturnValue
  @Nullable
  public static FocusRingDrawable layer(
      @NonNull Context context,
      @NonNull LayerDrawable layerDrawable,
      @Nullable MaterialShapeDrawable materialShapeDrawable) {
    if (!shouldUseFocusRing(context)) {
      return null;
    }

    FocusRingDrawable focusRingDrawable = new FocusRingDrawable(context, EMPTY_DRAWABLE);
    if (materialShapeDrawable != null) {
      focusRingDrawable.setFocusRingMaterialShapeDrawable(materialShapeDrawable);
    }
    layerDrawable.addLayer(focusRingDrawable);
    // Needed when the FocusRingDrawable is not the view's overall background, to ensure that
    // invalidateSelf() calls during the animation work.
    focusRingDrawable.setCallback(layerDrawable);
    return focusRingDrawable;
  }

  private static boolean shouldUseFocusRing(@NonNull Context context) {
    // Only add focus rings on API Level 24 and above (to be consistent with XML drawables which
    // require drawable inflation) and if focus rings are enabled in the theme.
    return VERSION.SDK_INT >= VERSION_CODES.N
        && MaterialAttributes.resolveBoolean(context.getTheme(), R.attr.focusRingsEnabled, false);
  }

  /**
   * Search for and return a FocusRingDrawable if it is either the provided drawable or one level
   * deep in a DrawableWrapper or LayerDrawable.
   *
   * <p>This is useful for finding a FocusRingDrawable that has been set up with either {@link
   * #wrap(Context, Drawable)} or {@link #layer(Context, LayerDrawable, MaterialShapeDrawable)}, as
   * well as the equivalent XML drawable configurations.
   */
  @Nullable
  public static FocusRingDrawable find(@Nullable Drawable drawable) {
    if (drawable instanceof FocusRingDrawable) {
      return (FocusRingDrawable) drawable;
    }
    if (drawable instanceof DrawableWrapper) {
      Drawable inner = ((DrawableWrapper) drawable).getDrawable();
      if (inner instanceof FocusRingDrawable) {
        return (FocusRingDrawable) inner;
      }
    }
    if (drawable instanceof LayerDrawable) {
      LayerDrawable layerDrawable = (LayerDrawable) drawable;
      for (int i = 0; i < layerDrawable.getNumberOfLayers(); i++) {
        Drawable layer = layerDrawable.getDrawable(i);
        if (layer instanceof FocusRingDrawable) {
          return (FocusRingDrawable) layer;
        }
      }
    }
    return null;
  }

  public FocusRingDrawable() {
    super(null);
    state = new FocusRingState(null);
  }

  public FocusRingDrawable(@NonNull Context context, @Nullable Drawable drawable) {
    super(drawable);

    state = new FocusRingState(null);
    if (drawable != null) {
      state.wrappedState = drawable.getConstantState();
    }

    init(context.getTheme());
  }

  private FocusRingDrawable(@NonNull FocusRingState state, @Nullable Resources resources) {
    super(null);
    this.state = new FocusRingState(state);

    if (this.state.wrappedState != null) {
      Drawable wrappedDrawable;
      if (resources != null) {
        wrappedDrawable = this.state.wrappedState.newDrawable(resources);
      } else {
        wrappedDrawable = this.state.wrappedState.newDrawable();
      }
      setDrawable(wrappedDrawable);
    }

    updateLocalState();
  }

  @Override
  public boolean canApplyTheme() {
    return true;
  }

  @Override
  public void applyTheme(@NonNull Theme theme) {
    super.applyTheme(theme);

    init(theme);
  }

  @Override
  public void inflate(
      @NonNull Resources res, @NonNull XmlPullParser parser, @NonNull AttributeSet attrs)
      throws IOException, XmlPullParserException {
    inflate(res, parser, attrs, null);
  }

  @Override
  public void inflate(
      @NonNull Resources res,
      @NonNull XmlPullParser parser,
      @NonNull AttributeSet attrs,
      @Nullable Theme theme)
      throws IOException, XmlPullParserException {
    super.inflate(res, parser, attrs, theme);

    TypedArray a;
    if (theme != null) {
      a = theme.obtainStyledAttributes(attrs, R.styleable.FocusRingDrawable, 0, 0);
    } else {
      a = res.obtainAttributes(attrs, R.styleable.FocusRingDrawable);
    }
    updateStateFromTypedArrayWithoutThemeAttrsOrDefaults(a);
    a.recycle();

    inflateChildDrawable(res, parser, attrs, theme);
  }

  private void updateStateFromTypedArrayWithoutThemeAttrsOrDefaults(@NonNull TypedArray a) {
    state.ringEnabledAttr = getValueDataIfAttr(a, R.styleable.FocusRingDrawable_focusRingsEnabled);
    if (state.ringEnabledAttr == Integer.MIN_VALUE
        && a.hasValue(R.styleable.FocusRingDrawable_focusRingsEnabled)) {
      state.ringEnabled =
          a.getBoolean(R.styleable.FocusRingDrawable_focusRingsEnabled, state.ringEnabled);
      state.ringEnabledInflated = true;
    }

    state.ringOuterColorAttr =
        getValueDataIfAttr(a, R.styleable.FocusRingDrawable_focusRingsOuterStrokeColor);
    if (state.ringOuterColorAttr == Integer.MIN_VALUE) {
      state.ringOuterColor =
          a.getColor(R.styleable.FocusRingDrawable_focusRingsOuterStrokeColor, Integer.MIN_VALUE);
    }

    state.ringInnerColorAttr =
        getValueDataIfAttr(a, R.styleable.FocusRingDrawable_focusRingsInnerStrokeColor);
    if (state.ringInnerColorAttr == Integer.MIN_VALUE) {
      state.ringInnerColor =
          a.getColor(R.styleable.FocusRingDrawable_focusRingsInnerStrokeColor, Integer.MIN_VALUE);
    }

    state.ringOuterStrokeWidthAttr =
        getValueDataIfAttr(a, R.styleable.FocusRingDrawable_focusRingsOuterStrokeWidth);
    if (state.ringOuterStrokeWidthAttr == Integer.MIN_VALUE) {
      state.ringOuterStrokeWidth =
          a.getDimension(R.styleable.FocusRingDrawable_focusRingsOuterStrokeWidth, Float.NaN);
    }

    state.ringInnerStrokeWidthAttr =
        getValueDataIfAttr(a, R.styleable.FocusRingDrawable_focusRingsInnerStrokeWidth);
    if (state.ringInnerStrokeWidthAttr == Integer.MIN_VALUE) {
      state.ringInnerStrokeWidth =
          a.getDimension(R.styleable.FocusRingDrawable_focusRingsInnerStrokeWidth, Float.NaN);
    }

    state.ringInnerStrokeWidthAttr =
        getValueDataIfAttr(a, R.styleable.FocusRingDrawable_focusRingsInnerStrokeWidth);
    if (state.ringInnerStrokeWidthAttr == Integer.MIN_VALUE) {
      state.ringInnerStrokeWidth =
          a.getDimension(R.styleable.FocusRingDrawable_focusRingsInnerStrokeWidth, Float.NaN);
    }

    state.ringRadiusAttr = getValueDataIfAttr(a, R.styleable.FocusRingDrawable_focusRingsRadius);
    if (state.ringRadiusAttr == Integer.MIN_VALUE) {
      state.ringRadius = a.getDimension(R.styleable.FocusRingDrawable_focusRingsRadius, Float.NaN);
    }

    state.ringInsetAttr = getValueDataIfAttr(a, R.styleable.FocusRingDrawable_focusRingsInset);
    if (state.ringInsetAttr == Integer.MIN_VALUE) {
      state.ringInset = a.getDimension(R.styleable.FocusRingDrawable_focusRingsInset, Float.NaN);
    }

    state.ringInnerInsetAttr =
        getValueDataIfAttr(a, R.styleable.FocusRingDrawable_focusRingsInnerStrokeInset);
    if (state.ringInnerInsetAttr == Integer.MIN_VALUE) {
      state.ringInnerInset =
          a.getDimension(R.styleable.FocusRingDrawable_focusRingsInnerStrokeInset, Float.NaN);
    }

    state.ringShapeAppearanceAttr =
        getValueDataIfAttr(a, R.styleable.FocusRingDrawable_focusRingsShapeAppearance);
    state.ringShapeAppearanceResId =
        getResIdIfReference(a, R.styleable.FocusRingDrawable_focusRingsShapeAppearance);
  }

  private void updateStateFromTypedArrayWithThemeAttrsAndDefaults(
      @NonNull TypedArray a, @NonNull Theme theme) {
    Resources res = theme.getResources();

    if (state.ringEnabledAttr != Integer.MIN_VALUE) {
      TypedValue typedValue = MaterialAttributes.resolve(theme, state.ringEnabledAttr);
      if (typedValue != null) {
        state.ringEnabled = typedValue.data != 0;
        state.ringEnabledInflated = true;
      }
    }
    if (!state.ringEnabledInflated) {
      state.ringEnabled =
          MaterialAttributes.resolveBoolean(theme, R.attr.focusRingsEnabled, state.ringEnabled);
    }
    if (!state.ringEnabled) {
      return;
    }

    state.ringOuterColor =
        maybeResolveColor(
            state.ringOuterColor,
            theme,
            state.ringOuterColorAttr,
            a,
            R.styleable.FocusRingDrawable_focusRingsOuterStrokeColor,
            Color.BLACK);

    state.ringInnerColor =
        maybeResolveColor(
            state.ringInnerColor,
            theme,
            state.ringInnerColorAttr,
            a,
            R.styleable.FocusRingDrawable_focusRingsInnerStrokeColor,
            Color.WHITE);

    float defaultStrokeWidth =
        res.getDimensionPixelSize(R.dimen.mtrl_focus_ring_outer_stroke_width);

    state.ringOuterStrokeWidth =
        maybeResolveDimension(
            state.ringOuterStrokeWidth,
            theme,
            state.ringOuterStrokeWidthAttr,
            a,
            R.styleable.FocusRingDrawable_focusRingsOuterStrokeWidth,
            defaultStrokeWidth);

    state.ringInnerStrokeWidth =
        maybeResolveDimension(
            state.ringInnerStrokeWidth,
            theme,
            state.ringInnerStrokeWidthAttr,
            a,
            R.styleable.FocusRingDrawable_focusRingsInnerStrokeWidth,
            defaultStrokeWidth);

    state.ringRadius =
        maybeResolveDimension(
            state.ringRadius,
            theme,
            state.ringRadiusAttr,
            a,
            R.styleable.FocusRingDrawable_focusRingsRadius,
            Float.NaN);

    state.ringInset =
        maybeResolveDimension(
            state.ringInset,
            theme,
            state.ringInsetAttr,
            a,
            R.styleable.FocusRingDrawable_focusRingsInset,
            0f);

    state.ringInnerInset =
        maybeResolveDimension(
            state.ringInnerInset,
            theme,
            state.ringInnerInsetAttr,
            a,
            R.styleable.FocusRingDrawable_focusRingsInnerStrokeInset,
            0f);

    if (state.ringShapeAppearanceResId != Integer.MIN_VALUE) {
      state.ringShapeAppearance =
          ShapeAppearanceModel.builder(theme, state.ringShapeAppearanceResId).build();
    } else {
      int shapeAppearanceAttr =
          state.ringShapeAppearanceAttr != Integer.MIN_VALUE
              ? state.ringShapeAppearanceAttr
              : R.attr.focusRingsShapeAppearance;
      TypedValue typedValue = MaterialAttributes.resolve(theme, shapeAppearanceAttr);
      if (typedValue != null) {
        state.ringShapeAppearance =
            ShapeAppearanceModel.builder(theme, typedValue.resourceId).build();
      }
    }

    if (DEBUG_COLORS) {
      state.ringOuterColor = Color.RED;
      state.ringInnerColor = Color.GREEN;
    }
  }

  private int getValueDataIfAttr(TypedArray a, @StyleableRes int index) {
    if (a.getType(index) == TypedValue.TYPE_ATTRIBUTE) {
      TypedValue value = new TypedValue();
      if (a.getValue(index, value)) {
        return value.data;
      }
    }
    return Integer.MIN_VALUE;
  }

  private int getResIdIfReference(TypedArray a, @StyleableRes int index) {
    if (a.getType(index) == TypedValue.TYPE_REFERENCE) {
      return a.getResourceId(index, Integer.MIN_VALUE);
    }
    return Integer.MIN_VALUE;
  }

  private int maybeResolveColor(
      int currentValue,
      @NonNull Theme theme,
      @StyleableRes int attrIndex,
      @NonNull TypedArray a,
      @StyleableRes int regularIndex,
      int defaultValue) {
    if (currentValue != Integer.MIN_VALUE) {
      return currentValue;
    }
    if (attrIndex != Integer.MIN_VALUE) {
      TypedValue value = new TypedValue();
      if (theme.resolveAttribute(attrIndex, value, true)) {
        return value.data;
      }
    }
    return a.getColor(regularIndex, defaultValue);
  }

  private float maybeResolveDimension(
      float currentValue,
      @NonNull Theme theme,
      @StyleableRes int attrIndex,
      @NonNull TypedArray a,
      @StyleableRes int regularIndex,
      float defaultValue) {
    if (!Float.isNaN(currentValue)) {
      return currentValue;
    }
    if (attrIndex != Float.MIN_VALUE) {
      TypedValue value = new TypedValue();
      if (theme.resolveAttribute(attrIndex, value, true)) {
        return value.getDimension(theme.getResources().getDisplayMetrics());
      }
    }
    return a.getDimension(regularIndex, defaultValue);
  }

  private void inflateChildDrawable(
      @NonNull Resources res,
      @NonNull XmlPullParser parser,
      @NonNull AttributeSet attrs,
      @Nullable Theme theme)
      throws XmlPullParserException, IOException {
    Drawable drawable = null;
    int type;
    final int outerDepth = parser.getDepth();
    while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
        && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
      if (type == XmlPullParser.START_TAG) {
        drawable = Drawable.createFromXmlInner(res, parser, attrs, theme);
      }
    }

    if (drawable != null) {
      setDrawable(drawable);
      state.wrappedState = drawable.getConstantState();
    } else {
      setDrawable(EMPTY_DRAWABLE);
      state.wrappedState = EMPTY_DRAWABLE.getConstantState();
    }
  }

  private void init(@NonNull Theme theme) {
    if (VERSION.SDK_INT < VERSION_CODES.N) {
      // Don't support focus rings before API Level 24, to be consistent with XML which lacks
      // custom drawable class inflation.
      return;
    }

    TypedArray a = theme.obtainStyledAttributes(R.styleable.FocusRingDrawable);
    updateStateFromTypedArrayWithThemeAttrsAndDefaults(a, theme);
    a.recycle();

    updateLocalState();
  }

  private void updateLocalState() {
    paint.setStyle(Style.STROKE);
    if (!Float.isNaN(state.ringOuterStrokeWidth)) {
      paint.setStrokeWidth(state.ringOuterStrokeWidth);
    }
  }

  @Override
  protected void onBoundsChange(Rect bounds) {
    super.onBoundsChange(bounds);

    if (!state.ringEnabled) {
      return;
    }

    calculateShapeAppearanceRoundRectOrPath();
  }

  @RequiresApi(api = VERSION_CODES.Q)
  @Override
  public boolean isProjected() {
    Drawable drawable = getDrawable();
    return drawable != null && drawable.isProjected();
  }

  @Override
  protected boolean onStateChange(@NonNull int[] stateSet) {
    if (!state.ringEnabled) {
      this.focused = false;
      return super.onStateChange(stateSet);
    }
    boolean focused = StateSet.stateSetMatches(state.ringStateSet, stateSet);
    boolean changed = this.focused != focused;
    this.focused = focused;

    // Don't cancel or start the animation if the current or previous state set is / was empty. This
    // is a workaround for MaterialButton which strangely has empty state sets come through on focus
    // and press, which causes the focus ring animation to be played multiple times.
    if (changed && stateSet.length > 0 && !previousStateSetEmpty) {
      maybeAnimate(focused);
    }

    previousStateSetEmpty = stateSet.length == 0;

    return super.onStateChange(stateSet) || changed;
  }

  private void maybeAnimate(boolean focused) {
    if (animator != null) {
      animator.cancel();
      animator = null;
    }
    if (focused) {
      if (VERSION.SDK_INT >= VERSION_CODES.N) {
        animator = createAnimator();
        animator.start();
      }
    } else {
      interpolation = 1f;
    }
  }

  @Override
  public void jumpToCurrentState() {
    super.jumpToCurrentState();

    if (animator != null) {
      animator.end();
      animator = null;
    }
  }

  @Override
  public boolean isStateful() {
    return super.isStateful() || state.ringEnabled;
  }

  @Override
  public boolean hasFocusStateSpecified() {
    // Handle strange test failures related to super.hasFocusStateSpecified() not being found...
    // Can't do a version check because somehow the hasFocusStateSpecified() method seems to be
    // called on older devices than what the docs say, so we have to try calling
    // super.hasFocusStateSpecified() whenever possible to preserve the behavior.
    try {
      return super.hasFocusStateSpecified() || state.ringEnabled;
    } catch (NoSuchMethodError e) {
      return state.ringEnabled;
    }
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    super.draw(canvas);

    if (!state.ringEnabled || !focused) {
      return;
    }

    float outerInset = calculateOuterInset();
    float innerInset = calculateInnerInset();

    Path path = getNonEmptyPath();
    if (path != null) {
      drawPath(canvas, path, innerInset, state.ringInnerStrokeWidth, state.ringInnerColor);
      drawPath(canvas, path, outerInset, state.ringOuterStrokeWidth, state.ringOuterColor);
    } else {
      float outerRadius = calculateOuterRadius();
      float innerRadius = calculateInnerRadius(outerRadius);

      drawRoundRect(
          canvas, innerRadius, innerInset, state.ringInnerStrokeWidth, state.ringInnerColor);
      drawRoundRect(
          canvas, outerRadius, outerInset, state.ringOuterStrokeWidth, state.ringOuterColor);
    }
  }

  @Nullable
  private Path getNonEmptyPath() {
    if (!shapeAppearancePath.isEmpty()) {
      return shapeAppearancePath;
    }
    if (materialShapeDrawable != null && materialShapeDrawable.get() != null) {
      Path path = materialShapeDrawable.get().getPath();
      if (!path.isEmpty()) {
        return path;
      }
    }
    return null;
  }

  private void drawPath(Canvas canvas, Path path, float inset, float strokeWidth, int color) {
    calculateBounds(tmpRectF);
    float scaleX = 1 - (inset * 2 / tmpRectF.width());
    float scaleY = 1 - (inset * 2 / tmpRectF.height());
    matrix.reset();
    matrix.postScale(scaleX, scaleY, tmpRectF.centerX(), tmpRectF.centerY());
    path.transform(matrix, tmpPath);

    paint.setStrokeWidth(strokeWidth * interpolation);
    paint.setColor(color);
    canvas.drawPath(tmpPath, paint);
  }

  private void drawRoundRect(
      Canvas canvas, float radius, float inset, float strokeWidth, int color) {
    calculateBounds(tmpRectF);
    tmpRectF.inset(inset, inset);

    paint.setStrokeWidth(strokeWidth * interpolation);
    paint.setColor(color);
    canvas.drawRoundRect(tmpRectF, radius, radius, paint);
  }

  public boolean isFocusRingEnabled() {
    return state.ringEnabled;
  }

  public void setFocusRingEnabled(boolean enabled) {
    state.ringEnabled = enabled;
  }

  @Nullable
  public MaterialShapeDrawable getFocusRingMaterialShapeDrawable() {
    return materialShapeDrawable != null ? materialShapeDrawable.get() : null;
  }

  public void setFocusRingMaterialShapeDrawable(
      @Nullable MaterialShapeDrawable materialShapeDrawable) {
    this.materialShapeDrawable = new WeakReference<>(materialShapeDrawable);
  }

  @Nullable
  public ShapeAppearance getFocusRingShapeAppearance() {
    return state.ringShapeAppearance;
  }

  public void setFocusRingShapeAppearance(@Nullable ShapeAppearance shapeAppearance) {
    state.ringShapeAppearance = shapeAppearance;
  }

  @Nullable
  public Rect getFocusRingBounds() {
    return state.ringCustomBounds;
  }

  public void setFocusRingBounds(@Nullable Rect bounds) {
    state.ringCustomBounds = bounds;
  }

  public void setFocusRingBounds(int left, int top, int right, int bottom) {
    if (state.ringCustomBounds == null) {
      state.ringCustomBounds = new Rect();
    }
    state.ringCustomBounds.set(left, top, right, bottom);
  }

  @NonNull
  public int[] getFocusRingStateSet() {
    return state.ringStateSet;
  }

  public void setFocusRingStateSet(@NonNull int[] stateSet) {
    state.ringStateSet = stateSet;
  }

  private void calculateBounds(RectF rectF) {
    if (state.ringCustomBounds != null) {
      rectF.set(state.ringCustomBounds);
    } else if (materialShapeDrawable != null && materialShapeDrawable.get() != null) {
      rectF.set(materialShapeDrawable.get().getBounds());
    } else if (getDrawable() instanceof RippleDrawable) {
      RippleDrawable rippleDrawable = (RippleDrawable) getDrawable();
      rippleDrawable.getHotspotBounds(tmpRect);
      int radius = rippleDrawable.getRadius();
      if (radius > 0) {
        int insetHorizontal = Math.max(0, tmpRect.width() / 2 - radius);
        int insetVertical = Math.max(0, tmpRect.height() / 2 - radius);
        tmpRect.inset(insetHorizontal, insetVertical);
      }
      rectF.set(tmpRect);
    } else {
      rectF.set(getBounds());
    }
  }

  private float calculateOuterInset() {
    return state.ringInset + state.ringOuterStrokeWidth / 2f * interpolation;
  }

  private float calculateInnerInset() {
    return state.ringInset + state.ringInnerInset + state.ringInnerStrokeWidth / 2f * interpolation;
  }

  private float calculateOuterRadius() {
    if (!Float.isNaN(state.ringRadius)) {
      return state.ringRadius;
    }
    if (shapeAppearanceCornerSize >= 0) {
      return shapeAppearanceCornerSize;
    }
    if (materialShapeDrawable != null && materialShapeDrawable.get() != null) {
      float roundRectCornerSize = materialShapeDrawable.get().calculateRoundRectCornerSize();
      if (roundRectCornerSize >= 0) {
        return Math.max(0, roundRectCornerSize - state.ringOuterStrokeWidth / 2);
      }
    }
    Drawable drawable = getDrawable();
    if (drawable instanceof RippleDrawable) {
      int radius = ((RippleDrawable) drawable).getRadius();
      if (radius >= 0) {
        return radius;
      }
    }
    return 0;
  }

  private float calculateInnerRadius(float outerRadius) {
    return Math.max(0, outerRadius - state.ringOuterStrokeWidth / 2);
  }

  private void calculateShapeAppearanceRoundRectOrPath() {
    if (state.ringShapeAppearance != null) {
      calculateBounds(tmpRectF);

      ShapeAppearanceModel shapeAppearanceModel =
          state.ringShapeAppearance.getShapeForState(FOCUSED_STATE_SET);

      if (shapeAppearanceModel.isRoundRect(tmpRectF)) {
        float outerInset = calculateOuterInset();
        tmpRectF.inset(outerInset, outerInset);
        shapeAppearanceCornerSize =
            shapeAppearanceModel.getTopLeftCornerSize().getCornerSize(tmpRectF);
        shapeAppearancePath.reset();
      } else {
        pathProvider.calculatePath(
            shapeAppearanceModel, null, 1, tmpRectF, null, shapeAppearancePath);
        shapeAppearanceCornerSize = -1;
      }
    } else {
      shapeAppearanceCornerSize = -1;
      shapeAppearancePath.reset();
    }
  }

  @RequiresApi(VERSION_CODES.N)
  private ObjectAnimator createAnimator() {
    ObjectAnimator animator = ObjectAnimator.ofFloat(this, PROPERTY_INTERPOLATION, 0f, 1f);
    animator.setDuration(ANIMATION_DURATION);
    animator.setInterpolator(INTERPOLATOR);
    animator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationCancel(Animator animation) {
            super.onAnimationCancel(animation);
            interpolation = 1f;
            invalidateSelf();
          }
        });
    return animator;
  }

  @CanIgnoreReturnValue
  @NonNull
  @Override
  public Drawable mutate() {
    if (!mutated && super.mutate() == this) {
      state = new FocusRingState(state);

      Drawable drawable = getDrawable();
      if (drawable != null) {
        state.wrappedState = drawable.getConstantState();
      }
      mutated = true;
    }
    return this;
  }

  @Nullable
  @Override
  public ConstantState getConstantState() {
    if (state.canConstantState()) {
      state.mChangingConfigurations = getChangingConfigurations();
      return state;
    }
    return null;
  }

  private static final class FocusRingState extends ConstantState {
    ConstantState wrappedState;
    int mChangingConfigurations = 0;

    private boolean ringEnabled = false;
    private int ringEnabledAttr = Integer.MIN_VALUE;
    private boolean ringEnabledInflated = false;
    private int ringOuterColor = Integer.MIN_VALUE;
    private int ringOuterColorAttr = Integer.MIN_VALUE;
    private int ringInnerColor = Integer.MIN_VALUE;
    private int ringInnerColorAttr = Integer.MIN_VALUE;
    private float ringOuterStrokeWidth = Float.NaN;
    private int ringOuterStrokeWidthAttr = Integer.MIN_VALUE;
    private float ringInnerStrokeWidth = Float.NaN;
    private int ringInnerStrokeWidthAttr = Integer.MIN_VALUE;
    private float ringRadius = Float.NaN;
    private int ringRadiusAttr = Integer.MIN_VALUE;
    private float ringInset = Float.NaN;
    private int ringInsetAttr = Integer.MIN_VALUE;
    private float ringInnerInset = Float.NaN;
    private int ringInnerInsetAttr = Integer.MIN_VALUE;
    @Nullable private ShapeAppearance ringShapeAppearance = null;
    private int ringShapeAppearanceResId = Integer.MIN_VALUE;
    private int ringShapeAppearanceAttr = Integer.MIN_VALUE;
    @Nullable private Rect ringCustomBounds = null;
    @NonNull private int[] ringStateSet = FOCUSED_STATE_SET;

    FocusRingState(@Nullable FocusRingState orig) {
      if (orig != null) {
        wrappedState = orig.wrappedState;
        mChangingConfigurations = orig.mChangingConfigurations;

        this.ringEnabled = orig.ringEnabled;
        this.ringEnabledAttr = orig.ringEnabledAttr;
        this.ringEnabledInflated = orig.ringEnabledInflated;
        this.ringOuterColor = orig.ringOuterColor;
        this.ringOuterColorAttr = orig.ringOuterColorAttr;
        this.ringInnerColor = orig.ringInnerColor;
        this.ringInnerColorAttr = orig.ringInnerColorAttr;
        this.ringOuterStrokeWidth = orig.ringOuterStrokeWidth;
        this.ringOuterStrokeWidthAttr = orig.ringOuterStrokeWidthAttr;
        this.ringInnerStrokeWidth = orig.ringInnerStrokeWidth;
        this.ringInnerStrokeWidthAttr = orig.ringInnerStrokeWidthAttr;
        this.ringRadius = orig.ringRadius;
        this.ringRadiusAttr = orig.ringRadiusAttr;
        this.ringInset = orig.ringInset;
        this.ringInsetAttr = orig.ringInsetAttr;
        this.ringInnerInset = orig.ringInnerInset;
        this.ringInnerInsetAttr = orig.ringInnerInsetAttr;
        this.ringShapeAppearanceResId = orig.ringShapeAppearanceResId;
        this.ringShapeAppearanceAttr = orig.ringShapeAppearanceAttr;
        if (orig.ringShapeAppearance instanceof ShapeAppearanceModel) {
          this.ringShapeAppearance =
              ((ShapeAppearanceModel) orig.ringShapeAppearance).toBuilder().build();
        } else if (orig.ringShapeAppearance instanceof StateListShapeAppearanceModel) {
          this.ringShapeAppearance =
              ((StateListShapeAppearanceModel) orig.ringShapeAppearance).toBuilder().build();
        } else {
          this.ringShapeAppearance = orig.ringShapeAppearance;
        }
        if (orig.ringCustomBounds != null) {
          this.ringCustomBounds = new Rect(orig.ringCustomBounds);
        }
        this.ringStateSet = Arrays.copyOf(orig.ringStateSet, orig.ringStateSet.length);
      }
    }

    @NonNull
    @Override
    public Drawable newDrawable() {
      return new FocusRingDrawable(this, null);
    }

    @NonNull
    @Override
    public Drawable newDrawable(@Nullable Resources res) {
      return new FocusRingDrawable(this, res);
    }

    @Override
    public int getChangingConfigurations() {
      int wrappedChangingConfigs =
          wrappedState != null ? wrappedState.getChangingConfigurations() : 0;
      return mChangingConfigurations | wrappedChangingConfigs;
    }

    boolean canConstantState() {
      return wrappedState != null;
    }
  }
}
