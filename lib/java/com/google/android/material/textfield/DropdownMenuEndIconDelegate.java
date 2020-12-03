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

package com.google.android.material.textfield;

import com.google.android.material.R;

import static androidx.core.view.ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO;
import static android.view.accessibility.AccessibilityEvent.TYPE_VIEW_CLICKED;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.appcompat.content.res.AppCompatResources;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.AutoCompleteTextView;
import android.widget.AutoCompleteTextView.OnDismissListener;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.internal.TextWatcherAdapter;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.textfield.TextInputLayout.AccessibilityDelegate;
import com.google.android.material.textfield.TextInputLayout.BoxBackgroundMode;
import com.google.android.material.textfield.TextInputLayout.OnEditTextAttachedListener;
import com.google.android.material.textfield.TextInputLayout.OnEndIconChangedListener;

/** Default initialization of the exposed dropdown menu {@link TextInputLayout.EndIconMode}. */
class DropdownMenuEndIconDelegate extends EndIconDelegate {

  private static final boolean IS_LOLLIPOP = VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;
  private static final int ANIMATION_FADE_OUT_DURATION = 50;
  private static final int ANIMATION_FADE_IN_DURATION = 67;

  private final TextWatcher exposedDropdownEndIconTextWatcher =
      new TextWatcherAdapter() {

        @Override
        public void afterTextChanged(Editable s) {
          final AutoCompleteTextView editText =
              castAutoCompleteTextViewOrThrow(textInputLayout.getEditText());
          // Don't show dropdown list if we're in a11y mode and the menu is editable.
          if (accessibilityManager.isTouchExplorationEnabled()
              && isEditable(editText)
              && !endIconView.hasFocus()) {
            editText.dismissDropDown();
          }
          editText.post(
              new Runnable() {
                @Override
                public void run() {
                  boolean isPopupShowing = editText.isPopupShowing();
                  setEndIconChecked(isPopupShowing);
                  dropdownPopupDirty = isPopupShowing;
                }
              });
        }
      };
  private final OnFocusChangeListener onFocusChangeListener =
      new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
          textInputLayout.setEndIconActivated(hasFocus);
          if (!hasFocus) {
            setEndIconChecked(false);
            dropdownPopupDirty = false;
          }
        }
      };
  private final TextInputLayout.AccessibilityDelegate accessibilityDelegate =
      new AccessibilityDelegate(textInputLayout) {
        @Override
        public void onInitializeAccessibilityNodeInfo(
            View host, @NonNull AccessibilityNodeInfoCompat info) {
          super.onInitializeAccessibilityNodeInfo(host, info);
          // The non-editable exposed dropdown menu behaves like a Spinner.
          if (!isEditable(textInputLayout.getEditText())) {
            info.setClassName(Spinner.class.getName());
          }
          if (info.isShowingHintText()) {
            // Set hint text to null so TalkBack doesn't announce the label twice when there is no
            // item selected.
            info.setHintText(null);
          }
        }

        @Override
        public void onPopulateAccessibilityEvent(View host, @NonNull AccessibilityEvent event) {
          super.onPopulateAccessibilityEvent(host, event);
          AutoCompleteTextView editText =
              castAutoCompleteTextViewOrThrow(textInputLayout.getEditText());

          // If dropdown is non editable, layout click is what triggers showing/hiding the popup
          // list. Otherwise, arrow icon alone is what triggers it.
          if (event.getEventType() == TYPE_VIEW_CLICKED
              && accessibilityManager.isTouchExplorationEnabled()
              && !isEditable(textInputLayout.getEditText())) {
            showHideDropdown(editText);
          }
        }
      };
  private final OnEditTextAttachedListener dropdownMenuOnEditTextAttachedListener =
      new OnEditTextAttachedListener() {
        @Override
        public void onEditTextAttached(@NonNull TextInputLayout textInputLayout) {
          AutoCompleteTextView autoCompleteTextView =
              castAutoCompleteTextViewOrThrow(textInputLayout.getEditText());

          setPopupBackground(autoCompleteTextView);
          addRippleEffect(autoCompleteTextView);
          setUpDropdownShowHideBehavior(autoCompleteTextView);
          autoCompleteTextView.setThreshold(0);
          autoCompleteTextView.removeTextChangedListener(exposedDropdownEndIconTextWatcher);
          autoCompleteTextView.addTextChangedListener(exposedDropdownEndIconTextWatcher);
          textInputLayout.setEndIconCheckable(true);
          textInputLayout.setErrorIconDrawable(null);
          if (!isEditable(autoCompleteTextView)) {
            ViewCompat.setImportantForAccessibility(endIconView, IMPORTANT_FOR_ACCESSIBILITY_NO);
          }
          textInputLayout.setTextInputAccessibilityDelegate(accessibilityDelegate);

          textInputLayout.setEndIconVisible(true);
        }
      };

  @SuppressLint("ClickableViewAccessibility") // There's an accessibility delegate that handles
  // interactions with the dropdown menu.
  private final OnEndIconChangedListener endIconChangedListener =
      new OnEndIconChangedListener() {
        @Override
        public void onEndIconChanged(@NonNull TextInputLayout textInputLayout, int previousIcon) {
          final AutoCompleteTextView editText =
              (AutoCompleteTextView) textInputLayout.getEditText();
          if (editText != null && previousIcon == TextInputLayout.END_ICON_DROPDOWN_MENU) {
            // Remove any listeners set on the edit text.
            editText.post(
                new Runnable() {
                  @Override
                  public void run() {
                    editText.removeTextChangedListener(exposedDropdownEndIconTextWatcher);
                  }
                });
            if (editText.getOnFocusChangeListener() == onFocusChangeListener) {
              editText.setOnFocusChangeListener(null);
            }
            editText.setOnTouchListener(null);
            if (IS_LOLLIPOP) {
              editText.setOnDismissListener(null);
            }
          }
        }
      };

  private boolean dropdownPopupDirty = false;
  private boolean isEndIconChecked = false;
  private long dropdownPopupActivatedAt = Long.MAX_VALUE;
  private StateListDrawable filledPopupBackground;
  private MaterialShapeDrawable outlinedPopupBackground;
  @Nullable private AccessibilityManager accessibilityManager;
  private ValueAnimator fadeOutAnim;
  private ValueAnimator fadeInAnim;

  DropdownMenuEndIconDelegate(@NonNull TextInputLayout textInputLayout) {
    super(textInputLayout);
  }

  @Override
  void initialize() {
    float popupCornerRadius =
        context
            .getResources()
            .getDimensionPixelOffset(R.dimen.mtrl_shape_corner_size_small_component);
    float exposedDropdownPopupElevation =
        context
            .getResources()
            .getDimensionPixelOffset(R.dimen.mtrl_exposed_dropdown_menu_popup_elevation);
    int exposedDropdownPopupVerticalPadding =
        context
            .getResources()
            .getDimensionPixelOffset(R.dimen.mtrl_exposed_dropdown_menu_popup_vertical_padding);
    // Background for the popups of the outlined variation and for the filled variation when it is
    // being displayed above the layout.
    MaterialShapeDrawable roundedCornersPopupBackground =
        getPopUpMaterialShapeDrawable(
            popupCornerRadius,
            popupCornerRadius,
            exposedDropdownPopupElevation,
            exposedDropdownPopupVerticalPadding);
    // Background for the popup of the filled variation when it is being displayed below the layout.
    MaterialShapeDrawable roundedBottomCornersPopupBackground =
        getPopUpMaterialShapeDrawable(
            0,
            popupCornerRadius,
            exposedDropdownPopupElevation,
            exposedDropdownPopupVerticalPadding);

    outlinedPopupBackground = roundedCornersPopupBackground;
    filledPopupBackground = new StateListDrawable();
    filledPopupBackground.addState(
        new int[] {android.R.attr.state_above_anchor}, roundedCornersPopupBackground);
    filledPopupBackground.addState(new int[] {}, roundedBottomCornersPopupBackground);

    // For lollipop+, the arrow icon changes orientation based on dropdown popup, otherwise it
    // always points down.
    int drawableResId =
        IS_LOLLIPOP ? R.drawable.mtrl_dropdown_arrow : R.drawable.mtrl_ic_arrow_drop_down;
    textInputLayout.setEndIconDrawable(AppCompatResources.getDrawable(context, drawableResId));
    textInputLayout.setEndIconContentDescription(
        textInputLayout.getResources().getText(R.string.exposed_dropdown_menu_content_description));
    textInputLayout.setEndIconOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            AutoCompleteTextView editText = (AutoCompleteTextView) textInputLayout.getEditText();
            showHideDropdown(editText);
          }
        });
    textInputLayout.addOnEditTextAttachedListener(dropdownMenuOnEditTextAttachedListener);
    textInputLayout.addOnEndIconChangedListener(endIconChangedListener);
    initAnimators();
    accessibilityManager =
        (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
  }

  @Override
  boolean shouldTintIconOnError() {
    return true;
  }

  @Override
  boolean isBoxBackgroundModeSupported(@BoxBackgroundMode int boxBackgroundMode) {
    return boxBackgroundMode != TextInputLayout.BOX_BACKGROUND_NONE;
  }

  private void showHideDropdown(@Nullable AutoCompleteTextView editText) {
    if (editText == null) {
      return;
    }
    if (isDropdownPopupActive()) {
      dropdownPopupDirty = false;
    }
    if (!dropdownPopupDirty) {
      if (IS_LOLLIPOP) {
        setEndIconChecked(!isEndIconChecked);
      } else {
        isEndIconChecked = !isEndIconChecked;
        endIconView.toggle();
      }
      if (isEndIconChecked) {
        editText.requestFocus();
        editText.showDropDown();
      } else {
        editText.dismissDropDown();
      }
    } else {
      dropdownPopupDirty = false;
    }
  }

  private void setPopupBackground(@NonNull AutoCompleteTextView editText) {
    if (IS_LOLLIPOP) {
      int boxBackgroundMode = textInputLayout.getBoxBackgroundMode();
      if (boxBackgroundMode == TextInputLayout.BOX_BACKGROUND_OUTLINE) {
        editText.setDropDownBackgroundDrawable(outlinedPopupBackground);
      } else if (boxBackgroundMode == TextInputLayout.BOX_BACKGROUND_FILLED) {
        editText.setDropDownBackgroundDrawable(filledPopupBackground);
      }
    }
  }

  /* Add ripple effect to non editable layouts. */
  private void addRippleEffect(@NonNull AutoCompleteTextView editText) {
    if (isEditable(editText)) {
      return;
    }

    int boxBackgroundMode = textInputLayout.getBoxBackgroundMode();
    MaterialShapeDrawable boxBackground = textInputLayout.getBoxBackground();
    int rippleColor = MaterialColors.getColor(editText, R.attr.colorControlHighlight);
    int[][] states =
        new int[][] {
            new int[] {android.R.attr.state_pressed}, new int[] {},
        };

    if (boxBackgroundMode == TextInputLayout.BOX_BACKGROUND_OUTLINE) {
      addRippleEffectOnOutlinedLayout(editText, rippleColor, states, boxBackground);
    } else if (boxBackgroundMode == TextInputLayout.BOX_BACKGROUND_FILLED) {
      addRippleEffectOnFilledLayout(editText, rippleColor, states, boxBackground);
    }
  }

  private void addRippleEffectOnOutlinedLayout(
      @NonNull AutoCompleteTextView editText,
      int rippleColor,
      int[][] states,
      @NonNull MaterialShapeDrawable boxBackground) {
    LayerDrawable editTextBackground;
    int surfaceColor = MaterialColors.getColor(editText, R.attr.colorSurface);
    MaterialShapeDrawable rippleBackground =
        new MaterialShapeDrawable(boxBackground.getShapeAppearanceModel());
    int pressedBackgroundColor = MaterialColors.layer(rippleColor, surfaceColor, 0.1f);
    int[] rippleBackgroundColors = new int[] {pressedBackgroundColor, Color.TRANSPARENT};
    rippleBackground.setFillColor(new ColorStateList(states, rippleBackgroundColors));

    if (IS_LOLLIPOP) {
      rippleBackground.setTint(surfaceColor);
      int[] colors = new int[] {pressedBackgroundColor, surfaceColor};
      ColorStateList rippleColorStateList = new ColorStateList(states, colors);
      MaterialShapeDrawable mask =
          new MaterialShapeDrawable(boxBackground.getShapeAppearanceModel());
      mask.setTint(Color.WHITE);
      Drawable rippleDrawable = new RippleDrawable(rippleColorStateList, rippleBackground, mask);
      Drawable[] layers = {rippleDrawable, boxBackground};
      editTextBackground = new LayerDrawable(layers);
    } else {
      Drawable[] layers = {rippleBackground, boxBackground};
      editTextBackground = new LayerDrawable(layers);
    }

    ViewCompat.setBackground(editText, editTextBackground);
  }

  private void addRippleEffectOnFilledLayout(
      @NonNull AutoCompleteTextView editText,
      int rippleColor,
      int[][] states,
      @NonNull MaterialShapeDrawable boxBackground) {
    int boxBackgroundColor = textInputLayout.getBoxBackgroundColor();
    int pressedBackgroundColor = MaterialColors.layer(rippleColor, boxBackgroundColor, 0.1f);
    int[] colors = new int[] {pressedBackgroundColor, boxBackgroundColor};

    if (IS_LOLLIPOP) {
      ColorStateList rippleColorStateList = new ColorStateList(states, colors);
      Drawable editTextBackground =
          new RippleDrawable(rippleColorStateList, boxBackground, boxBackground);
      ViewCompat.setBackground(editText, editTextBackground);
    } else {
      MaterialShapeDrawable rippleBackground =
          new MaterialShapeDrawable(boxBackground.getShapeAppearanceModel());
      rippleBackground.setFillColor(new ColorStateList(states, colors));
      Drawable[] layers = {boxBackground, rippleBackground};
      LayerDrawable editTextBackground = new LayerDrawable(layers);
      int start = ViewCompat.getPaddingStart(editText);
      int top = editText.getPaddingTop();
      int end = ViewCompat.getPaddingEnd(editText);
      int bottom = editText.getPaddingBottom();
      ViewCompat.setBackground(editText, editTextBackground);
      ViewCompat.setPaddingRelative(editText, start, top, end, bottom);
    }
  }

  @SuppressLint("ClickableViewAccessibility") // There's an accessibility delegate that handles
  // interactions with the dropdown menu.
  private void setUpDropdownShowHideBehavior(@NonNull final AutoCompleteTextView editText) {
    // Set whole layout clickable.
    editText.setOnTouchListener(
        new OnTouchListener() {
          @Override
          public boolean onTouch(@NonNull View v, @NonNull MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
              if (isDropdownPopupActive()) {
                dropdownPopupDirty = false;
              }
              showHideDropdown(editText);
            }
            return false;
          }
        });
    editText.setOnFocusChangeListener(onFocusChangeListener);
    if (IS_LOLLIPOP) {
      editText.setOnDismissListener(
          new OnDismissListener() {
            @Override
            public void onDismiss() {
              dropdownPopupDirty = true;
              dropdownPopupActivatedAt = System.currentTimeMillis();
              setEndIconChecked(false);
            }
          });
    }
  }

  private MaterialShapeDrawable getPopUpMaterialShapeDrawable(
      float topCornerRadius, float bottomCornerRadius, float elevation, int verticalPadding) {
    ShapeAppearanceModel shapeAppearanceModel =
        ShapeAppearanceModel.builder()
            .setTopLeftCornerSize(topCornerRadius)
            .setTopRightCornerSize(topCornerRadius)
            .setBottomLeftCornerSize(bottomCornerRadius)
            .setBottomRightCornerSize(bottomCornerRadius)
            .build();
    MaterialShapeDrawable popupDrawable =
        MaterialShapeDrawable.createWithElevationOverlay(context, elevation);
    popupDrawable.setShapeAppearanceModel(shapeAppearanceModel);
    popupDrawable.setPadding(0, verticalPadding, 0, verticalPadding);
    return popupDrawable;
  }

  private boolean isDropdownPopupActive() {
    long activeFor = System.currentTimeMillis() - dropdownPopupActivatedAt;
    return activeFor < 0 || activeFor > 300;
  }

  @NonNull
  private static AutoCompleteTextView castAutoCompleteTextViewOrThrow(EditText editText) {
    if (!(editText instanceof AutoCompleteTextView)) {
      throw new RuntimeException(
          "EditText needs to be an AutoCompleteTextView if an Exposed Dropdown Menu is being"
              + " used.");
    }

    return (AutoCompleteTextView) editText;
  }

  private static boolean isEditable(@NonNull EditText editText) {
    return editText.getKeyListener() != null;
  }

  private void setEndIconChecked(boolean checked) {
    if (isEndIconChecked != checked) {
      isEndIconChecked = checked;
      fadeInAnim.cancel();
      fadeOutAnim.start();
    }
  }

  private void initAnimators() {
    fadeInAnim = getAlphaAnimator(ANIMATION_FADE_IN_DURATION, 0, 1);
    fadeOutAnim = getAlphaAnimator(ANIMATION_FADE_OUT_DURATION, 1, 0);
    fadeOutAnim.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            endIconView.setChecked(isEndIconChecked);
            fadeInAnim.start();
          }
        });
  }

  private ValueAnimator getAlphaAnimator(int duration, float... values) {
    ValueAnimator animator = ValueAnimator.ofFloat(values);
    animator.setInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
    animator.setDuration(duration);
    animator.addUpdateListener(
        new AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(@NonNull ValueAnimator animation) {
            float alpha = (float) animation.getAnimatedValue();
            endIconView.setAlpha(alpha);
          }
        });

    return animator;
  }
}
