/*
 * Copyright 2018 The Android Open Source Project
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
package io.material.catalog.dialog;

import io.material.catalog.R;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import io.material.catalog.feature.DemoFragment;
import java.util.ArrayList;
import java.util.List;

/** A fragment that displays the main Dialog demos for the Catalog app. */
public class DialogMainDemoFragment extends DemoFragment {

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(R.layout.dialog_main_demo, viewGroup, false);
    LinearLayout dialogLaunchersLayout = view.findViewById(R.id.dialog_launcher_buttons_layout);
    CharSequence[] choices = {"Choice1", "Choice2", "Choice3"};
    boolean[] choicesInitial = {false, true, false};
    StringBuilder multiLineMessage = new StringBuilder();
    String line = getResources().getString(R.string.line);
    for (int i = 0; i < 100; i++) {
      multiLineMessage.append(line).append(i).append("\n");
    }
    String positiveText = getResources().getString(R.string.positive);
    String negativeText = getResources().getString(R.string.negative);
    String title = getResources().getString(R.string.title);
    String message = getResources().getString(R.string.message);
    String longMessage = getResources().getString(R.string.long_message);

    // AppCompat title, message, 3 actions
    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.app_compat_alert_dialog,
        new AlertDialog.Builder(getContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText, null)
            .setNegativeButton(negativeText, null));

    // message, 2 actions
    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.message_2_actions,
        new MaterialAlertDialogBuilder(getContext())
            .setMessage(message)
            .setPositiveButton(positiveText, null)
            .setNegativeButton(negativeText, null));

    // long message, 2 actions
    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.long_message_2_actions,
        new MaterialAlertDialogBuilder(getContext())
            .setMessage(longMessage)
            .setPositiveButton(positiveText, null)
            .setNegativeButton(negativeText, null));

    // title, 2 actions
    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.title_2_actions,
        new MaterialAlertDialogBuilder(getContext())
            .setTitle(title)
            .setPositiveButton(positiveText, null)
            .setNegativeButton(negativeText, null));

    // title, message, 3 actions (long)
    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.title_message_3_long_actions,
        new MaterialAlertDialogBuilder(getContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getResources().getString(R.string.long_positive), null)
            .setNegativeButton(getResources().getString(R.string.long_negative), null)
            .setNeutralButton(getResources().getString(R.string.long_neutral), null));

    // long title, message, 1 action (too long)
    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.long_title_message_too_long_actions,
        new MaterialAlertDialogBuilder(getContext())
            .setTitle(getResources().getString(R.string.long_title))
            .setMessage(message)
            .setPositiveButton(getResources().getString(R.string.too_long_positive), null)
            .setNegativeButton(getResources().getString(R.string.too_long_negative), null)
            .setNeutralButton(getResources().getString(R.string.too_long_neutral), null));

    // icon, title, message, 2 actions
    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.icon_title_message_2_actions,
        new MaterialAlertDialogBuilder(getContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText, null)
            .setNegativeButton(negativeText, null)
            .setIcon(R.drawable.ic_dialogs_24px));

    // icon, title, message, 2 actions (centered)
    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.icon_title_message_2_actions_centered,
        new MaterialAlertDialogBuilder(getContext(), getCenteredTitleThemeOverlay())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText, null)
            .setNegativeButton(negativeText, null)
            .setIcon(R.drawable.ic_dialogs_24px));

    // edit text
    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.edit_text,
        new MaterialAlertDialogBuilder(getContext())
            .setTitle(title)
            .setView(R.layout.edit_text)
            .setPositiveButton(
                positiveText,
                new OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    TextView input = ((AlertDialog) dialog).findViewById(android.R.id.text1);
                    Toast.makeText(getContext(), input.getText(), Toast.LENGTH_LONG).show();
                  }
                })
            .setNegativeButton(negativeText, null));

    // title, auto-action choice dialog
    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.title_choices_as_actions,
        new MaterialAlertDialogBuilder(getContext())
            .setTitle(title)
            .setPositiveButton(positiveText, null)
            .setItems(choices, null));

    // title, checkboxes, 2 actions dialog
    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.title_checkboxes_2_actions,
        new MaterialAlertDialogBuilder(getContext())
            .setTitle(title)
            .setPositiveButton(
                positiveText,
                (DialogInterface dialog, int which) -> {
                  SparseBooleanArray checkedItemPositions =
                      ((AlertDialog) dialog).getListView().getCheckedItemPositions();
                  List<CharSequence> result = new ArrayList<>();
                  for (int i = 0; i < choices.length; i++) {
                    if (checkedItemPositions.get(i)) {
                      result.add(choices[i]);
                    }
                  }
                  Toast.makeText(getContext(), result.toString(), Toast.LENGTH_LONG).show();
                })
            .setNegativeButton(negativeText, null)
            .setMultiChoiceItems(choices, choicesInitial, null));

    // title, radiobutton, 2 actions dialog
    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.title_radiobuttons_2_actions,
        new MaterialAlertDialogBuilder(getContext())
            .setTitle(title)
            .setPositiveButton(
                positiveText,
                (DialogInterface dialog, int which) -> {
                  int checkedItemPosition =
                      ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                  if (checkedItemPosition != AdapterView.INVALID_POSITION) {
                    Toast.makeText(getContext(), choices[checkedItemPosition], Toast.LENGTH_LONG)
                        .show();
                  }
                })
            .setNegativeButton(negativeText, null)
            .setSingleChoiceItems(choices, 1, null));

    // title, custom view, actions dialog
    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.title_slider_2_actions,
        new MaterialAlertDialogBuilder(getContext())
            .setTitle(title)
            .setPositiveButton(positiveText, null)
            .setNegativeButton(negativeText, null)
            .setView(R.layout.seekbar_layout));

    // title, scrolling long view, actions dialog
    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.title_scrolling_2_actions,
        new MaterialAlertDialogBuilder(getContext())
            .setTitle(title)
            .setMessage(multiLineMessage.toString())
            .setPositiveButton(positiveText, null)
            .setNegativeButton(negativeText, null));

    // scrolling view
    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.title_scrolling,
        new MaterialAlertDialogBuilder(getContext()).setMessage(multiLineMessage.toString()));

    // title, short buttons
    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.title_2_short_actions,
        new MaterialAlertDialogBuilder(getContext())
            .setTitle(title)
            .setPositiveButton(R.string.short_text_1, null)
            .setNegativeButton(R.string.short_text_2, null));

    return view;
  }

  @StyleRes
  protected int getCenteredTitleThemeOverlay() {
    return R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered;
  }

  private void addDialogLauncher(
      ViewGroup viewGroup, @StringRes int stringResId, AlertDialog.Builder alertDialogBuilder) {
    MaterialButton dialogLauncherButton = new MaterialButton(viewGroup.getContext());
    dialogLauncherButton.setOnClickListener(v -> alertDialogBuilder.show());
    dialogLauncherButton.setText(stringResId);
    viewGroup.addView(dialogLauncherButton);
  }
}
