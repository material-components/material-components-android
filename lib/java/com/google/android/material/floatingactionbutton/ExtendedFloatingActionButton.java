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

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;
import static java.lang.Math.min;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import androidx.core.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Property;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import androidx.annotation.AnimatorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout.AttachedBehavior;
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior;
import com.google.android.material.animation.MotionSpec;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.internal.DescendantOffsetUtils;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.shape.ShapeAppearanceModel;
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
 * <p>The background color of this view defaults to the your theme's {@code colorSecondary}. If you
 * wish to change this at runtime then you can do so via
 * {@link #setBackgroundTintList(android.content.res.ColorStateList)}.
 */
public class ExtendedFloatingActionButton extends MaterialButton implements AttachedBehavior {

  private static final int DEF_STYLE_RES =
      R.style.Widget_MaterialComponents_ExtendedFloatingActionButton_Icon;

  private static final int ANIM_STATE_NONE = 0;
  private static final int ANIM_STATE_HIDING = 1;
  private static final int ANIM_STATE_SHOWING = 2;

  private int animState = ANIM_STATE_NONE;

  private final AnimatorTracker changeVisibilityTracker = new AnimatorTracker();
  @NonNull private final MotionStrategy shrinkStrategy;
  @NonNull private final MotionStrategy extendStrategy;
  private final MotionStrategy showStrategy = new ShowStrategy(changeVisibilityTracker);
  private final MotionStrategy hideStrategy = new HideStrategy(changeVisibilityTracker);
  private final int collapsedSize;

  private int extendedPaddingStart;
  private int extendedPaddingEnd;

  @NonNull private final Behavior<ExtendedFloatingActionButton> behavior;

  private boolean isExtended = true;
  private boolean isTransforming = false;
  private boolean animateShowBeforeLayout = false;

  @NonNull protected ColorStateList originalTextCsl;


  /**
   * Callback to be invoked when the visibility or the state of an ExtendedFloatingActionButton
   * changes.
   */
  public abstract static class OnChangedCallback {

    /**
     * Called when a ExtendedFloatingActionButton has been {@link
     * #show(OnChangedCallback) shown}.
     *
     * @param extendedFab the FloatingActionButton that was shown.
     */
    public void onShown(ExtendedFloatingActionButton extendedFab) {}

    /**
     * Called when a ExtendedFloatingActionButton has been {@link
     * #hide(OnChangedCallback) hidden}.
     *
     * @param extendedFab the ExtendedFloatingActionButton that was hidden.
     */
    public void onHidden(ExtendedFloatingActionButton extendedFab) {}

    /**
     * Called when a ExtendedFloatingActionButton has been {@link
     * #extend(OnChangedCallback) extended} to show the icon and the
     * text.
     *
     * @param extendedFab the ExtendedFloatingActionButton that was extended.
     */
    public void onExtended(ExtendedFloatingActionButton extendedFab) {}

    /**
     * Called when a ExtendedFloatingActionButton has been {@link
     * #shrink(OnChangedCallback) shrunken} to show just the icon.
     *
     * @param extendedFab the ExtendedFloatingActionButton that was shrunk.
     */
    public void onShrunken(ExtendedFloatingActionButton extendedFab) {}
  }

  public ExtendedFloatingActionButton(@NonNull Context context) {
    this(context, null);
  }

  public ExtendedFloatingActionButton(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.extendedFloatingActionButtonStyle);
  }

  @SuppressWarnings("nullness")
  public ExtendedFloatingActionButton(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();
    behavior = new ExtendedFloatingActionButtonBehavior<>(context, attrs);
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

    collapsedSize =
        a.getDimensionPixelSize(R.styleable.ExtendedFloatingActionButton_collapsedSize, -1);
    extendedPaddingStart = ViewCompat.getPaddingStart(this);
    extendedPaddingEnd = ViewCompat.getPaddingEnd(this);

    AnimatorTracker changeSizeTracker = new AnimatorTracker();
    extendStrategy =
        new ChangeSizeStrategy(
            changeSizeTracker,
            new Size() {
              @Override
              public int getWidth() {
                return getMeasuredWidth()
                    - getCollapsedPadding() * 2
                    + extendedPaddingStart
                    + extendedPaddingEnd;
              }

              @Override
              public int getHeight() {
                return getMeasuredHeight();
              }

              @Override
              public int getPaddingStart() {
                return extendedPaddingStart;
              }

              @Override
              public int getPaddingEnd() {
                return extendedPaddingEnd;
              }

              @Override
              public LayoutParams getLayoutParams() {
                return new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
              }
            },
            /* extending= */ true);

    shrinkStrategy =
        new ChangeSizeStrategy(
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

              @Override
              public int getPaddingStart() {
                return getCollapsedPadding();
              }

              @Override
              public int getPaddingEnd() {
                return getCollapsedPadding();
              }

              @Override
              public LayoutParams getLayoutParams() {
                return new LayoutParams(getWidth(), getHeight());
              }
            },
            /* extending= */ false);

    showStrategy.setMotionSpec(showMotionSpec);
    hideStrategy.setMotionSpec(hideMotionSpec);
    extendStrategy.setMotionSpec(extendMotionSpec);
    shrinkStrategy.setMotionSpec(shrinkMotionSpec);
    a.recycle();

    ShapeAppearanceModel shapeAppearanceModel =
        ShapeAppearanceModel.builder(
            context, attrs, defStyleAttr, DEF_STYLE_RES, ShapeAppearanceModel.PILL
        ).build();
    setShapeAppearanceModel(shapeAppearanceModel);
    saveOriginalTextCsl();
  }

  @Override
  public void setTextColor(int color) {
    super.setTextColor(color);
    saveOriginalTextCsl();
  }

  @Override
  public void setTextColor(@NonNull ColorStateList colors) {
    super.setTextColor(colors);
    saveOriginalTextCsl();
  }

  private void saveOriginalTextCsl() {
    originalTextCsl = getTextColors();
  }

  /**
   * Update the text color without affecting the original, client-set color.
   */
  protected void silentlyUpdateTextColor(@NonNull  ColorStateList csl) {
    // Call super to avoid saving this silent update through extended FAB's setTextColor overrides.
    super.setTextColor(csl);
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

  @NonNull
  @Override
  public Behavior<ExtendedFloatingActionButton> getBehavior() {
    return behavior;
  }


  /**
   * Extends or shrinks the fab depending on the value of {@param extended}.
   */
  public void setExtended(boolean extended) {
    if (this.isExtended == extended) {
      return;
    }

    MotionStrategy motionStrategy = extended ? extendStrategy : shrinkStrategy;
    if (motionStrategy.shouldCancel()) {
      return;
    }

    motionStrategy.performNow();
  }

  public final boolean isExtended() {
    return isExtended;
  }

  /**
   * Sets whether to enable animation for a call to show {@link #show} even if the view has not been
   * laid out yet.
   *
   * <p>This may be set to {@code true} if the button is initially hidden but should animate when
   * later shown. The default is {@code false}.
   */
  public void setAnimateShowBeforeLayout(boolean animateShowBeforeLayout) {
    this.animateShowBeforeLayout = animateShowBeforeLayout;
  }

  @Override
  public void setPaddingRelative(int start, int top, int end, int bottom) {
    super.setPaddingRelative(start, top, end, bottom);
    if (isExtended && !isTransforming) {
      extendedPaddingStart = start;
      extendedPaddingEnd = end;
    }
  }

  @Override
  public void setPadding(int left, int top, int right, int bottom) {
    super.setPadding(left, top, right, bottom);
    if (isExtended && !isTransforming) {
      extendedPaddingStart = ViewCompat.getPaddingStart(this);
      extendedPaddingEnd = ViewCompat.getPaddingEnd(this);
    }
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
  }

  /**
   * Remove a listener that was previously added via
   * {@link #addOnShowAnimationListener(AnimatorListener)}.
   *
   * @param listener listener to remove
   */
  public void removeOnShowAnimationListener(@NonNull AnimatorListener listener) {
    showStrategy.removeAnimationListener(listener);
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
  }

  /**
   * Remove a listener that was previously added via
   * {@link #addOnHideAnimationListener(AnimatorListener)}.
   *
   * @param listener listener to remove
   */
  public void removeOnHideAnimationListener(@NonNull AnimatorListener listener) {
    hideStrategy.removeAnimationListener(listener);
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
    performMotion(hideStrategy, null);
  }

  /**
   * Hides the button.
   *
   * <p>This method will animate the button hide if the view has already been laid out.
   *
   * @param callback the callback to notify when this view is hidden
   */
  public void hide(@NonNull OnChangedCallback callback) {
    performMotion(hideStrategy, callback);
  }

  /**
   * Shows the button.
   *
   * <p>This method will animate the button show if the view has already been laid out, or if {@link
   * #setAnimateShowBeforeLayout} is {@code true}.
   */
  public void show() {
    performMotion(showStrategy, null);
  }

  /**
   * Shows the button.
   *
   * <p>This method will animate the button show if the view has already been laid out, or if {@link
   * #setAnimateShowBeforeLayout} is {@code true}.
   *
   * @param callback the callback to notify when this view is shown
   */
  public void show(@NonNull OnChangedCallback callback) {
    performMotion(showStrategy, callback);
  }

  /**
   * Extends the FAB to show the text and the icon.
   *
   * <p>This method will not affect an extended FAB which holds just text and no icon. Also, this
   * method will animate the button show if the view has already been laid out.
   *
   * @see #extend(OnChangedCallback)
   */
  public void extend() {
    performMotion(extendStrategy, null);
  }

  /**
   * Extends the FAB to show the text and the icon.
   *
   * <p>This method will not affect an extended FAB which holds just text and no icon. Also, this
   * method will animate the button show if the view has already been laid out.
   *
   * @param callback the callback to notify when the FAB is extended
   */
  public void extend(@NonNull final OnChangedCallback callback) {
    performMotion(extendStrategy, callback);
  }


  /**
   * Shrinks the FAB to show just the icon.
   *
   * <p>This method will not affect an extended FAB which holds just text and no icon. Also, this
   * method will animate the button show if the view has already been laid out.
   *
   * @see #shrink(OnChangedCallback)
   */
  public void shrink() {
    performMotion(shrinkStrategy, null);
  }

  /**
   * Shrinks the FAB to show just the icon.
   *
   * <p>This method will not affect an extended FAB which holds just text and no icon. Also, this
   * method will animate the button show if the view has already been laid out.
   *
   * @param callback the callback to notify when the FAB shrank
   */
  public void shrink(@NonNull final OnChangedCallback callback) {
    performMotion(shrinkStrategy, callback);
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

  private void performMotion(
      @NonNull final MotionStrategy strategy, @Nullable final OnChangedCallback callback) {
    if (strategy.shouldCancel()) {
      return;
    }

    boolean shouldAnimate = shouldAnimateVisibilityChange();
    if (!shouldAnimate) {
      strategy.performNow();
      strategy.onChange(callback);
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
            if (!cancelled) {
              strategy.onChange(callback);
            }
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
    return (ViewCompat.isLaidOut(this) || (!isOrWillBeShown() && animateShowBeforeLayout))
        && !isInEditMode();
  }

  /**
   * A Property wrapper around the <code>width</code> functionality handled by the {@link
   * LayoutParams#width} value.
   */
  static final Property<View, Float> WIDTH =
      new Property<View, Float>(Float.class, "width") {
        @Override
        public void set(@NonNull View object, @NonNull Float value) {
          object.getLayoutParams().width = value.intValue();
          object.requestLayout();
        }

        @NonNull
        @Override
        public Float get(@NonNull View object) {
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
        public void set(@NonNull View object, @NonNull Float value) {
          object.getLayoutParams().height = value.intValue();
          object.requestLayout();
        }

        @NonNull
        @Override
        public Float get(@NonNull View object) {
          return (float) object.getLayoutParams().height;
        }
      };

  /**
   * A Property wrapper around the <code>paddingStart</code> functionality handled by the {@link
   * ViewCompat#setPaddingRelative(View, int, int, int, int)}.
   */
  static final Property<View, Float> PADDING_START =
      new Property<View, Float>(Float.class, "paddingStart") {
        @Override
        public void set(@NonNull View object, @NonNull Float value) {
          ViewCompat.setPaddingRelative(
              object,
              value.intValue(),
              object.getPaddingTop(),
              ViewCompat.getPaddingEnd(object),
              object.getPaddingBottom());
        }

        @NonNull
        @Override
        public Float get(@NonNull View object) {
          return (float) ViewCompat.getPaddingStart(object);
        }
      };

  /**
   * A Property wrapper around the <code>paddingEnd</code> functionality handled by the {@link
   * ViewCompat#setPaddingRelative(View, int, int, int, int)}.
   */
  static final Property<View, Float> PADDING_END =
      new Property<View, Float>(Float.class, "paddingEnd") {
        @Override
        public void set(@NonNull View object, @NonNull Float value) {
          ViewCompat.setPaddingRelative(
              object,
              ViewCompat.getPaddingStart(object),
              object.getPaddingTop(),
              value.intValue(),
              object.getPaddingBottom());
        }

        @NonNull
        @Override
        public Float get(@NonNull View object) {
          return (float) ViewCompat.getPaddingEnd(object);
        }
      };

  /**
   * Shrink to the smaller value between paddingStart and paddingEnd, such that when shrunk the icon
   * will be centered.
   */
  @VisibleForTesting
  int getCollapsedSize() {
    return collapsedSize < 0
        ? min(ViewCompat.getPaddingStart(this), ViewCompat.getPaddingEnd(this)) * 2 + getIconSize()
        : collapsedSize;
  }

  int getCollapsedPadding() {
    return (getCollapsedSize() - getIconSize()) / 2;
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
    @Nullable private OnChangedCallback internalAutoHideCallback;
    @Nullable private OnChangedCallback internalAutoShrinkCallback;
    private boolean autoHideEnabled;
    private boolean autoShrinkEnabled;

    public ExtendedFloatingActionButtonBehavior() {
      super();
      autoHideEnabled = AUTO_HIDE_DEFAULT;
      autoShrinkEnabled = AUTO_SHRINK_DEFAULT;
    }

    // Behavior attrs should be nullable in the framework
    @SuppressWarnings("argument.type.incompatible")
    public ExtendedFloatingActionButtonBehavior(
        @NonNull Context context, @Nullable AttributeSet attrs) {
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
    @SuppressWarnings({"unchecked"})
    // TODO: remove this stub method. Adding now to mantain compatibility since the override is
    // not correct.
    public boolean getInsetDodgeRect(
        @NonNull CoordinatorLayout parent,
        @NonNull ExtendedFloatingActionButton child,
        @NonNull Rect rect) {
      return super.getInsetDodgeRect(parent, (T) child, rect);
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
        CoordinatorLayout parent, @NonNull ExtendedFloatingActionButton child, View dependency) {
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
    void setInternalAutoHideCallback(@Nullable OnChangedCallback callback) {
      internalAutoHideCallback = callback;
    }

    @VisibleForTesting
    void setInternalAutoShrinkCallback(@Nullable OnChangedCallback callback) {
      internalAutoShrinkCallback = callback;
    }

    private boolean shouldUpdateVisibility(
        @NonNull View dependency, @NonNull ExtendedFloatingActionButton child) {
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

      return true;
    }

    private boolean updateFabVisibilityForAppBarLayout(
        CoordinatorLayout parent,
        @NonNull AppBarLayout appBarLayout,
        @NonNull ExtendedFloatingActionButton child) {
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
        @NonNull View bottomSheet, @NonNull ExtendedFloatingActionButton child) {
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
      OnChangedCallback callback = autoShrinkEnabled
          ? internalAutoShrinkCallback
          : internalAutoHideCallback;
      MotionStrategy strategy = autoShrinkEnabled
          ? fab.shrinkStrategy
          : fab.hideStrategy;

      fab.performMotion(strategy, callback);
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
      OnChangedCallback callback = autoShrinkEnabled
          ? internalAutoShrinkCallback
          : internalAutoHideCallback;
      MotionStrategy strategy = autoShrinkEnabled
          ? fab.extendStrategy
          : fab.showStrategy;

      fab.performMotion(strategy, callback);
    }

    @Override
    public boolean onLayoutChild(
        @NonNull CoordinatorLayout parent,
        @NonNull ExtendedFloatingActionButton child,
        int layoutDirection) {
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
      return true;
    }
  }

  interface Size {
    int getWidth();
    int getHeight();

    int getPaddingStart();

    int getPaddingEnd();

    LayoutParams getLayoutParams();
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
      isExtended = extending;
      LayoutParams layoutParams = getLayoutParams();
      if (layoutParams == null) {
        return;
      }

      layoutParams.width = size.getLayoutParams().width;
      layoutParams.height = size.getLayoutParams().height;
      ViewCompat.setPaddingRelative(
          ExtendedFloatingActionButton.this,
          size.getPaddingStart(),
          getPaddingTop(),
          size.getPaddingEnd(),
          getPaddingBottom());
      requestLayout();
    }

    @Override
    public void onChange(@Nullable final OnChangedCallback callback) {
      if (callback == null) {
        return;
      }

      if (extending) {
        callback.onExtended(ExtendedFloatingActionButton.this);
      } else {
        callback.onShrunken(ExtendedFloatingActionButton.this);
      }
    }

    @Override
    public int getDefaultMotionSpecResource() {
      return extending
          ? R.animator.mtrl_extended_fab_change_size_expand_motion_spec
          : R.animator.mtrl_extended_fab_change_size_collapse_motion_spec;
    }

    @NonNull
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

      if (spec.hasPropertyValues("paddingStart")) {
        PropertyValuesHolder[] paddingValues = spec.getPropertyValues("paddingStart");
        paddingValues[0].setFloatValues(
            ViewCompat.getPaddingStart(ExtendedFloatingActionButton.this), size.getPaddingStart());
        spec.setPropertyValues("paddingStart", paddingValues);
      }

      if (spec.hasPropertyValues("paddingEnd")) {
        PropertyValuesHolder[] paddingValues = spec.getPropertyValues("paddingEnd");
        paddingValues[0].setFloatValues(
            ViewCompat.getPaddingEnd(ExtendedFloatingActionButton.this), size.getPaddingEnd());
        spec.setPropertyValues("paddingEnd", paddingValues);
      }

      if (spec.hasPropertyValues("labelOpacity")) {
        PropertyValuesHolder[] labelOpacityValues = spec.getPropertyValues("labelOpacity");
        float startValue = extending ? 0F : 1F;
        float endValue = extending ? 1F : 0F;
        labelOpacityValues[0].setFloatValues(startValue, endValue);
        spec.setPropertyValues("labelOpacity", labelOpacityValues);
      }

      return super.createAnimator(spec);
    }

    @Override
    public void onAnimationStart(Animator animator) {
      super.onAnimationStart(animator);
      isExtended = extending;
      isTransforming = true;
      setHorizontallyScrolling(true);
    }

    @Override
    public void onAnimationEnd() {
      super.onAnimationEnd();
      isTransforming = false;
      setHorizontallyScrolling(false);

      LayoutParams layoutParams = getLayoutParams();
      if (layoutParams == null) {
        return;
      }
      layoutParams.width = size.getLayoutParams().width;
      layoutParams.height = size.getLayoutParams().height;
    }

    @Override
    public boolean shouldCancel() {
      return extending == isExtended || getIcon() == null || TextUtils.isEmpty(getText());
    }
  }

  class ShowStrategy extends BaseMotionStrategy {

    public ShowStrategy(AnimatorTracker animatorTracker) {
      super(ExtendedFloatingActionButton.this, animatorTracker);
    }

    @Override
    public void performNow() {
      setVisibility(VISIBLE);
      setAlpha(1f);
      setScaleY(1f);
      setScaleX(1f);
    }

    @Override
    public void onChange(@Nullable final OnChangedCallback callback) {
      if (callback != null) {
        callback.onShown(ExtendedFloatingActionButton.this);
      }
    }

    @Override
    public int getDefaultMotionSpecResource() {
      return R.animator.mtrl_extended_fab_show_motion_spec;
    }

    @Override
    public void onAnimationStart(Animator animation) {
      super.onAnimationStart(animation);
      setVisibility(VISIBLE);
      animState = ANIM_STATE_SHOWING;
    }

    @Override
    public void onAnimationEnd() {
      super.onAnimationEnd();
      animState = ANIM_STATE_NONE;
    }

    @Override
    public boolean shouldCancel() {
      return isOrWillBeShown();
    }
  }

  class HideStrategy extends BaseMotionStrategy {

    private boolean isCancelled;

    public HideStrategy(AnimatorTracker animatorTracker) {
      super(ExtendedFloatingActionButton.this, animatorTracker);
    }

    @Override
    public void performNow() {
      setVisibility(GONE);
    }

    @Override
    public void onChange(@Nullable final OnChangedCallback callback) {
      if (callback != null) {
        callback.onHidden(ExtendedFloatingActionButton.this);
      }
    }

    @Override
    public boolean shouldCancel() {
      return isOrWillBeHidden();
    }

    @Override
    public int getDefaultMotionSpecResource() {
      return R.animator.mtrl_extended_fab_hide_motion_spec;
    }

    @Override
    public void onAnimationStart(Animator animator) {
      super.onAnimationStart(animator);
      isCancelled = false;
      setVisibility(VISIBLE);
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
        setVisibility(GONE);
      }
    }
  }
}
