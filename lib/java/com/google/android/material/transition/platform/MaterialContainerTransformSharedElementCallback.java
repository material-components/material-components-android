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

package com.google.android.material.transition.platform;

import com.google.android.material.R;

import static com.google.android.material.transition.platform.TransitionUtils.getRelativeBoundsRect;

import android.app.Activity;
import android.app.SharedElementCallback;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION_CODES;
import android.os.Parcelable;
import android.transition.Transition;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.Window;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;
import com.google.android.material.internal.ContextUtils;
import com.google.android.material.shape.ShapeAppearanceModel;
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
  private boolean sharedElementReenterTransitionEnabled = false;
  @Nullable private Rect returnEndBounds;
  @Nullable private ShapeProvider shapeProvider = new ShapeableViewShapeProvider();

  /** Allows providing a {@link ShapeAppearanceModel} for the shared element view. */
  public interface ShapeProvider {
    @Nullable
    ShapeAppearanceModel provideShape(@NonNull View sharedElement);
  }

  /**
   * A {@link ShapeProvider} that provides the view's {@link ShapeAppearanceModel} if the view
   * implements the {@link Shapeable} interface.
   */
  public static class ShapeableViewShapeProvider implements ShapeProvider {
    @Nullable
    @Override
    public ShapeAppearanceModel provideShape(@NonNull View sharedElement) {
      return sharedElement instanceof Shapeable
          ? ((Shapeable) sharedElement).getShapeAppearanceModel()
          : null;
    }
  }

  @Nullable
  @Override
  public Parcelable onCaptureSharedElementSnapshot(
      @NonNull View sharedElement,
      @NonNull Matrix viewToGlobalMatrix,
      @NonNull RectF screenBounds) {
    capturedSharedElement = new WeakReference<>(sharedElement);
    return super.onCaptureSharedElementSnapshot(sharedElement, viewToGlobalMatrix, screenBounds);
  }

  @Nullable
  @Override
  public View onCreateSnapshotView(@NonNull Context context, @Nullable Parcelable snapshot) {
    View snapshotView = super.onCreateSnapshotView(context, snapshot);
    if (snapshotView != null && capturedSharedElement != null && shapeProvider != null) {
      View sharedElement = capturedSharedElement.get();
      if (sharedElement != null) {
        ShapeAppearanceModel shapeAppearanceModel = shapeProvider.provideShape(sharedElement);
        if (shapeAppearanceModel != null) {
          // Set shape appearance as snapshot view tag, which will be used by the transform.
          snapshotView.setTag(R.id.mtrl_motion_snapshot_view, shapeAppearanceModel);
        }
      }
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
      sharedElements.get(0).setTag(R.id.mtrl_motion_snapshot_view, sharedElementSnapshots.get(0));
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
    if (!sharedElements.isEmpty()
        && sharedElements.get(0).getTag(R.id.mtrl_motion_snapshot_view) instanceof View) {
      // Reset tag so we only use it for the start or end view depending on enter vs return.
      sharedElements.get(0).setTag(R.id.mtrl_motion_snapshot_view, null);
    }

    if (!entering && !sharedElements.isEmpty()) {
      returnEndBounds = getRelativeBoundsRect(sharedElements.get(0));
    }

    entering = false;
  }

  /** Get the {@link ShapeProvider} for this callback, or null if it is not set. */
  @Nullable
  public ShapeProvider getShapeProvider() {
    return shapeProvider;
  }

  /**
   * Set the {@link ShapeProvider} for this callback, which allows providing a {@link
   * ShapeAppearanceModel} for the shared element view.
   *
   * <p>The default is a {@link ShapeableViewShapeProvider}, which will use the view's {@link
   * ShapeAppearanceModel} if the view implements the {@link Shapeable} interface.
   */
  public void setShapeProvider(@Nullable ShapeProvider shapeProvider) {
    this.shapeProvider = shapeProvider;
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

  /**
   * Returns whether incoming Activity's sharedElementReenterTransition will be respected.
   *
   * @see #setSharedElementReenterTransitionEnabled(boolean)
   */
  public boolean isSharedElementReenterTransitionEnabled() {
    return sharedElementReenterTransitionEnabled;
  }

  /**
   * If enabled, the Activity's sharedElementReenterTransition will be respected; otherwise it will
   * be set to null. Default is false, meaning the sharedElementReenterTransition will be set to
   * null.
   */
  public void setSharedElementReenterTransitionEnabled(
      boolean sharedElementReenterTransitionEnabled) {
    this.sharedElementReenterTransitionEnabled = sharedElementReenterTransitionEnabled;
  }

  private void setUpEnterTransform(final Window window) {
    Transition transition = window.getSharedElementEnterTransition();
    if (transition instanceof MaterialContainerTransform) {
      MaterialContainerTransform transform = (MaterialContainerTransform) transition;
      if (!sharedElementReenterTransitionEnabled) {
        window.setSharedElementReenterTransition(null);
      }
      if (transparentWindowBackgroundEnabled) {
        updateBackgroundFadeDuration(window, transform);
        transform.addListener(
            new TransitionListenerAdapter() {
              @Override
              public void onTransitionStart(Transition transition) {
                removeWindowBackground(window);
              }

              @Override
              public void onTransitionEnd(Transition transition) {
                restoreWindowBackground(window);
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
              if (capturedSharedElement != null) {
                View sharedElement = capturedSharedElement.get();
                if (sharedElement != null) {
                  sharedElement.setAlpha(1);
                  capturedSharedElement = null;
                }
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
                removeWindowBackground(window);
              }
            });
      }
    }
  }

  /**
   * Make the window background transparent during the container transform.
   *
   * <p>This applies a color filter to the window background which clears the background's source
   * pixels. If the client has set a color filter on the window background manually, this will be
   * overridden and will not be restored after the transition. If you need to manipulate the color
   * of the window background and have that manipulation restored after the transition, use {@link
   * android.graphics.drawable.Drawable#setTint(int)} instead.
   */
  private static void removeWindowBackground(Window window) {
    Drawable windowBackground = getWindowBackground(window);
    if (windowBackground == null) {
      return;
    }
    windowBackground
        .mutate()
        .setColorFilter(
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                Color.TRANSPARENT, BlendModeCompat.CLEAR));
  }

  /**
   * Restores the window background to its state before running the container transform.
   *
   * @see #removeWindowBackground(Window)
   */
  private static void restoreWindowBackground(Window window) {
    Drawable windowBackground = getWindowBackground(window);
    if (windowBackground == null) {
      return;
    }
    windowBackground.mutate().clearColorFilter();
  }

  @Nullable
  private static Drawable getWindowBackground(Window window) {
    return window.getDecorView().getBackground();
  }

  /**
   * When using a transparent window background, make sure that the background fade duration is as
   * long as the transform's duration. This will help to avoid a black background visual artifact.
   */
  private static void updateBackgroundFadeDuration(
      Window window, MaterialContainerTransform transform) {
    if (transform.getDuration() >= 0) {
      window.setTransitionBackgroundFadeDuration(transform.getDuration());
    }
  }
}
