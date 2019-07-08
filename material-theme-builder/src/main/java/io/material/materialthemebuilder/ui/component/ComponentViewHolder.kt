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

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import io.material.materialthemebuilder.R
import io.material.materialthemebuilder.ui.component.Component.BUTTON
import io.material.materialthemebuilder.ui.component.Component.FAB
import io.material.materialthemebuilder.ui.component.Component.CARD
import io.material.materialthemebuilder.ui.component.Component.TOP_APP_BAR
import io.material.materialthemebuilder.ui.component.Component.CHIP
import io.material.materialthemebuilder.ui.component.Component.DRAWER
import io.material.materialthemebuilder.ui.component.Component.TEXT_FIELD
import io.material.materialthemebuilder.ui.component.Component.BOTTOM_NAVIGATION
import io.material.materialthemebuilder.ui.component.Component.SWITCH
import io.material.materialthemebuilder.ui.component.Component.RADIO_BUTTON
import io.material.materialthemebuilder.ui.component.Component.CHECKBOX
import io.material.materialthemebuilder.ui.component.Component.BOTTOM_APP_BAR
import io.material.materialthemebuilder.ui.component.Component.TABS
import io.material.materialthemebuilder.ui.component.Component.SNACKBAR
import io.material.materialthemebuilder.ui.component.Component.DIALOG
import io.material.materialthemebuilder.ui.component.Component.BOTTOM_SHEET

/**
 * Sealed class to define all [RecyclerView.ViewHolder]s used to display [Component]s.
 */
