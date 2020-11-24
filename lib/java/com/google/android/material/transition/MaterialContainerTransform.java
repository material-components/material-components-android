/*
 * Copyright 2019 The Android Open Source Project
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

package com.google.android.material.transition;

import com.google.android.material.R;

import static androidx.core.util.Preconditions.checkNotNull;
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.google.android.material.transition.TransitionUtils.calculateArea;
import static com.google.android.material.transition.TransitionUtils.convertToRelativeCornerSizes;
import static com.google.android.material.transition.TransitionUtils.createColorShader;
import static com.google.android.material.transition.TransitionUtils.defaultIfNull;
import static com.google.android.material.transition.TransitionUtils.findAncestorById;
import static com.google.android.material.transition.TransitionUtils.findDescendantOrAncestorById;
import static com.google.android.material.transition.TransitionUtils.getLocationOnScreen;
import static com.google.android.material.transition.TransitionUtils.getRelativeBounds;
import static com.google.android.material.transition.TransitionUtils.lerp;
import static com.google.android.material.transition.TransitionUtils.transform;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.core.view.ViewCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import androidx.transition.ArcMotion;
import androidx.transition.PathMotion;
import androidx.transition.Transition;
import androidx.transition.TransitionValues;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.Shapeable;
import com.google.android.material.transition.TransitionUtils.CanvasOperation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A shared element {@link Transition} that transforms one container to another.
 *
 * <p>MaterialContainerTransform can be used to morph between two Activities, Fragments, Views or a
 * View to a Fragment.
 *
 * <p>This transition captures a start and end View which are used to create a {@link Drawable}
 * which will be added to the view hierarchy. The drawable will be added to the view hierarchy as an
 * overlay and handles drawing a mask that morphs between the shape of the start View to the shape
 * of the end View. During the animation, the start and end View's are drawn inside the masking
 * container and faded in and/or out over a duration of the transition. Additionally, the masking
 * container will be translated and scaled from the position and size of the start View to the
 * position and size of the end View.
 *
 * <p>MaterialContainerTransform supports theme-based easing, duration, and path values. In order to
 * have the transition load these values upfront, use the {@link
 * #MaterialContainerTransform(Context, boolean)} constructor. Otherwise, use the default
 * constructor and the transition will load theme values from the View context before it runs, and
 * only use them if the corresponding properties weren't already set on the transition instance.
 *
 * <p>The composition of MaterialContainerTransform's animation can be customized in a number of
 * ways. The two most prominent customizations are the way in which content inside the container is
 * swapped via {@link #setFadeMode(int)} and path the container follows from its starting position
 * to its ending position via {@link #setPathMotion(PathMotion)}. For other ways to customize the
 * container transform, see:
 *
 * @see #setInterpolator(TimeInterpolator)
 * @see #setDuration(long)
 * @see #setStartShapeAppearanceModel(ShapeAppearanceModel)
 * @see #setEndShapeAppearanceModel(ShapeAppearanceModel)
 * @see #setDrawingViewId(int)
 * @see #setScrimColor(int)
 * @see #setFadeMode(int)
 * @see #setFitMode(int)
 * @see #setPathMotion(PathMotion)
 * @see #setFadeProgressThresholds(ProgressThresholds)
 * @see #setScaleProgressThresholds(ProgressThresholds)
 * @see #setScaleMaskProgressThresholds(ProgressThresholds)
 * @see #setShapeMaskProgressThresholds(ProgressThresholds)
 */
public final class MaterialContainerTransform extends Transition {

  /**
   * Indicates that this transition should use automatic detection to determine whether it is an
   * Enter or a Return. If the end container has a larger area than the start container then it is
   * considered an Enter transition, otherwise it is a Return transition.
   */
  public static final int TRANSITION_DIRECTION_AUTO = 0;

  /** Indicates that this is an Enter transition, i.e., when elements are entering the scene. */
  public static final int TRANSITION_DIRECTION_ENTER = 1;

  /** Indicates that this is a Return transition, i.e., when elements are exiting the scene. */
  public static final int TRANSITION_DIRECTION_RETURN = 2;

  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({TRANSITION_DIRECTION_AUTO, TRANSITION_DIRECTION_ENTER, TRANSITION_DIRECTION_RETURN})
  @Retention(RetentionPolicy.SOURCE)
  public @interface TransitionDirection {}

  /**
   * Indicates that this transition should only fade in the incoming content, without changing the
   * opacity of the outgoing content.
   */
  public static final int FADE_MODE_IN = 0;

  /**
   * Indicates that this transition should only fade out the outgoing content, without changing the
   * opacity of the incoming content.
   */
  public static final int FADE_MODE_OUT = 1;

  /** Indicates that this transition should cross fade the outgoing and incoming content. */
  public static final int FADE_MODE_CROSS = 2;

  /**
   * Indicates that this transition should sequentially fade out the outgoing content and fade in
   * the incoming content.
   */
  public static final int FADE_MODE_THROUGH = 3;

  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({FADE_MODE_IN, FADE_MODE_OUT, FADE_MODE_CROSS, FADE_MODE_THROUGH})
  @Retention(RetentionPolicy.SOURCE)
  public @interface FadeMode {}

  /**
   * Indicates that this transition should automatically choose whether to use {@link
   * #FIT_MODE_WIDTH} or {@link #FIT_MODE_HEIGHT}.
   */
  public static final int FIT_MODE_AUTO = 0;

  /**
   * Indicates that this transition should fit the incoming content to the width of the outgoing
   * content during the scale animation.
   */
  public static final int FIT_MODE_WIDTH = 1;

  /**
   * Indicates that this transition should fit the incoming content to the height of the outgoing
   * content during the scale animation.
   */
  public static final int FIT_MODE_HEIGHT = 2;

  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({FIT_MODE_AUTO, FIT_MODE_WIDTH, FIT_MODE_HEIGHT})
  @Retention(RetentionPolicy.SOURCE)
  public @interface FitMode {}

  private static final String TAG = MaterialContainerTransform.class.getSimpleName();
  private static final String PROP_BOUNDS = "materialContainerTransition:bounds";
  private static final String PROP_SHAPE_APPEARANCE = "materialContainerTransition:shapeAppearance";
  private static final String[] TRANSITION_PROPS =
      new String[] {PROP_BOUNDS, PROP_SHAPE_APPEARANCE};

  // Default animation thresholds. Will be used by default when the default linear PathMotion is
  // being used or when no other progress thresholds are appropriate (e.g., the arc thresholds for
  // an arc path).
  private static final ProgressThresholdsGroup DEFAULT_ENTER_THRESHOLDS =
      new ProgressThresholdsGroup(
          /* fade= */ new ProgressThresholds(0f, 0.25f),
          /* scale= */ new ProgressThresholds(0f, 1f),
          /* scaleMask= */ new ProgressThresholds(0f, 1f),
          /* shapeMask= */ new ProgressThresholds(0f, 0.75f));
  private static final ProgressThresholdsGroup DEFAULT_RETURN_THRESHOLDS =
      new ProgressThresholdsGroup(
          /* fade= */ new ProgressThresholds(0.60f, 0.90f),
          /* scale= */ new ProgressThresholds(0f, 1f),
          /* scaleMask= */ new ProgressThresholds(0f, 0.90f),
          /* shapeMask= */ new ProgressThresholds(0.30f, 0.90f));

