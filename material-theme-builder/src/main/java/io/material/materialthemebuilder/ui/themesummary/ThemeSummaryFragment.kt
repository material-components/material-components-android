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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.material.materialthemebuilder.R

/**
 * Fragment to display a list of subsystems that show the values of this app's theme.
 */
class ThemeSummaryFragment : Fragment() {

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_theme_summary, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceBundle: Bundle?) {
    super.onViewCreated(view, savedInstanceBundle)

    val adapter = SubsystemAdapter()
    val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
    recyclerView.layoutManager = LinearLayoutManager(requireContext())
    recyclerView.adapter = adapter
    adapter.submitList(Subsystem.values().toList())
  }
}
