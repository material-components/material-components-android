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

package io.material.materialthemebuilder.ui.instruction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.switchmaterial.SwitchMaterial
import io.material.materialthemebuilder.App
import io.material.materialthemebuilder.R

/**
 * Fragment to display static instructions text and in-app theming options.
 */
class InstructionsFragment : Fragment() {

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_instructions, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val darkThemeSwitch: SwitchMaterial = view.findViewById(R.id.dark_theme_switch)
    val preferenceRepository = (requireActivity().application as App).preferenceRepository

    preferenceRepository.isDarkThemeLive.observe(this, Observer { isDarkTheme ->
      isDarkTheme?.let { darkThemeSwitch.isChecked = it }
    })

    darkThemeSwitch.setOnCheckedChangeListener { _, checked ->
      preferenceRepository.isDarkTheme = checked
    }
  }
}
