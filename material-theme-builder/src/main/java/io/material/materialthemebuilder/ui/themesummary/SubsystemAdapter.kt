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

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

/**
 * Adapter to display [Subsystem]s using their corresponding [SubsystemViewHolder].
 */
class SubsystemAdapter : ListAdapter<Subsystem, SubsystemViewHolder>(DIFF_CALLBACK) {

  override fun getItemViewType(position: Int): Int = getItem(position).ordinal

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubsystemViewHolder {
    return SubsystemViewHolder.create(parent, viewType)
  }

  override fun onBindViewHolder(holder: SubsystemViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  companion object {
    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Subsystem>() {
      override fun areItemsTheSame(oldItem: Subsystem, newItem: Subsystem): Boolean {
        return oldItem == newItem
      }
      override fun areContentsTheSame(oldItem: Subsystem, newItem: Subsystem): Boolean {
        return oldItem == newItem
      }
    }
  }
}
