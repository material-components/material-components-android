/*
 * Copyright (C) 2017 The Android Open Source Project
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

package io.material.demo.shrine.products;

import static com.google.common.truth.Truth.assertThat;

import androidx.recyclerview.widget.RecyclerView;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

/** Unit tests for {@link ProductListActivity}. */
@RunWith(RobolectricTestRunner.class)
public class ProductListActivityTest {

  private ProductListActivity productListActivity;
  private RecyclerView productsRecyclerView;

  @Before
  public void setUp() {
    productListActivity = Robolectric.setupActivity(ProductListActivity.class);
    productsRecyclerView = productListActivity.findViewById(R.id.ProductGrid);
  }

  @Ignore
  @Test
  public void testProductListIsNotNull() {
    assertThat(productsRecyclerView).isNotNull();
  }

  @Ignore
  @Test
  public void testProductListHasProducts() {
    int productCount = productsRecyclerView.getChildCount();
    assertThat(productCount).isAtLeast(1);
  }
}
