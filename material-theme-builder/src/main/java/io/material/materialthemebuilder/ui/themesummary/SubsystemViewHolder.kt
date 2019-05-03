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

package io.material.materialthemebuilder.ui.themesummary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.material.materialthemebuilder.R
import io.material.materialthemebuilder.ui.themesummary.Subsystem.COLOR
import io.material.materialthemebuilder.ui.themesummary.Subsystem.TYPE
import io.material.materialthemebuilder.ui.themesummary.Subsystem.SHAPE

/**
 * Sealed class to define all [RecyclerView.ViewHolder]s used to display [Subsystem]s.
 */
sealed class SubsystemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

  open fun bind(subsystem: Subsystem) {
    // Override in subclass if needed.
  }

  class ColorSubsystemViewHolder(
    parent: ViewGroup
  ) : SubsystemViewHolder(inflate(parent, R.layout.subsystem_color))

  class TypeSubsystemViewHolder(
    parent: ViewGroup
  ) : SubsystemViewHolder(inflate(parent, R.layout.subsystem_type))

  class ShapeSubsystemViewHolder(
    parent: ViewGroup
  ) : SubsystemViewHolder(inflate(parent, R.layout.subsystem_shape))

  companion object {
    fun create(parent: ViewGroup, viewType: Int): SubsystemViewHolder {
      return when (Subsystem.values()[viewType]) {
        COLOR -> SubsystemViewHolder.ColorSubsystemViewHolder(parent)
        TYPE -> SubsystemViewHolder.TypeSubsystemViewHolder(parent)
        SHAPE -> SubsystemViewHolder.ShapeSubsystemViewHolder(parent)
      }
    }

    private fun inflate(parent: ViewGroup, layout: Int): View {
      return LayoutInflater.from(parent.context).inflate(layout, parent, false)
    }
  }
}
