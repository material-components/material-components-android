/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.dialog;

import com.google.android.material.R;

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.res.ColorStateList;
import android.content.res.Resources.Theme;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import androidx.core.view.ViewCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import androidx.annotation.ArrayRes;
import androidx.annotation.AttrRes;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.resources.MaterialAttributes;
import com.google.android.material.shape.MaterialShapeDrawable;

/**
 * An extension of {@link AlertDialog.Builder} for use with a Material theme (e.g.,
 * Theme.MaterialComponents).
 *
 * <p>This Builder must be used in order for AlertDialog objects to respond to color and shape
 * theming provided by Material themes.
 *
 * <p>The type of dialog returned is still an {@link AlertDialog}; there is no specific Material
 * implementation of {@link AlertDialog}.
 */
public class MaterialAlertDialogBuilder extends AlertDialog.Builder {

  @AttrRes private static final int DEF_STYLE_ATTR = R.attr.alertDialogStyle;
  @StyleRes private static final int DEF_STYLE_RES = R.style.MaterialAlertDialog_MaterialComponents;

  @AttrRes
  private static final int MATERIAL_ALERT_DIALOG_THEME_OVERLAY = R.attr.materialAlertDialogTheme;

  @Nullable private Drawable background;
  @NonNull @Dimension private final Rect backgroundInsets;

  private static int getMaterialAlertDialogThemeOverlay(@NonNull Context context) {
    TypedValue materialAlertDialogThemeOverlay =
        MaterialAttributes.resolve(context, MATERIAL_ALERT_DIALOG_THEME_OVERLAY);
    if (materialAlertDialogThemeOverlay == null) {
      return 0;
    }
    return materialAlertDialogThemeOverlay.data;
  }

  private static Context createMaterialAlertDialogThemedContext(@NonNull Context context) {
    int themeOverlayId = getMaterialAlertDialogThemeOverlay(context);
    Context themedContext = wrap(context, null, DEF_STYLE_ATTR, DEF_STYLE_RES);
    if (themeOverlayId == 0) {
      return themedContext;
    }
    return new ContextThemeWrapper(themedContext, themeOverlayId);
  }

  private static int getOverridingThemeResId(@NonNull Context context, int overrideThemeResId) {
    return overrideThemeResId == 0
        ? getMaterialAlertDialogThemeOverlay(context)
        : overrideThemeResId;
  }

  public MaterialAlertDialogBuilder(@NonNull Context context) {
    this(context, 0);
  }

  public MaterialAlertDialogBuilder(@NonNull Context context, int overrideThemeResId) {
    // Only pass in 0 for overrideThemeResId if both overrideThemeResId and
    // MATERIAL_ALERT_DIALOG_THEME_OVERLAY are 0 otherwise alertDialogTheme will override both.
    super(
        createMaterialAlertDialogThemedContext(context),
        getOverridingThemeResId(context, overrideThemeResId));
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();
    Theme theme = context.getTheme();

    backgroundInsets =
        MaterialDialogs.getDialogBackgroundInsets(context, DEF_STYLE_ATTR, DEF_STYLE_RES);

    int surfaceColor =
        MaterialColors.getColor(context, R.attr.colorSurface, getClass().getCanonicalName());
    MaterialShapeDrawable materialShapeDrawable =
        new MaterialShapeDrawable(context, null, DEF_STYLE_ATTR, DEF_STYLE_RES);
    materialShapeDrawable.initializeElevationOverlay(context);
    materialShapeDrawable.setFillColor(ColorStateList.valueOf(surfaceColor));

    // dialogCornerRadius first appeared in Android Pie
    if (Build.VERSION.SDK_INT >= VERSION_CODES.P) {
      TypedValue dialogCornerRadiusValue = new TypedValue();
      theme.resolveAttribute(android.R.attr.dialogCornerRadius, dialogCornerRadiusValue, true);
      float dialogCornerRadius =
          dialogCornerRadiusValue.getDimension(getContext().getResources().getDisplayMetrics());
      if (dialogCornerRadiusValue.type == TypedValue.TYPE_DIMENSION && dialogCornerRadius >= 0) {
        materialShapeDrawable.setCornerSize(dialogCornerRadius);
      }
    }
    background = materialShapeDrawable;
  }

