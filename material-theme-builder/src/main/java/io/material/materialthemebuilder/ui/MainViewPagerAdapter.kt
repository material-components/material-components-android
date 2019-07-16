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

package io.material.materialthemebuilder.ui

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import io.material.materialthemebuilder.R
import io.material.materialthemebuilder.ui.component.ComponentFragment
import io.material.materialthemebuilder.ui.instruction.InstructionsFragment
import io.material.materialthemebuilder.ui.themesummary.ThemeSummaryFragment

/**
 * View pager to show all tabbed destinations - Instructions, Theme Summary and Components.
 */
class MainViewPagerAdapter(
  private val context: Context,
  fragmentManager: FragmentManager
) : FragmentStatePagerAdapter(fragmentManager) {

  enum class MainFragments(val titleRes: Int) {
    INSTRUCTIONS(R.string.tab_title_instructions),
    THEME_SUMMARY(R.string.tab_title_theme_summary),
    COMPONENTS(R.string.tab_title_components)
  }

  override fun getCount(): Int = MainFragments.values().size

  private fun getItemType(position: Int): MainFragments {
    return MainFragments.values()[position]
  }

  override fun getPageTitle(position: Int): CharSequence? {
    return context.getString(getItemType(position).titleRes)
  }

  override fun getItem(position: Int): Fragment {
    return when (getItemType(position)) {
      MainFragments.INSTRUCTIONS -> InstructionsFragment()
      MainFragments.THEME_SUMMARY -> ThemeSummaryFragment()
      MainFragments.COMPONENTS -> ComponentFragment()
    }
  }
}
