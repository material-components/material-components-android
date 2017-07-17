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

import static android.support.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;

import android.view.View;
import android.view.ViewConfiguration;
import android.support.test.espresso.PerformException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.PrecisionDescriber;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.support.test.espresso.action.Tapper;
import android.support.test.espresso.util.HumanReadables;
import org.hamcrest.Matcher;

/**
 * Simple click action that allows clicking on views whose contents are at least 50% within the
 * bounds of the screen.
 *
 * <p>By design several of the views in this test will be less than 90% visible and {@link
 * android.support.test.espresso.action.GeneralClickAction} is constrained by
 * at least 90% visibility. Since GeneralClickAction is a final class we need to copy a lot of the
 * code.
 */
public class SimpleClickAction implements ViewAction {

  CoordinatesProvider coordinatesProvider = GeneralLocation.VISIBLE_CENTER;
  Tapper tapper = Tap.SINGLE;
  PrecisionDescriber precisionDescriber = Press.FINGER;

  @Override
  public Matcher<View> getConstraints() {
    return isDisplayingAtLeast(50);
  }

  @Override
  public String getDescription() {
    return "Simple click action that allows clicking on views whose contents are at least 50%"
        + " within the bounds of the screen.";
  }

  @Override
  public void perform(UiController uiController, View view) {
    float[] coordinates = coordinatesProvider.calculateCoordinates(view);
    float[] precision = precisionDescriber.describePrecision();
    Tapper.Status status = Tapper.Status.FAILURE;
    int loopCount = 0;
    // Native event injection is quite a tricky process. A tap is actually 2
    // separate motion events which need to get injected into the system. Injection
    // makes an RPC call from our app under test to the Android system server, the
    // system server decides which window layer to deliver the event to, the system
    // server makes an RPC to that window layer, that window layer delivers the event
    // to the correct UI element, activity, or window object. Now we need to repeat
    // that 2x. for a simple down and up. Oh and the down event triggers timers to
    // detect whether or not the event is a long vs. short press. The timers are
    // removed the moment the up event is received (NOTE: the possibility of eventTime
    // being in the future is totally ignored by most motion event processors).
    //
    // Phew.
    //
    // The net result of this is sometimes we'll want to do a regular tap, and for
    // whatever reason the up event (last half) of the tap is delivered after long
    // press timeout (depending on system load) and the long press behaviour is
    // displayed (EG: show a context menu). There is no way to avoid or handle this more
    // gracefully.
    while (status != Tapper.Status.SUCCESS && loopCount < 3) {
      try {
        status = tapper.sendTap(uiController, coordinates, precision);
      } catch (RuntimeException re) {
        throw new PerformException.Builder()
            .withActionDescription(this.getDescription())
            .withViewDescription(HumanReadables.describe(view))
            .withCause(re)
            .build();
      }
      int duration = ViewConfiguration.getPressedStateDuration();
      // ensures that all work enqueued to process the tap has been run.
      if (duration > 0) {
        uiController.loopMainThreadForAtLeast(duration);
      }
      if (status == Tapper.Status.WARNING) {
        break;
      }
      loopCount++;
    }
    if (status == Tapper.Status.FAILURE) {
      throw new PerformException.Builder()
          .withActionDescription(this.getDescription())
          .withViewDescription(HumanReadables.describe(view))
          .withCause(
              new RuntimeException(
                  String.format(
                      "Couldn't click at: %s,%s precision: %s, %s . Tapper: %s"
                          + " coordinate provider: %s precision "
                          + "describer: %s. Tried %s times.",
                      coordinates[0],
                      coordinates[1],
                      precision[0],
                      precision[1],
                      tapper,
                      coordinatesProvider,
                      precisionDescriber,
                      loopCount)))
          .build();
    }
  }
}
