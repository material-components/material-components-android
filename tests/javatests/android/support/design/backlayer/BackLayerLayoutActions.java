/*
 * Copyright 2017 The Android Open Source Project
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

package android.support.design.backlayer;

import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;

import android.view.View;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import org.hamcrest.Matcher;

public class BackLayerLayoutActions {

  public static final int WAIT_MILLISECONDS = 400;

  /** Simple click action that allows 50% visible views. */
  public static ViewAction simpleClick() {
    return new SimpleClickAction();
  }

  public static ViewAction expand() {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(BackLayerLayout.class);
      }

      @Override
      public String getDescription() {
        return "Expands the backlayer";
      }

      @Override
      public void perform(UiController uiController, View view) {
        BackLayerLayout layout = (BackLayerLayout) view;
        layout.setExpanded(true);

        uiController.loopMainThreadForAtLeast(WAIT_MILLISECONDS);
      }
    };
  }

  public static ViewAction collapse() {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(BackLayerLayout.class);
      }

      @Override
      public String getDescription() {
        return "Collapses the backlayer";
      }

      @Override
      public void perform(UiController uiController, View view) {
        BackLayerLayout layout = (BackLayerLayout) view;
        layout.setExpanded(false);

        uiController.loopMainThreadForAtLeast(WAIT_MILLISECONDS);
      }
    };
  }

  public static ViewAction waitUntilIdle() {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(BackLayerLayout.class);
      }

      @Override
      public String getDescription() {
        return "Waits until the backlayer is idle";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadForAtLeast(WAIT_MILLISECONDS);
      }
    };
  }
}
