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

package android.support.design.widget;

import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.design.R;
import android.support.v4.text.TextDirectionHeuristicsCompat;
import android.support.v4.view.ViewCompat;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Interpolator;

final class CollapsingTextHelper {

    // Pre-JB-MR2 doesn't support HW accelerated canvas scaled text so we will workaround it
    // by using our own texture
    private static final boolean USE_SCALING_TEXTURE = Build.VERSION.SDK_INT < 18;

    private static final boolean DEBUG_DRAW = false;
    private static final Paint DEBUG_DRAW_PAINT;
    static {
        DEBUG_DRAW_PAINT = DEBUG_DRAW ? new Paint() : null;
        if (DEBUG_DRAW_PAINT != null) {
            DEBUG_DRAW_PAINT.setAntiAlias(true);
            DEBUG_DRAW_PAINT.setColor(Color.MAGENTA);
        }
    }

    private final View mView;

    private float mExpandedFraction;

    private final Rect mExpandedBounds;
    private final Rect mCollapsedBounds;
    private int mExpandedTextVerticalGravity = Gravity.CENTER_VERTICAL;
    private int mCollapsedTextVerticalGravity = Gravity.CENTER_VERTICAL;
    private float mExpandedTextSize;
    private float mCollapsedTextSize;
    private int mExpandedTextColor;
    private int mCollapsedTextColor;

    private float mExpandedTop;
    private float mCollapsedTop;

    private CharSequence mText;
    private CharSequence mTextToDraw;
    private float mTextWidth;
    private boolean mIsRtl;

    private boolean mUseTexture;
    private Bitmap mExpandedTitleTexture;
    private Paint mTexturePaint;
    private float mTextureAscent;
    private float mTextureDescent;

    private float mCurrentLeft;
    private float mCurrentRight;
    private float mCurrentTop;
    private float mScale;
    private float mCurrentTextSize;

    private final TextPaint mTextPaint;

    private Interpolator mPositionInterpolator;
    private Interpolator mTextSizeInterpolator;

    public CollapsingTextHelper(View view) {
        mView = view;

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);

