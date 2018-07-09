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

package com.google.android.material.floatingactionbutton;

import com.google.android.material.R;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.AnimatorRes;
import android.support.annotation.ColorInt;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import com.google.android.material.animation.MotionSpec;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.expandable.ExpandableTransformationWidget;
import com.google.android.material.expandable.ExpandableWidgetHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButtonImpl.InternalVisibilityChangedListener;
import com.google.android.material.internal.DescendantOffsetUtils;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.internal.VisibilityAwareImageButton;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shadow.ShadowViewDelegate;
import com.google.android.material.stateful.ExtendableSavedState;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.TintableBackgroundView;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.TintableImageSourceView;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.AppCompatImageHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Floating action buttons are used for a special type of promoted action. They are distinguished by
 * a circled icon floating above the UI and have special motion behaviors related to morphing,
 * launching, and the transferring anchor point.
 *
 * <p>Floating action buttons come in two sizes: the default and the mini. The size can be
 * controlled with the {@code fabSize} attribute.
 *
 * <p>As this class descends from {@link ImageView}, you can control the icon which is displayed via
 * {@link #setImageDrawable(Drawable)}.
 *
 * <p>The background color of this view defaults to the your theme's {@code colorAccent}. If you
 * wish to change this at runtime then you can do so via {@link
 * #setBackgroundTintList(ColorStateList)}.
 */
@CoordinatorLayout.DefaultBehavior(FloatingActionButton.Behavior.class)
public class FloatingActionButton extends VisibilityAwareImageButton
    implements TintableBackgroundView, TintableImageSourceView, ExpandableTransformationWidget {

  private static final String LOG_TAG = "FloatingActionButton";
  private static final String EXPANDABLE_WIDGET_HELPER_KEY = "expandableWidgetHelper";

  /** Callback to be invoked when the visibility of a FloatingActionButton changes. */
  public abstract static class OnVisibilityChangedListener {
    /**
     * Called when a FloatingActionButton has been {@link #show(OnVisibilityChangedListener) shown}.
     *
     * @param fab the FloatingActionButton that was shown.
     */
    public void onShown(FloatingActionButton fab) {}

    /**
     * Called when a FloatingActionButton has been {@link #hide(OnVisibilityChangedListener)
     * hidden}.
     *
     * @param fab the FloatingActionButton that was hidden.
     */
    public void onHidden(FloatingActionButton fab) {}
  }

  // These values must match those in the attrs declaration

  /**
   * The mini sized button, 40dp. Will always be smaller than {@link #SIZE_NORMAL}.
   *
   * @see #setSize(int)
   */
  public static final int SIZE_MINI = 1;

  /**
   * The normal sized button, 56dp. Will always be larger than {@link #SIZE_MINI}.
   *
   * @see #setSize(int)
   */
  public static final int SIZE_NORMAL = 0;

  /**
   * Size which will change based on the window size. For small sized windows (largest screen
   * dimension < 470dp) this will select a mini sized button ({@link #SIZE_MINI}), and for larger
   * sized windows it will select a normal sized button ({@link #SIZE_NORMAL}).
   *
   * @see #setSize(int)
   */
  public static final int SIZE_AUTO = -1;

  /**
   * Indicates that the {@link FloatingActionButton} should not have a custom size, and instead that
   * the size should be calculated based on the value set using {@link #setSize(int)} or the {@code
   * fabSize} attribute. Instead of using this constant directly, you can call the {@link
   * #clearCustomSize()} method.
   */
  public static final int NO_CUSTOM_SIZE = 0;

  /**
   * The switch point for the largest screen edge where {@link #SIZE_AUTO} switches from mini to
   * normal.
   */
  private static final int AUTO_MINI_LARGEST_SCREEN_WIDTH = 470;

  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({SIZE_MINI, SIZE_NORMAL, SIZE_AUTO})
  public @interface Size {}

  private ColorStateList backgroundTint;
  private PorterDuff.Mode backgroundTintMode;
  @Nullable private ColorStateList imageTint;
  @Nullable private PorterDuff.Mode imageMode;

  private int borderWidth;
  private ColorStateList rippleColor;
  private int size;
  private int customSize;
  private int imagePadding;
  private int maxImageSize;

  boolean compatPadding;
  final Rect shadowPadding = new Rect();
  private final Rect touchArea = new Rect();

  private final AppCompatImageHelper imageHelper;
  private final ExpandableWidgetHelper expandableWidgetHelper;

  private FloatingActionButtonImpl impl;

  public FloatingActionButton(Context context) {
    this(context, null);
  }

  public FloatingActionButton(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.floatingActionButtonStyle);
  }

  public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.FloatingActionButton,
            defStyleAttr,
            R.style.Widget_Design_FloatingActionButton);
    backgroundTint =
        MaterialResources.getColorStateList(
            context, a, R.styleable.FloatingActionButton_backgroundTint);
    backgroundTintMode =
        ViewUtils.parseTintMode(
            a.getInt(R.styleable.FloatingActionButton_backgroundTintMode, -1), null);
    rippleColor =
        MaterialResources.getColorStateList(
            context, a, R.styleable.FloatingActionButton_rippleColor);
    size = a.getInt(R.styleable.FloatingActionButton_fabSize, SIZE_AUTO);
    customSize =
        a.getDimensionPixelSize(R.styleable.FloatingActionButton_fabCustomSize, NO_CUSTOM_SIZE);
    borderWidth = a.getDimensionPixelSize(R.styleable.FloatingActionButton_borderWidth, 0);
    final float elevation = a.getDimension(R.styleable.FloatingActionButton_elevation, 0f);
    final float hoveredFocusedTranslationZ =
        a.getDimension(R.styleable.FloatingActionButton_hoveredFocusedTranslationZ, 0f);
    final float pressedTranslationZ =
        a.getDimension(R.styleable.FloatingActionButton_pressedTranslationZ, 0f);
    compatPadding = a.getBoolean(R.styleable.FloatingActionButton_useCompatPadding, false);
    maxImageSize = a.getDimensionPixelSize(R.styleable.FloatingActionButton_maxImageSize, 0);

    MotionSpec showMotionSpec =
        MotionSpec.createFromAttribute(context, a, R.styleable.FloatingActionButton_showMotionSpec);
    MotionSpec hideMotionSpec =
        MotionSpec.createFromAttribute(context, a, R.styleable.FloatingActionButton_hideMotionSpec);

    a.recycle();

    imageHelper = new AppCompatImageHelper(this);
    imageHelper.loadFromAttributes(attrs, defStyleAttr);

    expandableWidgetHelper = new ExpandableWidgetHelper(this);

    getImpl().setBackgroundDrawable(backgroundTint, backgroundTintMode, rippleColor, borderWidth);
    getImpl().setElevation(elevation);
    getImpl().setHoveredFocusedTranslationZ(hoveredFocusedTranslationZ);
    getImpl().setPressedTranslationZ(pressedTranslationZ);
    getImpl().setMaxImageSize(maxImageSize);
    getImpl().setShowMotionSpec(showMotionSpec);
    getImpl().setHideMotionSpec(hideMotionSpec);

    setScaleType(ScaleType.MATRIX);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final int preferredSize = getSizeDimension();

    imagePadding = (preferredSize - maxImageSize) / 2;
    getImpl().updatePadding();

    final int w = resolveAdjustedSize(preferredSize, widthMeasureSpec);
    final int h = resolveAdjustedSize(preferredSize, heightMeasureSpec);

    // As we want to stay circular, we set both dimensions to be the
    // smallest resolved dimension
    final int d = Math.min(w, h);

    // We add the shadow's padding to the measured dimension
    setMeasuredDimension(
        d + shadowPadding.left + shadowPadding.right, d + shadowPadding.top + shadowPadding.bottom);
  }

  /**
   * Returns the ripple color for this button.
   *
   * @return the ARGB color used for the ripple
   * @see #setRippleColor(int)
   * @deprecated Use {@link #getRippleColorStateList()} instead.
   */
  @ColorInt
  @Deprecated
  public int getRippleColor() {
    return rippleColor != null ? rippleColor.getDefaultColor() : 0;
  }

  /**
   * Returns the ripple color for this button.
   *
   * @return the color state list used for the ripple
   * @see #setRippleColor(ColorStateList)
   */
  @Nullable
  public ColorStateList getRippleColorStateList() {
    return rippleColor;
  }

  /**
   * Sets the ripple color for this button.
   *
   * <p>When running on devices with KitKat or below, we draw this color as a filled circle rather
   * than a ripple.
   *
   * @param color ARGB color to use for the ripple
   * @attr ref com.google.android.material.R.styleable#FloatingActionButton_rippleColor
   * @see #getRippleColor()
   */
  public void setRippleColor(@ColorInt int color) {
    setRippleColor(ColorStateList.valueOf(color));
  }

  /**
   * Sets the ripple color for this button.
   *
   * <p>When running on devices with KitKat or below, we draw this color as a filled circle rather
   * than a ripple.
   *
   * @param color color state list to use for the ripple
   * @attr ref com.google.android.material.R.styleable#FloatingActionButton_rippleColor
   * @see #getRippleColor()
   */
  public void setRippleColor(@Nullable ColorStateList color) {
    if (rippleColor != color) {
      rippleColor = color;
      getImpl().setRippleColor(rippleColor);
    }
  }

  /**
   * Returns the tint applied to the background drawable, if specified.
   *
   * @return the tint applied to the background drawable
   * @see #setBackgroundTintList(ColorStateList)
   */
  @Nullable
  @Override
  public ColorStateList getBackgroundTintList() {
    return backgroundTint;
  }

  /**
   * Applies a tint to the background drawable. Does not modify the current tint mode, which is
   * {@link PorterDuff.Mode#SRC_IN} by default.
   *
   * @param tint the tint to apply, may be {@code null} to clear tint
   */
  @Override
  public void setBackgroundTintList(@Nullable ColorStateList tint) {
    if (backgroundTint != tint) {
      backgroundTint = tint;
      getImpl().setBackgroundTintList(tint);
    }
  }

  /**
   * Returns the blending mode used to apply the tint to the background drawable, if specified.
   *
   * @return the blending mode used to apply the tint to the background drawable
   * @see #setBackgroundTintMode(PorterDuff.Mode)
   */
  @Nullable
  @Override
  public PorterDuff.Mode getBackgroundTintMode() {
    return backgroundTintMode;
  }

  /**
   * Specifies the blending mode used to apply the tint specified by {@link
   * #setBackgroundTintList(ColorStateList)}} to the background drawable. The default mode is {@link
   * PorterDuff.Mode#SRC_IN}.
   *
   * @param tintMode the blending mode used to apply the tint, may be {@code null} to clear tint
   */
  @Override
  public void setBackgroundTintMode(@Nullable PorterDuff.Mode tintMode) {
    if (backgroundTintMode != tintMode) {
      backgroundTintMode = tintMode;
      getImpl().setBackgroundTintMode(tintMode);
    }
  }

  /**
   * Compat method to support {@link TintableBackgroundView}. Use {@link
   * #setBackgroundTintList(ColorStateList)} directly instead.
   */
  @Override
  public void setSupportBackgroundTintList(@Nullable ColorStateList tint) {
    setBackgroundTintList(tint);
  }

  /**
   * Compat method to support {@link TintableBackgroundView}. Use {@link #getBackgroundTintList()}
   * directly instead.
   */
  @Nullable
  @Override
  public ColorStateList getSupportBackgroundTintList() {
    return getBackgroundTintList();
  }

  /**
   * Compat method to support {@link TintableBackgroundView}. Use {@link
   * #setBackgroundTintMode(Mode)} directly instead.
   */
  @Override
  public void setSupportBackgroundTintMode(@Nullable Mode tintMode) {
    setBackgroundTintMode(tintMode);
  }

  /**
   * Compat method to support {@link TintableBackgroundView}. Use {@link #getBackgroundTintMode()}
   * directly instead.
   */
  @Nullable
  @Override
  public Mode getSupportBackgroundTintMode() {
    return getBackgroundTintMode();
  }

  @Override
  public void setSupportImageTintList(@Nullable ColorStateList tint) {
    if (imageTint != tint) {
      imageTint = tint;
      onApplySupportImageTint();
    }
  }

  @Nullable
  @Override
  public ColorStateList getSupportImageTintList() {
    return imageTint;
  }

  @Override
  public void setSupportImageTintMode(@Nullable Mode tintMode) {
    if (imageMode != tintMode) {
      imageMode = tintMode;
      onApplySupportImageTint();
    }
  }

  @Nullable
  @Override
  public Mode getSupportImageTintMode() {
    return imageMode;
  }

  private void onApplySupportImageTint() {
    Drawable drawable = getDrawable();
    if (drawable == null) {
      return;
    }

    if (imageTint == null) {
      DrawableCompat.clearColorFilter(drawable);
      return;
    }

    int color = imageTint.getColorForState(getDrawableState(), Color.TRANSPARENT);
    Mode mode = imageMode;
    if (mode == null) {
      mode = Mode.SRC_IN;
    }

    drawable
        .mutate()
        .setColorFilter(AppCompatDrawableManager.getPorterDuffColorFilter(color, mode));
  }

  @Override
  public void setBackgroundDrawable(Drawable background) {
    Log.i(LOG_TAG, "Setting a custom background is not supported.");
  }

  @Override
  public void setBackgroundResource(int resid) {
    Log.i(LOG_TAG, "Setting a custom background is not supported.");
  }

  @Override
  public void setBackgroundColor(int color) {
    Log.i(LOG_TAG, "Setting a custom background is not supported.");
  }

  @Override
  public void setImageResource(@DrawableRes int resId) {
    // Intercept this call and instead retrieve the Drawable via the image helper
    imageHelper.setImageResource(resId);
  }

  @Override
  public void setImageDrawable(@Nullable Drawable drawable) {
    super.setImageDrawable(drawable);
    getImpl().updateImageMatrixScale();
  }

  /**
   * Shows the button.
   *
   * <p>This method will animate the button show if the view has already been laid out.
   */
  public void show() {
    show(null);
  }

  /**
   * Shows the button.
   *
   * <p>This method will animate the button show if the view has already been laid out.
   *
   * @param listener the listener to notify when this view is shown
   */
  public void show(@Nullable final OnVisibilityChangedListener listener) {
    show(listener, true);
  }

  void show(OnVisibilityChangedListener listener, boolean fromUser) {
    getImpl().show(wrapOnVisibilityChangedListener(listener), fromUser);
  }

  public void addOnShowAnimationListener(@NonNull AnimatorListener listener) {
    getImpl().addOnShowAnimationListener(listener);
  }

  public void removeOnShowAnimationListener(@NonNull AnimatorListener listener) {
    getImpl().removeOnShowAnimationListener(listener);
  }

  /**
   * Hides the button.
   *
   * <p>This method will animate the button hide if the view has already been laid out.
   */
  public void hide() {
    hide(null);
  }

  /**
   * Hides the button.
   *
   * <p>This method will animate the button hide if the view has already been laid out.
   *
   * @param listener the listener to notify when this view is hidden
   */
  public void hide(@Nullable OnVisibilityChangedListener listener) {
    hide(listener, true);
  }

  void hide(@Nullable OnVisibilityChangedListener listener, boolean fromUser) {
    getImpl().hide(wrapOnVisibilityChangedListener(listener), fromUser);
  }

  public void addOnHideAnimationListener(@NonNull AnimatorListener listener) {
    getImpl().addOnHideAnimationListener(listener);
  }

  public void removeOnHideAnimationListener(@NonNull AnimatorListener listener) {
    getImpl().removeOnHideAnimationListener(listener);
  }

  @Override
  public boolean setExpanded(boolean expanded) {
    return expandableWidgetHelper.setExpanded(expanded);
  }

  @Override
  public boolean isExpanded() {
    return expandableWidgetHelper.isExpanded();
  }

  @Override
  public void setExpandedComponentIdHint(@IdRes int expandedComponentIdHint) {
    expandableWidgetHelper.setExpandedComponentIdHint(expandedComponentIdHint);
  }

  @Override
  public int getExpandedComponentIdHint() {
    return expandableWidgetHelper.getExpandedComponentIdHint();
  }

  /**
   * Set whether FloatingActionButton should add inner padding on platforms Lollipop and after, to
   * ensure consistent dimensions on all platforms.
   *
   * @param useCompatPadding true if FloatingActionButton is adding inner padding on platforms
   *     Lollipop and after, to ensure consistent dimensions on all platforms.
   * @attr ref com.google.android.material.R.styleable#FloatingActionButton_useCompatPadding
   * @see #getUseCompatPadding()
   */
  public void setUseCompatPadding(boolean useCompatPadding) {
    if (compatPadding != useCompatPadding) {
      compatPadding = useCompatPadding;
      getImpl().onCompatShadowChanged();
    }
  }

  /**
   * Returns whether FloatingActionButton will add inner padding on platforms Lollipop and after.
   *
   * @return true if FloatingActionButton is adding inner padding on platforms Lollipop and after,
   *     to ensure consistent dimensions on all platforms.
   * @attr ref com.google.android.material.R.styleable#FloatingActionButton_useCompatPadding
   * @see #setUseCompatPadding(boolean)
   */
  public boolean getUseCompatPadding() {
    return compatPadding;
  }

  /**
   * Sets the size of the button.
   *
   * <p>The options relate to the options available on the material design specification. {@link
   * #SIZE_NORMAL} is larger than {@link #SIZE_MINI}. {@link #SIZE_AUTO} will choose an appropriate
   * size based on the screen size.
   *
   * <p>Calling this method will turn off custom sizing (see {@link #setCustomSize(int)}) if it was
   * previously on.
   *
   * @param size one of {@link #SIZE_NORMAL}, {@link #SIZE_MINI} or {@link #SIZE_AUTO}
   * @attr ref com.google.android.material.R.styleable#FloatingActionButton_fabSize
   */
  public void setSize(@Size int size) {
    customSize = NO_CUSTOM_SIZE;
    if (size != this.size) {
      this.size = size;
      requestLayout();
    }
  }

  /**
   * Returns the chosen size for this button.
   *
   * @return one of {@link #SIZE_NORMAL}, {@link #SIZE_MINI} or {@link #SIZE_AUTO}
   * @see #setSize(int)
   */
  @Size
  public int getSize() {
    return size;
  }

  @Nullable
  private InternalVisibilityChangedListener wrapOnVisibilityChangedListener(
      @Nullable final OnVisibilityChangedListener listener) {
    if (listener == null) {
      return null;
    }

    return new InternalVisibilityChangedListener() {
      @Override
      public void onShown() {
        listener.onShown(FloatingActionButton.this);
      }

      @Override
      public void onHidden() {
        listener.onHidden(FloatingActionButton.this);
      }
    };
  }

  public boolean isOrWillBeHidden() {
    return getImpl().isOrWillBeHidden();
  }

  public boolean isOrWillBeShown() {
    return getImpl().isOrWillBeShown();
  }

  /**
   * Sets the size of the button to be a custom value in pixels.
   *
   * <p>If you've set a custom size and would like to clear it, you can use the {@link
   * #clearCustomSize()} method. If called, custom sizing will not be used and the size will be
   * calculated based on the value set using {@link #setSize(int)} or the {@code fabSize} attribute.
   *
   * @param size preferred size in pixels, or {@link #NO_CUSTOM_SIZE}
   * @attr ref com.google.android.material.R.styleable.FloatingActionButton_fabCustomSize
   */
  public void setCustomSize(@Px int size) {
    if (size < 0) {
      throw new IllegalArgumentException("Custom size must be non-negative");
    }

    customSize = size;
  }

  /**
   * Returns the custom size for this {@link FloatingActionButton}.
   *
   * @return size in pixels, or {@link #NO_CUSTOM_SIZE}
   */
  @Px
  public int getCustomSize() {
    return customSize;
  }

  /**
   * Clears the custom size for this {@link FloatingActionButton}.
   *
   * <p>If called, custom sizing will not be used and the size will be calculated based on the value
   * set using {@link #setSize(int)} or the {@code fabSize} attribute
   */
  public void clearCustomSize() {
    setCustomSize(NO_CUSTOM_SIZE);
  }

  int getSizeDimension() {
    return getSizeDimension(size);
  }

  private int getSizeDimension(@Size final int size) {
    if (customSize != NO_CUSTOM_SIZE) {
      return customSize;
    }

    final Resources res = getResources();
    switch (size) {
      case SIZE_AUTO:
        // If we're set to auto, grab the size from resources and refresh
        final int width = res.getConfiguration().screenWidthDp;
        final int height = res.getConfiguration().screenHeightDp;
        return Math.max(width, height) < AUTO_MINI_LARGEST_SCREEN_WIDTH
            ? getSizeDimension(SIZE_MINI)
            : getSizeDimension(SIZE_NORMAL);
      case SIZE_MINI:
        return res.getDimensionPixelSize(R.dimen.design_fab_size_mini);
      case SIZE_NORMAL:
      default:
        return res.getDimensionPixelSize(R.dimen.design_fab_size_normal);
    }
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    getImpl().onAttachedToWindow();
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    getImpl().onDetachedFromWindow();
  }

  @Override
  protected void drawableStateChanged() {
    super.drawableStateChanged();
    getImpl().onDrawableStateChanged(getDrawableState());
  }

  @Override
  public void jumpDrawablesToCurrentState() {
    super.jumpDrawablesToCurrentState();
    getImpl().jumpDrawableToCurrentState();
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    ExtendableSavedState state = new ExtendableSavedState(superState);

    state.extendableStates.put(
        EXPANDABLE_WIDGET_HELPER_KEY, expandableWidgetHelper.onSaveInstanceState());

    return state;
  }

  @Override
  protected void onRestoreInstanceState(Parcelable state) {
    if (!(state instanceof ExtendableSavedState)) {
      super.onRestoreInstanceState(state);
      return;
    }

    ExtendableSavedState ess = (ExtendableSavedState) state;
    super.onRestoreInstanceState(ess.getSuperState());

    expandableWidgetHelper.onRestoreInstanceState(
        ess.extendableStates.get(EXPANDABLE_WIDGET_HELPER_KEY));
  }

  /**
   * Return in {@code rect} the bounds of the actual floating action button content in view-local
   * coordinates. This is defined as anything within any visible shadow.
   *
   * @return true if this view actually has been laid out and has a content rect, else false.
   * @deprecated prefer {@link FloatingActionButton#getMeasuredContentRect} instead, so you don't
   *     need to handle the case where the view isn't laid out.
   */
  @Deprecated
  public boolean getContentRect(@NonNull Rect rect) {
    if (ViewCompat.isLaidOut(this)) {
      rect.set(0, 0, getWidth(), getHeight());
      offsetRectWithShadow(rect);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Return in {@code rect} the bounds of the actual floating action button content in view-local
   * coordinates. This is defined as anything within any visible shadow.
   */
  public void getMeasuredContentRect(@NonNull Rect rect) {
    rect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
    offsetRectWithShadow(rect);
  }

  private void offsetRectWithShadow(@NonNull Rect rect) {
    rect.left += shadowPadding.left;
    rect.top += shadowPadding.top;
    rect.right -= shadowPadding.right;
    rect.bottom -= shadowPadding.bottom;
  }

  /** Returns the FloatingActionButton's background, minus any compatible shadow implementation. */
  @NonNull
  public Drawable getContentBackground() {
    return getImpl().getContentBackground();
  }

  private static int resolveAdjustedSize(int desiredSize, int measureSpec) {
    int result = desiredSize;
    int specMode = MeasureSpec.getMode(measureSpec);
    int specSize = MeasureSpec.getSize(measureSpec);
    switch (specMode) {
      case MeasureSpec.UNSPECIFIED:
        // Parent says we can be as big as we want. Just don't be larger
        // than max size imposed on ourselves.
        result = desiredSize;
        break;
      case MeasureSpec.AT_MOST:
        // Parent says we can be as big as we want, up to specSize.
        // Don't be larger than specSize, and don't be larger than
        // the max size imposed on ourselves.
        result = Math.min(desiredSize, specSize);
        break;
      case MeasureSpec.EXACTLY:
        // No choice. Do what we are told.
        result = specSize;
        break;
      default:
        throw new IllegalArgumentException();
    }
    return result;
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    if (ev.getAction() == MotionEvent.ACTION_DOWN) {
      // Skipping the gesture if it doesn't start in the FAB 'content' area
      if (getContentRect(touchArea) && !touchArea.contains((int) ev.getX(), (int) ev.getY())) {
        return false;
      }
    }
    return super.onTouchEvent(ev);
  }

  /**
   * Behavior designed for use with {@link FloatingActionButton} instances. Its main function is to
   * move {@link FloatingActionButton} views so that any displayed {@link Snackbar}s do not cover
   * them.
   */
  // TODO: remove this generic type after the widget migration is done
  public static class Behavior extends BaseBehavior<FloatingActionButton> {

    public Behavior() {
      super();
    }

    public Behavior(Context context, AttributeSet attrs) {
      super(context, attrs);
    }
  }

  /**
   * Behavior designed for use with {@link FloatingActionButton} instances. Its main function is to
   * move {@link FloatingActionButton} views so that any displayed {@link Snackbar}s do not cover
   * them.
   */
  // TODO: remove this generic type after the widget migration is done
  protected static class BaseBehavior<T extends FloatingActionButton>
      extends CoordinatorLayout.Behavior<T> {
    private static final boolean AUTO_HIDE_DEFAULT = true;

    private Rect tmpRect;
    private OnVisibilityChangedListener internalAutoHideListener;
    private boolean autoHideEnabled;

    public BaseBehavior() {
      super();
      autoHideEnabled = AUTO_HIDE_DEFAULT;
    }

    public BaseBehavior(Context context, AttributeSet attrs) {
      super(context, attrs);
      TypedArray a =
          context.obtainStyledAttributes(attrs, R.styleable.FloatingActionButton_Behavior_Layout);
      autoHideEnabled =
          a.getBoolean(
              R.styleable.FloatingActionButton_Behavior_Layout_behavior_autoHide,
              AUTO_HIDE_DEFAULT);
      a.recycle();
    }

    /**
     * Sets whether the associated FloatingActionButton automatically hides when there is not enough
     * space to be displayed. This works with {@link AppBarLayout} and {@link BottomSheetBehavior}.
     *
     * @attr ref
     *     com.google.android.material.R.styleable#FloatingActionButton_Behavior_Layout_behavior_autoHide
     * @param autoHide true to enable automatic hiding
     */
    public void setAutoHideEnabled(boolean autoHide) {
      autoHideEnabled = autoHide;
    }

    /**
     * Returns whether the associated FloatingActionButton automatically hides when there is not
     * enough space to be displayed.
     *
     * @attr ref
     *     com.google.android.material.R.styleable#FloatingActionButton_Behavior_Layout_behavior_autoHide
     * @return true if enabled
     */
    public boolean isAutoHideEnabled() {
      return autoHideEnabled;
    }

    @Override
    public void onAttachedToLayoutParams(@NonNull CoordinatorLayout.LayoutParams lp) {
      if (lp.dodgeInsetEdges == Gravity.NO_GRAVITY) {
        // If the developer hasn't set dodgeInsetEdges, lets set it to BOTTOM so that
        // we dodge any Snackbars
        lp.dodgeInsetEdges = Gravity.BOTTOM;
      }
    }

    @Override
    public boolean onDependentViewChanged(
        CoordinatorLayout parent, FloatingActionButton child, View dependency) {
      if (dependency instanceof AppBarLayout) {
        // If we're depending on an AppBarLayout we will show/hide it automatically
        // if the FAB is anchored to the AppBarLayout
        updateFabVisibilityForAppBarLayout(parent, (AppBarLayout) dependency, child);
      } else if (isBottomSheet(dependency)) {
        updateFabVisibilityForBottomSheet(dependency, child);
      }
      return false;
    }

    private static boolean isBottomSheet(@NonNull View view) {
      final ViewGroup.LayoutParams lp = view.getLayoutParams();
      if (lp instanceof CoordinatorLayout.LayoutParams) {
        return ((CoordinatorLayout.LayoutParams) lp).getBehavior() instanceof BottomSheetBehavior;
      }
      return false;
    }

    @VisibleForTesting
    public void setInternalAutoHideListener(OnVisibilityChangedListener listener) {
      internalAutoHideListener = listener;
    }

    private boolean shouldUpdateVisibility(View dependency, FloatingActionButton child) {
      final CoordinatorLayout.LayoutParams lp =
          (CoordinatorLayout.LayoutParams) child.getLayoutParams();
      if (!autoHideEnabled) {
        return false;
      }

      if (lp.getAnchorId() != dependency.getId()) {
        // The anchor ID doesn't match the dependency, so we won't automatically
        // show/hide the FAB
        return false;
      }

      //noinspection RedundantIfStatement
      if (child.getUserSetVisibility() != VISIBLE) {
        // The view isn't set to be visible so skip changing its visibility
        return false;
      }

      return true;
    }

    private boolean updateFabVisibilityForAppBarLayout(
        CoordinatorLayout parent, AppBarLayout appBarLayout, FloatingActionButton child) {
      if (!shouldUpdateVisibility(appBarLayout, child)) {
        return false;
      }

      if (tmpRect == null) {
        tmpRect = new Rect();
      }

      // First, let's get the visible rect of the dependency
      final Rect rect = tmpRect;
      DescendantOffsetUtils.getDescendantRect(parent, appBarLayout, rect);

      if (rect.bottom <= appBarLayout.getMinimumHeightForVisibleOverlappingContent()) {
        // If the anchor's bottom is below the seam, we'll animate our FAB out
        child.hide(internalAutoHideListener, false);
      } else {
        // Else, we'll animate our FAB back in
        child.show(internalAutoHideListener, false);
      }
      return true;
    }

    private boolean updateFabVisibilityForBottomSheet(
        View bottomSheet, FloatingActionButton child) {
      if (!shouldUpdateVisibility(bottomSheet, child)) {
        return false;
      }
      CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
      if (bottomSheet.getTop() < child.getHeight() / 2 + lp.topMargin) {
        child.hide(internalAutoHideListener, false);
      } else {
        child.show(internalAutoHideListener, false);
      }
      return true;
    }

    @Override
    public boolean onLayoutChild(
        CoordinatorLayout parent, FloatingActionButton child, int layoutDirection) {
      // First, let's make sure that the visibility of the FAB is consistent
      final List<View> dependencies = parent.getDependencies(child);
      for (int i = 0, count = dependencies.size(); i < count; i++) {
        final View dependency = dependencies.get(i);
        if (dependency instanceof AppBarLayout) {
          if (updateFabVisibilityForAppBarLayout(parent, (AppBarLayout) dependency, child)) {
            break;
          }
        } else if (isBottomSheet(dependency)) {
          if (updateFabVisibilityForBottomSheet(dependency, child)) {
            break;
          }
        }
      }
      // Now let the CoordinatorLayout lay out the FAB
      parent.onLayoutChild(child, layoutDirection);
      // Now offset it if needed
      offsetIfNeeded(parent, child);
      return true;
    }

    @Override
    public boolean getInsetDodgeRect(
        @NonNull CoordinatorLayout parent,
        @NonNull FloatingActionButton child,
        @NonNull Rect rect) {
      // Since we offset so that any internal shadow padding isn't shown, we need to make
      // sure that the shadow isn't used for any dodge inset calculations
      final Rect shadowPadding = child.shadowPadding;
      rect.set(
          child.getLeft() + shadowPadding.left,
          child.getTop() + shadowPadding.top,
          child.getRight() - shadowPadding.right,
          child.getBottom() - shadowPadding.bottom);
      return true;
    }

    /**
     * Pre-Lollipop we use padding so that the shadow has enough space to be drawn. This method
     * offsets our layout position so that we're positioned correctly if we're on one of our
     * parent's edges.
     */
    private void offsetIfNeeded(CoordinatorLayout parent, FloatingActionButton fab) {
      final Rect padding = fab.shadowPadding;

      if (padding != null && padding.centerX() > 0 && padding.centerY() > 0) {
        final CoordinatorLayout.LayoutParams lp =
            (CoordinatorLayout.LayoutParams) fab.getLayoutParams();

        int offsetTB = 0;
        int offsetLR = 0;

        if (fab.getRight() >= parent.getWidth() - lp.rightMargin) {
          // If we're on the right edge, shift it the right
          offsetLR = padding.right;
        } else if (fab.getLeft() <= lp.leftMargin) {
          // If we're on the left edge, shift it the left
          offsetLR = -padding.left;
        }
        if (fab.getBottom() >= parent.getHeight() - lp.bottomMargin) {
          // If we're on the bottom edge, shift it down
          offsetTB = padding.bottom;
        } else if (fab.getTop() <= lp.topMargin) {
          // If we're on the top edge, shift it up
          offsetTB = -padding.top;
        }

        if (offsetTB != 0) {
          ViewCompat.offsetTopAndBottom(fab, offsetTB);
        }
        if (offsetLR != 0) {
          ViewCompat.offsetLeftAndRight(fab, offsetLR);
        }
      }
    }
  }

  /**
   * Returns the backward compatible elevation of the FloatingActionButton.
   *
   * @return the backward compatible elevation in pixels.
   * @attr ref com.google.android.material.R.styleable#FloatingActionButton_elevation
   * @see #setCompatElevation(float)
   */
  public float getCompatElevation() {
    return getImpl().getElevation();
  }

  /**
   * Updates the backward compatible elevation of the FloatingActionButton.
   *
   * @param elevation The backward compatible elevation in pixels.
   * @attr ref com.google.android.material.R.styleable#FloatingActionButton_elevation
   * @see #getCompatElevation()
   * @see #setUseCompatPadding(boolean)
   */
  public void setCompatElevation(float elevation) {
    getImpl().setElevation(elevation);
  }

  /**
   * Updates the backward compatible elevation of the FloatingActionButton.
   *
   * @param id The resource id of the backward compatible elevation.
   * @attr ref com.google.android.material.R.styleable#FloatingActionButton_elevation
   * @see #getCompatElevation()
   * @see #setUseCompatPadding(boolean)
   */
  public void setCompatElevationResource(@DimenRes int id) {
    setCompatElevation(getResources().getDimension(id));
  }

  /**
   * Returns the backward compatible hovered/focused translationZ of the FloatingActionButton.
   *
   * @return the backward compatible hovered/focused translationZ in pixels.
   * @attr ref com.google.android.material.R.styleable#FloatingActionButton_hoveredFocusedTranslationZ
   * @see #setCompatHoveredFocusedTranslationZ(float)
   */
  public float getCompatHoveredFocusedTranslationZ() {
    return getImpl().getHoveredFocusedTranslationZ();
  }

  /**
   * Updates the backward compatible hovered/focused translationZ of the FloatingActionButton.
   *
   * @param translationZ The backward compatible hovered/focused translationZ in pixels.
   * @attr ref com.google.android.material.R.styleable#FloatingActionButton_hoveredFocusedTranslationZ
   * @see #getCompatHoveredFocusedTranslationZ()
   * @see #setUseCompatPadding(boolean)
   */
  public void setCompatHoveredFocusedTranslationZ(float translationZ) {
    getImpl().setHoveredFocusedTranslationZ(translationZ);
  }

  /**
   * Updates the backward compatible hovered/focused translationZ of the FloatingActionButton.
   *
   * @param id The resource id of the backward compatible hovered/focused translationZ.
   * @attr ref com.google.android.material.R.styleable#FloatingActionButton_hoveredFocusedTranslationZ
   * @see #getCompatHoveredFocusedTranslationZ()
   * @see #setUseCompatPadding(boolean)
   */
  public void setCompatHoveredFocusedTranslationZResource(@DimenRes int id) {
    setCompatHoveredFocusedTranslationZ(getResources().getDimension(id));
  }

  /**
   * Returns the backward compatible pressed translationZ of the FloatingActionButton.
   *
   * @return the backward compatible pressed translationZ in pixels.
   * @attr ref com.google.android.material.R.styleable#FloatingActionButton_pressedTranslationZ
   * @see #setCompatPressedTranslationZ(float)
   */
  public float getCompatPressedTranslationZ() {
    return getImpl().getPressedTranslationZ();
  }

  /**
   * Updates the backward compatible pressed translationZ of the FloatingActionButton.
   *
   * @param translationZ The backward compatible pressed translationZ in pixels.
   * @attr ref com.google.android.material.R.styleable#FloatingActionButton_pressedTranslationZ
   * @see #getCompatPressedTranslationZ()
   * @see #setUseCompatPadding(boolean)
   */
  public void setCompatPressedTranslationZ(float translationZ) {
    getImpl().setPressedTranslationZ(translationZ);
  }

  /**
   * Updates the backward compatible pressed translationZ of the FloatingActionButton.
   *
   * @param id The resource id of the backward compatible pressed translationZ.
   * @attr ref com.google.android.material.R.styleable#FloatingActionButton_pressedTranslationZ
   * @see #getCompatPressedTranslationZ()
   * @see #setUseCompatPadding(boolean)
   */
  public void setCompatPressedTranslationZResource(@DimenRes int id) {
    setCompatPressedTranslationZ(getResources().getDimension(id));
  }

  /** Returns the motion spec for the show animation. */
  public MotionSpec getShowMotionSpec() {
    return getImpl().getShowMotionSpec();
  }

  /**
   * Updates the motion spec for the show animation.
   *
   * @attr ref com.google.android.material.R.styleable#FloatingActionButton_showMotionSpec
   */
  public void setShowMotionSpec(MotionSpec spec) {
    getImpl().setShowMotionSpec(spec);
  }

  /**
   * Updates the motion spec for the show animation.
   *
   * @attr ref com.google.android.material.R.styleable#FloatingActionButton_showMotionSpec
   */
  public void setShowMotionSpecResource(@AnimatorRes int id) {
    setShowMotionSpec(MotionSpec.createFromResource(getContext(), id));
  }

  /** Returns the motion spec for the hide animation. */
  public MotionSpec getHideMotionSpec() {
    return getImpl().getHideMotionSpec();
  }

  /**
   * Updates the motion spec for the hide animation.
   *
   * @attr ref com.google.android.material.R.styleable#FloatingActionButton_hideMotionSpec
   */
  public void setHideMotionSpec(MotionSpec spec) {
    getImpl().setHideMotionSpec(spec);
  }

  /**
   * Updates the motion spec for the hide animation.
   *
   * @attr ref com.google.android.material.R.styleable#FloatingActionButton_hideMotionSpec
   */
  public void setHideMotionSpecResource(@AnimatorRes int id) {
    setHideMotionSpec(MotionSpec.createFromResource(getContext(), id));
  }

  private FloatingActionButtonImpl getImpl() {
    if (impl == null) {
      impl = createImpl();
    }
    return impl;
  }

  private FloatingActionButtonImpl createImpl() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      return new FloatingActionButtonImplLollipop(this, new ShadowDelegateImpl());
    } else {
      return new FloatingActionButtonImpl(this, new ShadowDelegateImpl());
    }
  }

  private class ShadowDelegateImpl implements ShadowViewDelegate {
    ShadowDelegateImpl() {}

    @Override
    public float getRadius() {
      return getSizeDimension() / 2f;
    }

    @Override
    public void setShadowPadding(int left, int top, int right, int bottom) {
      shadowPadding.set(left, top, right, bottom);
      setPadding(
          left + imagePadding, top + imagePadding, right + imagePadding, bottom + imagePadding);
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
      FloatingActionButton.super.setBackgroundDrawable(background);
    }

    @Override
    public boolean isCompatPaddingEnabled() {
      return compatPadding;
    }
  }
}
