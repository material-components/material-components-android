/*
 * Copyright 2025 The Android Open Source Project
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
package io.material.catalog.listitem;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.Gravity;
import com.google.android.material.listitem.SwipeableListItem;

/** A sample data class used to represent List Items * */
class CustomListItemData implements Parcelable {
  String text;
  boolean checked;
  int indexInSection;
  int sectionCount;
  String subheading;
  boolean expanded;
  int swipeState;
  int swipeGravity;

  public CustomListItemData(String text, int indexInSection, int sectionCount) {
    this.text = text;
    this.indexInSection = indexInSection;
    this.sectionCount = sectionCount;
    this.expanded = false;
    this.swipeState = SwipeableListItem.STATE_CLOSED;
    this.swipeGravity = Gravity.END;
  }

  public CustomListItemData(String subheading) {
    this.subheading = subheading;
  }

  protected CustomListItemData(Parcel in) {
    text = in.readString();
    checked = in.readByte() != 0;
    indexInSection = in.readInt();
    sectionCount = in.readInt();
    subheading = in.readString();
    expanded = in.readByte() != 0;
    swipeState = in.readInt();
    swipeGravity = in.readInt();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(text);
    dest.writeByte((byte) (checked ? 1 : 0));
    dest.writeInt(indexInSection);
    dest.writeInt(sectionCount);
    dest.writeString(subheading);
    dest.writeByte((byte) (expanded ? 1 : 0));
    dest.writeInt(swipeState);
    dest.writeInt(swipeGravity);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<CustomListItemData> CREATOR =
      new Creator<CustomListItemData>() {
        @Override
        public CustomListItemData createFromParcel(Parcel in) {
          return new CustomListItemData(in);
        }

        @Override
        public CustomListItemData[] newArray(int size) {
          return new CustomListItemData[size];
        }
      };
}
