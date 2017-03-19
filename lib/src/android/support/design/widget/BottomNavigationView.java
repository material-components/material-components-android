/*
 * Copyright (C) 2016 The Android Open Source Project
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

package android.support.design.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.R;
import android.support.design.internal.BottomNavigationMenu;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.internal.BottomNavigationPresenter;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.view.SupportMenuInflater;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.TintTypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import java.util.ArrayList;

import static android.support.design.widget.AnimationUtils.DECELERATE_INTERPOLATOR;
import static android.support.design.widget.AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR;
import static android.support.design.widget.AnimationUtils.LINEAR_INTERPOLATOR;
import static android.support.design.widget.ViewUtils.objectEquals;

/**
 * Represents a standard bottom navigation bar for application. It is an implementation of <a
 * href="https://material.google.com/components/bottom-navigation.html">material design bottom
 * navigation</a>.
 *
 * <p>Bottom navigation bars make it easy for users to explore and switch between top-level views in
 * a single tap. It should be used when application has three to five top-level destinations.
 *
 * <p>The bar contents can be populated by specifying a menu resource file. Each menu item title,
 * icon and enabled state will be used for displaying bottom navigation bar items. Menu items can
 * also be used for programmatically selecting which destination is currently active. It can be done
 * using {@code MenuItem#setChecked(true)}
 *
 * <pre>
 * layout resource file:
 * &lt;android.support.design.widget.BottomNavigationView
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:app="http://schema.android.com/apk/res/res-auto"
 *     android:id="@+id/navigation"
 *     android:layout_width="match_parent"
 *     android:layout_height="56dp"
 *     android:layout_gravity="start"
 *     app:menu="@menu/my_navigation_items" /&gt;
 *
 * res/menu/my_navigation_items.xml:
 * &lt;menu xmlns:android="http://schemas.android.com/apk/res/android"&gt;
 *     &lt;item android:id="@+id/action_search"
 *          android:title="@string/menu_search"
 *          android:icon="@drawable/ic_search" /&gt;
 *     &lt;item android:id="@+id/action_settings"
 *          android:title="@string/menu_settings"
 *          android:icon="@drawable/ic_add" /&gt;
 *     &lt;item android:id="@+id/action_navigation"
 *          android:title="@string/menu_navigation"
 *          android:icon="@drawable/ic_action_navigation_menu" /&gt;
 * &lt;/menu&gt;
 * </pre>
 */
@CoordinatorLayout.DefaultBehavior(BottomNavigationView.Behavior.class)
public class BottomNavigationView extends FrameLayout {

  private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
  private static final int[] DISABLED_STATE_SET = {-android.R.attr.state_enabled};

