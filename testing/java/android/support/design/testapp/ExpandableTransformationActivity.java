/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.design.testapp;

import android.support.annotation.VisibleForTesting;
import android.support.design.testapp.base.BaseTestActivity;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

/** Test Activity for ExpandableTransformationBehaviors. */
public class ExpandableTransformationActivity extends BaseTestActivity {

  @VisibleForTesting public FloatingActionButton fab;
  @VisibleForTesting public View sheet;
  @VisibleForTesting public View scrim;

  @Override
  protected int getContentViewLayoutResId() {
    return R.layout.design_expandable_transformation;
  }

  @Override
  protected void onContentViewSet() {
    fab = findViewById(R.id.fab);
    sheet = findViewById(R.id.sheet);
    scrim = findViewById(R.id.scrim);
  }
}