  // Default animation thresholds for an arc path. Will be used by default when the PathMotion is
  // set to ArcMotion or MaterialArcMotion.
  private static final ProgressThresholdsGroup DEFAULT_ENTER_THRESHOLDS_ARC =
      new ProgressThresholdsGroup(
          /* fade= */ new ProgressThresholds(0.10f, 0.40f),
          /* scale= */ new ProgressThresholds(0.10f, 1f),
          /* scaleMask= */ new ProgressThresholds(0.10f, 1f),
          /* shapeMask= */ new ProgressThresholds(0.10f, 0.90f));
  private static final ProgressThresholdsGroup DEFAULT_RETURN_THRESHOLDS_ARC =
      new ProgressThresholdsGroup(
          /* fade= */ new ProgressThresholds(0.60f, 0.90f),
          /* scale= */ new ProgressThresholds(0f, 0.90f),
          /* scaleMask= */ new ProgressThresholds(0f, 0.90f),
          /* shapeMask= */ new ProgressThresholds(0.20f, 0.90f));

  private static final float ELEVATION_NOT_SET = -1f;

  private boolean drawDebugEnabled = false;
  private boolean holdAtEndEnabled = false;
  private boolean pathMotionCustom = false;
  private boolean appliedThemeValues = false;
  @IdRes private int drawingViewId = android.R.id.content;
  @IdRes private int startViewId = View.NO_ID;
  @IdRes private int endViewId = View.NO_ID;
  @ColorInt private int containerColor = Color.TRANSPARENT;
  @ColorInt private int startContainerColor = Color.TRANSPARENT;
  @ColorInt private int endContainerColor = Color.TRANSPARENT;
  @ColorInt private int scrimColor = 0x52000000;
  @TransitionDirection private int transitionDirection = TRANSITION_DIRECTION_AUTO;
  @FadeMode private int fadeMode = FADE_MODE_IN;
  @FitMode private int fitMode = FIT_MODE_AUTO;
  @Nullable private View startView;
  @Nullable private View endView;
  @Nullable private ShapeAppearanceModel startShapeAppearanceModel;
  @Nullable private ShapeAppearanceModel endShapeAppearanceModel;
  @Nullable private ProgressThresholds fadeProgressThresholds;
  @Nullable private ProgressThresholds scaleProgressThresholds;
  @Nullable private ProgressThresholds scaleMaskProgressThresholds;
  @Nullable private ProgressThresholds shapeMaskProgressThresholds;
  private boolean elevationShadowEnabled = VERSION.SDK_INT >= VERSION_CODES.P;
  private float startElevation = ELEVATION_NOT_SET;
  private float endElevation = ELEVATION_NOT_SET;

  public MaterialContainerTransform() {
    // Default constructor
  }

  public MaterialContainerTransform(@NonNull Context context, boolean entering) {
    applyThemeValues(context, entering);
    appliedThemeValues = true;
  }

  /** Get the id of the View which will be used as the start shared element container. */
  @IdRes
  public int getStartViewId() {
    return startViewId;
  }

  /**
   * Set the id of the View to be used as the start shared element container. The matching View will
   * be searched for in the hierarchy when starting this transition.
   *
   * <p>Setting a start View can be helpful when transitioning from a View to another View or if
   * transitioning from a View to a Fragment.
   *
   * <p>Manually setting the start View id will override any View explicitly set via {@link
   * #setStartView(View)} or any View picked up by the Transition system marked with a
   * transitionName.
   *
   * <p>If the start view cannot be found during the initialization of the {@code
   * MaterialContainerTransform}, then an {@link IllegalArgumentException} will be thrown.
   */
  public void setStartViewId(@IdRes int startViewId) {
    this.startViewId = startViewId;
  }

  /**
   * Get the id of the View which will be used as the end shared element container.
   *
   * <p>Setting an end View id can be used to manually configure MaterialContainerTransform when
   * transitioning between two Views in a single layout when the Transition system will not
   * automatically capture shared element start or end Views for you.
   *
   * <p>If the end view cannot be found during the initialization of the {@code
   * MaterialContainerTransform}, then an {@link IllegalArgumentException} will be thrown.
   */
  @IdRes
  public int getEndViewId() {
    return endViewId;
  }

  /**
   * Set the id of the View to be used as the end shared element container. The matching View will
   * be searched for in the hierarchy when starting this transition.
   *
   * <p>Manually setting the end View id will override any View explicitly set via {@link
   * #setEndView(View)} or any View picked up by the Transition system marked with a transitionName.
   */
  public void setEndViewId(@IdRes int endViewId) {
    this.endViewId = endViewId;
  }

  /** Get the View which will be used as the start shared element container. */
  @Nullable
  public View getStartView() {
    return startView;
  }

  /**
   * Set the View to be used as the start shared element container.
   *
   * @see #setStartViewId(int)
   */
  public void setStartView(@Nullable View startView) {
    this.startView = startView;
  }

  /** Get the View which will be used as the end shared element container. */
  @Nullable
  public View getEndView() {
    return endView;
  }

  /**
   * Set the View to be used as the end shared element container.
   *
   * @see #setEndViewId(int)
   */
  public void setEndView(@Nullable View endView) {
    this.endView = endView;
  }

  /**
   * Get the {@link ShapeAppearanceModel} which will be used to determine the shape from which the
   * container will be transformed.
   */
  @Nullable
  public ShapeAppearanceModel getStartShapeAppearanceModel() {
    return startShapeAppearanceModel;
  }

  /**
   * Set the {@link ShapeAppearanceModel} which will be used to determine the shape from which the
   * container will be transformed.
   *
   * <p>Manually setting a shape appearance will override both your theme's
   * transitionShapeAppearance attribute (if set) and the shape appearance of the start View (or end
   * View via {@link #setEndShapeAppearanceModel(ShapeAppearanceModel)} if the View implements the
   * {@link Shapeable} interface. Setting this property can be useful if your start or end View does
   * not implement {@link Shapeable} but does have a shape (eg. a rounded rect background drawable)
   * and you would like MaterialContainerTransform to morph from or to your View's shape.
   */
  public void setStartShapeAppearanceModel(
      @Nullable ShapeAppearanceModel startShapeAppearanceModel) {
    this.startShapeAppearanceModel = startShapeAppearanceModel;
  }

  /**
   * Get the {@link ShapeAppearanceModel} which will be used to determine the shape into which the
   * container will be transformed.
   */
  @Nullable
  public ShapeAppearanceModel getEndShapeAppearanceModel() {
    return endShapeAppearanceModel;
  }

  /**
   * Set the {@link ShapeAppearanceModel} which will be used to determine the shape into which the
   * container will be transformed.
   *
   * @see #setStartShapeAppearanceModel(ShapeAppearanceModel)
   */
  public void setEndShapeAppearanceModel(@Nullable ShapeAppearanceModel endShapeAppearanceModel) {
    this.endShapeAppearanceModel = endShapeAppearanceModel;
  }

  /**
   * Get whether shadows should be drawn around the container to approximate native elevation
   * shadows on the start and end views.
   */
  public boolean isElevationShadowEnabled() {
    return elevationShadowEnabled;
  }

