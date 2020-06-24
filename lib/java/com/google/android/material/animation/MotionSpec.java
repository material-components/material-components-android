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

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;
import android.util.Property;
import androidx.annotation.AnimatorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleableRes;
import androidx.collection.SimpleArrayMap;
import java.util.ArrayList;
import java.util.List;

/**
 * A motion spec contains multiple named {@link MotionTiming motion timings}.
 *
 * <p>Inflate an instance of MotionSpec from XML by creating a <a
 * href="https://developer.android.com/guide/topics/resources/animation-resource.html#Property">Property
 * Animation resource</a> in {@code res/animator}. The file must contain an {@code <objectAnimator>}
 * or a {@code <set>} of object animators.
 *
 * <p>This class will store a map of String keys to MotionTiming values. Each animator's {@code
 * android:propertyName} attribute will be used as the key, while the other attributes {@code
 * android:startOffset}, {@code android:duration}, {@code android:interpolator}, {@code
 * android:repeatCount}, and {@code android:repeatMode} will be used to create the MotionTiming
 * instance.
 *
 * <p>A motion spec resource can either be an &lt;objectAnimator&gt; or a &lt;set&gt; of multiple
 * &lt;objectAnimator&gt;.
 *
 * <pre>{@code
 * <set xmlns:android="http://schemas.android.com/apk/res/android">
 *   <objectAnimator
 *       android:propertyName="alpha"
 *       android:startOffset="0"
 *       android:duration="100"
 *       android:interpolator="@interpolator/mtrl_fast_out_slow_in"/>
 *   <objectAnimator
 *       android:propertyName="translation"
 *       android:startOffset="50"
 *       android:duration="150"/>
 * </set>
 * }</pre>
 */
public class MotionSpec {

  private static final String TAG = "MotionSpec";

  private final SimpleArrayMap<String, MotionTiming> timings = new SimpleArrayMap<>();
  private final SimpleArrayMap<String, PropertyValuesHolder[]> propertyValues =
      new SimpleArrayMap<>();

  /** Returns whether this motion spec contains a MotionTiming with the given name. */
  public boolean hasTiming(String name) {
    return timings.get(name) != null;
  }

  /**
   * Returns the MotionTiming with the given name, or throws IllegalArgumentException if it does not
   * exist.
   */
  public MotionTiming getTiming(String name) {
    if (!hasTiming(name)) {
      throw new IllegalArgumentException();
    }
    return timings.get(name);
  }

  /** Sets a MotionTiming with the given name. */
  public void setTiming(String name, @Nullable MotionTiming timing) {
    timings.put(name, timing);
  }

  /**
   * Returns whether this motion spec contains a {@link PropertyValuesHolder[]} with the given name.
   */
  public boolean hasPropertyValues(String name) {
    return propertyValues.get(name) != null;
  }

  /**
   * Get values for a property in this MotionSpec.
   *
   * @param name Name of the property to get values for, e.g. "width" or "opacity".
   * @return Array of {@link PropertyValuesHolder} values for the property.
   */
  @NonNull
  public PropertyValuesHolder[] getPropertyValues(String name) {
    if (!hasPropertyValues(name)) {
      throw new IllegalArgumentException();
    }
    return clonePropertyValuesHolder(propertyValues.get(name));
  }

  /**
   * Set values for a property in this MotionSpec.
   *
   * @param name Name of the property to set values for, e.g. "width" or "opacity".
   * @param values Array of {@link PropertyValuesHolder} values for the property.
   */
  public void setPropertyValues(String name, PropertyValuesHolder[] values) {
    propertyValues.put(name, values);
  }

  @NonNull
  private PropertyValuesHolder[] clonePropertyValuesHolder(@NonNull PropertyValuesHolder[] values) {
    PropertyValuesHolder[] ret = new PropertyValuesHolder[values.length];
    for (int i = 0; i < values.length; i++) {
      ret[i] = values[i].clone();
    }
    return ret;
  }

