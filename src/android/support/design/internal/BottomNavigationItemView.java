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

package android.support.design.internal;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.R;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.view.menu.MenuItemImpl;
import android.support.v7.view.menu.MenuView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @hide
 */
public class BottomNavigationItemView extends ForegroundLinearLayout implements MenuView.ItemView {
    public static final int INVALID_ITEM_POSTION = -1;

    private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };
    private static final long ACTIVE_ANIMATION_DURATION_MS = 115L;

    private final int mHorizontalPadding;
    private final int mBottomPadding;
    private final int mTopPadding;
    private final int mActiveTopPadding;
    private final float mInactiveLabelSize;
    private final float mActiveLabelSize;

    private ImageView mIcon;
    private TextView mLabel;
    private int mItemPosition = INVALID_ITEM_POSTION;

    private MenuItemImpl mItemData;

    private ColorStateList mIconTint;
    private ColorStateList mTextColor;

    public BottomNavigationItemView(@NonNull Context context) {
        this(context, null);
    }

    public BottomNavigationItemView(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomNavigationItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHorizontalPadding = getResources().getDimensionPixelSize(
                R.dimen.design_bottom_navigation_horizontal_padding);
        mBottomPadding = getResources().getDimensionPixelSize(
                R.dimen.design_bottom_navigation_bottom_padding);
        mTopPadding = getResources().getDimensionPixelSize(
                R.dimen.design_bottom_navigation_top_padding);
        mActiveTopPadding = getResources().getDimensionPixelSize(
                R.dimen.design_bottom_navigation_active_top_padding);
        mInactiveLabelSize =
                getResources().getDimension(R.dimen.design_bottom_navigation_text_size);
        mActiveLabelSize =
                getResources().getDimension(R.dimen.design_bottom_navigation_active_text_size);

        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        LayoutInflater.from(context).inflate(R.layout.design_bottom_navigation_item, this, true);
        setBackgroundResource(R.drawable.design_bottom_navigation_item_background);
        mIcon = (ImageView) findViewById(R.id.icon);
        mLabel = (TextView) findViewById(R.id.label);
    }

    @Override
    public void initialize(MenuItemImpl itemData, int menuType) {
        mItemData = itemData;
        setCheckable(itemData.isCheckable());
        setChecked(itemData.isChecked());
        setEnabled(itemData.isEnabled());
        setIcon(itemData.getIcon());
        setTitle(itemData.getTitle());
        setId(itemData.getItemId());
    }

    public void setItemPosition(int position) {
        mItemPosition = position;
    }

    public int getItemPosition() {
        return mItemPosition;
    }

    @Override
    public MenuItemImpl getItemData() {
        return mItemData;
    }

    @Override
    public void setTitle(CharSequence title) {
        mLabel.setText(title);
    }

    @Override
    public void setCheckable(boolean checkable) {
        refreshDrawableState();
    }

    @Override
    public void setChecked(boolean checked) {
        mItemData.setChecked(checked);
        if (checked) {
            mLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, mActiveLabelSize);
            setPadding(mHorizontalPadding, mActiveTopPadding, mHorizontalPadding, mBottomPadding);
        } else {
            mLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, mInactiveLabelSize);
            setPadding(mHorizontalPadding, mTopPadding, mHorizontalPadding, mBottomPadding);
        }
        refreshDrawableState();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mLabel.setEnabled(enabled);
        mIcon.setEnabled(enabled);
    }

    @Override
    public int[] onCreateDrawableState(final int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (mItemData != null && mItemData.isCheckable() && mItemData.isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    public void setShortcut(boolean showShortcut, char shortcutKey) {
    }

    @Override
    public void setIcon(Drawable icon) {
        if (icon != null) {
            Drawable.ConstantState state = icon.getConstantState();
            icon = DrawableCompat.wrap(state == null ? icon : state.newDrawable()).mutate();
            DrawableCompat.setTintList(icon, mIconTint);
        }
        mIcon.setImageDrawable(icon);
    }

    @Override
    public boolean prefersCondensedTitle() {
        return false;
    }

    @Override
    public boolean showsIcon() {
        return true;
    }

    public void setIconTintList(ColorStateList tint) {
        mIconTint = tint;
        if (mItemData != null) {
            // Update the icon so that the tint takes effect
            setIcon(mItemData.getIcon());
        }
    }

    public void setTextColor(ColorStateList color) {
        mTextColor = color;
        mLabel.setTextColor(color);
    }

    public void setItemBackground(int background) {
        Drawable backgroundDrawable = background == 0
                ? null : ContextCompat.getDrawable(getContext(), background);
        setBackgroundDrawable(backgroundDrawable);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public Animator getAnimator(boolean active) {
        final float currentTextSize = mLabel.getTextSize();
        final int currentTopPadding = getPaddingTop();

        final float finalTextSize = active ? mActiveLabelSize : mInactiveLabelSize;
        final int finalTopPadding = active ? mActiveTopPadding : mTopPadding;

        if (currentTextSize == finalTextSize && currentTopPadding == finalTopPadding) {
            return null;
        }

        // Grow or shrink the text of the tab.
        ValueAnimator textAnimator = ValueAnimator.ofFloat(currentTextSize, finalTextSize);
        textAnimator.setDuration(ACTIVE_ANIMATION_DURATION_MS);
        textAnimator.setInterpolator(new LinearOutSlowInInterpolator());
        textAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedValue = (float) valueAnimator.getAnimatedValue();
                mLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, animatedValue);
            }
        });

        // Reduce or increase the padding top of the tab.
        ValueAnimator paddingTopAnimator = ValueAnimator.ofInt(currentTopPadding, finalTopPadding);
        paddingTopAnimator.setDuration(ACTIVE_ANIMATION_DURATION_MS);
        paddingTopAnimator.setInterpolator(new LinearOutSlowInInterpolator());
        paddingTopAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int animatedValue = (int) valueAnimator.getAnimatedValue();
                setPadding(mHorizontalPadding, animatedValue,
                        mHorizontalPadding, mBottomPadding);
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.playTogether(textAnimator, paddingTopAnimator);
        return set;
    }
}