  /**
   * Set whether shadows should be drawn around the container to approximate native elevation
   * shadows on the start and end views.
   *
   * <p>By default, the elevation shadows are only enabled for API level 28 and above, because
   * {@link Paint} shadows are not supported with hardware acceleration below API level 28. If
   * enabled for below API level 28, then the shadows will be drawn using {@link
   * MaterialShapeDrawable}, however this may cause performance issues.
   *
   * <p>Additionally, the rendering of elevation shadows may cause performance issues if the
   * container's shape is not a round rect or a regular rect, e.g., a rect with cut corners.
   */
  public void setElevationShadowEnabled(boolean elevationShadowEnabled) {
    this.elevationShadowEnabled = elevationShadowEnabled;
  }

  /**
   * Get the elevation that will be used to render a shadow around the container at the start of the
   * transition.
   *
   * <p>Default is -1, which means the elevation of the start view will be used.
   */
  public float getStartElevation() {
    return startElevation;
  }

  /**
   * Set the elevation that will be used to render a shadow around the container at the start of the
   * transition.
   *
   * <p>By default the elevation of the start view will be used.
   */
  public void setStartElevation(float startElevation) {
    this.startElevation = startElevation;
  }

  /**
   * Get the elevation that will be used to render a shadow around the container at the end of the
   * transition.
   *
   * <p>Default is -1, which means the elevation of the end view will be used.
   */
  public float getEndElevation() {
    return endElevation;
  }

  /**
   * Set the elevation that will be used to render a shadow around the container at the end of the
   * transition.
   *
   * <p>By default the elevation of the end view will be used.
   */
  public void setEndElevation(float endElevation) {
    this.endElevation = endElevation;
  }

  /** Get the id of the View whose overlay this transitions will be added to. */
  @IdRes
  public int getDrawingViewId() {
    return drawingViewId;
  }

  /**
   * Set the id of the View whose overlay this transition will be added to.
   *
   * <p>This can be used to limit the bounds of the animation (including the background scrim) to
   * the bounds of the provided drawing view, and also have the animation drawn at the relative
   * z-order of the drawing view.
   *
   * <p>By default, the {@code drawingViewId} will be {@code android.R.id.content}. Additionally, if
   * {@code drawingViewId} is the same as the end View's id, {@code MaterialContainerTransform} will
   * add the transition's drawable to the {@code drawingViewId}'s parent instead.
   *
   * <p>If the drawing view cannot be found during the initialization of the {@code
   * MaterialContainerTransform}, then an {@link IllegalArgumentException} will be thrown.
   */
  public void setDrawingViewId(@IdRes int drawingViewId) {
    this.drawingViewId = drawingViewId;
  }

  /**
   * Get the color to be drawn beneath both the start view and end view.
   *
   * @see #setContainerColor(int)
   */
  @ColorInt
  public int getContainerColor() {
    return containerColor;
  }

  /**
   * Set a color to be drawn beneath both the start and end view.
   *
   * <p>This color is the background color of the transforming container inside of which the start
   * and end views are drawn. Unlike the start view, start container color, end view and end
   * container color, this color will always be drawn as fully opaque, beneath all other content in
   * the container. By default, this color is set to transparent (0), meaning a container color will
   * not be drawn.
   *
   * <p>If a default container transform results in the start view being visible beneath the end
   * view, or vica versa, this is due to one or both views not having a background. The most common
   * way to solve this issue is by sequentially fading the contents with {@link #FADE_MODE_THROUGH}
   * and setting this color to the start and end view's desired background color.
   *
   * <p>If the start and end views have different background colors, or you would like to use a fade
   * mode other than {@link #FADE_MODE_THROUGH}, handle this by using {@link
   * #setStartContainerColor(int)} and {@link #setEndContainerColor(int)}.
   */
  public void setContainerColor(@ColorInt int containerColor) {
    this.containerColor = containerColor;
  }

  /**
   * Get the color to be drawn beneath the start view.
   *
   * @see #setStartContainerColor(int)
   */
  @ColorInt
  public int getStartContainerColor() {
    return startContainerColor;
  }

  /**
   * Set a color to be drawn beneath the start view.
   *
   * <p>This color will be drawn directly beneath the start view, will fill the entire transforming
   * container, and will animate its opacity to match the start view's. By default, this color is
   * set to transparent (0), meaning no color will be drawn.
   *
   * <p>This method can be useful when the color of the start and end view differ and the start view
   * does not handle drawing its own background. This can also be used if an expanding container is
   * larger than the start view. Setting this color to match that of the start view's background
   * will cause the start view to look like its background is expanding to fill the transforming
   * container.
   */
  public void setStartContainerColor(@ColorInt int containerColor) {
    this.startContainerColor = containerColor;
  }

  /**
   * Get the color to be drawn beneath the end view.
   *
   * @see #setEndContainerColor(int)
   */
  @ColorInt
  public int getEndContainerColor() {
    return endContainerColor;
  }

  /**
   * Set a color to be drawn beneath the end view.
   *
   * <p>This color will be drawn directly beneath the end view, will fill the entire transforming
   * container, and the will animate its opacity to match the end view's. By default, this color is
   * set to transparent (0), meaning no color will be drawn.
   *
   * <p>This method can be useful when the color of the start and end view differ and the end view
   * does not handle drawing its own background. Setting this color will prevent the start view from
   * being visible beneath the end view while transforming.
   */
  public void setEndContainerColor(@ColorInt int containerColor) {
    this.endContainerColor = containerColor;
  }

  /**
   * Set the container color, the start container color and the end container color.
   *
   * <p>This is a helper for the common case of transitioning between a start and end view when
   * neither draws its own background but a common color is shared. This prevents the start or end
   * view from being visible below one another.
   *
   * @see #setContainerColor(int)
   * @see #setStartContainerColor(int)
   * @see #setEndContainerColor(int)
   */
  public void setAllContainerColors(@ColorInt int containerColor) {
    this.containerColor = containerColor;
    this.startContainerColor = containerColor;
    this.endContainerColor = containerColor;
  }

  /**
   * Get the color to be drawn under the morphing container but within the bounds of the {@link
   * #getDrawingViewId()}.
   */
  @ColorInt
  public int getScrimColor() {
    return scrimColor;
  }

  /**
   * Set the color to be drawn under the morphing container but within the bounds of the {@link
   * #getDrawingViewId()}.
   *
   * <p>By default this is set to black with 32% opacity. Drawing a scrim is primarily useful for
   * transforming from a partial-screen View (eg. Card in a grid) to a full screen. The scrim will
   * gradually fade in and cover the content being transformed over by the morphing container.
   *
   * <p>Changing the default scrim color can be useful when transitioning between two Views in a
   * layout, where the ending View does not cover any outgoing content (eg. a FAB to a bottom
   * toolbar). For scenarios such as these, set the scrim color to transparent.
   */
  public void setScrimColor(@ColorInt int scrimColor) {
    this.scrimColor = scrimColor;
  }

  /**
   * The direction to be used by this transform.
   *
   * @see #TRANSITION_DIRECTION_AUTO
   * @see #TRANSITION_DIRECTION_ENTER
   * @see #TRANSITION_DIRECTION_RETURN
   */
  @TransitionDirection
  public int getTransitionDirection() {
    return transitionDirection;
  }

