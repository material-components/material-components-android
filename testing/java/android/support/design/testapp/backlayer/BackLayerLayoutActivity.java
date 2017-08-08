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
import android.support.annotation.IntDef;
import android.support.design.backlayer.BackLayerLayout;
import android.support.design.testapp.base.BaseTestActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Activity to test BackLayerLayout. */
public abstract class BackLayerLayoutActivity extends BaseTestActivity {

  BackLayerLayout backLayer;
  ImageView primaryExpandIcon;
  ImageView secondaryExpandIcon;
  ImageView primaryExtraContent;
  ImageView secondaryExtraContent;

  private static final int PRIMARY = 0;
  private static final int SECONDARY = 1;

  @Retention(RetentionPolicy.SOURCE)
  @IntDef(value = {PRIMARY, SECONDARY})
  private @interface BackLayerExperience {}

  @BackLayerExperience private int experience = PRIMARY;

  private class SwitchExperienceOnClickListener implements OnClickListener {
    @BackLayerExperience private final int buttonExperience;

    public SwitchExperienceOnClickListener(@BackLayerExperience int buttonExperience) {
      this.buttonExperience = buttonExperience;
    }

    @Override
    public void onClick(View view) {
      if (experience == buttonExperience) {
        backLayer.setExpanded(!backLayer.isExpanded());
      } else {
        experience = buttonExperience;
        primaryExtraContent.setVisibility(experience == PRIMARY ? View.VISIBLE : View.GONE);
        secondaryExtraContent.setVisibility(experience == SECONDARY ? View.VISIBLE : View.GONE);
        backLayer.setExpanded(true);
      }
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    backLayer = (BackLayerLayout) findViewById(R.id.design_backlayer_backlayer_layout);
    primaryExtraContent = (ImageView) findViewById(R.id.design_backlayer_extra_content);
    secondaryExtraContent = (ImageView) findViewById(R.id.design_backlayer_secondary_extra_content);
    primaryExpandIcon = (ImageView) findViewById(R.id.design_backlayer_primary_expand_icon);
    primaryExpandIcon.setOnClickListener(new SwitchExperienceOnClickListener(PRIMARY));
    secondaryExpandIcon = (ImageView) findViewById(R.id.design_backlayer_secondary_expand_icon);
    secondaryExpandIcon.setOnClickListener(new SwitchExperienceOnClickListener(SECONDARY));
  }
}
