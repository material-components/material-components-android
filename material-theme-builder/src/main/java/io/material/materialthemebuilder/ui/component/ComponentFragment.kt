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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import io.material.materialthemebuilder.R

/**
 * Fragment to hold a list of all [Component]s.
 */
class ComponentFragment : Fragment(), ComponentAdapter.ComponentAdapterListener {

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_component, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceBundle: Bundle?) {
    super.onViewCreated(view, savedInstanceBundle)

    val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
    val adapter = ComponentAdapter(this)
    recyclerView.layoutManager = LinearLayoutManager(requireContext())
    recyclerView.adapter = adapter
    adapter.submitList(Component.values().toList())
  }

  override fun onShowBottomSheetClicked() {
    BottomSheetFragment().show(requireFragmentManager(), BottomSheetFragment.FRAGMENT_TAG)
  }
}
