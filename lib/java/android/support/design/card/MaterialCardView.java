/*
 * Copyright (C) 2017 The Android Open Source Project
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

package android.support.design.card;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.internal.ThemeEnforcement;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

/**
 * Provides a Material card.
 *
 * <p>This class supplies Material styles for the card in the constructor. The widget will
 * display the correct default Material styles without the use of a style flag.
 *
 * <p>Stroke width can be set using the {@code strokeWidth} attribute. Set the stroke color using
 * the {@code strokeColor} attribute. Without a {@code strokeColor}, the card will not render a
 * stroked border, regardless of the {@code strokeWidth} value.
 */
public class MaterialCardView extends CardView {

  public MaterialCardView(Context context) {
    this(context, null /* attrs */);
  }

  public MaterialCardView(Context context, AttributeSet attrs) {
    this(context, attrs, android.support.design.card.R.attr.cardViewStyle);
  }

  public MaterialCardView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    ThemeEnforcement.checkAppCompatTheme(context);

    TypedArray attributes =
        context.obtainStyledAttributes(
            attrs,
            R.styleable.MaterialCardView,
            defStyleAttr,
            R.style.Widget_MaterialComponents_CardView);

    // Loads and sets background drawable attributes
    MaterialCardViewHelper cardViewHelper = new MaterialCardViewHelper(this);
    cardViewHelper.loadFromAttributes(attributes);

    attributes.recycle();
  }
}
