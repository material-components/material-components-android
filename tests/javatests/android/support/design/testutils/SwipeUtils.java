/*
 * Copyright (C) 2017 The Android Open Source Project
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

package android.support.design.testutils;

import android.view.View;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;

public final class SwipeUtils {
  private SwipeUtils() {}

  public static GeneralSwipeAction swipeUp(
      final int swipeX, final int swipeStartY, final int swipeAmountY) {
    return new GeneralSwipeAction(
        Swipe.SLOW,
        new CoordinatesProvider() {
          @Override
          public float[] calculateCoordinates(View view) {
            return new float[] {swipeX, swipeStartY};
          }
        },
        new CoordinatesProvider() {
          @Override
          public float[] calculateCoordinates(View view) {
            return new float[] {swipeX, swipeStartY - swipeAmountY};
          }
        },
        Press.FINGER);
  }

  public static GeneralSwipeAction swipeDown(
      final int swipeX, final int swipeStartY, final int swipeAmountY) {
    return new GeneralSwipeAction(
        Swipe.SLOW,
        new CoordinatesProvider() {
          @Override
          public float[] calculateCoordinates(View view) {
            return new float[] {swipeX, swipeStartY};
          }
        },
        new CoordinatesProvider() {
          @Override
          public float[] calculateCoordinates(View view) {
            return new float[] {swipeX, swipeStartY + swipeAmountY};
          }
        },
        Press.FINGER);
  }
}
