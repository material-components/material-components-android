# Copyright (C) 2015 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

LOCAL_PATH := $(call my-dir)

# Here is the final static library that apps can link against.
# The R class is automatically excluded from the generated library.
# Applications that use this library must specify LOCAL_RESOURCE_DIR
# in their makefiles to include the resources in their package.
include $(CLEAR_VARS)
LOCAL_MODULE := android-support-design
LOCAL_SDK_VERSION := current
LOCAL_SRC_FILES := $(call all-java-files-under,src)
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
LOCAL_JAVA_LIBRARIES += android-support-v4
include $(BUILD_STATIC_JAVA_LIBRARY)
