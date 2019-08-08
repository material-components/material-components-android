/*
 * Copyright (C) 2019 The Android Open Source Project
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

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import androidx.annotation.AnimatorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.shape.ShapeAppearanceModel;
import androidx.core.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Property;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout.AttachedBehavior;
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior;
import com.google.android.material.animation.MotionSpec;
import com.google.android.material.internal.DescendantOffsetUtils;
import com.google.android.material.internal.ThemeEnforcement;
import java.util.List;

/**
 * Extended floating action buttons are used for a special type of promoted action. They are
 * distinguished by an icon and a text floating above the UI and have special motion behaviors
 * related to morphing, launching, and the transferring anchor point.
 *
 * <p>Extended floating action buttons may have icon and text, but may also hold just an icon or
 * text.
 *
 * <p>As this class descends from {@link MaterialButton}, you can control the icon which is
 * displayed via {@link #setIcon(android.graphics.drawable.Drawable)}, and the text via {@link
 * #setText(CharSequence)}.
 *
 * <p>The background color of this view defaults to the your theme's {@code colorPrimary}. If you
 * wish to change this at runtime then you can do so via
 * {@link #setBackgroundTintList(android.content.res.ColorStateList)}.
 */
public class ExtendedFloatingActionButton extends MaterialButton implements AttachedBehavior {

  private static final int DEF_STYLE_RES =
      R.style.Widget_MaterialComponents_ExtendedFloatingActionButton_Icon;

  private static final int ANIM_STATE_NONE = 0;
  private static final int ANIM_STATE_HIDING = 1;
  private static final int ANIM_STATE_SHOWING = 2;

  private final Rect shadowPadding = new Rect();
  private int animState = ANIM_STATE_NONE;

  private final AnimatorTracker changeVisibilityTracker = new AnimatorTracker();
  private final MotionStrategy shrinkStrategy;
  private final MotionStrategy extendStrategy;
  private final MotionStrategy showStrategyFromUser = new ShowStrategy(
      changeVisibilityTracker, /* fromUser= */ true);
  private final MotionStrategy showStrategy = new ShowStrategy(
      changeVisibilityTracker, /* fromUser= */ false);
  private final MotionStrategy hideStrategyFromUser = new HideStrategy(
      changeVisibilityTracker, /* fromUser= */ true);
  private final MotionStrategy hideStrategy = new HideStrategy(
      changeVisibilityTracker, /* fromUser= */ false);

  private final Behavior<ExtendedFloatingActionButton> behavior;
  private int userSetVisibility;

  private boolean isExtended = true;
  private boolean isUsingPillCorner = true;

  /**
   * Callback to be invoked when the visibility or the state of an ExtendedFloatingActionButton
   * changes.
   */
  public abstract static class OnChangedListener {

    /**
     * Called when a ExtendedFloatingActionButton has been {@link
     * #show(ExtendedFloatingActionButton.OnChangedListener) shown}.
     *
     * @param extendedFab the FloatingActionButton that was shown.
     */
    public void onShown(ExtendedFloatingActionButton extendedFab) {}

    /**
     * Called when a ExtendedFloatingActionButton has been {@link
     * #hide(ExtendedFloatingActionButton.OnChangedListener) hidden}.
     *
     * @param extendedFab the ExtendedFloatingActionButton that was hidden.
     */
    public void onHidden(ExtendedFloatingActionButton extendedFab) {}

    /**
     * Called when a ExtendedFloatingActionButton has been {@link
     * #extend(ExtendedFloatingActionButton.OnChangedListener) extended} to show the icon and the
     * text.
     *
     * @param extendedFab the ExtendedFloatingActionButton that was extended.
     */
    public void onExtended(ExtendedFloatingActionButton extendedFab) {}

    /**
     * Called when a ExtendedFloatingActionButton has been {@link
     * #shrink(ExtendedFloatingActionButton.OnChangedListener) shrunken} to show just the icon.
     *
     * @param extendedFab the ExtendedFloatingActionButton that was shrunk.
     */
    public void onShrunken(ExtendedFloatingActionButton extendedFab) {}
  }

  public ExtendedFloatingActionButton(Context context) {
    this(context, null);
  }