  /**
   * Creates and returns an {@link ObjectAnimator} that animates the given property. This can be
   * added to an {@link AnimatorSet} to play multiple synchronized animations.
   *
   * @param name Name of the property to be animated.
   * @param target The target whose property is to be animated. See {@link
   *     ObjectAnimator#ofPropertyValuesHolder(T, PropertyValuesHolder...)} for more details.
   * @param property The {@link Property} object being animated.
   * @return An {@link ObjectAnimator} which animates the given property.
   */
  @NonNull
  public <T> ObjectAnimator getAnimator(
      @NonNull String name, @NonNull T target, @NonNull Property<T, ?> property) {
    ObjectAnimator animator =
        ObjectAnimator.ofPropertyValuesHolder(target, getPropertyValues(name));
    animator.setProperty(property);
    getTiming(name).apply(animator);
    return animator;
  }

  /**
   * Returns the total duration of this motion spec, which is the maximum delay+duration of its
   * motion timings.
   */
  public long getTotalDuration() {
    long duration = 0;
    for (int i = 0, count = timings.size(); i < count; i++) {
      MotionTiming timing = timings.valueAt(i);
      duration = Math.max(duration, timing.getDelay() + timing.getDuration());
    }
    return duration;
  }

  /**
   * Inflates an instance of MotionSpec from the animator resource indexed in the given attributes
   * array.
   */
  @Nullable
  public static MotionSpec createFromAttribute(
      @NonNull Context context, @NonNull TypedArray attributes, @StyleableRes int index) {
    if (attributes.hasValue(index)) {
      int resourceId = attributes.getResourceId(index, 0);
      if (resourceId != 0) {
        return createFromResource(context, resourceId);
      }
    }
    return null;
  }

  /** Inflates an instance of MotionSpec from the given animator resource. */
  @Nullable
  public static MotionSpec createFromResource(@NonNull Context context, @AnimatorRes int id) {
    try {
      Animator animator = AnimatorInflater.loadAnimator(context, id);
      if (animator instanceof AnimatorSet) {
        AnimatorSet set = (AnimatorSet) animator;
        return createSpecFromAnimators(set.getChildAnimations());
      } else if (animator != null) {
        List<Animator> animators = new ArrayList<>();
        animators.add(animator);
        return createSpecFromAnimators(animators);
      } else {
        return null;
      }
    } catch (Exception e) {
      Log.w(TAG, "Can't load animation resource ID #0x" + Integer.toHexString(id), e);
      return null;
    }
  }

  @NonNull
  private static MotionSpec createSpecFromAnimators(@NonNull List<Animator> animators) {
    MotionSpec spec = new MotionSpec();
    for (int i = 0, count = animators.size(); i < count; i++) {
      addInfoFromAnimator(spec, animators.get(i));
    }
    return spec;
  }

  private static void addInfoFromAnimator(@NonNull MotionSpec spec, Animator animator) {
    if (animator instanceof ObjectAnimator) {
      ObjectAnimator anim = (ObjectAnimator) animator;
      spec.setPropertyValues(anim.getPropertyName(), anim.getValues());
      spec.setTiming(anim.getPropertyName(), MotionTiming.createFromAnimator(anim));
    } else {
      throw new IllegalArgumentException("Animator must be an ObjectAnimator: " + animator);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MotionSpec)) {
      return false;
    }

    MotionSpec that = (MotionSpec) o;

    return timings.equals(that.timings);
  }

  @Override
  public int hashCode() {
    return timings.hashCode();
  }

  @NonNull
  @Override
  public String toString() {
    StringBuilder out = new StringBuilder();
    out.append('\n');
    out.append(getClass().getName());
    out.append('{');
    out.append(Integer.toHexString(System.identityHashCode(this)));
    out.append(" timings: ");
    out.append(timings);
    out.append("}\n");
    return out.toString();
  }
}
