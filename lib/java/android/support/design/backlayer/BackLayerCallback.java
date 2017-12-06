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

/** Interface to listen to changes of the backlayer status. */
public interface BackLayerCallback {
  /**
   * This callback is called right before the backlayer expansion animation begins. At this point
   * backlayer.isExpanded() would return false.
   */
  void onBeforeExpand();
  /**
   * This callback is called after the backlayer expansion animation is finished and
   * backlayer.isExpanded() would return true.
   */
  void onAfterExpand();
  /**
   * This callback is called right before the backlayer collapse animation begins and when
   * backlayer.isExpanded() would return true.
   */
  void onBeforeCollapse();
  /**
   * This callback is called after the backlayer collapse animation is finished and when
   * backlayer.isExpanded() would return false.
   */
  void onAfterCollapse();
  /**
   * This callback is called when {@link View#onRestoreInstanceState()} restores a expanded back
   * layer.
   *
   * <p>In this case neither {@link #onBeforeExpand()} nor {@link #onAfterExpand()} will be called
   * and the expansion of the back layer will not be animated. You should not animate any views in
   * this method, just bring them to their final expanded state.
   */
  void onRestoringExpandedBackLayer();
}
