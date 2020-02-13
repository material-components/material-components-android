/*
 * Copyright 2020 The Android Open Source Project
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

package com.google.android.material.transition;

import static com.google.android.material.transition.TransitionUtils.getRelativeBoundsRect;

import android.app.Activity;
import android.app.SharedElementCallback;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION_CODES;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.transition.Transition;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.Window;
import com.google.android.material.internal.ContextUtils;
import com.google.android.material.shape.Shapeable;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

/**
 * A {@link SharedElementCallback} to be used for {@link MaterialContainerTransform} transitions.
 */
@RequiresApi(VERSION_CODES.LOLLIPOP)
public class MaterialContainerTransformSharedElementCallback extends SharedElementCallback {

  @Nullable private static WeakReference<View> capturedSharedElement;

  private boolean entering = true;
  private boolean transparentWindowBackgroundEnabled = true;
  @Nullable private Rect returnEndBounds;
  @Nullable private Drawable originalWindowBackground;

  @NonNull
  @Override
  public Parcelable onCaptureSharedElementSnapshot(
      @NonNull View sharedElement,
      @NonNull Matrix viewToGlobalMatrix,
      @NonNull RectF screenBounds) {
    capturedSharedElement = new WeakReference<>(sharedElement);
    return super.onCaptureSharedElementSnapshot(sharedElement, viewToGlobalMatrix, screenBounds);
  }

  @NonNull
  @Override
  public View onCreateSnapshotView(@NonNull Context context, @NonNull Parcelable snapshot) {
    View snapshotView = super.onCreateSnapshotView(context, snapshot);
    if (capturedSharedElement != null && capturedSharedElement.get() instanceof Shapeable) {
      // Set shape appearance as snapshot view tag, which will be used by the transform.
      snapshotView.setTag(((Shapeable) capturedSharedElement.get()).getShapeAppearanceModel());
    }
    return snapshotView;
  }

  @Override
  public void onMapSharedElements(
      @NonNull List<String> names, @NonNull Map<String, View> sharedElements) {
    if (!names.isEmpty() && !sharedElements.isEmpty()) {
      View sharedElement = sharedElements.get(names.get(0));
      if (sharedElement != null) {
        Activity activity = ContextUtils.getActivity(sharedElement.getContext());
        if (activity != null) {
          Window window = activity.getWindow();
          if (entering) {
            setUpEnterTransform(window);
          } else {
            setUpReturnTransform(activity, window);
          }
        }
      }
    }
  }

  @Override
  public void onSharedElementStart(
      @NonNull List<String> sharedElementNames,
      @NonNull List<View> sharedElements,
      @NonNull List<View> sharedElementSnapshots) {
    if (!sharedElements.isEmpty() && !sharedElementSnapshots.isEmpty()) {
      // Set up the snapshot view tag so that the transform knows when to use the snapshot view
      // instead of the view provided by TransitionValues.
      sharedElements.get(0).setTag(sharedElementSnapshots.get(0));
    }

    if (!entering && !sharedElements.isEmpty() && returnEndBounds != null) {
      // Counteract the manipulation of the shared element view's bounds in the framework's
      // ExitTransitionCoordinator so that the return animation starts from the correct location.
      View sharedElement = sharedElements.get(0);
      int widthSpec = MeasureSpec.makeMeasureSpec(returnEndBounds.width(), MeasureSpec.EXACTLY);
      int heightSpec = MeasureSpec.makeMeasureSpec(returnEndBounds.height(), MeasureSpec.EXACTLY);
      sharedElement.measure(widthSpec, heightSpec);
      sharedElement.layout(
          returnEndBounds.left, returnEndBounds.top, returnEndBounds.right, returnEndBounds.bottom);
    }
  }

  @Override
  public void onSharedElementEnd(
      @NonNull List<String> sharedElementNames,
      @NonNull List<View> sharedElements,
      @NonNull List<View> sharedElementSnapshots) {
    if (!sharedElements.isEmpty() && sharedElements.get(0).getTag() instanceof View) {
      // Reset tag so we only use it for the start or end view depending on enter vs return.
      sharedElements.get(0).setTag(null);
    }

    if (!entering && !sharedElements.isEmpty()) {
      returnEndBounds = getRelativeBoundsRect(sharedElements.get(0));
    }

    entering = false;
  }

  /**
   * Returns whether the incoming window's background should be made transparent during the
   * transition.
   *
   * @see #setTransparentWindowBackgroundEnabled(boolean)
   */
  public boolean isTransparentWindowBackgroundEnabled() {
    return transparentWindowBackgroundEnabled;
  }

  /**
   * If enabled, the incoming window's background will be made transparent during the transition.
   * This results in an effect where the outgoing activity's content is visible for the duration of
   * the transition, because the incoming window background will not be faded in on top of it.
   * Default is true.
   *
   * <p>Note: in order to avoid some visual artifacts, when this setting is enabled the window's
   * transition background fade duration (see {@link
   * Window#setTransitionBackgroundFadeDuration(long)}) will be overridden to be greater than the
   * duration of the transform transition.
   */
  public void setTransparentWindowBackgroundEnabled(boolean transparentWindowBackgroundEnabled) {
    this.transparentWindowBackgroundEnabled = transparentWindowBackgroundEnabled;
  }

  private void setUpEnterTransform(final Window window) {
    Transition transition = window.getSharedElementEnterTransition();
    if (transition instanceof MaterialContainerTransform) {
      MaterialContainerTransform transform = (MaterialContainerTransform) transition;
      if (transparentWindowBackgroundEnabled) {
        updateBackgroundFadeDuration(window, transform);
        transform.addListener(
            new TransitionListenerAdapter() {
              @Override
              public void onTransitionStart(Transition transition) {
                originalWindowBackground = window.getDecorView().getBackground();
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
              }

              @Override
              public void onTransitionEnd(Transition transition) {
                if (originalWindowBackground != null) {
                  window.setBackgroundDrawable(originalWindowBackground);
                }
              }
            });
      }
    }
  }

  private void setUpReturnTransform(final Activity activity, final Window window) {
    Transition transition = window.getSharedElementReturnTransition();
    if (transition instanceof MaterialContainerTransform) {
      MaterialContainerTransform transform = (MaterialContainerTransform) transition;
      transform.setHoldAtEndEnabled(true);
      transform.addListener(
          new TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
              // Make sure initial shared element view is visible to avoid blinking effect.
              if (capturedSharedElement != null && capturedSharedElement.get() != null) {
                capturedSharedElement.get().setAlpha(1);
                capturedSharedElement = null;
              }

              // Prevent extra transform from happening after the return transform is finished.
              activity.finish();
              activity.overridePendingTransition(0, 0);
            }
          });
      if (transparentWindowBackgroundEnabled) {
        updateBackgroundFadeDuration(window, transform);
        transform.addListener(
            new TransitionListenerAdapter() {
              @Override
              public void onTransitionStart(Transition transition) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
              }
            });
      }
    }
  }

  /**
   * When using a transparent window background, make sure that the background fade duration is at
   * least as long as the transform's duration. This will help to avoid a black background visual
   * artifact.
   */
  private static void updateBackgroundFadeDuration(
      Window window, MaterialContainerTransform transform) {
    window.setTransitionBackgroundFadeDuration(transform.getDuration() * 2);
  }
}
