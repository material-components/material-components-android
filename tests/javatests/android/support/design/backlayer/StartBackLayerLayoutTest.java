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

import android.support.design.testapp.backlayer.BackLayerLayoutActivity;
import android.support.design.testapp.backlayer.R;
import android.support.design.testapp.backlayer.StartBackLayerLayoutActivity;
import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class StartBackLayerLayoutTest extends BackLayerLayoutTestBase {

  @Rule
  public ActivityTestRule<? extends BackLayerLayoutActivity> activityTestRule =
      new ActivityTestRule<StartBackLayerLayoutActivity>(StartBackLayerLayoutActivity.class);

  @Before
  public void setUp() throws Exception {
    setUp(
        activityTestRule,
        R.id.design_backlayer_coordinator_layout,
        R.id.design_backlayer_backlayer_layout,
        R.id.design_backlayer_content_layer,
        R.id.design_backlayer_primary_expand_icon,
        R.id.design_backlayer_secondary_expand_icon,
        R.id.design_backlayer_extra_content,
        R.id.design_backlayer_secondary_extra_content);
  }

  @Override
  @Test
  @SmallTest
  public void testTopBackLayerLaidOutCorrectly() {
    super.testTopBackLayerLaidOutCorrectly();
  }

  @Override
  @Test
  @SmallTest
  public void testExpandingSlidesContentLayerOut() throws InterruptedException {
    super.testExpandingSlidesContentLayerOut();
  }

  @Override
  @Test
  @SmallTest
  public void testExpandAndCollapseBackLayer() throws InterruptedException {
    super.testExpandAndCollapseBackLayer();
  }

  @Override
  @Test
  @SmallTest
  public void testBackLayerCollapsesOnContentLayerClick() throws InterruptedException {
    super.testBackLayerCollapsesOnContentLayerClick();
  }

  @Test
  @SmallTest
  @Override
  public void testBackLayerChangesFromOneExperienceToTheOther() throws InterruptedException {
    super.testBackLayerChangesFromOneExperienceToTheOther();
  }
}
