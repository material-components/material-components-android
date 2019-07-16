/*
 * Copyright 2019 The Android Open Source Project
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

package io.material.materialthemebuilder.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import android.widget.LinearLayout
import io.material.materialthemebuilder.R

/**
 * A composite view to display a text label and a preview of a TextAppearance theme attribute.
 */
class TypeAttributeView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

  private val typeAttributeTextView: AppCompatTextView
  private val typeAttributePreviewTextView: AppCompatTextView

  var typeAttrText: String = "?textAppearanceHeadline1"
    set(value) {
      typeAttributeTextView.text = value
      field = value
    }

  var typeAttrPreviewText: String = context.getString(R.string.text_appearance_h1_label)
    set(value) {
      typeAttributePreviewTextView.text = value
      field = value
    }

  var typeAttrPreviewTextAppearance: Int = R.attr.textAppearanceHeadline1
    set(value) {
      typeAttributePreviewTextView.setTextAppearance(value)
      field = value
    }

  var typeAttrPreviewAlpha: Float = 1F
    set(value) {
      typeAttributePreviewTextView.alpha = value
      field = value
    }

  init {
    orientation = LinearLayout.HORIZONTAL
    val view = View.inflate(context, R.layout.type_attribute_view_layout, this)
    typeAttributeTextView = view.findViewById(R.id.type_attribute)
    typeAttributePreviewTextView = view.findViewById(R.id.type_attribute_preview)

    val a = context.theme.obtainStyledAttributes(
      attrs,
      R.styleable.TypeAttributeView,
      defStyleAttr,
      defStyleRes
    )
    typeAttrText = a.getString(R.styleable.TypeAttributeView_android_text) ?: typeAttrText
    typeAttrPreviewText = a.getString(
      R.styleable.TypeAttributeView_previewText
    ) ?: typeAttrPreviewText
    typeAttrPreviewTextAppearance = a.getResourceId(
      R.styleable.TypeAttributeView_previewTextAppearance,
      typeAttrPreviewTextAppearance
    )
    typeAttrPreviewAlpha = a.getFloat(
      R.styleable.TypeAttributeView_previewAlpha,
      typeAttrPreviewAlpha
    )
    a.recycle()
  }
}
