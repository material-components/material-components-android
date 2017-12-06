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

/**
 * This adapter class provides empty implementations of the methods from {@link BackLayerCallback}.
 * Any custom listener that cares only about a subset of the methods of this listener can simply
 * subclass this adapter class instead of implementing the interface directly.
 */
public class BackLayerCallbackAdapter implements BackLayerCallback {

  @Override
  public void onBeforeExpand() {}

  @Override
  public void onAfterExpand() {}

  @Override
  public void onBeforeCollapse() {}

  @Override
  public void onAfterCollapse() {}

  @Override
  public void onRestoringExpandedBackLayer() {}
}
