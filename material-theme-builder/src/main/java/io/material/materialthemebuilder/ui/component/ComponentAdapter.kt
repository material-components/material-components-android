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

package io.material.materialthemebuilder.ui.component

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

/**
 * An adapter to display all [Component]s using their corresponding [ComponentViewHolder].
 */
class ComponentAdapter(
  private val listener: ComponentAdapterListener
) : ListAdapter<Component, ComponentViewHolder>(DIFF_CALLBACK) {

  interface ComponentAdapterListener {
    fun onShowBottomSheetClicked()
  }

  override fun getItemViewType(position: Int): Int = getItem(position).ordinal

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComponentViewHolder {
    return ComponentViewHolder.create(parent, viewType, listener)
  }

  override fun onBindViewHolder(holder: ComponentViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  companion object {
    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Component>() {
      override fun areItemsTheSame(oldItem: Component, newItem: Component): Boolean {
        return oldItem == newItem
      }
      override fun areContentsTheSame(oldItem: Component, newItem: Component): Boolean {
        return oldItem == newItem
      }
    }
  }
}