  // On JB/KK versions of the platform sometimes View.setTranslationY does not result in
  // layout / draw pass, and CoordinatorLayout relies on a draw pass to happen to sync vertical
  // positioning of all its child views
  private static final boolean USE_OFFSET_API =
      (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
          && (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT);

  private static final int SHOW_HIDE_ANIMATION_DURATION = 350;
  private static final int FADE_IN_ANIMATION_DURATION = SHOW_HIDE_ANIMATION_DURATION;
  private static final int FADE_OUT_ANIMATION_DURATION = SHOW_HIDE_ANIMATION_DURATION / 3;
  private static final int MENU_PRESENTER_ID = 1;

  private static final int SHOW_STATUS_HIDDEN = 0;
  private static final int SHOW_STATUS_SHOWING = 1;
  private static final int SHOW_STATUS_SHOWN = 2;
  private static final int SHOW_STATUS_HIDING = 3;

  private static final int REQUEST_NONE = -1;
  private static final int REQUEST_SHOW = 0;
  private static final int REQUEST_HIDE = 1;

  private int mShowStatus = SHOW_STATUS_SHOWN;
  private int mLastRequest = REQUEST_NONE;
  private final MenuBuilder mMenu;
  private final BottomNavigationMenuView mMenuView;
  private final BottomNavigationPresenter mPresenter = new BottomNavigationPresenter();
  private WindowInsetsCompat mLastInsets;
  private MenuInflater mMenuInflater;

  private OnNavigationItemSelectedListener mSelectedListener;
  private OnNavigationItemReselectedListener mReselectedListener;
  private ArrayList<OnVisibilityChangedListener> mOnVisibilityChangedListeners = new ArrayList<>();

  public BottomNavigationView(Context context) {
    this(context, null);
  }

  public BottomNavigationView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public BottomNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    ThemeUtils.checkAppCompatTheme(context);

    // Create the menu
    mMenu = new BottomNavigationMenu(context);

    mMenuView = new BottomNavigationMenuView(context);
    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.gravity = Gravity.CENTER;
    mMenuView.setLayoutParams(params);

    mPresenter.setBottomNavigationMenuView(mMenuView);
    mPresenter.setId(MENU_PRESENTER_ID);
    mMenuView.setPresenter(mPresenter);
    mMenu.addMenuPresenter(mPresenter);
    mPresenter.initForMenu(getContext(), mMenu);

    // Custom attributes
    TintTypedArray a =
        TintTypedArray.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.BottomNavigationView,
            defStyleAttr,
            R.style.Widget_Design_BottomNavigationView);

    if (a.hasValue(R.styleable.BottomNavigationView_itemIconTint)) {
      mMenuView.setIconTintList(a.getColorStateList(R.styleable.BottomNavigationView_itemIconTint));
    } else {
      mMenuView.setIconTintList(createDefaultColorStateList(android.R.attr.textColorSecondary));
    }
    if (a.hasValue(R.styleable.BottomNavigationView_itemTextColor)) {
      mMenuView.setItemTextColor(
          a.getColorStateList(R.styleable.BottomNavigationView_itemTextColor));
    } else {
      mMenuView.setItemTextColor(createDefaultColorStateList(android.R.attr.textColorSecondary));
    }
    if (a.hasValue(R.styleable.BottomNavigationView_elevation)) {
      ViewCompat.setElevation(
          this, a.getDimensionPixelSize(R.styleable.BottomNavigationView_elevation, 0));
    }

    int itemBackground = a.getResourceId(R.styleable.BottomNavigationView_itemBackground, 0);
    mMenuView.setItemBackgroundRes(itemBackground);

    if (a.hasValue(R.styleable.BottomNavigationView_menu)) {
      inflateMenu(a.getResourceId(R.styleable.BottomNavigationView_menu, 0));
    }
    a.recycle();

    addView(mMenuView, params);
    if (Build.VERSION.SDK_INT < 21) {
      addCompatibilityTopDivider(context);
    }

