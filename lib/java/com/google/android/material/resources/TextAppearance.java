/*
 * Copyright 2017 The Android Open Source Project
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

package com.google.android.material.resources;

import com.google.android.material.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import androidx.annotation.FontRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.StyleRes;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.content.res.ResourcesCompat.FontCallback;
import androidx.core.provider.FontsContractCompat.FontRequestCallback;
import org.xmlpull.v1.XmlPullParser;

/**
 * Utility class that contains the data from parsing a TextAppearance style resource.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class TextAppearance {

  private static final String TAG = "TextAppearance";

  // Enums from AppCompatTextHelper.
  private static final int TYPEFACE_SANS = 1;
  private static final int TYPEFACE_SERIF = 2;
  private static final int TYPEFACE_MONOSPACE = 3;

  @Nullable public final ColorStateList textColorHint;
  @Nullable public final ColorStateList textColorLink;
  @Nullable public final ColorStateList shadowColor;
  @Nullable public final String fontFamily;
  @Nullable public String fontVariationSettings;

  public final int textStyle;
  public final int typeface;
  public final boolean textAllCaps;
  public final float shadowDx;
  public final float shadowDy;
  public final float shadowRadius;
  public final boolean hasLetterSpacing;
  public final float letterSpacing;

  @Nullable
  private ColorStateList textColor;
  private float textSize;

  @FontRes private final int fontFamilyResourceId;

  private boolean fontResolved = false;
  private boolean systemFontLoadAttempted = false;
  private Typeface font;

  /** Parses the given TextAppearance style resource. */
  public TextAppearance(@NonNull Context context, @StyleRes int id) {
    TypedArray a =
        context.obtainStyledAttributes(id, androidx.appcompat.R.styleable.TextAppearance);

    setTextSize(
        a.getDimension(
            androidx.appcompat.R.styleable.TextAppearance_android_textSize, 0f));
    setTextColor(
        MaterialResources.getColorStateList(
            context, a, androidx.appcompat.R.styleable.TextAppearance_android_textColor));
    textColorHint =
        MaterialResources.getColorStateList(
            context,
            a,
            androidx.appcompat.R.styleable.TextAppearance_android_textColorHint);
    textColorLink =
        MaterialResources.getColorStateList(
            context,
            a,
            androidx.appcompat.R.styleable.TextAppearance_android_textColorLink);
    textStyle =
        a.getInt(
            androidx.appcompat.R.styleable.TextAppearance_android_textStyle,
            Typeface.NORMAL);
    typeface =
        a.getInt(
            androidx.appcompat.R.styleable.TextAppearance_android_typeface,
            TYPEFACE_SANS);
    int fontFamilyIndex =
        MaterialResources.getIndexWithValue(
            a,
            androidx.appcompat.R.styleable.TextAppearance_fontFamily,
            androidx.appcompat.R.styleable.TextAppearance_android_fontFamily);
    fontFamilyResourceId = a.getResourceId(fontFamilyIndex, 0);
    fontFamily = a.getString(fontFamilyIndex);
    textAllCaps =
        a.getBoolean(androidx.appcompat.R.styleable.TextAppearance_textAllCaps, false);
    shadowColor =
        MaterialResources.getColorStateList(
            context,
            a,
            androidx.appcompat.R.styleable.TextAppearance_android_shadowColor);
    shadowDx =
        a.getFloat(androidx.appcompat.R.styleable.TextAppearance_android_shadowDx, 0);
    shadowDy =
        a.getFloat(androidx.appcompat.R.styleable.TextAppearance_android_shadowDy, 0);
    shadowRadius =
        a.getFloat(androidx.appcompat.R.styleable.TextAppearance_android_shadowRadius, 0);

    a.recycle();

    a = context.obtainStyledAttributes(id, R.styleable.MaterialTextAppearance);
    hasLetterSpacing = a.hasValue(R.styleable.MaterialTextAppearance_android_letterSpacing);
    letterSpacing = a.getFloat(R.styleable.MaterialTextAppearance_android_letterSpacing, 0);
    if (VERSION.SDK_INT >= VERSION_CODES.O) {
      int fontVariationSettingsIndex = MaterialResources.getIndexWithValue(
          a,
          R.styleable.MaterialTextAppearance_fontVariationSettings,
          R.styleable.MaterialTextAppearance_android_fontVariationSettings);
      fontVariationSettings = a.getString(fontVariationSettingsIndex);
    }
    a.recycle();
  }

  /**
   * Synchronously resolves the font Typeface using the fontFamily, style, and typeface.
   *
   * @see androidx.appcompat.widget.AppCompatTextHelper
   */
  @VisibleForTesting
  @NonNull
  public Typeface getFont(@NonNull Context context) {
    if (fontResolved) {
      return font;
    }

    // Try resolving fontFamily as a font resource.
    if (!context.isRestricted()) {
      try {
        font = ResourcesCompat.getFont(context, fontFamilyResourceId);
        if (font != null) {
          font = Typeface.create(font, textStyle);
        }
      } catch (UnsupportedOperationException | Resources.NotFoundException e) {
        // Expected if it is not a font resource.
      } catch (Exception e) {
        Log.d(TAG, "Error loading font " + fontFamily, e);
      }
    }

    // If not resolved create fallback and resolve.
    createFallbackFont();
    fontResolved = true;

    return font;
  }

  /**
   * Resolves the requested font using the fontFamily, style, and typeface. Immediately (and
   * synchronously) calls {@link TextAppearanceFontCallback#onFontRetrieved(Typeface, boolean)} with
   * the requested font, if it has been resolved already, or {@link
   * TextAppearanceFontCallback#onFontRetrievalFailed(int)} if requested fontFamily is invalid.
   * Otherwise callback is invoked asynchronously when the font is loaded (or async loading fails).
   * While font is being fetched asynchronously, {@link #getFallbackFont()} can be used as a
   * temporary font.
   *
   * @param context the {@link Context}.
   * @param callback callback to notify when font is loaded.
   * @see androidx.appcompat.widget.AppCompatTextHelper
   */
  public void getFontAsync(
      @NonNull Context context, @NonNull final TextAppearanceFontCallback callback) {
    if (!maybeLoadFontSynchronously(context)) {
      // No-op if font already resolved.
      createFallbackFont();
    }

    if (fontFamilyResourceId == 0) {
      // Only fontFamily id requires async fetch, if undefined the fallback font is the actual font.
      fontResolved = true;
    }

    if (fontResolved) {
      callback.onFontRetrieved(font, true);
      return;
    }

    // Try to resolve fontFamily asynchronously. If failed fallback font is used instead.
    try {
      ResourcesCompat.getFont(
          context,
          fontFamilyResourceId,
          new FontCallback() {
            @Override
            public void onFontRetrieved(@NonNull Typeface typeface) {
              font = Typeface.create(typeface, textStyle);
              fontResolved = true;
              callback.onFontRetrieved(font, false);
            }

            @Override
            public void onFontRetrievalFailed(int reason) {
              fontResolved = true;
              callback.onFontRetrievalFailed(reason);
            }
          },
          /* handler */ null);
    } catch (Resources.NotFoundException e) {
      // Expected if it is not a font resource.
      fontResolved = true;
      callback.onFontRetrievalFailed(FontRequestCallback.FAIL_REASON_FONT_NOT_FOUND);
    } catch (Exception e) {
      Log.d(TAG, "Error loading font " + fontFamily, e);
      fontResolved = true;
      callback.onFontRetrievalFailed(FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR);
    }
  }

  /**
   * Asynchronously resolves the requested font Typeface using the fontFamily, style, and typeface,
   * and automatically updates given {@code textPaint} using {@link #updateTextPaintMeasureState} on
   * successful load.
   *
   * @param context The {@link Context}.
   * @param textPaint {@link TextPaint} to be updated.
   * @param callback Callback to notify when font is available.
   * @see #getFontAsync(Context, TextAppearanceFontCallback)
   */
  public void getFontAsync(
      @NonNull final Context context,
      @NonNull final TextPaint textPaint,
      @NonNull final TextAppearanceFontCallback callback) {
    // Updates text paint using fallback font while waiting for font to be requested.
    updateTextPaintMeasureState(context, textPaint, getFallbackFont());

    getFontAsync(
        context,
        new TextAppearanceFontCallback() {
          @Override
          public void onFontRetrieved(
              @NonNull Typeface typeface, boolean fontResolvedSynchronously) {
            updateTextPaintMeasureState(context, textPaint, typeface);
            callback.onFontRetrieved(typeface, fontResolvedSynchronously);
          }

          @Override
          public void onFontRetrievalFailed(int i) {
            callback.onFontRetrievalFailed(i);
          }
        });
  }

  /**
   * Returns a fallback {@link Typeface} that is retrieved synchronously, in case the actual font is
   * not yet resolved or pending async fetch or an actual {@link Typeface} if resolved already.
   *
   * <p>Fallback font is a font that can be resolved using typeface attributes not requiring any
   * async operations, i.e. android:typeface, android:textStyle and android:fontFamily defined as
   * string rather than resource id.
   */
  public Typeface getFallbackFont() {
    createFallbackFont();
    return font;
  }

  private void createFallbackFont() {
    // Try resolving fontFamily as a string name if specified.
    if (font == null && fontFamily != null) {
      font = Typeface.create(fontFamily, textStyle);
    }

    // Try resolving typeface if specified otherwise fallback to Typeface.DEFAULT.
    if (font == null) {
      switch (typeface) {
        case TYPEFACE_SANS:
          font = Typeface.SANS_SERIF;
          break;
        case TYPEFACE_SERIF:
          font = Typeface.SERIF;
          break;
        case TYPEFACE_MONOSPACE:
          font = Typeface.MONOSPACE;
          break;
        default:
          font = Typeface.DEFAULT;
          break;
      }
      font = Typeface.create(font, textStyle);
    }
  }

  /**
   * Applies the attributes that affect drawing from TextAppearance to the given TextPaint. Note
   * that not all attributes can be applied to the TextPaint.
   *
   * @see android.text.style.TextAppearanceSpan#updateDrawState(TextPaint)
   */
  public void updateDrawState(
      @NonNull Context context,
      @NonNull TextPaint textPaint,
      @NonNull TextAppearanceFontCallback callback) {
    updateMeasureState(context, textPaint, callback);

    textPaint.setColor(
        textColor != null
            ? textColor.getColorForState(textPaint.drawableState, textColor.getDefaultColor())
            : Color.BLACK);
    textPaint.setShadowLayer(
        shadowRadius,
        shadowDx,
        shadowDy,
        shadowColor != null
            ? shadowColor.getColorForState(textPaint.drawableState, shadowColor.getDefaultColor())
            : Color.TRANSPARENT);
  }

  /**
   * Applies the attributes that affect measurement from TextAppearance to the given TextPaint. Note
   * that not all attributes can be applied to the TextPaint.
   *
   * @see android.text.style.TextAppearanceSpan#updateMeasureState(TextPaint)
   */
  public void updateMeasureState(
      @NonNull Context context,
      @NonNull TextPaint textPaint,
      @NonNull TextAppearanceFontCallback callback) {
    if (maybeLoadFontSynchronously(context) && fontResolved && font != null) {
      updateTextPaintMeasureState(context, textPaint, font);
    } else {
      getFontAsync(context, textPaint, callback);
    }
  }

  /**
   * Applies the attributes that affect measurement from Typeface to the given TextPaint.
   *
   * @see android.text.style.TextAppearanceSpan#updateMeasureState(TextPaint)
   */
  public void updateTextPaintMeasureState(
      @NonNull Context context, @NonNull TextPaint textPaint, @NonNull Typeface typeface) {
    Typeface boldTypeface = TypefaceUtils.maybeCopyWithFontWeightAdjustment(context, typeface);
    if (boldTypeface != null) {
      typeface = boldTypeface;
    }
    textPaint.setTypeface(typeface);

    int fake = textStyle & ~typeface.getStyle();
    textPaint.setFakeBoldText((fake & Typeface.BOLD) != 0);
    textPaint.setTextSkewX((fake & Typeface.ITALIC) != 0 ? -0.25f : 0f);

    textPaint.setTextSize(textSize);

    if (VERSION.SDK_INT >= VERSION_CODES.O) {
      // Workaround for b/353609778
      textPaint.setFontVariationSettings(null);
      textPaint.setFontVariationSettings(fontVariationSettings);
    }

    if (hasLetterSpacing) {
      textPaint.setLetterSpacing(letterSpacing);
    }
  }

  @Nullable
  public ColorStateList getTextColor() {
    return textColor;
  }

  public void setTextColor(@Nullable ColorStateList textColor) {
    this.textColor = textColor;
  }

  public float getTextSize() {
    return textSize;
  }

  public void setTextSize(float textSize) {
    this.textSize = textSize;
  }

  @RequiresApi(VERSION_CODES.O)
  @Nullable
  public String getFontVariationSettings() {
    return fontVariationSettings;
  }

  @RequiresApi(VERSION_CODES.O)
  public void setFontVariationSettings(@Nullable String fontVariationSettings) {
    this.fontVariationSettings = fontVariationSettings;
  }

  private boolean maybeLoadFontSynchronously(Context context) {
    if (TextAppearanceConfig.shouldLoadFontSynchronously()) {
      getFont(context);
      return true;
    }
    if (fontResolved) {
      return true;
    }
    if (fontFamilyResourceId == 0) {
      return false;
    }
    Typeface cachedFont = ResourcesCompat.getCachedFont(context, fontFamilyResourceId);
    if (cachedFont != null) {
      font = cachedFont;
      fontResolved = true;
      return true;
    }
    Typeface systemFont = getSystemTypeface(context);
    if (systemFont != null) {
      font = systemFont;
      fontResolved = true;
      return true;
    }
    return false;
  }

  @Nullable
  private Typeface getSystemTypeface(Context context) {
    if (systemFontLoadAttempted) {
      // Only attempt to load the system font once.
      return null;
    }
    systemFontLoadAttempted = true;

    String systemFontFamily = readFontProviderSystemFontFamily(context, fontFamilyResourceId);
    if (systemFontFamily == null) {
      return null;
    }

    Typeface regularSystemTypeface = Typeface.create(systemFontFamily, Typeface.NORMAL);
    if (regularSystemTypeface == Typeface.DEFAULT) {
      // If Typeface#create returned Typeface.DEFAULT then systemFontFamily is not present on the
      // device as a system font, so we will have to load the font asynchronously.
      return null;
    }

    return Typeface.create(regularSystemTypeface, textStyle);
  }

  @SuppressLint("ResourceType")
  @Nullable
  private static String readFontProviderSystemFontFamily(
      Context context, @FontRes int fontFamilyResourceId) {
    Resources resources = context.getResources();
    if (fontFamilyResourceId == 0
        || !resources.getResourceTypeName(fontFamilyResourceId).equals("font")) {
      return null;
    }

    try {
      XmlPullParser xpp = resources.getXml(fontFamilyResourceId);
      while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
        if (xpp.getEventType() == XmlPullParser.START_TAG && xpp.getName().equals("font-family")) {
          AttributeSet attrs = Xml.asAttributeSet(xpp);
          TypedArray a = resources.obtainAttributes(attrs, androidx.core.R.styleable.FontFamily);
          String systemFontFamily =
              a.getString(androidx.core.R.styleable.FontFamily_fontProviderSystemFontFamily);
          a.recycle();
          return systemFontFamily;
        }
        xpp.next();
      }
    } catch (Throwable t) {
      // Fail silently if we can't find fontProviderSystemFontFamily for any reason.
    }
    return null;
  }
}
