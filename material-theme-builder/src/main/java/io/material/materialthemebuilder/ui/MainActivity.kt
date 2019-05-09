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

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import io.material.materialthemebuilder.R
import io.material.materialthemebuilder.App
import io.material.materialthemebuilder.ui.instruction.InstructionsFragment
import io.material.materialthemebuilder.ui.themesummary.ThemeSummaryFragment
import io.material.materialthemebuilder.ui.component.ComponentFragment

/**
 * Single activity which contains the [MainViewPagerAdapter] that shows the [InstructionsFragment],
 * [ThemeSummaryFragment] and [ComponentFragment].
 */
class MainActivity : AppCompatActivity() {

  private lateinit var viewPager: ViewPager
  private lateinit var tabLayout: TabLayout

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    viewPager = findViewById(R.id.view_pager)
    tabLayout = findViewById(R.id.tab_layout)

    tabLayout.setupWithViewPager(viewPager)
    val adapter = MainViewPagerAdapter(this, supportFragmentManager)
    viewPager.adapter = adapter

    (application as App).preferenceRepository
      .nightModeLive.observe(this, Observer { nightMode ->
        nightMode?.let { delegate.localNightMode = it }
      }
    )
  }
}
