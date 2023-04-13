package com.google.android.material.internal;

import org.junit.Assert;
import org.junit.Test;

public class ManufacturerUtilsTest {

  @Test
  public void testIsMeizuDevice_shouldNotThrowNullPointerException() {
    Assert.assertFalse(ManufacturerUtils.isMeizuDevice());
  }

  @Test
  public void testIsLgeDevice_shouldNotThrowNullPointerException() {
    Assert.assertFalse(ManufacturerUtils.isLGEDevice());
  }

  @Test
  public void testIsSamsungDevice_shouldNotThrowNullPointerException() {
    Assert.assertFalse(ManufacturerUtils.isSamsungDevice());
  }
}
