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
package com.google.android.material.animation;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import android.animation.ValueAnimator;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import com.google.android.material.testapp.animation.R;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import android.view.animation.PathInterpolator;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MotionSpecTest {

  @Rule
  public final ActivityTestRule<AppCompatActivity> activityTestRule =
      new ActivityTestRule<>(AppCompatActivity.class);

  @Test
  public void loadMotionSpec() {
    MotionSpec.createFromResource(
        activityTestRule.getActivity(), R.animator.valid_set_of_object_animator_motion_spec);
    MotionSpec.createFromResource(
        activityTestRule.getActivity(), R.animator.valid_object_animator_motion_spec);
    MotionSpec.createFromResource(
        activityTestRule.getActivity(), R.animator.valid_empty_set_motion_spec);
    MotionSpec.createFromResource(
        activityTestRule.getActivity(), R.animator.valid_interpolators_motion_spec);
  }

  @Test
  public void setOfObjectAnimatorMotionSpecHasAlphaAndTranslationTimings() {
    MotionSpec spec =
        MotionSpec.createFromResource(
            activityTestRule.getActivity(), R.animator.valid_set_of_object_animator_motion_spec);
    assertNotNull(spec.getTiming("alpha"));
    assertNotNull(spec.getTiming("translation"));
  }

  @Test
  public void loadFromAttributes() {
    AppCompatActivity context = activityTestRule.getActivity();

    TypedArray attributes =
        context.obtainStyledAttributes(null, R.styleable.Test, 0, R.style.Widget_Test);

    MotionSpec spec1 =
        MotionSpec.createFromAttribute(context, attributes, R.styleable.Test_motionSpec);
    MotionSpec spec2 =
        MotionSpec.createFromResource(context, R.animator.valid_set_of_object_animator_motion_spec);
    assertEquals(spec1, spec2);
  }

  @Test
  public void validateSetOfObjectAnimatorAlphaMotionTiming() {
    MotionSpec spec =
        MotionSpec.createFromResource(
            activityTestRule.getActivity(), R.animator.valid_set_of_object_animator_motion_spec);
    MotionTiming alpha = spec.getTiming("alpha");

    assertEquals(3, alpha.getDelay());
    assertEquals(5, alpha.getDuration());
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      assertThat(alpha.getInterpolator(), instanceOf(PathInterpolator.class));
    } else {
      assertThat(alpha.getInterpolator(), instanceOf(FastOutLinearInInterpolator.class));
    }
    assertEquals(7, alpha.getRepeatCount());
    assertEquals(ValueAnimator.RESTART, alpha.getRepeatMode());
  }

  @Test
  public void validateSetOfObjectAnimatorTranslationMotionTiming() {
    MotionSpec spec =
        MotionSpec.createFromResource(
            activityTestRule.getActivity(), R.animator.valid_set_of_object_animator_motion_spec);
    MotionTiming translation = spec.getTiming("translation");

    assertEquals(11, translation.getDelay());
    assertEquals(13, translation.getDuration());
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      assertThat(translation.getInterpolator(), instanceOf(PathInterpolator.class));
    } else {
      assertThat(translation.getInterpolator(), instanceOf(FastOutSlowInInterpolator.class));
    }
    assertEquals(17, translation.getRepeatCount());
    assertEquals(ValueAnimator.REVERSE, translation.getRepeatMode());
  }

  public void inflateInvalidSetOfSetMotionSpec() {
    assertNull(
        MotionSpec.createFromResource(
            activityTestRule.getActivity(), R.animator.invalid_set_of_set_motion_spec));
  }

  public void inflateInvalidSetOfValueAnimatorMotionSpec() {
    assertNull(
        MotionSpec.createFromResource(
            activityTestRule.getActivity(), R.animator.invalid_set_of_value_animator_motion_spec));
  }
}