    ViewCompat.setOnApplyWindowInsetsListener(
        this,
        new android.support.v4.view.OnApplyWindowInsetsListener() {
          @Override
          public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
            return onWindowInsetChanged(insets);
          }
        });

    mMenu.setCallback(
        new MenuBuilder.Callback() {
          @Override
          public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
            if (mReselectedListener != null && item.getItemId() == getSelectedItemId()) {
              mReselectedListener.onNavigationItemReselected(item);
              return true; // item is already selected
            }
            return mSelectedListener != null && !mSelectedListener.onNavigationItemSelected(item);
          }

          @Override
          public void onMenuModeChange(MenuBuilder menu) {}
        });
  }

  WindowInsetsCompat onWindowInsetChanged(final WindowInsetsCompat insets) {
    WindowInsetsCompat newInsets = null;

    if (ViewCompat.getFitsSystemWindows(this)) {
      // If we're set to fit system windows, keep the insets
      newInsets = insets;

      setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(),
          insets.getSystemWindowInsetBottom());
    }

    // If our insets have changed, keep them
    if (!objectEquals(mLastInsets, newInsets)) {
      mLastInsets = newInsets;
    }

    return insets;
  }

  /**
   * Set a listener that will be notified when a bottom navigation item is selected. This listener
   * will also be notified when the currently selected item is reselected, unless an {@link
   * OnNavigationItemReselectedListener} has also been set.
   *
   * @param listener The listener to notify
   * @see #setOnNavigationItemReselectedListener(OnNavigationItemReselectedListener)
   */
  public void setOnNavigationItemSelectedListener(
      @Nullable OnNavigationItemSelectedListener listener) {
    mSelectedListener = listener;
  }

  /**
   * Set a listener that will be notified when the currently selected bottom navigation item is
   * reselected. This does not require an {@link OnNavigationItemSelectedListener} to be set.
   *
   * @param listener The listener to notify
   * @see #setOnNavigationItemSelectedListener(OnNavigationItemSelectedListener)
   */
  public void setOnNavigationItemReselectedListener(
      @Nullable OnNavigationItemReselectedListener listener) {
    mReselectedListener = listener;
  }

  /** Returns the {@link Menu} instance associated with this bottom navigation bar. */
  @NonNull
  public Menu getMenu() {
    return mMenu;
  }

  /**
   * Inflate a menu resource into this navigation view.
   *
   * <p>Existing items in the menu will not be modified or removed.
   *
   * @param resId ID of a menu resource to inflate
   */
  public void inflateMenu(int resId) {
    mPresenter.setUpdateSuspended(true);
    getMenuInflater().inflate(resId, mMenu);
    mPresenter.setUpdateSuspended(false);
    mPresenter.updateMenuView(true);
  }

  /** @return The maximum number of items that can be shown in BottomNavigationView. */
  public int getMaxItemCount() {
    return BottomNavigationMenu.MAX_ITEM_COUNT;
  }

  /**
   * Returns the tint which is applied to our menu items' icons.
   *
   * @see #setItemIconTintList(ColorStateList)
   * @attr ref R.styleable#BottomNavigationView_itemIconTint
   */
  @Nullable
  public ColorStateList getItemIconTintList() {
    return mMenuView.getIconTintList();
  }

  /**
   * Set the tint which is applied to our menu items' icons.
   *
   * @param tint the tint to apply.
   * @attr ref R.styleable#BottomNavigationView_itemIconTint
   */
  public void setItemIconTintList(@Nullable ColorStateList tint) {
    mMenuView.setIconTintList(tint);
  }

  /**
   * Returns colors used for the different states (normal, selected, focused, etc.) of the menu item
   * text.
   *
   * @see #setItemTextColor(ColorStateList)
   * @return the ColorStateList of colors used for the different states of the menu items text.
   * @attr ref R.styleable#BottomNavigationView_itemTextColor
   */
  @Nullable
  public ColorStateList getItemTextColor() {
    return mMenuView.getItemTextColor();
  }

  /**
   * Set the colors to use for the different states (normal, selected, focused, etc.) of the menu
   * item text.
   *
   * @see #getItemTextColor()
   * @attr ref R.styleable#BottomNavigationView_itemTextColor
   */
  public void setItemTextColor(@Nullable ColorStateList textColor) {
    mMenuView.setItemTextColor(textColor);
  }

  /**
   * Returns the background resource of the menu items.
   *
   * @see #setItemBackgroundResource(int)
   * @attr ref R.styleable#BottomNavigationView_itemBackground
   */
  @DrawableRes
  public int getItemBackgroundResource() {
    return mMenuView.getItemBackgroundRes();
  }

  /**
   * Set the background of our menu items to the given resource.
   *
   * @param resId The identifier of the resource.
   * @attr ref R.styleable#BottomNavigationView_itemBackground
   */
  public void setItemBackgroundResource(@DrawableRes int resId) {
    mMenuView.setItemBackgroundRes(resId);
  }

  /**
   * Returns the currently selected menu item ID, or zero if there is no menu.
   *
   * @see #setSelectedItemId(int)
   */
  @IdRes
  public int getSelectedItemId() {
    return mMenuView.getSelectedItemId();
  }

  /**
   * Set the selected menu item ID. This behaves the same as tapping on an item.
   *
   * @param itemId The menu item ID. If no item has this ID, the current selection is unchanged.
   * @see #getSelectedItemId()
   */
  public void setSelectedItemId(@IdRes int itemId) {
    MenuItem item = mMenu.findItem(itemId);
    if (item != null) {
      if (!mMenu.performItemAction(item, mPresenter, 0)) {
        item.setChecked(true);
      }
    }
  }

  /** Listener for handling selection events on bottom navigation items. */
  public interface OnNavigationItemSelectedListener {

    /**
     * Called when an item in the bottom navigation menu is selected.
     *
     * @param item The selected item
     * @return true to display the item as the selected item and false if the item should not be
     *     selected. Consider setting non-selectable items as disabled preemptively to make them
     *     appear non-interactive.
     */
    boolean onNavigationItemSelected(@NonNull MenuItem item);
  }

  /** Listener for handling reselection events on bottom navigation items. */
  public interface OnNavigationItemReselectedListener {

    /**
     * Called when the currently selected item in the bottom navigation menu is selected again.
     *
     * @param item The selected item
     */
    void onNavigationItemReselected(@NonNull MenuItem item);
  }

  public void addOnVisibilityChangedListener(OnVisibilityChangedListener listener) {
    mOnVisibilityChangedListeners.add(listener);
  }

  public void removeOnVisibilityChangedListener(OnVisibilityChangedListener listener) {
    mOnVisibilityChangedListeners.remove(listener);
  }

  /**
   * Shows the bar.
   * <p>
   * <p>This method will animate the view show.
   */
  public void show() {
    show(true);
  }

  void show(boolean fromUser) {
    //is showing or is already shown
    if (mShowStatus == SHOW_STATUS_SHOWING || mShowStatus == SHOW_STATUS_SHOWN) return;
    //is hiding, so we will execute the request once finished
    if (mShowStatus == SHOW_STATUS_HIDING) {
      if (fromUser) {
        mLastRequest = REQUEST_SHOW;
      }
      return;
    }
    mShowStatus = SHOW_STATUS_SHOWING;

    if (Build.VERSION.SDK_INT >= 12) {
      final ValueAnimatorCompat alphaAnimator = sdk12AlphaAnimator(0, 1);
      final ValueAnimatorCompat slideInAnimator = sdk12SlideInAnimator();

      alphaAnimator.setDuration(FADE_IN_ANIMATION_DURATION);
      slideInAnimator.addListener(
          new ValueAnimatorCompat.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(ValueAnimatorCompat animator) {
              onShowFinished();
            }
          });
      alphaAnimator.start();
      slideInAnimator.start();
    } else {
      final Animation animation = compatSlideInAnimation();
      animation.setAnimationListener(
          new AnimationUtils.AnimationListenerAdapter() {
            @Override
            public void onAnimationEnd(Animation animation) {
              onShowFinished();
            }
          });
      startAnimation(animation);
    }
  }

  /**
   * Hides the bar.
   * <p>
   * <p>This method will animate the navigation view hide.
   */
  public void hide() {
    hide(true);
  }

  void hide(boolean fromUser) {
    //is hiding or is already hidden
    if (mShowStatus == SHOW_STATUS_HIDING || mShowStatus == SHOW_STATUS_HIDDEN) return;
    //is showing, so we will execute the request once finished
    if (mShowStatus == SHOW_STATUS_SHOWING) {
      if (fromUser) {
        mLastRequest = REQUEST_HIDE;
      }
      return;
    }
    mShowStatus = SHOW_STATUS_HIDING;

    if (Build.VERSION.SDK_INT >= 12) {
      final ValueAnimatorCompat alphaAnimator = sdk12AlphaAnimator(1, 0);
      final ValueAnimatorCompat slideOutAnimator = sdk12SlideOutAnimator();

      alphaAnimator.setDuration(FADE_OUT_ANIMATION_DURATION);
      slideOutAnimator.addListener(
          new ValueAnimatorCompat.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(ValueAnimatorCompat animator) {
              onHideFinished();
            }
          });
      alphaAnimator.start();
      slideOutAnimator.start();

    } else {
      final Animation animation = compatSlideOutAnimation();
      animation.setAnimationListener(
          new AnimationUtils.AnimationListenerAdapter() {
            @Override
            public void onAnimationEnd(Animation animation) {
              onHideFinished();
            }
          });
      startAnimation(animation);
    }
  }

  private void executePendingRequestIfNeeded() {
    final int lastRequest = mLastRequest;
    if (lastRequest == REQUEST_NONE) return;

    mLastRequest = REQUEST_NONE;

    if (lastRequest == REQUEST_SHOW) show(true);
    if (lastRequest == REQUEST_HIDE) hide(true);
  }

  private void notifyAllShown() {
    for (OnVisibilityChangedListener listener : mOnVisibilityChangedListeners) {
      listener.onShown(BottomNavigationView.this);
    }
  }

  private void notifyAllHidden() {
    for (OnVisibilityChangedListener listener : mOnVisibilityChangedListeners) {
      listener.onHidden(BottomNavigationView.this);
    }
  }

  @NonNull
  private ValueAnimatorCompat sdk12AlphaAnimator(float from, float to) {
    final ValueAnimatorCompat alphaAnimator = ViewUtils.createAnimator();
    alphaAnimator.setFloatValues(from, to);
    alphaAnimator.setInterpolator(LINEAR_INTERPOLATOR);
    alphaAnimator.addUpdateListener(
        new ValueAnimatorCompat.AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(ValueAnimatorCompat animator) {
            float animatedAlphaValue = animator.getAnimatedFloatValue();
            ViewCompat.setAlpha(mMenuView, animatedAlphaValue);
          }
        });
    return alphaAnimator;
  }

  private ValueAnimatorCompat sdk12SlideInAnimator() {
    final int viewHeight = getHeight();
    if (USE_OFFSET_API) {
      ViewCompat.offsetTopAndBottom(this, viewHeight);
    } else {
      ViewCompat.setTranslationY(this, viewHeight);
    }

    final ValueAnimatorCompat animator = ViewUtils.createAnimator();
    animator.setIntValues(viewHeight, 0);
    animator.setInterpolator(DECELERATE_INTERPOLATOR);
    animator.setDuration(SHOW_HIDE_ANIMATION_DURATION);
    animator.addUpdateListener(
        new ValueAnimatorCompat.AnimatorUpdateListener() {
          private int previousAnimatedIntValue = viewHeight;

          @Override
          public void onAnimationUpdate(ValueAnimatorCompat animator) {
            int currentAnimatedIntValue = animator.getAnimatedIntValue();
            if (USE_OFFSET_API) {
              // On JB versions of the platform sometimes View.setTranslationY does not
              // result in layout / draw pass
              ViewCompat.offsetTopAndBottom(
                  BottomNavigationView.this, currentAnimatedIntValue - previousAnimatedIntValue);
            } else {
              ViewCompat.setTranslationY(BottomNavigationView.this, currentAnimatedIntValue);
            }
            previousAnimatedIntValue = currentAnimatedIntValue;
          }
        });
    return animator;
  }

  private Animation compatSlideInAnimation() {
    final Animation animation = android.view.animation.AnimationUtils.loadAnimation(getContext(),
        R.anim.design_bottomnav_in);
    animation.setInterpolator(DECELERATE_INTERPOLATOR);
    animation.setDuration(SHOW_HIDE_ANIMATION_DURATION);
    return animation;
  }

  private ValueAnimatorCompat sdk12SlideOutAnimator() {
    final ValueAnimatorCompat animator = ViewUtils.createAnimator();
    animator.setIntValues(0, getHeight());
    animator.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
    animator.setDuration(SHOW_HIDE_ANIMATION_DURATION);
    animator.addUpdateListener(
        new ValueAnimatorCompat.AnimatorUpdateListener() {
          private int previousAnimatedIntValue = 0;

          @Override
          public void onAnimationUpdate(ValueAnimatorCompat animator) {
            int currentAnimatedIntValue = animator.getAnimatedIntValue();
            if (USE_OFFSET_API) {
              // On JB versions of the platform sometimes View.setTranslationY does not
              // result in layout / draw pass
              ViewCompat.offsetTopAndBottom(
                  BottomNavigationView.this, currentAnimatedIntValue - previousAnimatedIntValue);
            } else {
              ViewCompat.setTranslationY(BottomNavigationView.this, currentAnimatedIntValue);
            }
            previousAnimatedIntValue = currentAnimatedIntValue;
          }
        });
    return animator;
  }

  private Animation compatSlideOutAnimation() {
    final Animation animation = android.view.animation.AnimationUtils.loadAnimation(getContext(),
        R.anim.design_bottomnav_out);
    animation.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
    animation.setDuration(SHOW_HIDE_ANIMATION_DURATION);
    return animation;
  }

  private void onShowFinished() {
    mShowStatus = SHOW_STATUS_SHOWN;
    notifyAllShown();
    executePendingRequestIfNeeded();
  }

  private void onHideFinished() {
    mShowStatus = SHOW_STATUS_HIDDEN;
    notifyAllHidden();
    executePendingRequestIfNeeded();
  }

  private void addCompatibilityTopDivider(Context context) {
    View divider = new View(context);
    divider.setBackgroundColor(
        ContextCompat.getColor(context, R.color.design_bottom_navigation_shadow_color));
    FrameLayout.LayoutParams dividerParams =
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            getResources().getDimensionPixelSize(R.dimen.design_bottom_navigation_shadow_height));
    divider.setLayoutParams(dividerParams);
    addView(divider);
  }

  private MenuInflater getMenuInflater() {
    if (mMenuInflater == null) {
      mMenuInflater = new SupportMenuInflater(getContext());
    }
    return mMenuInflater;
  }

  private ColorStateList createDefaultColorStateList(int baseColorThemeAttr) {
    final TypedValue value = new TypedValue();
    if (!getContext().getTheme().resolveAttribute(baseColorThemeAttr, value, true)) {
      return null;
    }
    ColorStateList baseColor = AppCompatResources.getColorStateList(getContext(), value.resourceId);
    if (!getContext()
        .getTheme()
        .resolveAttribute(android.support.v7.appcompat.R.attr.colorPrimary, value, true)) {
      return null;
    }
    int colorPrimary = value.data;
    int defaultColor = baseColor.getDefaultColor();
    return new ColorStateList(
        new int[][] {DISABLED_STATE_SET, CHECKED_STATE_SET, EMPTY_STATE_SET},
        new int[] {
          baseColor.getColorForState(DISABLED_STATE_SET, defaultColor), colorPrimary, defaultColor
        });
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState savedState = new SavedState(superState);
    savedState.menuPresenterState = new Bundle();
    mMenu.savePresenterStates(savedState.menuPresenterState);
    return savedState;
  }

  @Override
  protected void onRestoreInstanceState(Parcelable state) {
    if (!(state instanceof SavedState)) {
      super.onRestoreInstanceState(state);
      return;
    }
    SavedState savedState = (SavedState) state;
    super.onRestoreInstanceState(savedState.getSuperState());
    mMenu.restorePresenterStates(savedState.menuPresenterState);
  }

  static class SavedState extends AbsSavedState {
    Bundle menuPresenterState;

    public SavedState(Parcelable superState) {
      super(superState);
    }

    public SavedState(Parcel source, ClassLoader loader) {
      super(source, loader);
      readFromParcel(source, loader);
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeBundle(menuPresenterState);
    }

    private void readFromParcel(Parcel in, ClassLoader loader) {
      menuPresenterState = in.readBundle(loader);
    }

    public static final Creator<SavedState> CREATOR =
        ParcelableCompat.newCreator(
            new ParcelableCompatCreatorCallbacks<SavedState>() {
              @Override
              public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in, loader);
              }

              @Override
              public SavedState[] newArray(int size) {
                return new SavedState[size];
              }
            });
  }

  /**
   * Callback to be invoked when the visibility of a {@link BottomNavigationView} changes.
   */
  public abstract static class OnVisibilityChangedListener {
    /**
     * Called when a BottomNavigationView has been {@link #show() shown}.
     *
     * @param bottomNavigationView the BottomNavigationView that was shown.
     */
    public void onShown(BottomNavigationView bottomNavigationView) {
    }

    /**
     * Called when a BottomNavigationView has been {@link #hide()
     * hidden}.
     *
     * @param bottomNavigationView the BottomNavigationView that was hidden.
     */
    public void onHidden(BottomNavigationView bottomNavigationView) {
    }
  }

  /**
   * Behavior designed for use with {@link BottomNavigationView} instances. Its main function is to
   * hide {@link BottomNavigationView} when the scrolling view is scrolled in the forward direction.
   */
  public static class Behavior extends CoordinatorLayout.Behavior<BottomNavigationView> {
    private static final boolean AUTO_HIDE_DEFAULT = true;

    private boolean mAutoHideEnabled;

    public Behavior() {
      super();
      mAutoHideEnabled = AUTO_HIDE_DEFAULT;
    }

    public Behavior(Context context, AttributeSet attrs) {
      super(context, attrs);
      TypedArray a =
          context.obtainStyledAttributes(attrs, R.styleable.NavigationView_Behavior_Layout);
      mAutoHideEnabled =
          a.getBoolean(
              R.styleable.NavigationView_Behavior_Layout_behavior_autoHide,
              AUTO_HIDE_DEFAULT);
      a.recycle();
    }

    @Override
    public void onAttachedToLayoutParams(@NonNull CoordinatorLayout.LayoutParams params) {
      if (params.insetEdge == Gravity.NO_GRAVITY) {
        params.insetEdge = Gravity.BOTTOM;
      }
      super.onAttachedToLayoutParams(params);
    }

    /**
     * Sets whether the associated BottomNavigationView automatically hides when there is not enough
     * space to be displayed. This works with {@link AppBarLayout} and {@link BottomSheetBehavior}.
     *
     * @param autoHide true to enable automatic hiding
     * @attr ref
     * android.support.design.R.styleable#BottomNavigationView_Behavior_Layout_behavior_autoHide
     */
    public void setAutoHideEnabled(boolean autoHide) {
      mAutoHideEnabled = autoHide;
    }

    /**
     * Returns whether the associated BottomNavigationView automatically hides when there is not
     * enough space to be displayed.
     *
     * @return true if enabled
     * @attr ref
     * android.support.design.R.styleable#BottomNavigationView_Behavior_Layout_behavior_autoHide
     */
    public boolean isAutoHideEnabled() {
      return mAutoHideEnabled;
    }

    @Override
    public boolean onLayoutChild(
        CoordinatorLayout parent, BottomNavigationView child, int layoutDirection) {
      // Now let the CoordinatorLayout lay out the bar
      parent.onLayoutChild(child, layoutDirection);
      return true;
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, BottomNavigationView child,
                               View target, int dxConsumed, int dyConsumed, int dxUnconsumed,
                               int dyUnconsumed) {
      super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
      if (dyConsumed > 0) {
        // User scrolled down -> hide the navigation view
        child.hide(false);
      } else if (dyConsumed < 0) {
        // User scrolled up -> show the navigation view
        child.show(false);
      }
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, BottomNavigationView child, View directTargetChild, View target, int nestedScrollAxes) {
      return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }
  }
}
