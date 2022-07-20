/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.google.android.material.internal;

import static com.google.common.truth.Truth.assertThat;

import android.graphics.Rect;
import android.view.View;
import android.widget.FrameLayout;
import androidx.test.core.app.ApplicationProvider;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ViewUtilsTest {

  private final int left = 1;
  private final int top = 2;
  private final int right = 3;
  private final int bottom = 4;

  @Test
  public void givenViewAndRect_whenSetBoundsFromRect_thenSetsAllBounds() {
    View view = new View(ApplicationProvider.getApplicationContext());
    Rect rect = new Rect(left, top, right, bottom);

    ViewUtils.setBoundsFromRect(view, rect);

    assertThat(view.getLeft()).isEqualTo(left);
    assertThat(view.getTop()).isEqualTo(top);
    assertThat(view.getRight()).isEqualTo(right);
    assertThat(view.getBottom()).isEqualTo(bottom);
  }

  @Test
  public void givenView_whenCalculateRectFromBounds_thenReturnsRectWithBounds() {
    View view = new View(ApplicationProvider.getApplicationContext());
    view.setLeft(left);
    view.setTop(top);
    view.setRight(right);
    view.setBottom(bottom);

    Rect rect = ViewUtils.calculateRectFromBounds(view);

    assertThat(rect.left).isEqualTo(left);
    assertThat(rect.top).isEqualTo(top);
    assertThat(rect.right).isEqualTo(right);
    assertThat(rect.bottom).isEqualTo(bottom);
  }

  @Test
  public void givenViewAndOffsetY_whenCalculateRectFromBounds_thenReturnsRectWithBounds() {
    View view = new View(ApplicationProvider.getApplicationContext());
    view.setLeft(left);
    view.setTop(top);
    view.setRight(right);
    view.setBottom(bottom);
    int offsetY = 10;

    Rect rect = ViewUtils.calculateRectFromBounds(view, offsetY);

    assertThat(rect.left).isEqualTo(left);
    assertThat(rect.top).isEqualTo(top + offsetY);
    assertThat(rect.right).isEqualTo(right);
    assertThat(rect.bottom).isEqualTo(bottom + offsetY);
  }

  @Test
  public void givenNullView_whenGetChildren_thenReturnsEmptyList() {
    List<View> children = ViewUtils.getChildren(null);

    assertThat(children).isEmpty();
  }

  @Test
  public void givenNonViewGroup_whenGetChildren_thenReturnsEmptyList() {
    View view = new View(ApplicationProvider.getApplicationContext());

    List<View> children = ViewUtils.getChildren(view);

    assertThat(children).isEmpty();
  }

  @Test
  public void givenViewGroupWithNoChildren_whenGetChildren_thenReturnsEmptyList() {
    FrameLayout viewGroup = new FrameLayout(ApplicationProvider.getApplicationContext());

    List<View> children = ViewUtils.getChildren(viewGroup);

    assertThat(children).isEmpty();
  }

  @Test
  public void givenViewGroupWithChildren_whenGetChildren_thenReturnsListOfChildren() {
    View child1 = new View(ApplicationProvider.getApplicationContext());
    View child2 = new View(ApplicationProvider.getApplicationContext());

    FrameLayout viewGroup = new FrameLayout(ApplicationProvider.getApplicationContext());
    viewGroup.addView(child1);
    viewGroup.addView(child2);

    List<View> children = ViewUtils.getChildren(viewGroup);

    assertThat(children).containsExactly(child1, child2).inOrder();
  }
}
