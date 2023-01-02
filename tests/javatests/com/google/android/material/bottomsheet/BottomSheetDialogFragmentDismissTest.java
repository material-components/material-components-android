/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.bottomsheet;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.espresso.Espresso;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.material.testapp.BottomSheetDialogActivity;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class BottomSheetDialogFragmentDismissTest {

  @Rule
  public final ActivityTestRule<BottomSheetDialogActivity> activityTestRule =
      new ActivityTestRule<>(BottomSheetDialogActivity.class);

  private BottomSheetDialogTestFragment dialogFragment;

  private DialogInterface.OnDismissListener onDismissListener;

  @Before
  public void showDialog() {
    // Create a dismiss listener to check with verifyDialogWasDismissed().
    onDismissListener = mock(DialogInterface.OnDismissListener.class);

    dialogFragment = new BottomSheetDialogTestFragment();
    dialogFragment.setOnDismissListener(onDismissListener);
    dialogFragment.show(activityTestRule.getActivity().getSupportFragmentManager(), "dialog");

    // Wait for dialog to be shown.
    onView(withText("It is fine today.")).check(matches(isDisplayed()));
  }

  @After
  public void tearDown() {
    if (dialogFragment != null
        && dialogFragment.getDialog() != null
        && dialogFragment.getDialog().isShowing()) {
      // Close the dialog
      Espresso.pressBack();
    }
  }

  @Test
  public void testDismiss() {
    dialogFragment.dismiss();

    verifyDialogWasDismissed();
  }

  @Test
  public void testDismissWithBottomSheetAnimation() {
    dialogFragment.getDialog().setDismissWithAnimation(true);

    dialogFragment.dismiss();

    verifyDialogWasDismissed();
  }

  @Test
  public void testDismissWithBottomSheetAnimation_hideableIsFalse() {
    dialogFragment.getDialog().getBehavior().setHideable(false);
    dialogFragment.getDialog().setDismissWithAnimation(true);

    dialogFragment.dismiss();

    verifyDialogWasDismissed();
  }

  private void verifyDialogWasDismissed() {
    verify(onDismissListener, timeout(3000)).onDismiss(any(DialogInterface.class));
  }

  public static class BottomSheetDialogTestFragment extends BottomSheetDialogFragment {
    private DialogInterface.OnDismissListener onDismissListener;

    public BottomSheetDialogTestFragment() {}

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
      this.onDismissListener = onDismissListener;
    }

    @Nullable
    @Override
    public BottomSheetDialog getDialog() {
      return (BottomSheetDialog) super.getDialog();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
      onDismissListener.onDismiss(dialog);
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      AppCompatTextView text = new AppCompatTextView(getContext());
      StringBuilder builder = new StringBuilder();
      builder.append("It is fine today.");
      text.setText(builder);
      return text;
    }
  }
}
