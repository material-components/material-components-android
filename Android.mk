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

# Build the resources using the current SDK version.
# We do this here because the final static library must be compiled with an older
# SDK version than the resources.  The resources library and the R class that it
# contains will not be linked into the final static library.
include $(CLEAR_VARS)
LOCAL_MODULE := android-support-design-res
LOCAL_SDK_VERSION := current
LOCAL_SRC_FILES := $(call all-java-files-under, dummy)
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res \
    frameworks/support/v7/appcompat/res \
    frameworks/support/v7/recyclerview/res
LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages android.support.v7.appcompat \
    --extra-packages android.support.v7.recyclerview
LOCAL_JAR_EXCLUDE_FILES := none
include $(BUILD_STATIC_JAVA_LIBRARY)

support_module_src_files := $(LOCAL_SRC_FILES)

# A helper sub-library to resolve cyclic dependencies between src and the platform dependent
# implementations
include $(CLEAR_VARS)
LOCAL_MODULE := android-support-design-base
LOCAL_SDK_VERSION := 7
LOCAL_SRC_FILES := $(call all-java-files-under, base)
LOCAL_JAVA_LIBRARIES := android-support-design-res \
    android-support-v4 \
    android-support-v7-appcompat \
    android-support-v7-recyclerview
include $(BUILD_STATIC_JAVA_LIBRARY)

support_module_src_files += $(LOCAL_SRC_FILES)

# A helper sub-library that makes direct use of Eclair MR1 APIs
include $(CLEAR_VARS)
LOCAL_MODULE := android-support-design-eclair-mr1
LOCAL_SDK_VERSION := 7
LOCAL_SRC_FILES := $(call all-java-files-under, eclair-mr1)
LOCAL_STATIC_JAVA_LIBRARIES := android-support-design-base
LOCAL_JAVA_LIBRARIES := android-support-design-res \
    android-support-v4 \
    android-support-v7-appcompat \
    android-support-v7-recyclerview
include $(BUILD_STATIC_JAVA_LIBRARY)

support_module_src_files += $(LOCAL_SRC_FILES)

# A helper sub-library that makes direct use of Honeycomb APIs
include $(CLEAR_VARS)
LOCAL_MODULE := android-support-design-honeycomb
LOCAL_SDK_VERSION := 11
LOCAL_SRC_FILES := $(call all-java-files-under, honeycomb)
LOCAL_STATIC_JAVA_LIBRARIES := android-support-design-eclair-mr1
LOCAL_JAVA_LIBRARIES := android-support-design-res \
    android-support-v4 \
    android-support-v7-appcompat \
    android-support-v7-recyclerview
include $(BUILD_STATIC_JAVA_LIBRARY)

support_module_src_files += $(LOCAL_SRC_FILES)

# A helper sub-library that makes direct use of Honeycomb MR1 APIs
include $(CLEAR_VARS)
LOCAL_MODULE := android-support-design-honeycomb-mr1
LOCAL_SDK_VERSION := 12
LOCAL_SRC_FILES := $(call all-java-files-under, honeycomb-mr1)
LOCAL_STATIC_JAVA_LIBRARIES := android-support-design-honeycomb
LOCAL_JAVA_LIBRARIES := android-support-design-res \
    android-support-v4 \
    android-support-v7-appcompat \
    android-support-v7-recyclerview
include $(BUILD_STATIC_JAVA_LIBRARY)

support_module_src_files += $(LOCAL_SRC_FILES)

# A helper sub-library that makes direct use of ICS APIs
include $(CLEAR_VARS)
LOCAL_MODULE := android-support-design-ics
LOCAL_SDK_VERSION := 14
LOCAL_SRC_FILES := $(call all-java-files-under, ics)
LOCAL_STATIC_JAVA_LIBRARIES := android-support-design-honeycomb-mr1
LOCAL_JAVA_LIBRARIES := android-support-design-res \
    android-support-v4 \
    android-support-v7-appcompat \
    android-support-v7-recyclerview
include $(BUILD_STATIC_JAVA_LIBRARY)

support_module_src_files += $(LOCAL_SRC_FILES)

# A helper sub-library that makes direct use of Lollipop APIs
include $(CLEAR_VARS)
LOCAL_MODULE := android-support-design-lollipop
LOCAL_SDK_VERSION := 21
LOCAL_SRC_FILES := $(call all-java-files-under, lollipop)
LOCAL_STATIC_JAVA_LIBRARIES := android-support-design-ics
LOCAL_JAVA_LIBRARIES := android-support-design-res \
    android-support-v4 \
    android-support-v7-appcompat \
    android-support-v7-recyclerview
include $(BUILD_STATIC_JAVA_LIBRARY)

support_module_src_files += $(LOCAL_SRC_FILES)

# Here is the final static library that apps can link against.
# The R class is automatically excluded from the generated library.
# Applications that use this library must specify LOCAL_RESOURCE_DIR
# in their makefiles to include the resources in their package.
include $(CLEAR_VARS)
LOCAL_MODULE := android-support-design
LOCAL_SDK_VERSION := current
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_STATIC_JAVA_LIBRARIES := android-support-design-lollipop
LOCAL_JAVA_LIBRARIES := android-support-design-res \
    android-support-v4 \
    android-support-v7-appcompat \
    android-support-v7-recyclerview
include $(BUILD_STATIC_JAVA_LIBRARY)

support_module_src_files += $(LOCAL_SRC_FILES)

# API Check
# ---------------------------------------------
support_module := $(LOCAL_MODULE)
support_module_api_dir := $(LOCAL_PATH)/api
support_module_java_libraries := $(LOCAL_JAVA_LIBRARIES)
support_module_java_packages := android.support.design.*
include $(SUPPORT_API_CHECK)