sealed class ComponentViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

  open fun bind(component: Component) {
    // Override in subclass if needed.
  }

  class ButtonComponentViewHolder(
    parent: ViewGroup
  ) : ComponentViewHolder(inflate(parent, R.layout.component_buttons))

  class FabComponentViewHolder(
    parent: ViewGroup
  ) : ComponentViewHolder(inflate(parent, R.layout.component_fabs))

  class CardComponentViewHolder(
    parent: ViewGroup
  ) : ComponentViewHolder(inflate(parent, R.layout.component_cards))

  class TopAppBarComponentViewHolder(
    parent: ViewGroup
  ) : ComponentViewHolder(inflate(parent, R.layout.component_top_app_bar))

  class ChipComponentViewHolder(
    parent: ViewGroup
  ) : ComponentViewHolder(inflate(parent, R.layout.component_chips))

  class DrawerComponentViewHolder(
    parent: ViewGroup
  ) : ComponentViewHolder(inflate(parent, R.layout.component_drawer)) {
    private val drawerLayout: DrawerLayout = view.findViewById(R.id.drawer_layout)
    private val navigationView: NavigationView = view.findViewById(R.id.nav_view)

    override fun bind(component: Component) {
      drawerLayout.openDrawer(Gravity.LEFT)
      navigationView.setNavigationItemSelectedListener { true }
      navigationView.setCheckedItem(R.id.nav_item_one)
    }
  }

  class TextFieldComponentViewHolder(
    parent: ViewGroup
  ) : ComponentViewHolder(inflate(parent, R.layout.component_text_field))

  class BottomNavigationComponentViewHolder(
    parent: ViewGroup
  ) : ComponentViewHolder(inflate(parent, R.layout.component_bottom_navigation))

  class SwitchComponentViewHolder(
    parent: ViewGroup
  ) : ComponentViewHolder(inflate(parent, R.layout.component_switch))

  class RadioButtonComponentViewHolder(
    parent: ViewGroup
  ) : ComponentViewHolder(inflate(parent, R.layout.component_radio_button))

  class CheckboxComponentViewHolder(
    parent: ViewGroup
  ) : ComponentViewHolder(inflate(parent, R.layout.component_checkbox))

  class BottomAppBarComponentViewHolder(
    parent: ViewGroup
  ) : ComponentViewHolder(inflate(parent, R.layout.component_bottom_app_bar)) {
    private val bottomAppBar: BottomAppBar = view.findViewById(R.id.bottom_app_bar)

    override fun bind(component: Component) {
      bottomAppBar.overflowIcon = ContextCompat.getDrawable(
        bottomAppBar.context,
        R.drawable.ic_more_vert_on_surface_24dp
      )
    }
  }

  class TabsComponentViewHolder(
    parent: ViewGroup
  ) : ComponentViewHolder(inflate(parent, R.layout.component_tabs))

  class SnackbarComponentViewHolder(
    parent: ViewGroup
  ) : ComponentViewHolder(inflate(parent, R.layout.component_snackbar)) {

    init {
      val container: FrameLayout = view.findViewById(R.id.snackbar_container)
      val snackbarView = Snackbar.make(
        container,
        R.string.snackbar_message_text,
        Snackbar.LENGTH_INDEFINITE
      )
        .setAction(R.string.snackbar_action_text) { }
        .view
      (snackbarView.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.CENTER

      container.addView(snackbarView)
    }
  }

  class DialogComponentViewHolder(
    parent: ViewGroup
  ) : ComponentViewHolder(inflate(parent, R.layout.component_dialog)) {
    init {
      val button = view.findViewById<MaterialButton>(R.id.button)
      button.setOnClickListener {
        showDialog()
      }
    }

    private fun showDialog() {
      MaterialAlertDialogBuilder(view.context)
        .setTitle(R.string.text_headline_6)
        .setMessage(R.string.lorem_ipsum)
        .setPositiveButton(R.string.text_button, null)
        .setNegativeButton(R.string.text_button, null)
        .show()
    }
  }

  class BottomSheetComponentViewHolder(
    parent: ViewGroup,
    listener: ComponentAdapter.ComponentAdapterListener
  ) : ComponentViewHolder(inflate(parent, R.layout.component_bottom_sheet)) {
    init {
      view.findViewById<MaterialButton>(R.id.button).setOnClickListener {
        listener.onShowBottomSheetClicked()
      }
    }
  }

  companion object {
    fun create(
      parent: ViewGroup,
      viewType: Int,
      listener: ComponentAdapter.ComponentAdapterListener
    ): ComponentViewHolder {
      return when (Component.values()[viewType]) {
        BUTTON -> ComponentViewHolder.ButtonComponentViewHolder(parent)
        FAB -> ComponentViewHolder.FabComponentViewHolder(parent)
        CARD -> ComponentViewHolder.CardComponentViewHolder(parent)
        TOP_APP_BAR -> ComponentViewHolder.TopAppBarComponentViewHolder(parent)
        CHIP -> ComponentViewHolder.ChipComponentViewHolder(parent)
        DRAWER -> ComponentViewHolder.DrawerComponentViewHolder(parent)
        TEXT_FIELD -> ComponentViewHolder.TextFieldComponentViewHolder(parent)
        BOTTOM_NAVIGATION -> ComponentViewHolder.BottomNavigationComponentViewHolder(parent)
        SWITCH -> ComponentViewHolder.SwitchComponentViewHolder(parent)
        RADIO_BUTTON -> ComponentViewHolder.RadioButtonComponentViewHolder(parent)
        CHECKBOX -> ComponentViewHolder.CheckboxComponentViewHolder(parent)
        BOTTOM_APP_BAR -> ComponentViewHolder.BottomAppBarComponentViewHolder(parent)
        TABS -> ComponentViewHolder.TabsComponentViewHolder(parent)
        SNACKBAR -> ComponentViewHolder.SnackbarComponentViewHolder(parent)
        DIALOG -> ComponentViewHolder.DialogComponentViewHolder(parent)
        BOTTOM_SHEET -> ComponentViewHolder.BottomSheetComponentViewHolder(parent, listener)
      }
    }

    private fun inflate(parent: ViewGroup, layout: Int): View {
      return LayoutInflater.from(parent.context).inflate(layout, parent, false)
    }
  }
}
