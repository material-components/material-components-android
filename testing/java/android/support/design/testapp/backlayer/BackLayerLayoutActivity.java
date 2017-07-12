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

package android.support.design.testapp.backlayer;

import android.os.Bundle;
import android.support.design.backlayer.BackLayerLayout;
import android.support.design.testapp.base.BaseTestActivity;
import android.view.View;
import android.widget.ImageView;

/** Activity to test BackLayerLayout. */
public abstract class BackLayerLayoutActivity extends BaseTestActivity {

  ImageView expandIcon;
  BackLayerLayout backLayer;
  ImageView extraContent;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    backLayer = (BackLayerLayout) findViewById(R.id.design_backlayer_backlayer_layout);
    extraContent = (ImageView) findViewById(R.id.design_backlayer_extra_content);
    expandIcon = (ImageView) findViewById(R.id.design_backlayer_expand_icon);
    expandIcon.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            if (backLayer.isExpanded()) {
              backLayer.collapse();
            } else {
              backLayer.expand();
            }
          }
        });
  }
}
