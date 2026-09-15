/*
 * Copyright 2026 The Android Open Source Project
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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLooper;

@RunWith(RobolectricTestRunner.class)
public class AnimationCoordinatorTest {

  private Context context;
  private AnimationCoordinator coordinator;
  private AnimationCoordinator.Listener mockListener;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    coordinator = new AnimationCoordinator();
    mockListener = mock(AnimationCoordinator.Listener.class);
    coordinator.addListener(mockListener);
  }

  @Test
  public void start_noAnimations_callsListenersImmediately() {
    coordinator.start();
    ShadowLooper.idleMainLooper();

    verify(mockListener).onAnimationsStart();
    verify(mockListener).onAnimationsEnd();
  }

  @Test
  public void start_onlyDurationAnimations_callsListeners() {
    ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
    animator.setDuration(100);
    coordinator.addAnimator(animator);

    coordinator.start();
    ShadowLooper.idleMainLooper();

    verify(mockListener).onAnimationsStart();
    verify(mockListener).onAnimationsEnd();
  }

  @Test
  public void start_onlySpringAnimations_callsListeners() {
    View testView = new View(context);
    SpringAnimation springAnimation =
        new SpringAnimation(testView, SpringAnimation.ALPHA)
            .setSpring(new SpringForce(1f).setStiffness(0.01f).setDampingRatio(1f));
    coordinator.addDynamicAnimation(springAnimation);

    coordinator.start();
    ShadowLooper.idleMainLooper();

    verify(mockListener).onAnimationsStart();
    verify(mockListener).onAnimationsEnd();
  }

  @Test
  public void start_mixedAnimations_callsListeners() {
    ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
    animator.setDuration(100);
    coordinator.addAnimator(animator);

    View testView = new View(context);
    SpringAnimation springAnimation =
        new SpringAnimation(testView, SpringAnimation.ALPHA)
            .setSpring(new SpringForce(1f).setStiffness(0.01f).setDampingRatio(1f));
    coordinator.addDynamicAnimation(springAnimation);

    coordinator.start();
    ShadowLooper.idleMainLooper();

    verify(mockListener).onAnimationsStart();
    verify(mockListener).onAnimationsEnd();
  }

  @Test
  public void start_calledTwice_onlyRunsOnce() {
    ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
    animator.setDuration(100);
    coordinator.addAnimator(animator);

    coordinator.start();
    coordinator.start(); // Second call should be ignored
    ShadowLooper.idleMainLooper();

    verify(mockListener).onAnimationsStart();
    verify(mockListener).onAnimationsEnd();
  }

  @Test
  public void removeListener_preventsCallbacks() {
    coordinator.removeListener(mockListener);
    coordinator.start();
    ShadowLooper.idleMainLooper();

    verify(mockListener, never()).onAnimationsStart();
    verify(mockListener, never()).onAnimationsEnd();
  }

  @Test
  public void clear_removesAnimationsAndListeners() {
    ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
    animator.setDuration(100);
    coordinator.addAnimator(animator);

    coordinator.clear();
    coordinator.start();
    ShadowLooper.idleMainLooper();

    verify(mockListener, never()).onAnimationsStart();
    verify(mockListener, never()).onAnimationsEnd();
  }

  @Test
  public void clear_whileRunning_cancelsAndJumpsToEnd() {
    ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
    animator.setDuration(100);
    coordinator.addAnimator(animator);

    View testView = new View(context);
    SpringAnimation springAnimation =
        new SpringAnimation(testView, SpringAnimation.ALPHA)
            .setSpring(new SpringForce(1f).setStiffness(0.01f).setDampingRatio(1f));
    coordinator.addDynamicAnimation(springAnimation);

    coordinator.start();

    // Verify animations are running
    assertThat(animator.isRunning()).isTrue();
    assertThat(springAnimation.isRunning()).isTrue();

    coordinator.clear();
    ShadowLooper.idleMainLooper();

    // Verify animations are stopped and jumped to end
    assertThat(animator.isRunning()).isFalse();
    assertThat((float) animator.getAnimatedValue()).isEqualTo(1f);
    assertThat(springAnimation.isRunning()).isFalse();

    // Verify listeners are not called after clear
    ShadowLooper.idleMainLooper();
    verify(mockListener).onAnimationsStart();
    verify(mockListener, never()).onAnimationsEnd();
  }
}