        mCollapsedBounds = new Rect();
        mExpandedBounds = new Rect();
    }

    void setTextSizeInterpolator(Interpolator interpolator) {
        mTextSizeInterpolator = interpolator;
        recalculate();
    }

    void setPositionInterpolator(Interpolator interpolator) {
        mPositionInterpolator = interpolator;
        recalculate();
    }

    void setExpandedTextSize(float textSize) {
        if (mExpandedTextSize != textSize) {
            mExpandedTextSize = textSize;
            recalculate();
        }
    }

    void setCollapsedTextSize(float textSize) {
        if (mCollapsedTextSize != textSize) {
            mCollapsedTextSize = textSize;
            recalculate();
        }
    }

    void setCollapsedTextColor(int textColor) {
        if (mCollapsedTextColor != textColor) {
            mCollapsedTextColor = textColor;
            recalculate();
        }
    }

    void setExpandedTextColor(int textColor) {
        if (mExpandedTextColor != textColor) {
            mExpandedTextColor = textColor;
            recalculate();
        }
    }

    void setExpandedBounds(int left, int top, int right, int bottom) {
        mExpandedBounds.set(left, top, right, bottom);
        recalculate();
    }

    void setCollapsedBounds(int left, int top, int right, int bottom) {
        mCollapsedBounds.set(left, top, right, bottom);
        recalculate();
    }

    void setExpandedTextVerticalGravity(int gravity) {
        gravity &= Gravity.VERTICAL_GRAVITY_MASK;

        if (mExpandedTextVerticalGravity != gravity) {
            mExpandedTextVerticalGravity = gravity;
            recalculate();
        }
    }

    void setCollapsedTextVerticalGravity(int gravity) {
        gravity &= Gravity.VERTICAL_GRAVITY_MASK;

        if (mCollapsedTextVerticalGravity != gravity) {
            mCollapsedTextVerticalGravity = gravity;
            recalculate();
        }
    }

    void setCollapsedTextAppearance(int resId) {
        TypedArray a = mView.getContext().obtainStyledAttributes(resId, R.styleable.TextAppearance);
        if (a.hasValue(R.styleable.TextAppearance_android_textColor)) {
            mCollapsedTextColor = a.getColor(R.styleable.TextAppearance_android_textColor, 0);
        }
        if (a.hasValue(R.styleable.TextAppearance_android_textSize)) {
            mCollapsedTextSize = a.getDimensionPixelSize(
                    R.styleable.TextAppearance_android_textSize, 0);
        }
        a.recycle();

        recalculate();
    }

    void setExpandedTextAppearance(int resId) {
        TypedArray a = mView.getContext().obtainStyledAttributes(resId, R.styleable.TextAppearance);
        if (a.hasValue(R.styleable.TextAppearance_android_textColor)) {
            mExpandedTextColor = a.getColor(R.styleable.TextAppearance_android_textColor, 0);
        }
        if (a.hasValue(R.styleable.TextAppearance_android_textSize)) {
            mExpandedTextSize = a.getDimensionPixelSize(
                    R.styleable.TextAppearance_android_textSize, 0);
        }
        a.recycle();

        recalculate();
    }

    /**
     * Set the value indicating the current scroll value. This decides how much of the
     * background will be displayed, as well as the title metrics/positioning.
     *
     * A value of {@code 0.0} indicates that the layout is fully expanded.
     * A value of {@code 1.0} indicates that the layout is fully collapsed.
     */
    void setExpansionFraction(float fraction) {
        fraction = MathUtils.constrain(fraction, 0f, 1f);

        if (fraction != mExpandedFraction) {
            mExpandedFraction = fraction;
            calculateOffsets();
        }
    }

    float getExpansionFraction() {
        return mExpandedFraction;
    }

    float getCollapsedTextSize() {
        return mCollapsedTextSize;
    }

    float getExpandedTextSize() {
        return mExpandedTextSize;
    }

    private void calculateOffsets() {
        final float fraction = mExpandedFraction;

        mCurrentLeft = interpolate(mExpandedBounds.left, mCollapsedBounds.left,
                fraction, mPositionInterpolator);
        mCurrentTop = interpolate(mExpandedTop, mCollapsedTop, fraction, mPositionInterpolator);
        mCurrentRight = interpolate(mExpandedBounds.right, mCollapsedBounds.right,
                fraction, mPositionInterpolator);
        setInterpolatedTextSize(interpolate(mExpandedTextSize, mCollapsedTextSize,
                fraction, mTextSizeInterpolator));

        if (mCollapsedTextColor != mExpandedTextColor) {
            // If the collapsed and expanded text colors are different, blend them based on the
            // fraction
            mTextPaint.setColor(blendColors(mExpandedTextColor, mCollapsedTextColor, fraction));
        } else {
            mTextPaint.setColor(mCollapsedTextColor);
        }

        ViewCompat.postInvalidateOnAnimation(mView);
    }

    private void calculateBaselines() {
        // We then calculate the collapsed text size, using the same logic
        mTextPaint.setTextSize(mCollapsedTextSize);
        switch (mCollapsedTextVerticalGravity) {
            case Gravity.BOTTOM:
                mCollapsedTop = mCollapsedBounds.bottom;
                break;
            case Gravity.TOP:
                mCollapsedTop = mCollapsedBounds.top - mTextPaint.ascent();
                break;
            case Gravity.CENTER_VERTICAL:
            default:
                float textHeight = mTextPaint.descent() - mTextPaint.ascent();
                float textOffset = (textHeight / 2) - mTextPaint.descent();
                mCollapsedTop = mCollapsedBounds.centerY() + textOffset;
                break;
        }

        mTextPaint.setTextSize(mExpandedTextSize);
        switch (mExpandedTextVerticalGravity) {
            case Gravity.BOTTOM:
                mExpandedTop = mExpandedBounds.bottom;
                break;
            case Gravity.TOP:
                mExpandedTop = mExpandedBounds.top - mTextPaint.ascent();
                break;
            case Gravity.CENTER_VERTICAL:
            default:
                float textHeight = mTextPaint.descent() - mTextPaint.ascent();
                float textOffset = (textHeight / 2) - mTextPaint.descent();
                mExpandedTop = mExpandedBounds.centerY() + textOffset;
                break;
        }
        mTextureAscent = mTextPaint.ascent();
        mTextureDescent = mTextPaint.descent();

        // The bounds have changed so we need to clear the texture
        clearTexture();
    }

    public void draw(Canvas canvas) {
        final int saveCount = canvas.save();

        if (mTextToDraw != null) {
            final boolean isRtl = mIsRtl;

            float x = isRtl ? mCurrentRight : mCurrentLeft;
            float y = mCurrentTop;

            final boolean drawTexture = mUseTexture && mExpandedTitleTexture != null;

            final float ascent;
            final float descent;

            // Update the TextPaint to the current text size
            mTextPaint.setTextSize(mCurrentTextSize);

            if (drawTexture) {
                ascent = mTextureAscent * mScale;
                descent = mTextureDescent * mScale;
            } else {
                ascent = mTextPaint.ascent() * mScale;
                descent = mTextPaint.descent() * mScale;
            }

            if (DEBUG_DRAW) {
                // Just a debug tool, which drawn a Magneta rect in the text bounds
                canvas.drawRect(mCurrentLeft, y + ascent, mCurrentRight, y + descent,
                        DEBUG_DRAW_PAINT);
            }

            if (drawTexture) {
                y += ascent;
            }

            if (mScale != 1f) {
                canvas.scale(mScale, mScale, x, y);
            }

            if (isRtl) {
                x -= mTextWidth;
            }

            if (drawTexture) {
                // If we should use a texture, draw it instead of text
                canvas.drawBitmap(mExpandedTitleTexture, x, y, mTexturePaint);
            } else {
                canvas.drawText(mTextToDraw, 0, mTextToDraw.length(), x, y, mTextPaint);
            }
        }

        canvas.restoreToCount(saveCount);
    }

    private boolean calculateIsRtl(CharSequence text) {
        final boolean defaultIsRtl = ViewCompat.getLayoutDirection(mView)
                == ViewCompat.LAYOUT_DIRECTION_RTL;
        return (defaultIsRtl
                ? TextDirectionHeuristicsCompat.FIRSTSTRONG_RTL
                : TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR).isRtl(text, 0, text.length());
    }

    private void setInterpolatedTextSize(final float textSize) {
        if (mText == null) return;

        final float availableWidth;
        final float newTextSize;
        boolean updateDrawText = false;

        if (isClose(textSize, mCollapsedTextSize)) {
            availableWidth = mCollapsedBounds.width();
            newTextSize = mCollapsedTextSize;
            mScale = 1f;
        } else {
            availableWidth = mExpandedBounds.width();
            newTextSize = mExpandedTextSize;

            if (isClose(textSize, mExpandedTextSize)) {
                // If we're close to the expanded text size, snap to it and use a scale of 1
                mScale = 1f;
            } else {
                // Else, we'll scale down from the expanded text size
                mScale = textSize / mExpandedTextSize;
            }
        }

        if (availableWidth > 0) {
            updateDrawText = mCurrentTextSize != newTextSize;
            mCurrentTextSize = newTextSize;
        }

        if (mTextToDraw == null || updateDrawText) {
            mTextPaint.setTextSize(mCurrentTextSize);

            // If we don't currently have text to draw, or the text size has changed, ellipsize...
            final CharSequence title = TextUtils.ellipsize(mText, mTextPaint,
                    availableWidth, TextUtils.TruncateAt.END);
            if (mTextToDraw == null || !mTextToDraw.equals(title)) {
                mTextToDraw = title;
            }
            mIsRtl = calculateIsRtl(mTextToDraw);
            mTextWidth = mTextPaint.measureText(mTextToDraw, 0, mTextToDraw.length());
        }

        // Use our texture if the scale isn't 1.0
        mUseTexture = USE_SCALING_TEXTURE && mScale != 1f;

        if (mUseTexture) {
            // Make sure we have an expanded texture if needed
            ensureExpandedTexture();
        }

        ViewCompat.postInvalidateOnAnimation(mView);
    }

    private void ensureExpandedTexture() {
        if (mExpandedTitleTexture != null || mExpandedBounds.isEmpty()
                || TextUtils.isEmpty(mTextToDraw)) {
            return;
        }

        mTextPaint.setTextSize(mExpandedTextSize);
        mTextPaint.setColor(mExpandedTextColor);

        final int w = Math.round(mTextPaint.measureText(mTextToDraw, 0, mTextToDraw.length()));
        final int h = Math.round(mTextPaint.descent() - mTextPaint.ascent());
        mTextWidth = w;

        if (w <= 0 && h <= 0) {
            return; // If the width or height are 0, return
        }

        mExpandedTitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(mExpandedTitleTexture);
        c.drawText(mTextToDraw, 0, mTextToDraw.length(), 0, h - mTextPaint.descent(), mTextPaint);

        if (mTexturePaint == null) {
            // Make sure we have a paint
            mTexturePaint = new Paint();
            mTexturePaint.setAntiAlias(true);
            mTexturePaint.setFilterBitmap(true);
        }
    }

    private void recalculate() {
        if (ViewCompat.isLaidOut(mView)) {
            // If we've already been laid out, calculate everything now otherwise we'll wait
            // until a layout
            calculateBaselines();
            calculateOffsets();
        }
    }

    /**
     * Set the title to display
     *
     * @param text
     */
    void setText(CharSequence text) {
        if (text == null || !text.equals(mText)) {
            mText = text;
            clearTexture();
            recalculate();
        }
    }

    CharSequence getText() {
        return mText;
    }

    private void clearTexture() {
        if (mExpandedTitleTexture != null) {
            mExpandedTitleTexture.recycle();
            mExpandedTitleTexture = null;
        }
    }

    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        recalculate();
    }

    /**
     * Returns true if {@code value} is 'close' to it's closest decimal value. Close is currently
     * defined as it's difference being < 0.001.
     */
    private static boolean isClose(float value, float targetValue) {
        return Math.abs(value - targetValue) < 0.001f;
    }

    int getExpandedTextColor() {
        return mExpandedTextColor;
    }

    int getCollapsedTextColor() {
        return mCollapsedTextColor;
    }

    /**
     * Blend {@code color1} and {@code color2} using the given ratio.
     *
     * @param ratio of which to blend. 0.0 will return {@code color1}, 0.5 will give an even blend,
     *              1.0 will return {@code color2}.
     */
    private static int blendColors(int color1, int color2, float ratio) {
        final float inverseRatio = 1f - ratio;
        float a = (Color.alpha(color1) * inverseRatio) + (Color.alpha(color2) * ratio);
        float r = (Color.red(color1) * inverseRatio) + (Color.red(color2) * ratio);
        float g = (Color.green(color1) * inverseRatio) + (Color.green(color2) * ratio);
        float b = (Color.blue(color1) * inverseRatio) + (Color.blue(color2) * ratio);
        return Color.argb((int) a, (int) r, (int) g, (int) b);
    }

    private static float interpolate(float startValue, float endValue, float fraction,
            Interpolator interpolator) {
        if (interpolator != null) {
            fraction = interpolator.getInterpolation(fraction);
        }
        return AnimationUtils.lerp(startValue, endValue, fraction);
    }
}