  public ExtendedFloatingActionButton(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.extendedFloatingActionButtonStyle);
  }

  @SuppressWarnings("initialization")
  public ExtendedFloatingActionButton(
      Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    behavior = new ExtendedFloatingActionButtonBehavior<>(context, attrs);
    userSetVisibility = getVisibility();

    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.ExtendedFloatingActionButton, defStyleAttr, DEF_STYLE_RES);

    MotionSpec showMotionSpec =
        MotionSpec.createFromAttribute(
            context, a, R.styleable.ExtendedFloatingActionButton_showMotionSpec);
    MotionSpec hideMotionSpec =
        MotionSpec.createFromAttribute(
            context, a, R.styleable.ExtendedFloatingActionButton_hideMotionSpec);
    MotionSpec extendMotionSpec =
        MotionSpec.createFromAttribute(
            context, a, R.styleable.ExtendedFloatingActionButton_extendMotionSpec);
    MotionSpec shrinkMotionSpec =
        MotionSpec.createFromAttribute(
            context, a, R.styleable.ExtendedFloatingActionButton_shrinkMotionSpec);

    AnimatorTracker changeSizeTracker = new AnimatorTracker();
    extendStrategy = new ChangeSizeStrategy(
        changeSizeTracker,
        new Size() {
          @Override
          public int getWidth() {
            return getMeasuredWidth();
          }

          @Override
          public int getHeight() {
            return getMeasuredHeight();
          }
        },
        /* extending= */ true);

    shrinkStrategy = new ChangeSizeStrategy(
        changeSizeTracker,
        new Size() {
          @Override
          public int getWidth() {
            return getCollapsedSize();
          }

          @Override
          public int getHeight() {
            return getCollapsedSize();
          }
        },
        /* extending= */ false);

    showStrategy.setMotionSpec(showMotionSpec);
    hideStrategy.setMotionSpec(hideMotionSpec);
    showStrategyFromUser.setMotionSpec(showMotionSpec);
    hideStrategyFromUser.setMotionSpec(hideMotionSpec);

    extendStrategy.setMotionSpec(extendMotionSpec);
    shrinkStrategy.setMotionSpec(shrinkMotionSpec);
    a.recycle();

    ShapeAppearanceModel shapeAppearanceModel =
        new ShapeAppearanceModel(
            context, attrs, defStyleAttr, DEF_STYLE_RES, ShapeAppearanceModel.PILL);
    setShapeAppearanceModel(shapeAppearanceModel);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    // Shrink the button in case the text is empty.
    if (isExtended && TextUtils.isEmpty(getText()) && getIcon() != null) {
      isExtended = false;
      shrinkStrategy.performNow();
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    if (isUsingPillCorner) {
      getShapeAppearanceModel().setCornerRadius(getAdjustedRadius(getMeasuredHeight()));
    }
  }

  @NonNull
  @Override
  public Behavior<ExtendedFloatingActionButton> getBehavior() {
    return behavior;
  }

  @Override
  public void setShapeAppearanceModel(@NonNull ShapeAppearanceModel shapeAppearanceModel) {
    isUsingPillCorner = shapeAppearanceModel.isUsingPillCorner();
    super.setShapeAppearanceModel(shapeAppearanceModel);
  }

  @Override
  public void setCornerRadius(int cornerRadius) {
    isUsingPillCorner = cornerRadius == ShapeAppearanceModel.PILL;
    if (isUsingPillCorner) {
      cornerRadius = getAdjustedRadius(getMeasuredHeight());
    } else if (cornerRadius < 0) {
      cornerRadius = 0;
    }
    super.setCornerRadius(cornerRadius);
  }

  @Override
  public void setVisibility(int visibility) {
    internalSetVisibility(visibility, true);
  }

  private void internalSetVisibility(int visibility, boolean fromUser) {
    super.setVisibility(visibility);
    if (fromUser) {
      userSetVisibility = visibility;
    }
  }

  public final int getUserSetVisibility() {
    return userSetVisibility;
  }

  public final boolean isExtended() {
    return isExtended;
  }

  /**
   * Add a listener that will be invoked when this ExtendedFloatingActionButton is shown. See {@link
   * AnimatorListener}.
   *
   * <p>Components that add a listener should take care to remove it when finished via {@link
   * #removeOnShowAnimationListener(AnimatorListener)}.
   *
   * @param listener listener to add
   */
  public void addOnShowAnimationListener(@NonNull AnimatorListener listener) {
    showStrategy.addAnimationListener(listener);
    showStrategyFromUser.addAnimationListener(listener);
  }

  /**
   * Remove a listener that was previously added via
   * {@link #addOnShowAnimationListener(AnimatorListener)}.
   *
   * @param listener listener to remove
   */
  public void removeOnShowAnimationListener(@NonNull AnimatorListener listener) {
    showStrategy.removeAnimationListener(listener);
    showStrategyFromUser.addAnimationListener(listener);
  }

  /**
   * Add a listener that will be invoked when this ExtendedFloatingActionButton is hidden. See
   * {@link AnimatorListener}.
   *
   * <p>Components that add a listener should take care to remove it when finished via {@link
   * #removeOnHideAnimationListener(AnimatorListener)}.
   *
   * @param listener listener to add
   */
  public void addOnHideAnimationListener(@NonNull AnimatorListener listener) {
    hideStrategy.addAnimationListener(listener);
    hideStrategyFromUser.addAnimationListener(listener);
  }

  /**
   * Remove a listener that was previously added via
   * {@link #addOnHideAnimationListener(AnimatorListener)}.
   *
   * @param listener listener to remove
   */
  public void removeOnHideAnimationListener(@NonNull AnimatorListener listener) {
    hideStrategy.removeAnimationListener(listener);
    hideStrategyFromUser.removeAnimationListener(listener);
  }

  /**
   * Add a listener that will be invoked when this ExtendedFloatingActionButton is shrunk. See
   * {@link AnimatorListener}.
   *
   * <p>Components that add a listener should take care to remove it when finished via {@link
   * #removeOnShrinkAnimationListener(AnimatorListener)}.
   *
   * @param listener listener to add
   */
  public void addOnShrinkAnimationListener(@NonNull AnimatorListener listener) {
    shrinkStrategy.addAnimationListener(listener);
  }

  /**
   * Remove a listener that was previously added via
   * {@link #addOnShrinkAnimationListener(AnimatorListener)}.
   *
   * @param listener listener to remove
   */
  public void removeOnShrinkAnimationListener(@NonNull AnimatorListener listener) {
    shrinkStrategy.removeAnimationListener(listener);
  }

  /**
   * Add a listener that will be invoked when this ExtendedFloatingActionButton is extended. See
   * {@link AnimatorListener}.
   *
   * <p>Components that add a listener should take care to remove it when finished via {@link
   * #removeOnExtendAnimationListener(AnimatorListener)}.
   *
   * @param listener listener to add
   */
  public void addOnExtendAnimationListener(@NonNull AnimatorListener listener) {
    extendStrategy.addAnimationListener(listener);
  }

  /**
   * Remove a listener that was previously added via
   * {@link #addOnExtendAnimationListener(AnimatorListener)}.
   *
   * @param listener listener to remove
   */
  public void removeOnExtendAnimationListener(@NonNull AnimatorListener listener) {
    extendStrategy.removeAnimationListener(listener);
  }

  /**
   * Hides the button.
   *
   * <p>This method will animate the button hide if the view has already been laid out.
   */
  public void hide() {
    hide(true /* animate */);
  }

  /**
   * Hides the button.
   *
   * @param animate whether or not the button's hiding is animated
   */
  public void hide(boolean animate) {
    hide(true /* fromUser */, animate, null /* listener */);
  }

  /**
   * Hides the button.
   *
   * <p>This method will animate the button hide if the view has already been laid out.
   *
   * @param listener the listener to notify when this view is hidden
   */
  public void hide(@Nullable OnChangedListener listener) {
    hide(true /* fromUser */, true /* animate */, listener);
  }

  private void hide(
      final boolean fromUser, boolean animate, @Nullable final OnChangedListener listener) {
    if (isOrWillBeHidden()) {
      // We either are or will soon be hidden, skip the call
      return;
    }

    performMotion(fromUser ? hideStrategyFromUser : hideStrategy, animate, listener);
  }

  /**
   * Shows the button.
   *
   * <p>This method will animate the button show if the view has already been laid out.
   */
  public void show() {
    show(true /* animate */);
  }

  /**
   * Shows the button.
   *
   * @param animate whether or not the button's showing is animated
   */
  public void show(boolean animate) {
    show(true /* fromUser */, animate, null /* listener */);
  }

  /**
   * Shows the button.
   *
   * <p>This method will animate the button show if the view has already been laid out.
   *
   * @param listener the listener to notify when this view is shown
   */
  public void show(@Nullable OnChangedListener listener) {
    show(true /* fromUser */, true /* animate */, listener);
  }

  private void show(
      final boolean fromUser, boolean animate, @Nullable final OnChangedListener listener) {
    if (isOrWillBeShown()) {
      // We either are or will soon be visible, skip the call
      return;
    }

    performMotion(fromUser ? showStrategyFromUser : showStrategy, animate, listener);
  }

  /**
   * Extends the FAB to show the text and the icon.
   *
   * <p>This method will not affect an extended FAB which holds just text and no icon. Also, this
   * method will animate the button show if the view has already been laid out.
   *
   * @see #extend(boolean)
   */
  public void extend() {
    extend(true /* animate */);
  }

  /**
   * Extends the FAB to show the text and the icon.
   *
   * <p>This method will not affect an extended FAB which holds just text and no icon.
   *
   * @param animate whether or not the extending is animated
   */
  public void extend(boolean animate) {
    extend(animate, null);
  }

  /**
   * Extends the FAB to show the text and the icon.
   *
   * <p>This method will not affect an extended FAB which holds just text and no icon. Also, this
   * method will animate the button show if the view has already been laid out.
   *
   * @param listener the listener to notify when the FAB is extended
   */
  public void extend(@Nullable final OnChangedListener listener) {
    extend(true, listener);
  }

  private void extend(boolean animate, @Nullable final OnChangedListener listener) {
    if (isExtended || getIcon() == null || TextUtils.isEmpty(getText())) {
      return;
    }

    isExtended = true;
    performMotion(extendStrategy, animate, listener);
  }


  /**
   * Shrinks the FAB to show just the icon.
   *
   * <p>This method will not affect an extended FAB which holds just text and no icon. Also, this
   * method will animate the button show if the view has already been laid out.
   *
   * @see #shrink(boolean)
   */
  public void shrink() {
    shrink(true /* animate */);
  }

  /**
   * Shrinks the FAB to show just the icon.
   *
   * <p>This method will not affect an extended FAB which holds just text and no icon.
   *
   * @param animate whether or not the shrinking is animated
   */
  public void shrink(boolean animate) {
    shrink(animate, null);
  }

  /**
   * Shrinks the FAB to show just the icon.
   *
   * <p>This method will not affect an extended FAB which holds just text and no icon. Also, this
   * method will animate the button show if the view has already been laid out.
   *
   * @param listener the listener to notify when the FAB shrank
   */
  public void shrink(@Nullable final OnChangedListener listener) {
    shrink(true, listener);
  }

  private void shrink(boolean animate, @Nullable final OnChangedListener listener) {
    if (!isExtended || getIcon() == null || TextUtils.isEmpty(getText())) {
      return;
    }

    isExtended = false;
    performMotion(shrinkStrategy, animate, listener);
  }

  /** Returns the motion spec for the show animation. */
  @Nullable
  public MotionSpec getShowMotionSpec() {
    return showStrategy.getMotionSpec();
  }

  /**
   * Updates the motion spec for the show animation.
   *
   * @attr ref com.google.android.material.R.styleable#ExtendedFloatingActionButton_showMotionSpec
   */
  public void setShowMotionSpec(@Nullable MotionSpec spec) {
    showStrategy.setMotionSpec(spec);
    showStrategyFromUser.setMotionSpec(spec);
  }

  /**
   * Updates the motion spec for the show animation.
   *
   * @attr ref com.google.android.material.R.styleable#ExtendedFloatingActionButton_showMotionSpec
   */
  public void setShowMotionSpecResource(@AnimatorRes int id) {
    setShowMotionSpec(MotionSpec.createFromResource(getContext(), id));
  }

  /** Returns the motion spec for the hide animation. */
  @Nullable
  public MotionSpec getHideMotionSpec() {
    return hideStrategy.getMotionSpec();
  }

  /**
   * Updates the motion spec for the hide animation.
   *
   * @attr ref com.google.android.material.R.styleable#ExtendedFloatingActionButton_hideMotionSpec
   */
  public void setHideMotionSpec(@Nullable MotionSpec spec) {
    hideStrategyFromUser.setMotionSpec(spec);
    hideStrategy.setMotionSpec(spec);
  }

  /**
   * Updates the motion spec for the hide animation.
   *
   * @attr ref com.google.android.material.R.styleable#ExtendedFloatingActionButton_hideMotionSpec
   */
  public void setHideMotionSpecResource(@AnimatorRes int id) {
    setHideMotionSpec(MotionSpec.createFromResource(getContext(), id));
  }

  /** Returns the motion spec for the extend animation. */
  @Nullable
  public MotionSpec getExtendMotionSpec() {
    return extendStrategy.getMotionSpec();
  }

  /**
   * Updates the motion spec for the extend animation.
   *
   * @attr ref com.google.android.material.R.styleable#ExtendedFloatingActionButton_extendMotionSpec
   */
  public void setExtendMotionSpec(@Nullable MotionSpec spec) {
    extendStrategy.setMotionSpec(spec);
  }

  /**
   * Updates the motion spec for the extend animation.
   *
   * @attr ref com.google.android.material.R.styleable#ExtendedFloatingActionButton_extendMotionSpec
   */
  public void setExtendMotionSpecResource(@AnimatorRes int id) {
    setExtendMotionSpec(MotionSpec.createFromResource(getContext(), id));
  }

  /**
   * Returns the motion spec for the shrink animation.
   */
  @Nullable
  public MotionSpec getShrinkMotionSpec() {
    return shrinkStrategy.getMotionSpec();
  }

  /**
   * Updates the motion spec for the shrink animation.
   *
   * @attr ref com.google.android.material.R.styleable#ExtendedFloatingActionButton_shrinkMotionSpec
   */
  public void setShrinkMotionSpec(@Nullable MotionSpec spec) {
    shrinkStrategy.setMotionSpec(spec);
  }

  /**
   * Updates the motion spec for the shrink animation.
   *
   * @attr ref com.google.android.material.R.styleable#ExtendedFloatingActionButton_shrinkMotionSpec
   */
  public void setShrinkMotionSpecResource(@AnimatorRes int id) {
    setShrinkMotionSpec(MotionSpec.createFromResource(getContext(), id));
  }

  boolean isUsingPillCorner() {
    return isUsingPillCorner;
  }

  private void performMotion(
      final MotionStrategy strategy, boolean animate, @Nullable final OnChangedListener listener) {
    boolean shouldAnimate = animate && shouldAnimateVisibilityChange();
    if (!shouldAnimate) {
      strategy.performNow();
      strategy.onChange(listener);
      return;
    }

    measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
    Animator animator = strategy.createAnimator();
    animator.addListener(
        new AnimatorListenerAdapter() {
          private boolean cancelled;

          @Override
          public void onAnimationStart(Animator animation) {
            strategy.onAnimationStart(animation);
            cancelled = false;
          }

          @Override
          public void onAnimationCancel(Animator animation) {
            cancelled = true;
            strategy.onAnimationCancel();
          }

          @Override
          public void onAnimationEnd(Animator animation) {
            strategy.onAnimationEnd();
            if (cancelled || listener == null) {
              return;
            }

            strategy.onChange(listener);
          }
        });

    for (AnimatorListener l : strategy.getListeners()) {
      animator.addListener(l);
    }

    animator.start();
  }

  private boolean isOrWillBeShown() {
    if (getVisibility() != View.VISIBLE) {
      // If we're not currently visible, return true if we're animating to be shown
      return animState == ANIM_STATE_SHOWING;
    } else {
      // Otherwise if we're visible, return true if we're not animating to be hidden
      return animState != ANIM_STATE_HIDING;
    }
  }

  private boolean isOrWillBeHidden() {
    if (getVisibility() == View.VISIBLE) {
      // If we're currently visible, return true if we're animating to be hidden
      return animState == ANIM_STATE_HIDING;
    } else {
      // Otherwise if we're not visible, return true if we're not animating to be shown
      return animState != ANIM_STATE_SHOWING;
    }
  }

  private boolean shouldAnimateVisibilityChange() {
    return ViewCompat.isLaidOut(this) && !isInEditMode();
  }

  /**
   * A Property wrapper around the <code>width</code> functionality handled by the {@link
   * LayoutParams#width} value.
   */
   static final Property<View, Float> WIDTH =
      new Property<View, Float>(Float.class, "width") {
        @Override
        public void set(View object, Float value) {
          object.getLayoutParams().width = value.intValue();
          object.requestLayout();
        }

        @Override
        public Float get(View object) {
          return (float) object.getLayoutParams().width;
        }
      };

  /**
   * A Property wrapper around the <code>height</code> functionality handled by the {@link
   * LayoutParams#height} value.
   */
   static final Property<View, Float> HEIGHT =
      new Property<View, Float>(Float.class, "height") {
        @Override
        public void set(View object, Float value) {
          object.getLayoutParams().height = value.intValue();
          object.requestLayout();
        }

        @Override
        public Float get(View object) {
          return (float) object.getLayoutParams().height;
        }
      };

  /**
   * A Property wrapper around the <code>cornerRadius</code> functionality handled by the {@link
   * ExtendedFloatingActionButton#setCornerRadius(int)} and {@link
   * ExtendedFloatingActionButton#getCornerRadius()} methods.
   */
  static final Property<View, Float> CORNER_RADIUS =
      new Property<View, Float>(Float.class, "cornerRadius") {
        @Override
        public void set(View object, Float value) {
          ((ExtendedFloatingActionButton) object)
              .getShapeAppearanceModel()
              .setCornerRadius(value.intValue());
        }

        @Override
        public Float get(View object) {
          return ((ExtendedFloatingActionButton) object)
              .getShapeAppearanceModel()
              .getTopRightCorner()
              .getCornerSize();
        }
      };

  /**
   * Returns an adjusted radius value that corrects any rounding errors.
   *
   * <p>TODO(b/121352029): Remove this method once this bug is fixed.
   */
  private int getAdjustedRadius(int value) {
    return (value - 1) / 2;
  }

  /**
   * Shrink to the smaller value between paddingStart and paddingEnd, such that when shrunk the icon
   * will be centered.
   */
  @VisibleForTesting
  int getCollapsedSize() {
    return Math.min(ViewCompat.getPaddingStart(this), ViewCompat.getPaddingEnd(this)) * 2
        + getIconSize();
  }

  /**
   * Behavior designed for use with {@link ExtendedFloatingActionButton} instances. Its main
   * function is to move {@link ExtendedFloatingActionButton} views so that any displayed {@link
   * com.google.android.material.snackbar.Snackbar}s do not cover them.
   */
  protected static class ExtendedFloatingActionButtonBehavior<
      T extends ExtendedFloatingActionButton>
      extends CoordinatorLayout.Behavior<T> {
    private static final boolean AUTO_HIDE_DEFAULT = false;
    private static final boolean AUTO_SHRINK_DEFAULT = true;

    private Rect tmpRect;
    @Nullable private OnChangedListener internalAutoHideListener;
    @Nullable private OnChangedListener internalAutoShrinkListener;
    private boolean autoHideEnabled;
    private boolean autoShrinkEnabled;

    public ExtendedFloatingActionButtonBehavior() {
      super();
      autoHideEnabled = AUTO_HIDE_DEFAULT;
      autoShrinkEnabled = AUTO_SHRINK_DEFAULT;
    }

    // Behavior attrs should be nullable in the framework
    @SuppressWarnings("argument.type.incompatible")
    public ExtendedFloatingActionButtonBehavior(Context context, @Nullable AttributeSet attrs) {

      super(context, attrs);
      TypedArray a =
          context.obtainStyledAttributes(
              attrs, R.styleable.ExtendedFloatingActionButton_Behavior_Layout);
      autoHideEnabled =
          a.getBoolean(
              R.styleable.ExtendedFloatingActionButton_Behavior_Layout_behavior_autoHide,
              AUTO_HIDE_DEFAULT);
      autoShrinkEnabled =
          a.getBoolean(
              R.styleable.ExtendedFloatingActionButton_Behavior_Layout_behavior_autoShrink,
              AUTO_SHRINK_DEFAULT);
      a.recycle();
    }

    /**
     * Sets whether the associated ExtendedFloatingActionButton automatically hides when there is
     * not enough space to be displayed. This works with {@link AppBarLayout} and {@link
     * BottomSheetBehavior}.
     *
     * <p>In case auto-shrink is enabled, it will take precedence over the auto-hide option.
     *
     * @attr ref
     *     com.google.android.material.R.styleable#ExtendedFloatingActionButton_Behavior_Layout_behavior_autoHide
     * @param autoHide true to enable automatic hiding
     */
    public void setAutoHideEnabled(boolean autoHide) {
      autoHideEnabled = autoHide;
    }

    /**
     * Returns whether the associated ExtendedFloatingActionButton automatically hides when there is
     * not enough space to be displayed.
     *
     * @attr ref
     *     com.google.android.material.R.styleable#ExtendedFloatingActionButton_Behavior_Layout_behavior_autoHide
     * @return true if enabled
     */
    public boolean isAutoHideEnabled() {
      return autoHideEnabled;
    }

    /**
     * Sets whether the associated ExtendedFloatingActionButton automatically shrink when there is
     * not enough space to be displayed. This works with {@link AppBarLayout} and {@link
     * BottomSheetBehavior}.
     *
     * @attr ref
     *     com.google.android.material.R.styleable#ExtendedFloatingActionButton_Behavior_Layout_behavior_autoShrink
     * @param autoShrink true to enable automatic shrinking
     */
    public void setAutoShrinkEnabled(boolean autoShrink) {
      autoShrinkEnabled = autoShrink;
    }

    /**
     * Returns whether the associated ExtendedFloatingActionButton automatically shrinks when there
     * is not enough space to be displayed.
     *
     * @attr ref
     *     com.google.android.material.R.styleable#ExtendedFloatingActionButton_Behavior_Layout_behavior_autoShrink
     * @return true if enabled
     */
    public boolean isAutoShrinkEnabled() {
      return autoShrinkEnabled;
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
        CoordinatorLayout parent, ExtendedFloatingActionButton child, View dependency) {
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
    public void setInternalAutoHideListener(@Nullable OnChangedListener listener) {
      internalAutoHideListener = listener;
    }

    @VisibleForTesting
    public void setInternalAutoShrinkListener(@Nullable OnChangedListener listener) {
      internalAutoShrinkListener = listener;
    }

    private boolean shouldUpdateVisibility(View dependency, ExtendedFloatingActionButton child) {
      final CoordinatorLayout.LayoutParams lp =
          (CoordinatorLayout.LayoutParams) child.getLayoutParams();
      if (!autoHideEnabled && !autoShrinkEnabled) {
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
        CoordinatorLayout parent, AppBarLayout appBarLayout, ExtendedFloatingActionButton child) {
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
        shrinkOrHide(child);
      } else {
        // Else, we'll animate our FAB back in
        extendOrShow(child);
      }
      return true;
    }

    private boolean updateFabVisibilityForBottomSheet(
        View bottomSheet, ExtendedFloatingActionButton child) {
      if (!shouldUpdateVisibility(bottomSheet, child)) {
        return false;
      }
      CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
      if (bottomSheet.getTop() < child.getHeight() / 2 + lp.topMargin) {
        shrinkOrHide(child);
      } else {
        extendOrShow(child);
      }
      return true;
    }

    /**
     * Shrinks the Extended FAB, in case auto-shrink is enabled, or hides it in case auto-hide is
     * enabled. The priority is given to the default shrink option, and the button will be hidden
     * only when the auto-shrink is {@code false} and auto-hide is {@code true}.
     *
     * @attr ref
     *     com.google.android.material.R.styleable#ExtendedFloatingActionButton_Behavior_Layout_behavior_autoShrink
     * @attr ref
     *     com.google.android.material.R.styleable#ExtendedFloatingActionButton_Behavior_Layout_behavior_autoHide
     * @see #setAutoShrinkEnabled(boolean)
     * @see #setAutoHideEnabled(boolean)
     */
    protected void shrinkOrHide(@NonNull ExtendedFloatingActionButton fab) {
      if (autoShrinkEnabled) {
        fab.shrink(internalAutoShrinkListener);
      } else if (autoHideEnabled) {
        fab.hide(false /* fromUser */, true /* animate */, internalAutoHideListener);
      }
    }

    /**
     * Extends the Extended FAB, in case auto-shrink is enabled, or show it in case auto-hide is
     * enabled. The priority is given to the default extend option, and the button will be shown
     * only when the auto-shrink is {@code false} and auto-hide is {@code true}.
     *
     * @attr ref
     *     com.google.android.material.R.styleable#ExtendedFloatingActionButton_Behavior_Layout_behavior_autoShrink
     * @attr ref
     *     com.google.android.material.R.styleable#ExtendedFloatingActionButton_Behavior_Layout_behavior_autoHide
     * @see #setAutoShrinkEnabled(boolean)
     * @see #setAutoHideEnabled(boolean)
     */
    protected void extendOrShow(@NonNull ExtendedFloatingActionButton fab) {
      if (autoShrinkEnabled) {
        fab.extend(internalAutoShrinkListener);
      } else if (autoHideEnabled) {
        fab.show(false /* fromUser */, true /* animate */, internalAutoHideListener);
      }
    }

    @Override
    public boolean onLayoutChild(
        CoordinatorLayout parent, ExtendedFloatingActionButton child, int layoutDirection) {
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
        @NonNull ExtendedFloatingActionButton child,
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
    private void offsetIfNeeded(CoordinatorLayout parent, ExtendedFloatingActionButton fab) {
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

  interface Size {
    int getWidth();
    int getHeight();
  }

  class ChangeSizeStrategy extends BaseMotionStrategy {

    private final Size size;
    private final boolean extending;

    ChangeSizeStrategy(AnimatorTracker animatorTracker, Size size, boolean extending) {
      super(ExtendedFloatingActionButton.this, animatorTracker);
      this.size = size;
      this.extending = extending;
    }

    @Override
    public void performNow() {
      LayoutParams layoutParams = getLayoutParams();
      if (layoutParams == null) {
        return;
      }

      if (extending) {
        measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
      }

      layoutParams.width = size.getWidth();
      layoutParams.height = size.getHeight();
      requestLayout();
    }

    @Override
    public void onChange(@Nullable final OnChangedListener listener) {
      if (listener == null) {
        return;
      }

      if (extending) {
        listener.onExtended(ExtendedFloatingActionButton.this);
      } else {
        listener.onShrunken(ExtendedFloatingActionButton.this);
      }
    }

    @Override
    public int getDefaultMotionSpecResource() {
      return R.animator.mtrl_extended_fab_change_size_motion_spec;
    }

    @Override
    public AnimatorSet createAnimator() {
      MotionSpec spec = getCurrentMotionSpec();
      if (spec.hasPropertyValues("width")) {
        PropertyValuesHolder[] widthValues = spec.getPropertyValues("width");
        widthValues[0].setFloatValues(getWidth(), size.getWidth());
        spec.setPropertyValues("width", widthValues);
      }

      if (spec.hasPropertyValues("height")) {
        PropertyValuesHolder[] heightValues = spec.getPropertyValues("height");
        heightValues[0].setFloatValues(getHeight(), size.getHeight());
        spec.setPropertyValues("height", heightValues);
      }

      return super.createAnimator(spec);
    }

    @Override
    public void onAnimationStart(Animator animator) {
      super.onAnimationStart(animator);
      setHorizontallyScrolling(true);
    }

    @Override
    public void onAnimationEnd() {
      super.onAnimationEnd();
      setHorizontallyScrolling(false);
    }
  }

  class ShowStrategy extends BaseMotionStrategy {

    private final boolean fromUser;

    public ShowStrategy(AnimatorTracker animatorTracker, boolean fromUser) {
      super(ExtendedFloatingActionButton.this, animatorTracker);
      this.fromUser = fromUser;
    }

    @Override
    public void performNow() {
      internalSetVisibility(View.VISIBLE, fromUser);
      setAlpha(1f);
      setScaleY(1f);
      setScaleX(1f);
    }

    @Override
    public void onChange(@Nullable final OnChangedListener listener) {
      if (listener != null) {
        listener.onShown(ExtendedFloatingActionButton.this);
      }
    }

    @Override
    public int getDefaultMotionSpecResource() {
      return R.animator.mtrl_extended_fab_show_motion_spec;
    }

    @Override
    public void onAnimationStart(Animator animation) {
      super.onAnimationStart(animation);
      internalSetVisibility(View.VISIBLE, fromUser);
      animState = ANIM_STATE_SHOWING;
    }

    @Override
    public void onAnimationEnd() {
      super.onAnimationEnd();
      animState = ANIM_STATE_NONE;
    }
  }

  class HideStrategy extends BaseMotionStrategy {

    private final boolean fromUser;

    private boolean isCancelled;

    public HideStrategy(AnimatorTracker animatorTracker, boolean fromUser) {
      super(ExtendedFloatingActionButton.this, animatorTracker);
      this.fromUser = fromUser;
    }

    @Override
    public void performNow() {
      // If the view isn't laid out, or we're in the editor, don't run the animation
      internalSetVisibility(fromUser ? View.GONE : View.INVISIBLE, fromUser);
    }

    @Override
    public void onChange(@Nullable final OnChangedListener listener) {
      if (listener != null) {
        listener.onHidden(ExtendedFloatingActionButton.this);
      }
    }

    @Override
    public int getDefaultMotionSpecResource() {
      return R.animator.mtrl_extended_fab_hide_motion_spec;
    }

    @Override
    public void onAnimationStart(Animator animator) {
      super.onAnimationStart(animator);
      isCancelled = false;
      internalSetVisibility(View.VISIBLE, fromUser);
      animState = ANIM_STATE_HIDING;
    }

    @Override
    public void onAnimationCancel() {
      super.onAnimationCancel();
      isCancelled = true;
    }

    @Override
    public void onAnimationEnd() {
      super.onAnimationEnd();
      animState = ANIM_STATE_NONE;
      if (!isCancelled) {
        internalSetVisibility(fromUser ? View.GONE : View.INVISIBLE, fromUser);
      }
    }
  }
}
