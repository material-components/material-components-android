/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.material.catalog.carousel;

import io.material.catalog.R;

import java.util.Arrays;
import java.util.List;

/**
 * A data store used to populate Carousels.
 */
class CarouselData {

  private CarouselData() { }

  static List<CarouselItem> createItems() {
    return Arrays.asList(
        new CarouselItem(R.drawable.image_1, R.string.cat_carousel_image_1_content_desc),
        new CarouselItem(R.drawable.image_2, R.string.cat_carousel_image_2_content_desc),
        new CarouselItem(R.drawable.image_3, R.string.cat_carousel_image_3_content_desc),
        new CarouselItem(R.drawable.image_4, R.string.cat_carousel_image_4_content_desc),
        new CarouselItem(R.drawable.image_5, R.string.cat_carousel_image_5_content_desc),
        new CarouselItem(R.drawable.image_6, R.string.cat_carousel_image_6_content_desc),
        new CarouselItem(R.drawable.image_7, R.string.cat_carousel_image_7_content_desc),
        new CarouselItem(R.drawable.image_8, R.string.cat_carousel_image_8_content_desc),
        new CarouselItem(R.drawable.image_9, R.string.cat_carousel_image_9_content_desc),
        new CarouselItem(R.drawable.image_10, R.string.cat_carousel_image_10_content_desc)
    );
  }
}
