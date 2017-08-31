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
package android.support.design.animation;

/**
 * A representation of timing for a translation animation, where the timing is dependent on the
 * relative positions of start and end.
 */
public class TranslationTiming {

  public MotionTiming linear;
  public MotionTiming curveUpwards;
  public MotionTiming curveDownwards;

  public TranslationTiming(
      MotionTiming linear, MotionTiming curveUpwards, MotionTiming curveDownwards) {
    this.linear = linear;
    this.curveUpwards = curveUpwards;
    this.curveDownwards = curveDownwards;
  }
}