  /**
   * Set the transition direction to be used by this transform.
   *
   * <p>By default, the transition direction is determined by the change in size between the start
   * and end Views.
   *
   * @see #TRANSITION_DIRECTION_AUTO
   */
  public void setTransitionDirection(@TransitionDirection int transitionDirection) {
    this.transitionDirection = transitionDirection;
  }

  /** The fade mode to be used to swap the content of the start View with that of the end View. */
  @FadeMode
  public int getFadeMode() {
    return fadeMode;
  }

  /**
   * Set the fade mode to be used to swap the content of the start View with that of the end View.
   *
   * <p>By default, the fade mode is set to {@link #FADE_MODE_IN}.
   *
   * @see #FADE_MODE_IN
   * @see #FADE_MODE_OUT
   * @see #FADE_MODE_CROSS
   * @see #FADE_MODE_THROUGH
   */
  public void setFadeMode(@FadeMode int fadeMode) {
    this.fadeMode = fadeMode;
  }

  /** The fit mode to be used when scaling the incoming content of the end View. */
  @FitMode
  public int getFitMode() {
    return fitMode;
  }

  /**
   * Set the fit mode to be used when scaling the incoming content of the end View.
   *
   * <p>By default, the fit mode is set to {@link #FIT_MODE_AUTO}.
   */
  public void setFitMode(@FitMode int fitMode) {
    this.fitMode = fitMode;
  }

  /**
   * Get the {@link ProgressThresholds} which define the sub-range (any range inside the full
   * progress range of 0.0 - 1.0) between which the fade animation, determined by {@link
   * #getFadeMode()} will complete.
   */
  @Nullable
  public ProgressThresholds getFadeProgressThresholds() {
    return fadeProgressThresholds;
  }

  /**
   * Set the {@link ProgressThresholds} which define the sub-range (any range inside the full
   * progress range of 0.0 - 1.0) between which the fade animation, determined by {@link
   * #getFadeMode()} will complete.
   *
   * <p>See {@link ProgressThresholds} for an example of how the threshold ranges work.
   */
  public void setFadeProgressThresholds(@Nullable ProgressThresholds fadeProgressThresholds) {
    this.fadeProgressThresholds = fadeProgressThresholds;
  }

  /**
   * Get the {@link ProgressThresholds} which define the sub-range (any range inside the full
   * progress range of 0.0 - 1.0) between which the outgoing and incoming content will scale to the
   * full dimensions of the end container.
   */
  @Nullable
  public ProgressThresholds getScaleProgressThresholds() {
    return scaleProgressThresholds;
  }

  /**
   * Set the {@link ProgressThresholds} which define the sub-range (any range inside the full
   * progress range of 0.0 - 1.0) between which the outgoing and incoming content will scale to the
   * full dimensions of the end container.
   *
   * <p>See {@link ProgressThresholds} for an example of how the threshold ranges work.
   */
  public void setScaleProgressThresholds(@Nullable ProgressThresholds scaleProgressThresholds) {
    this.scaleProgressThresholds = scaleProgressThresholds;
  }

  /**
   * Get the {@link ProgressThresholds} which define the sub-range (any range inside the full
   * progress range of 0.0 and 1.0) between which the container will morph between the start and end
   * View's dimensions.
   */
  @Nullable
  public ProgressThresholds getScaleMaskProgressThresholds() {
    return scaleMaskProgressThresholds;
  }

  /**
   * Set the {@link ProgressThresholds} which define the sub-range (any range inside the full
   * progress range of 0.0 and 1.0) between which the container will morph between the start and end
   * View's dimensions.
   *
   * <p>See {@link ProgressThresholds} for an example of how the threshold ranges work.
   */
  public void setScaleMaskProgressThresholds(
      @Nullable ProgressThresholds scaleMaskProgressThresholds) {
    this.scaleMaskProgressThresholds = scaleMaskProgressThresholds;
  }

  /**
   * Get the {@link ProgressThresholds} which define the sub-range (any range inside the full
   * progress range of 0.0 and 1.0) between which the container will morph between the starting
   * {@link ShapeAppearanceModel} and ending {@link ShapeAppearanceModel}.
   */
  @Nullable
  public ProgressThresholds getShapeMaskProgressThresholds() {
    return shapeMaskProgressThresholds;
  }

  /**
   * Set the {@link ProgressThresholds} which define the sub-range (any range inside the full
   * progress range of 0.0 and 1.0) between which the container will morph between the starting
   * {@link ShapeAppearanceModel} and ending {@link ShapeAppearanceModel}.
   *
   * <p>See {@link ProgressThresholds} for an example of how the threshold ranges work.
   */
  public void setShapeMaskProgressThresholds(
      @Nullable ProgressThresholds shapeMaskProgressThresholds) {
    this.shapeMaskProgressThresholds = shapeMaskProgressThresholds;
  }

  /**
   * Whether to hold the last frame at the end of the animation.
   *
   * @see #setHoldAtEndEnabled(boolean)
   */
  public boolean isHoldAtEndEnabled() {
    return holdAtEndEnabled;
  }

  /**
   * If true, the last frame of the animation will be held in place, and the original outgoing and
   * incoming views will not be re-shown.
   *
   * <p>Useful for Activity return transitions to make sure the screen doesn't flash at the end.
   */
  public void setHoldAtEndEnabled(boolean holdAtEndEnabled) {
    this.holdAtEndEnabled = holdAtEndEnabled;
  }

  /**
   * Whether debug drawing is enabled.
   *
   * @see #setDrawDebugEnabled(boolean)
   */
  public boolean isDrawDebugEnabled() {
    return drawDebugEnabled;
  }

  /**
   * Set whether or not to draw paths which follow the shape and path of animating containers.
   *
   * @param drawDebugEnabled true if debugging lines and borders should be drawn during animation.
   */
  public void setDrawDebugEnabled(boolean drawDebugEnabled) {
    this.drawDebugEnabled = drawDebugEnabled;
  }

  @Override
  public void setPathMotion(@Nullable PathMotion pathMotion) {
    super.setPathMotion(pathMotion);
    pathMotionCustom = true;
  }

  @Nullable
  @Override
  public String[] getTransitionProperties() {
    return TRANSITION_PROPS;
  }

  @Override
  public void captureStartValues(@NonNull TransitionValues transitionValues) {
    captureValues(transitionValues, startView, startViewId, startShapeAppearanceModel);
  }

  @Override
  public void captureEndValues(@NonNull TransitionValues transitionValues) {
    captureValues(transitionValues, endView, endViewId, endShapeAppearanceModel);
  }

  private static void captureValues(
      @NonNull TransitionValues transitionValues,
      @Nullable View viewOverride,
      @IdRes int viewIdOverride,
      @Nullable ShapeAppearanceModel shapeAppearanceModelOverride) {
    if (viewIdOverride != View.NO_ID) {
      transitionValues.view = findDescendantOrAncestorById(transitionValues.view, viewIdOverride);
    } else if (viewOverride != null) {
      transitionValues.view = viewOverride;
    } else if (transitionValues.view.getTag(R.id.mtrl_motion_snapshot_view) instanceof View) {
      View snapshotView = (View) transitionValues.view.getTag(R.id.mtrl_motion_snapshot_view);

      // Clear snapshot so that we don't accidentally use it for another transform transition.
      transitionValues.view.setTag(R.id.mtrl_motion_snapshot_view, null);

      // Use snapshot if entering and capturing start values or returning and capturing end values.
      transitionValues.view = snapshotView;
    }
    View view = transitionValues.view;

    if (ViewCompat.isLaidOut(view) || view.getWidth() != 0 || view.getHeight() != 0) {
      // Capture location in screen co-ordinates
      RectF bounds = view.getParent() == null ? getRelativeBounds(view) : getLocationOnScreen(view);
      transitionValues.values.put(PROP_BOUNDS, bounds);
      transitionValues.values.put(
          PROP_SHAPE_APPEARANCE,
          captureShapeAppearance(view, bounds, shapeAppearanceModelOverride));
    }
  }