  @NonNull
  @Override
  public AlertDialog create() {
    AlertDialog alertDialog = super.create();
    Window window = alertDialog.getWindow();
    /* {@link Window#getDecorView()} should be called before any changes are made to the Window
     * as it locks in attributes and affects layout. */
    View decorView = window.getDecorView();
    if (background instanceof MaterialShapeDrawable) {
      ((MaterialShapeDrawable) background).setElevation(ViewCompat.getElevation(decorView));
    }

    Drawable insetDrawable = MaterialDialogs.insetDrawable(background, backgroundInsets);
    window.setBackgroundDrawable(insetDrawable);
    decorView.setOnTouchListener(new InsetDialogOnTouchListener(alertDialog, backgroundInsets));
    return alertDialog;
  }

  @Nullable
  public Drawable getBackground() {
    return background;
  }

  @NonNull
  public MaterialAlertDialogBuilder setBackground(@Nullable Drawable background) {
    this.background = background;
    return this;
  }

  @NonNull
  public MaterialAlertDialogBuilder setBackgroundInsetStart(@Px int backgroundInsetStart) {
    if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1
        && getContext().getResources().getConfiguration().getLayoutDirection()
            == ViewCompat.LAYOUT_DIRECTION_RTL) {
      backgroundInsets.right = backgroundInsetStart;
    } else {
      backgroundInsets.left = backgroundInsetStart;
    }
    return this;
  }

  @NonNull
  public MaterialAlertDialogBuilder setBackgroundInsetTop(@Px int backgroundInsetTop) {
    backgroundInsets.top = backgroundInsetTop;
    return this;
  }

  @NonNull
  public MaterialAlertDialogBuilder setBackgroundInsetEnd(@Px int backgroundInsetEnd) {
    if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1
        && getContext().getResources().getConfiguration().getLayoutDirection()
            == ViewCompat.LAYOUT_DIRECTION_RTL) {
      backgroundInsets.left = backgroundInsetEnd;
    } else {
      backgroundInsets.right = backgroundInsetEnd;
    }
    return this;
  }

  @NonNull
  public MaterialAlertDialogBuilder setBackgroundInsetBottom(@Px int backgroundInsetBottom) {
    backgroundInsets.bottom = backgroundInsetBottom;
    return this;
  }

  // The following methods are all pass-through methods used to specify the return type for the
  // builder chain.

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setTitle(@StringRes int titleId) {
    return (MaterialAlertDialogBuilder) super.setTitle(titleId);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setTitle(@Nullable CharSequence title) {
    return (MaterialAlertDialogBuilder) super.setTitle(title);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setCustomTitle(@Nullable View customTitleView) {
    return (MaterialAlertDialogBuilder) super.setCustomTitle(customTitleView);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setMessage(@StringRes int messageId) {
    return (MaterialAlertDialogBuilder) super.setMessage(messageId);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setMessage(@Nullable CharSequence message) {
    return (MaterialAlertDialogBuilder) super.setMessage(message);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setIcon(@DrawableRes int iconId) {
    return (MaterialAlertDialogBuilder) super.setIcon(iconId);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setIcon(@Nullable Drawable icon) {
    return (MaterialAlertDialogBuilder) super.setIcon(icon);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setIconAttribute(@AttrRes int attrId) {
    return (MaterialAlertDialogBuilder) super.setIconAttribute(attrId);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setPositiveButton(
      @StringRes int textId, @Nullable final OnClickListener listener) {
    return (MaterialAlertDialogBuilder) super.setPositiveButton(textId, listener);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setPositiveButton(
      @Nullable CharSequence text, @Nullable final OnClickListener listener) {
    return (MaterialAlertDialogBuilder) super.setPositiveButton(text, listener);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setPositiveButtonIcon(@Nullable Drawable icon) {
    return (MaterialAlertDialogBuilder) super.setPositiveButtonIcon(icon);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setNegativeButton(
      @StringRes int textId, @Nullable final OnClickListener listener) {
    return (MaterialAlertDialogBuilder) super.setNegativeButton(textId, listener);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setNegativeButton(
      @Nullable CharSequence text, @Nullable final OnClickListener listener) {
    return (MaterialAlertDialogBuilder) super.setNegativeButton(text, listener);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setNegativeButtonIcon(@Nullable Drawable icon) {
    return (MaterialAlertDialogBuilder) super.setNegativeButtonIcon(icon);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setNeutralButton(
      @StringRes int textId, @Nullable final OnClickListener listener) {
    return (MaterialAlertDialogBuilder) super.setNeutralButton(textId, listener);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setNeutralButton(
      @Nullable CharSequence text, @Nullable final OnClickListener listener) {
    return (MaterialAlertDialogBuilder) super.setNeutralButton(text, listener);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setNeutralButtonIcon(@Nullable Drawable icon) {
    return (MaterialAlertDialogBuilder) super.setNeutralButtonIcon(icon);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setCancelable(boolean cancelable) {
    return (MaterialAlertDialogBuilder) super.setCancelable(cancelable);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setOnCancelListener(
      @Nullable OnCancelListener onCancelListener) {
    return (MaterialAlertDialogBuilder) super.setOnCancelListener(onCancelListener);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setOnDismissListener(
      @Nullable OnDismissListener onDismissListener) {
    return (MaterialAlertDialogBuilder) super.setOnDismissListener(onDismissListener);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setOnKeyListener(@Nullable OnKeyListener onKeyListener) {
    return (MaterialAlertDialogBuilder) super.setOnKeyListener(onKeyListener);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setItems(
      @ArrayRes int itemsId, @Nullable final OnClickListener listener) {
    return (MaterialAlertDialogBuilder) super.setItems(itemsId, listener);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setItems(
      @Nullable CharSequence[] items, @Nullable final OnClickListener listener) {
    return (MaterialAlertDialogBuilder) super.setItems(items, listener);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setAdapter(
      @Nullable final ListAdapter adapter, @Nullable final OnClickListener listener) {
    return (MaterialAlertDialogBuilder) super.setAdapter(adapter, listener);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setCursor(
      @Nullable final Cursor cursor,
      @Nullable final OnClickListener listener,
      @NonNull String labelColumn) {
    return (MaterialAlertDialogBuilder) super.setCursor(cursor, listener, labelColumn);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setMultiChoiceItems(
      @ArrayRes int itemsId,
      @Nullable boolean[] checkedItems,
      @Nullable final OnMultiChoiceClickListener listener) {
    return (MaterialAlertDialogBuilder) super.setMultiChoiceItems(itemsId, checkedItems, listener);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setMultiChoiceItems(
      @Nullable CharSequence[] items,
      @Nullable boolean[] checkedItems,
      @Nullable final OnMultiChoiceClickListener listener) {
    return (MaterialAlertDialogBuilder) super.setMultiChoiceItems(items, checkedItems, listener);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setMultiChoiceItems(
      @Nullable Cursor cursor,
      @NonNull String isCheckedColumn,
      @NonNull String labelColumn,
      @Nullable final OnMultiChoiceClickListener listener) {
    return (MaterialAlertDialogBuilder)
        super.setMultiChoiceItems(cursor, isCheckedColumn, labelColumn, listener);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setSingleChoiceItems(
      @ArrayRes int itemsId, int checkedItem, @Nullable final OnClickListener listener) {
    return (MaterialAlertDialogBuilder) super.setSingleChoiceItems(itemsId, checkedItem, listener);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setSingleChoiceItems(
      @Nullable Cursor cursor,
      int checkedItem,
      @NonNull String labelColumn,
      @Nullable final OnClickListener listener) {
    return (MaterialAlertDialogBuilder)
        super.setSingleChoiceItems(cursor, checkedItem, labelColumn, listener);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setSingleChoiceItems(
      @Nullable CharSequence[] items, int checkedItem, @Nullable final OnClickListener listener) {
    return (MaterialAlertDialogBuilder) super.setSingleChoiceItems(items, checkedItem, listener);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setSingleChoiceItems(
      @Nullable ListAdapter adapter, int checkedItem, @Nullable final OnClickListener listener) {
    return (MaterialAlertDialogBuilder) super.setSingleChoiceItems(adapter, checkedItem, listener);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setOnItemSelectedListener(
      @Nullable final AdapterView.OnItemSelectedListener listener) {
    return (MaterialAlertDialogBuilder) super.setOnItemSelectedListener(listener);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setView(int layoutResId) {
    return (MaterialAlertDialogBuilder) super.setView(layoutResId);
  }

  @NonNull
  @Override
  public MaterialAlertDialogBuilder setView(@Nullable View view) {
    return (MaterialAlertDialogBuilder) super.setView(view);
  }
}
