/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.material.catalog.fab

import io.material.catalog.R

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.behavior.HideViewOnScrollBehavior
import com.google.android.material.color.DynamicColors
import com.google.android.material.resources.MaterialAttributes
import com.google.android.material.snackbar.Snackbar
import io.material.catalog.feature.DemoFragment

/** A fragment that displays the FAB Menu demo for the Catalog app. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
class FabMenuDemoFragment : DemoFragment() {

  private val itemCount = 50

  override fun getDemoTitleResId(): Int {
    return R.string.cat_fab_menu_demo_title
  }

  @SuppressLint("SetTextI18n")
  @OptIn(ExperimentalMaterial3ExpressiveApi::class)
  override fun onCreateDemoView(
    layoutInflater: LayoutInflater,
    viewGroup: ViewGroup?,
    bundle: Bundle?,
  ): View {
    val context = requireContext()
    val view =
      layoutInflater.inflate(R.layout.cat_fab_menu_fragment, viewGroup, false /* attachToRoot */)

    val mainList = view.findViewById<LinearLayout>(R.id.cat_fab_menu_main_list)
    val itemPadding = resources.getDimensionPixelOffset(R.dimen.cat_fab_menu_main_list_item_padding)

    for (i in 1..itemCount) {
      val typedValue =
        MaterialAttributes.resolveTypedValueOrThrow(view, android.R.attr.selectableItemBackground)
      val selectableItemBackground = context.getDrawable(typedValue.resourceId)

      val textView = TextView(context)
      textView.text = "Item $i"
      textView.isClickable = true
      textView.background = selectableItemBackground
      textView.setPadding(itemPadding, itemPadding, itemPadding, itemPadding)
      mainList.addView(textView)
    }

    val dynamicColorOn =
      DynamicColors.isDynamicColorAvailable() &&
        MaterialAttributes.resolveBoolean(context, com.google.android.material.R.attr.isMaterial3DynamicColorApplied, false)

    val composeView = view.findViewById<ComposeView>(R.id.cat_fab_menu_compose_view)
    composeView.setContent {
      val localContext = LocalContext.current
      val darkTheme = isSystemInDarkTheme()
      val colorsScheme =
        remember(localContext, darkTheme) {
          when {
            dynamicColorOn && darkTheme -> dynamicDarkColorScheme(localContext)
            dynamicColorOn && !darkTheme -> dynamicLightColorScheme(localContext)
            darkTheme -> darkColorScheme()
            else -> expressiveLightColorScheme()
          }
        }

      MaterialExpressiveTheme(colorScheme = colorsScheme) {
        FabMenuDemoContent(
          onItemClick = { itemText ->
            // Do something in Views based on the result of the Compose FAB Menu item click.
            Snackbar.make(view, "\"$itemText\" item clicked!", Snackbar.LENGTH_LONG).show()
          },
          onExpandedChange = { expanded ->
            val layoutParams = composeView.layoutParams as CoordinatorLayout.LayoutParams
            layoutParams.behavior =
              if (expanded) null
              else HideViewOnScrollBehavior<View>(HideViewOnScrollBehavior.EDGE_BOTTOM)
          },
        )
      }
    }

    return view
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FabMenuDemoContent(onItemClick: (String) -> Unit, onExpandedChange: (Boolean) -> Unit) {
  val listState = rememberLazyListState()
  val fabVisible by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
  val focusRequester = remember { FocusRequester() }

  Box {
    val items = remember {
      listOf(
        Icons.AutoMirrored.Filled.Message to "Reply",
        Icons.Filled.People to "Reply all",
        Icons.Filled.Contacts to "Forward",
        Icons.Filled.Snooze to "Snooze",
        Icons.Filled.Archive to "Archive",
        Icons.AutoMirrored.Filled.Label to "Label",
      )
    }

    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(fabMenuExpanded) { onExpandedChange(fabMenuExpanded) }

    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    FloatingActionButtonMenu(
      modifier =
        Modifier.align(Alignment.BottomEnd).windowInsetsPadding(WindowInsets.navigationBars),
      expanded = fabMenuExpanded,
      button = {
        ToggleFloatingActionButton(
          modifier =
            Modifier.semantics {
                traversalIndex = -1f
                stateDescription = if (fabMenuExpanded) "Expanded" else "Collapsed"
                contentDescription = "Toggle menu"
              }
              .animateFloatingActionButton(
                visible = fabVisible || fabMenuExpanded,
                alignment = Alignment.BottomEnd,
              )
              .focusRequester(focusRequester),
          checked = fabMenuExpanded,
          onCheckedChange = { fabMenuExpanded = !fabMenuExpanded },
        ) {
          val imageVector by remember {
            derivedStateOf { if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add }
          }
          Icon(
            painter = rememberVectorPainter(imageVector),
            contentDescription = null,
            modifier = Modifier.animateIcon({ checkedProgress }),
          )
        }
      },
    ) {
      items.forEachIndexed { i, item ->
        FloatingActionButtonMenuItem(
          modifier =
            Modifier.semantics {
              isTraversalGroup = true
              // Add a custom a11y action to allow closing the menu when focusing
              // the last menu item, since the close button comes before the first
              // menu item in the traversal order.
              if (i == items.size - 1) {
                customActions =
                  listOf(
                    CustomAccessibilityAction(
                      label = "Close menu",
                      action = {
                        fabMenuExpanded = false
                        true
                      },
                    )
                  )
              }
            }
              .then(
              if (i == 0) {
                Modifier.onKeyEvent {
                  // Navigating back from the first item should go back to the FAB menu button.
                  if (
                    it.type == KeyEventType.KeyDown &&
                    (it.key == Key.DirectionUp || (it.isShiftPressed && it.key == Key.Tab))
                  ) {
                    focusRequester.requestFocus()
                    return@onKeyEvent true
                  }
                  return@onKeyEvent false
                }
              } else {
                Modifier
              }
            ),
          onClick = {
            fabMenuExpanded = false
            onItemClick(item.second)
          },
          icon = { Icon(item.first, contentDescription = null) },
          text = { Text(text = item.second) },
        )
      }
    }
  }
}
