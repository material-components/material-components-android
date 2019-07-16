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
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import io.material.materialthemebuilder.R

/**
 * Composite view to show an item containing a text label and a [ColorDotView].
 */
class ColorAttributeView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

  private val colorAttributeTextView: AppCompatTextView
  private val colorDotView: ColorDotView

  private var attributeText: String = ""
    set(value) {
      colorAttributeTextView.text = value
      field = value
    }

  private var dotFillColor: Int = Color.LTGRAY
    set(value) {
      colorDotView.fillColor = value
      field = value
    }

  private var dotStrokeColor: Int = Color.DKGRAY
    set(value) {
      colorDotView.strokeColor = value
      field = value
    }

  init {
    val view = View.inflate(context, R.layout.color_attribute_view_layout, this)
    colorAttributeTextView = view.findViewById(R.id.color_attribute)
    colorDotView = view.findViewById(R.id.color_dot)

    val a = context.theme.obtainStyledAttributes(
      attrs,
      R.styleable.ColorAttributeView,
      defStyleAttr,
      defStyleRes
    )
    attributeText = a.getString(
      R.styleable.ColorAttributeView_android_text
    ) ?: attributeText
    dotFillColor = a.getColor(R.styleable.ColorAttributeView_colorFillColor, dotFillColor)
    dotStrokeColor = a.getColor(
      R.styleable.ColorAttributeView_colorStrokeColor,
      dotStrokeColor
    )
    a.recycle()
  }
}