  // Get the shape appearance and convert it to relative corner sizes to simplify the interpolation.
  private static ShapeAppearanceModel captureShapeAppearance(
      @NonNull View view,
      @NonNull RectF bounds,
      @Nullable ShapeAppearanceModel shapeAppearanceModelOverride) {
    ShapeAppearanceModel shapeAppearanceModel =
        getShapeAppearance(view, shapeAppearanceModelOverride);
    return convertToRelativeCornerSizes(shapeAppearanceModel, bounds);
  }

  // Use the shape appearance from the override if it's present, the transitionShapeAppearance attr
  // if it's set, the view if it's [Shapeable], or else an empty model.
  private static ShapeAppearanceModel getShapeAppearance(
      @NonNull View view, @Nullable ShapeAppearanceModel shapeAppearanceModelOverride) {
    if (shapeAppearanceModelOverride != null) {
      return shapeAppearanceModelOverride;
    }

    if (view.getTag(R.id.mtrl_motion_snapshot_view) instanceof ShapeAppearanceModel) {
      return (ShapeAppearanceModel) view.getTag(R.id.mtrl_motion_snapshot_view);
    }

    Context context = view.getContext();
    int transitionShapeAppearanceResId = getTransitionShapeAppearanceResId(context);
    if (transitionShapeAppearanceResId != -1) {
      return ShapeAppearanceModel.builder(context, transitionShapeAppearanceResId, 0).build();
    }

    if (view instanceof Shapeable) {
      return ((Shapeable) view).getShapeAppearanceModel();
    }

    return ShapeAppearanceModel.builder().build();
  }

  @StyleRes
  private static int getTransitionShapeAppearanceResId(Context context) {
    TypedArray a = context.obtainStyledAttributes(new int[] {R.attr.transitionShapeAppearance});
    int transitionShapeAppearanceResId = a.getResourceId(0, -1);
    a.recycle();
    return transitionShapeAppearanceResId;
  }

  @Nullable
  @Override
  public Animator createAnimator(
      @NonNull ViewGroup sceneRoot,
      @Nullable TransitionValues startValues,
      @Nullable TransitionValues endValues) {
    if (startValues == null || endValues == null) {
      return null;
    }

    RectF startBounds = (RectF) startValues.values.get(PROP_BOUNDS);
    ShapeAppearanceModel startShapeAppearanceModel =
        (ShapeAppearanceModel) startValues.values.get(PROP_SHAPE_APPEARANCE);
    if (startBounds == null || startShapeAppearanceModel == null) {
      Log.w(TAG, "Skipping due to null start bounds. Ensure start view is laid out and measured.");
      return null;
    }

    RectF endBounds = (RectF) endValues.values.get(PROP_BOUNDS);
    ShapeAppearanceModel endShapeAppearanceModel =
        (ShapeAppearanceModel) endValues.values.get(PROP_SHAPE_APPEARANCE);
    if (endBounds == null || endShapeAppearanceModel == null) {
      Log.w(TAG, "Skipping due to null end bounds. Ensure end view is laid out and measured.");
      return null;
    }

    final View startView = startValues.view;
    final View endView = endValues.view;
    final View drawingView;
    View boundingView;
    View drawingBaseView = endView.getParent() != null ? endView : startView;
    if (drawingViewId == drawingBaseView.getId()) {
      drawingView = (View) drawingBaseView.getParent();
      boundingView = drawingBaseView;
    } else {
      drawingView = findAncestorById(drawingBaseView, drawingViewId);
      boundingView = null;
    }

    // Calculate drawable bounds and offset start/end bounds as needed
    RectF drawingViewBounds = getLocationOnScreen(drawingView);
    float offsetX = -drawingViewBounds.left;
    float offsetY = -drawingViewBounds.top;
    RectF drawableBounds = calculateDrawableBounds(drawingView, boundingView, offsetX, offsetY);
    startBounds.offset(offsetX, offsetY);
    endBounds.offset(offsetX, offsetY);

    boolean entering = isEntering(startBounds, endBounds);

    if (!appliedThemeValues) {
      // Apply theme values if we didn't already apply them up front in the constructor.
      applyThemeValues(drawingBaseView.getContext(), entering);
    }

    final TransitionDrawable transitionDrawable =
        new TransitionDrawable(
            getPathMotion(),
            startView,
            startBounds,
            startShapeAppearanceModel,
            getElevationOrDefault(startElevation, startView),
            endView,
            endBounds,
            endShapeAppearanceModel,
            getElevationOrDefault(endElevation, endView),
            containerColor,
            startContainerColor,
            endContainerColor,
            scrimColor,
            entering,
            elevationShadowEnabled,
            FadeModeEvaluators.get(fadeMode, entering),
            FitModeEvaluators.get(fitMode, entering, startBounds, endBounds),
            buildThresholdsGroup(entering),
            drawDebugEnabled);

    // Set the bounds of the transition drawable to not exceed the bounds of the drawingView.
    transitionDrawable.setBounds(
        Math.round(drawableBounds.left),
        Math.round(drawableBounds.top),
        Math.round(drawableBounds.right),
        Math.round(drawableBounds.bottom));

    ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
    animator.addUpdateListener(
        new AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(ValueAnimator animation) {
            transitionDrawable.setProgress(animation.getAnimatedFraction());
          }
        });

    addListener(
        new TransitionListenerAdapter() {
          @Override
          public void onTransitionStart(@NonNull Transition transition) {
            // Add the transition drawable to the root ViewOverlay
            ViewUtils.getOverlay(drawingView).add(transitionDrawable);

            // Hide the actual views at the beginning of the transition
            startView.setAlpha(0);
            endView.setAlpha(0);
          }

          @Override
          public void onTransitionEnd(@NonNull Transition transition) {
            removeListener(this);
            if (holdAtEndEnabled) {
              // Keep drawable showing and views hidden (useful for Activity return transitions)
              return;
            }
            // Show the actual views at the end of the transition
            startView.setAlpha(1);
            endView.setAlpha(1);

            // Remove the transition drawable from the root ViewOverlay
            ViewUtils.getOverlay(drawingView).remove(transitionDrawable);
          }
        });

    return animator;
  }

  private void applyThemeValues(Context context, boolean entering) {
    TransitionUtils.applyThemeInterpolator(
        this, context, R.attr.motionEasingStandard, AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
    TransitionUtils.applyThemeDuration(
        this, context, entering ? R.attr.motionDurationLong1 : R.attr.motionDurationMedium2);
    if (!pathMotionCustom) {
      TransitionUtils.applyThemePath(this, context, R.attr.motionPath);
    }
  }

  private static float getElevationOrDefault(float elevation, View view) {
    return elevation != ELEVATION_NOT_SET ? elevation : ViewCompat.getElevation(view);
  }

  private static RectF calculateDrawableBounds(
      View drawingView, @Nullable View boundingView, float offsetX, float offsetY) {
    if (boundingView != null) {
      RectF drawableBounds = getLocationOnScreen(boundingView);
      drawableBounds.offset(offsetX, offsetY);
      return drawableBounds;
    } else {
      return new RectF(0, 0, drawingView.getWidth(), drawingView.getHeight());
    }
  }

  private boolean isEntering(@NonNull RectF startBounds, @NonNull RectF endBounds) {
    switch (transitionDirection) {
      case TRANSITION_DIRECTION_AUTO:
        return calculateArea(endBounds) > calculateArea(startBounds);
      case TRANSITION_DIRECTION_ENTER:
        return true;
      case TRANSITION_DIRECTION_RETURN:
        return false;
      default:
        throw new IllegalArgumentException("Invalid transition direction: " + transitionDirection);
    }
  }

  private ProgressThresholdsGroup buildThresholdsGroup(boolean entering) {
    PathMotion pathMotion = getPathMotion();
    if (pathMotion instanceof ArcMotion || pathMotion instanceof MaterialArcMotion) {
      return getThresholdsOrDefault(
          entering, DEFAULT_ENTER_THRESHOLDS_ARC, DEFAULT_RETURN_THRESHOLDS_ARC);
    } else {
      return getThresholdsOrDefault(entering, DEFAULT_ENTER_THRESHOLDS, DEFAULT_RETURN_THRESHOLDS);
    }
  }

  private ProgressThresholdsGroup getThresholdsOrDefault(
      boolean entering,
      ProgressThresholdsGroup defaultEnterThresholds,
      ProgressThresholdsGroup defaultReturnThresholds) {
    ProgressThresholdsGroup defaultThresholds =
        entering ? defaultEnterThresholds : defaultReturnThresholds;
    return new ProgressThresholdsGroup(
        defaultIfNull(fadeProgressThresholds, defaultThresholds.fade),
        defaultIfNull(scaleProgressThresholds, defaultThresholds.scale),
        defaultIfNull(scaleMaskProgressThresholds, defaultThresholds.scaleMask),
        defaultIfNull(shapeMaskProgressThresholds, defaultThresholds.shapeMask));
  }

  /**
   * A {@link Drawable} that is able to draw a point in a container transformation given a progress
   * between 0.0 and 1.0.
   */
  private static final class TransitionDrawable extends Drawable {

    // Elevation shadow colors
    private static final int SHADOW_COLOR = 0x2D000000;
    private static final int COMPAT_SHADOW_COLOR = 0xFF888888;

    // Elevation shadow offset multiplier adjustments which help approximate native shadows
    private static final float SHADOW_DX_MULTIPLIER_ADJUSTMENT = 0.3f;
    private static final float SHADOW_DY_MULTIPLIER_ADJUSTMENT = 1.5f;

    // Start container
    private final View startView;
    private final RectF startBounds;
    private final ShapeAppearanceModel startShapeAppearanceModel;
    private final float startElevation;

    // End container
    private final View endView;
    private final RectF endBounds;
    private final ShapeAppearanceModel endShapeAppearanceModel;
    private final float endElevation;

    // Paint
    private final Paint containerPaint = new Paint();
    private final Paint startContainerPaint = new Paint();
    private final Paint endContainerPaint = new Paint();
    private final Paint shadowPaint = new Paint();
    private final Paint scrimPaint = new Paint();

    // Motion path
    private final MaskEvaluator maskEvaluator = new MaskEvaluator();
    private final PathMeasure motionPathMeasure;
    private final float motionPathLength;
    private final float[] motionPathPosition = new float[2];

    // Drawing
    private final boolean entering;
    private final float displayWidth;
    private final float displayHeight;
    private final boolean elevationShadowEnabled;
    private final MaterialShapeDrawable compatShadowDrawable = new MaterialShapeDrawable();
    private final RectF currentStartBounds;
    private final RectF currentStartBoundsMasked;
    private final RectF currentEndBounds;
    private final RectF currentEndBoundsMasked;
    private final ProgressThresholdsGroup progressThresholds;
    private final FadeModeEvaluator fadeModeEvaluator;
    private final FitModeEvaluator fitModeEvaluator;

    // Debug drawing
    private final boolean drawDebugEnabled;
    private final Paint debugPaint = new Paint();
    private final Path debugPath = new Path();

    // Current progress calculations
    private FadeModeResult fadeModeResult;
    private FitModeResult fitModeResult;
    private RectF currentMaskBounds;
    private float currentElevation;
    private float currentElevationDy;
    private float progress;

    private TransitionDrawable(
        PathMotion pathMotion,
        View startView,
        RectF startBounds,
        ShapeAppearanceModel startShapeAppearanceModel,
        float startElevation,
        View endView,
        RectF endBounds,
        ShapeAppearanceModel endShapeAppearanceModel,
        float endElevation,
        @ColorInt int containerColor,
        @ColorInt int startContainerColor,
        @ColorInt int endContainerColor,
        int scrimColor,
        boolean entering,
        boolean elevationShadowEnabled,
        FadeModeEvaluator fadeModeEvaluator,
        FitModeEvaluator fitModeEvaluator,
        ProgressThresholdsGroup progressThresholds,
        boolean drawDebugEnabled) {
      this.startView = startView;
      this.startBounds = startBounds;
      this.startShapeAppearanceModel = startShapeAppearanceModel;
      this.startElevation = startElevation;
      this.endView = endView;
      this.endBounds = endBounds;
      this.endShapeAppearanceModel = endShapeAppearanceModel;
      this.endElevation = endElevation;
      this.entering = entering;
      this.elevationShadowEnabled = elevationShadowEnabled;
      this.fadeModeEvaluator = fadeModeEvaluator;
      this.fitModeEvaluator = fitModeEvaluator;
      this.progressThresholds = progressThresholds;
      this.drawDebugEnabled = drawDebugEnabled;

      WindowManager windowManager =
          (WindowManager) startView.getContext().getSystemService(Context.WINDOW_SERVICE);
      DisplayMetrics displayMetrics = new DisplayMetrics();
      windowManager.getDefaultDisplay().getMetrics(displayMetrics);
      displayWidth = displayMetrics.widthPixels;
      displayHeight = displayMetrics.heightPixels;

      containerPaint.setColor(containerColor);
      startContainerPaint.setColor(startContainerColor);
      endContainerPaint.setColor(endContainerColor);

      compatShadowDrawable.setFillColor(ColorStateList.valueOf(Color.TRANSPARENT));
      compatShadowDrawable.setShadowCompatibilityMode(
          MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS);
      compatShadowDrawable.setShadowBitmapDrawingEnable(false);
      compatShadowDrawable.setShadowColor(COMPAT_SHADOW_COLOR);

      currentStartBounds = new RectF(startBounds);
      currentStartBoundsMasked = new RectF(currentStartBounds);
      currentEndBounds = new RectF(currentStartBounds);
      currentEndBoundsMasked = new RectF(currentEndBounds);

      // Calculate motion path
      PointF startPoint = getMotionPathPoint(startBounds);
      PointF endPoint = getMotionPathPoint(endBounds);
      Path motionPath = pathMotion.getPath(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
      motionPathMeasure = new PathMeasure(motionPath, false);
      motionPathLength = motionPathMeasure.getLength();
      // Fill the motion path with default positions in case a zero-length path is specified which
      // causes motionPathMeasure.getPosTan to skip filling this int array.
      // A zero-length path happens when the startBounds are top aligned and horizontally centered
      // on the endBounds.
      motionPathPosition[0] = startBounds.centerX();
      motionPathPosition[1] = startBounds.top;

      scrimPaint.setStyle(Paint.Style.FILL);
      scrimPaint.setShader(createColorShader(scrimColor));

      debugPaint.setStyle(Paint.Style.STROKE);
      debugPaint.setStrokeWidth(10);

      // Initializes calculations the drawable
      updateProgress(0);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
      if (scrimPaint.getAlpha() > 0) {
        canvas.drawRect(getBounds(), scrimPaint);
      }

      int debugCanvasSave = drawDebugEnabled ? canvas.save() : -1;

      if (elevationShadowEnabled && currentElevation > 0) {
        drawElevationShadow(canvas);
      }

      // Clip the canvas to container's path. Anything drawn to the canvas after this clipping will
      // be masked inside the clipped area.
      maskEvaluator.clip(canvas);

      maybeDrawContainerColor(canvas, containerPaint);

      if (fadeModeResult.endOnTop) {
        drawStartView(canvas);
        drawEndView(canvas);
      } else {
        drawEndView(canvas);
        drawStartView(canvas);
      }

      if (drawDebugEnabled) {
        canvas.restoreToCount(debugCanvasSave);
        drawDebugCumulativePath(canvas, currentStartBounds, debugPath, Color.MAGENTA);
        drawDebugRect(canvas, currentStartBoundsMasked, Color.YELLOW);
        drawDebugRect(canvas, currentStartBounds, Color.GREEN);
        drawDebugRect(canvas, currentEndBoundsMasked, Color.CYAN);
        drawDebugRect(canvas, currentEndBounds, Color.BLUE);
      }
    }

    // Draw shadow based on current path and clip shape path itself to leave only shadow.
    private void drawElevationShadow(Canvas canvas) {
      canvas.save();
      canvas.clipPath(maskEvaluator.getPath(), Op.DIFFERENCE);

      if (VERSION.SDK_INT > VERSION_CODES.P) {
        drawElevationShadowWithPaintShadowLayer(canvas);
      } else {
        drawElevationShadowWithMaterialShapeDrawable(canvas);
      }

      canvas.restore();
    }

    private void drawElevationShadowWithPaintShadowLayer(Canvas canvas) {
      ShapeAppearanceModel currentShapeAppearanceModel =
          maskEvaluator.getCurrentShapeAppearanceModel();
      if (currentShapeAppearanceModel.isRoundRect(currentMaskBounds)) {
        // Optimize for the common round rect case, should also account for regular rect
        float radius =
            currentShapeAppearanceModel.getTopLeftCornerSize().getCornerSize(currentMaskBounds);
        canvas.drawRoundRect(currentMaskBounds, radius, radius, shadowPaint);
      } else {
        // This will be less performant but should be a minority of cases
        canvas.drawPath(maskEvaluator.getPath(), shadowPaint);
      }
    }

    private void drawElevationShadowWithMaterialShapeDrawable(Canvas canvas) {
      compatShadowDrawable.setBounds(
          (int) currentMaskBounds.left,
          (int) currentMaskBounds.top,
          (int) currentMaskBounds.right,
          (int) currentMaskBounds.bottom);
      compatShadowDrawable.setElevation(currentElevation);
      compatShadowDrawable.setShadowVerticalOffset((int) currentElevationDy);
      compatShadowDrawable.setShapeAppearanceModel(maskEvaluator.getCurrentShapeAppearanceModel());
      compatShadowDrawable.draw(canvas);
    }

    // Transform the canvas to the current bounds, scale and alpha before drawing the start view.
    private void drawStartView(Canvas canvas) {
      maybeDrawContainerColor(canvas, startContainerPaint);
      transform(
          canvas,
          getBounds(),
          currentStartBounds.left,
          currentStartBounds.top,
          fitModeResult.startScale,
          fadeModeResult.startAlpha,
          new CanvasOperation() {
            @Override
            public void run(Canvas canvas) {
              startView.draw(canvas);
            }
          });
    }

    // Transform the canvas to the current bounds, scale and alpha before drawing the end view.
    private void drawEndView(Canvas canvas) {
      maybeDrawContainerColor(canvas, endContainerPaint);
      transform(
          canvas,
          getBounds(),
          currentEndBounds.left,
          currentEndBounds.top,
          fitModeResult.endScale,
          fadeModeResult.endAlpha,
          new CanvasOperation() {
            @Override
            public void run(Canvas canvas) {
              endView.draw(canvas);
            }
          });
    }

    private void maybeDrawContainerColor(Canvas canvas, Paint containerPaint) {
      // Fill the container at the current layer with a color. Useful when the start or end view
      // does not have a background or when the container size exceeds the image size which it can
      // in large start/end size changes.
      if (containerPaint.getColor() != Color.TRANSPARENT && containerPaint.getAlpha() > 0) {
        canvas.drawRect(getBounds(), containerPaint);
      }
    }

    @Override
    public void setAlpha(int alpha) {
      throw new UnsupportedOperationException("Setting alpha on is not supported");
    }

    public void setColorFilter(@Nullable ColorFilter colorFilter) {
      throw new UnsupportedOperationException("Setting a color filter is not supported");
    }

    @Override
    public int getOpacity() {
      return PixelFormat.TRANSLUCENT;
    }

    private void setProgress(float progress) {
      if (this.progress != progress) {
        updateProgress(progress);
      }
    }

    private void updateProgress(float progress) {
      this.progress = progress;

      // Fade in/out scrim over non-shared elements
      scrimPaint.setAlpha((int) (entering ? lerp(0, 255, progress) : lerp(255, 0, progress)));

      // Calculate position based on motion path
      motionPathMeasure.getPosTan(motionPathLength * progress, motionPathPosition, null);
      float motionPathX = motionPathPosition[0];
      float motionPathY = motionPathPosition[1];

      // Allow overshoot by extrapolating position using trajectory at closest part of motion path
      if (progress > 1 || progress < 0) {
        float trajectoryProgress;
        float trajectoryMultiplier;
        if (progress > 1) {
          trajectoryProgress = 0.99f;
          trajectoryMultiplier = (progress - 1f) / (1f - trajectoryProgress);
        } else {
          trajectoryProgress = 0.01f;
          trajectoryMultiplier = progress / trajectoryProgress * -1;
        }

        motionPathMeasure.getPosTan(
            motionPathLength * trajectoryProgress, motionPathPosition, null);
        float trajectoryMotionPathX = motionPathPosition[0];
        float trajectoryMotionPathY = motionPathPosition[1];
        motionPathX += (motionPathX - trajectoryMotionPathX) * trajectoryMultiplier;
        motionPathY += (motionPathY - trajectoryMotionPathY) * trajectoryMultiplier;
      }

      // Calculate current start and end bounds
      float scaleStartFraction = checkNotNull(progressThresholds.scale.start);
      float scaleEndFraction = checkNotNull(progressThresholds.scale.end);
      fitModeResult =
          fitModeEvaluator.evaluate(
              progress,
              scaleStartFraction,
              scaleEndFraction,
              startBounds.width(),
              startBounds.height(),
              endBounds.width(),
              endBounds.height());
      currentStartBounds.set(
          motionPathX - fitModeResult.currentStartWidth / 2,
          motionPathY,
          motionPathX + fitModeResult.currentStartWidth / 2,
          motionPathY + fitModeResult.currentStartHeight);
      currentEndBounds.set(
          motionPathX - fitModeResult.currentEndWidth / 2,
          motionPathY,
          motionPathX + fitModeResult.currentEndWidth / 2,
          motionPathY + fitModeResult.currentEndHeight);

      // Mask start or end bounds based on fit mode, over the duration of the fade
      currentStartBoundsMasked.set(currentStartBounds);
      currentEndBoundsMasked.set(currentEndBounds);
      float maskStartFraction = checkNotNull(progressThresholds.scaleMask.start);
      float maskEndFraction = checkNotNull(progressThresholds.scaleMask.end);
      boolean shouldMaskStartBounds = fitModeEvaluator.shouldMaskStartBounds(fitModeResult);
      RectF maskBounds = shouldMaskStartBounds ? currentStartBoundsMasked : currentEndBoundsMasked;
      float maskProgress = lerp(0f, 1f, maskStartFraction, maskEndFraction, progress);
      float maskMultiplier = shouldMaskStartBounds ? maskProgress : 1 - maskProgress;
      fitModeEvaluator.applyMask(maskBounds, maskMultiplier, fitModeResult);

      // Union start and end mask bounds
      currentMaskBounds =
          new RectF(
              Math.min(currentStartBoundsMasked.left, currentEndBoundsMasked.left),
              Math.min(currentStartBoundsMasked.top, currentEndBoundsMasked.top),
              Math.max(currentStartBoundsMasked.right, currentEndBoundsMasked.right),
              Math.max(currentStartBoundsMasked.bottom, currentEndBoundsMasked.bottom));

      maskEvaluator.evaluate(
          progress,
          startShapeAppearanceModel,
          endShapeAppearanceModel,
          currentStartBounds,
          currentStartBoundsMasked,
          currentEndBoundsMasked,
          progressThresholds.shapeMask);

      // Calculate current elevation and set up shadow layer
      currentElevation = lerp(startElevation, endElevation, progress);
      float dxMultiplier = calculateElevationDxMultiplier(currentMaskBounds, displayWidth);
      float dyMultiplier = calculateElevationDyMultiplier(currentMaskBounds, displayHeight);
      float currentElevationDx = (int) (currentElevation * dxMultiplier);
      currentElevationDy = (int) (currentElevation * dyMultiplier);
      shadowPaint.setShadowLayer(
          currentElevation, currentElevationDx, currentElevationDy, SHADOW_COLOR);

      // Cross-fade images of the start/end states over range of `progress`
      float fadeStartFraction = checkNotNull(progressThresholds.fade.start);
      float fadeEndFraction = checkNotNull(progressThresholds.fade.end);
      fadeModeResult = fadeModeEvaluator.evaluate(progress, fadeStartFraction, fadeEndFraction);

      // Update start and end container paints to share the same opacity as their respective view
      if (startContainerPaint.getColor() != Color.TRANSPARENT) {
        startContainerPaint.setAlpha(fadeModeResult.startAlpha);
      }
      if (endContainerPaint.getColor() != Color.TRANSPARENT) {
        endContainerPaint.setAlpha(fadeModeResult.endAlpha);
      }

      invalidateSelf();
    }

    private static PointF getMotionPathPoint(RectF bounds) {
      return new PointF(bounds.centerX(), bounds.top);
    }

    /**
     * The dx value for the elevation shadow's horizontal offset should be based on where the
     * current bounds are located in relation to the horizontal mid-point of the screen, since
     * that's where the native light source is located.
     *
     * <p>If the bounds are at the mid-point, the offset should be 0, which results in even shadows
     * to the left and right of the view.
     *
     * <p>If the bounds are to the left of the mid-point, the offset should be negative, which
     * results in more shadow to the left of the view.
     *
     * <p>If the bounds are to the right of the mid-point, the offset should be positive, which
     * results in more shadow to the right of the view.
     */
    private static float calculateElevationDxMultiplier(RectF bounds, float displayWidth) {
      return (bounds.centerX() / (displayWidth / 2) - 1) * SHADOW_DX_MULTIPLIER_ADJUSTMENT;
    }

    /**
     * The dy value for the elevation shadow's vertical offset should be based on where the current
     * bounds are located in relation to the top of the screen, since that's where the native light
     * source is located.
     *
     * <p>The offset should be 0 at the top of the screen and increase as the bounds get further
     * away from the top of the screen.
     */
    private static float calculateElevationDyMultiplier(RectF bounds, float displayHeight) {
      return bounds.centerY() / displayHeight * SHADOW_DY_MULTIPLIER_ADJUSTMENT;
    }

    private void drawDebugCumulativePath(
        Canvas canvas, RectF bounds, Path path, @ColorInt int color) {
      PointF point = getMotionPathPoint(bounds);
      if (progress == 0) {
        path.reset();
        path.moveTo(point.x, point.y);
      } else {
        path.lineTo(point.x, point.y);
        debugPaint.setColor(color);
        canvas.drawPath(path, debugPaint);
      }
    }

    private void drawDebugRect(Canvas canvas, RectF bounds, @ColorInt int color) {
      debugPaint.setColor(color);
      canvas.drawRect(bounds, debugPaint);
    }
  }

  /**
   * A class which holds a start and end value which represent a range within 0.0 - 1.0.
   *
   * <p>This class is used to define the period, or sub-range, over which a child animation is run
   * inside a parent animation's overall 0.0 - 1.0 progress.
   *
   * <p>For example, setting the fade thresholds to a range of 0.3 - 0.6 would mean that for the
   * first 30% of the animation, the start view would be fully opaque and the end view would be
   * fully transparent. Then, the fade would begin at the 30% point of the animation and complete at
   * the 60% point. For the remainder of the animation after the 60% point, the start view would be
   * fully transparent and the end view would be fully opaque.
   */
  public static class ProgressThresholds {
    @FloatRange(from = 0.0, to = 1.0)
    private final float start;

    @FloatRange(from = 0.0, to = 1.0)
    private final float end;

    public ProgressThresholds(
        @FloatRange(from = 0.0, to = 1.0) float start,
        @FloatRange(from = 0.0, to = 1.0) float end) {
      this.start = start;
      this.end = end;
    }

    @FloatRange(from = 0.0, to = 1.0)
    public float getStart() {
      return start;
    }

    @FloatRange(from = 0.0, to = 1.0)
    public float getEnd() {
      return end;
    }
  }

  private static class ProgressThresholdsGroup {
    @NonNull private final ProgressThresholds fade;
    @NonNull private final ProgressThresholds scale;
    @NonNull private final ProgressThresholds scaleMask;
    @NonNull private final ProgressThresholds shapeMask;

    private ProgressThresholdsGroup(
        @NonNull ProgressThresholds fade,
        @NonNull ProgressThresholds scale,
        @NonNull ProgressThresholds scaleMask,
        @NonNull ProgressThresholds shapeMask) {
      this.fade = fade;
      this.scale = scale;
      this.scaleMask = scaleMask;
      this.shapeMask = shapeMask;
    }
  }
}
