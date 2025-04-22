/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.google.android.material.color;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.content.Context;
import android.util.Pair;
import androidx.annotation.ColorInt;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This class consists of definitions of resource data structures and helps creates a Color
 * Resources Table on the fly. It is a Java replicate of the framework's code, see
 * frameworks/base/include/ResourceTypes.h.
 */
final class ColorResourcesTableCreator {
  private ColorResourcesTableCreator() {}

  private static final short HEADER_TYPE_RES_TABLE = 0x0002;
  private static final short HEADER_TYPE_STRING_POOL = 0x0001;
  private static final short HEADER_TYPE_PACKAGE = 0x0200;
  private static final short HEADER_TYPE_TYPE = 0x0201;
  private static final short HEADER_TYPE_TYPE_SPEC = 0x0202;

  private static final byte ANDROID_PACKAGE_ID = 0x01;
  private static final byte APPLICATION_PACKAGE_ID = 0x7F;

  private static final String RESOURCE_TYPE_NAME_COLOR = "color";

  private static byte typeIdColor;

  private static final PackageInfo ANDROID_PACKAGE_INFO =
      new PackageInfo(ANDROID_PACKAGE_ID, "android");

  private static final Comparator<ColorResource> COLOR_RESOURCE_COMPARATOR =
      new Comparator<ColorResource>() {
        @Override
        public int compare(ColorResource res1, ColorResource res2) {
          return res1.entryId - res2.entryId;
        }
      };

  static byte[] create(Context context, Map<Integer, Integer> colorMapping) throws IOException {
    if (colorMapping.entrySet().isEmpty()) {
      throw new IllegalArgumentException("No color resources provided for harmonization.");
    }
    PackageInfo applicationPackageInfo =
        new PackageInfo(APPLICATION_PACKAGE_ID, context.getPackageName());

    Map<PackageInfo, List<ColorResource>> colorResourceMap = new HashMap<>();
    ColorResource colorResource = null;
    for (Map.Entry<Integer, Integer> entry : colorMapping.entrySet()) {
      colorResource =
          new ColorResource(
              entry.getKey(),
              context.getResources().getResourceEntryName(entry.getKey()),
              entry.getValue());
      if (!context
          .getResources()
          .getResourceTypeName(entry.getKey())
          .equals(RESOURCE_TYPE_NAME_COLOR)) {
        throw new IllegalArgumentException(
            "Non color resource found: name="
                + colorResource.name
                + ", typeId="
                + Integer.toHexString(colorResource.typeId & 0xFF));
      }
      PackageInfo packageInfo;
      if (colorResource.packageId == ANDROID_PACKAGE_ID) {
        packageInfo = ANDROID_PACKAGE_INFO;
      } else if (colorResource.packageId == APPLICATION_PACKAGE_ID) {
        packageInfo = applicationPackageInfo;
      } else {
        throw new IllegalArgumentException(
            "Not supported with unknown package id: " + colorResource.packageId);
      }
      if (!colorResourceMap.containsKey(packageInfo)) {
        colorResourceMap.put(packageInfo, new ArrayList<ColorResource>());
      }
      colorResourceMap.get(packageInfo).add(colorResource);
    }
    // Resource Type Ids are assigned by aapt arbitrarily, for each new type the next available
    // number is assigned and used. The type id will be the same for resources that are the same
    // type.
    typeIdColor = colorResource.typeId;
    if (typeIdColor == 0) {
      throw new IllegalArgumentException("No color resources found for harmonization.");
    }
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    new ResTable(colorResourceMap).writeTo(outputStream);
    return outputStream.toByteArray();
  }

  /**
   * A Table chunk contains: a set of Packages, where a Package is a collection of Resources and a
   * set of strings used by the Resources contained in those Packages.
   *
   * <p>The set of strings are contained in a StringPool chunk. Each Package is contained in a
   * corresponding Package chunk. The StringPool chunk immediately follows the Table chunk header.
   * The Package chunks follow the StringPool chunk.
   */
  private static class ResTable {
    private static final short HEADER_SIZE = 0x000C;

    private final ResChunkHeader header;
    private final int packageCount;
    private final StringPoolChunk stringPool;
    private final List<PackageChunk> packageChunks = new ArrayList<>();

    ResTable(Map<PackageInfo, List<ColorResource>> colorResourceMap) {
      packageCount = colorResourceMap.size();
      stringPool = new StringPoolChunk();
      for (Entry<PackageInfo, List<ColorResource>> entry : colorResourceMap.entrySet()) {
        List<ColorResource> colorResources = entry.getValue();
        Collections.sort(colorResources, COLOR_RESOURCE_COMPARATOR);
        packageChunks.add(new PackageChunk(entry.getKey(), colorResources));
      }
      header = new ResChunkHeader(HEADER_TYPE_RES_TABLE, HEADER_SIZE, getOverallSize());
    }

    void writeTo(ByteArrayOutputStream outputStream) throws IOException {
      header.writeTo(outputStream);
      outputStream.write(intToByteArray(packageCount));
      stringPool.writeTo(outputStream);
      for (PackageChunk packageChunk : packageChunks) {
        packageChunk.writeTo(outputStream);
      }
    }

    private int getOverallSize() {
      int packageChunkSize = 0;
      for (PackageChunk packageChunk : packageChunks) {
        packageChunkSize += packageChunk.getChunkSize();
      }
      return HEADER_SIZE + stringPool.getChunkSize() + packageChunkSize;
    }
  }

  /** Header that appears at the front of every data chunk in a resource. */
  private static class ResChunkHeader {
    // Type identifier for this chunk.  The meaning of this value depends
    // on the containing chunk.
    private final short type;
    // Size of the chunk header (in bytes).  Adding this value to
    // the address of the chunk allows you to find its associated data
    // (if any).
    private final short headerSize;
    // Total size of this chunk (in bytes).  This is the chunkSize plus
    // the size of any data associated with the chunk.  Adding this value
    // to the chunk allows you to completely skip its contents (including
    // any child chunks).  If this value is the same as chunkSize, there is
    // no data associated with the chunk.
    private final int chunkSize;

    ResChunkHeader(short type, short headerSize, int chunkSize) {
      this.type = type;
      this.headerSize = headerSize;
      this.chunkSize = chunkSize;
    }

    void writeTo(ByteArrayOutputStream outputStream) throws IOException {
      outputStream.write(shortToByteArray(type));
      outputStream.write(shortToByteArray(headerSize));
      outputStream.write(intToByteArray(chunkSize));
    }
  }

  /**
   * Immediately following the Table header is a StringPool chunk. It consists of StringPool chunk
   * header and StringPool chunk body.
   */
  private static class StringPoolChunk {
    private static final short HEADER_SIZE = 0x001C;
    private static final int FLAG_UTF8 = 0x00000100;
    private static final int STYLED_SPAN_LIST_END = 0xFFFFFFFF;

    private final ResChunkHeader header;
    private final int stringCount;
    private final int styledSpanCount;
    private final int stringsStart;
    private final int styledSpansStart;
    private final List<Integer> stringIndex = new ArrayList<>();
    private final List<Integer> styledSpanIndex = new ArrayList<>();
    private final List<byte[]> strings = new ArrayList<>();
    private final List<List<StringStyledSpan>> styledSpans = new ArrayList<>();

    private final boolean utf8Encode;
    private final int stringsPaddingSize;
    private final int chunkSize;

    StringPoolChunk(String... rawStrings) {
      this(false, rawStrings);
    }

    StringPoolChunk(boolean utf8, String... rawStrings) {
      utf8Encode = utf8;
      int stringOffset = 0;
      for (String string : rawStrings) {
        Pair<byte[], List<StringStyledSpan>> processedString = processString(string);
        stringIndex.add(stringOffset);
        stringOffset += processedString.first.length;
        strings.add(processedString.first);
        styledSpans.add(processedString.second);
      }
      int styledSpanOffset = 0;
      for (List<StringStyledSpan> styledSpanList : styledSpans) {
        for (StringStyledSpan styledSpan : styledSpanList) {
          stringIndex.add(stringOffset);
          stringOffset += styledSpan.styleString.length;
          strings.add(styledSpan.styleString);
        }
        styledSpanIndex.add(styledSpanOffset);
        // Each span occupies 3 int32, plus one end mark per chunk
        styledSpanOffset += styledSpanList.size() * 12 + 4;
      }

      // All chunk size needs to be a multiple of 4
      int stringOffsetResidue = stringOffset % 4;
      stringsPaddingSize = stringOffsetResidue == 0 ? 0 : 4 - stringOffsetResidue;
      stringCount = strings.size();
      styledSpanCount = strings.size() - rawStrings.length;

      boolean hasStyledSpans = strings.size() - rawStrings.length > 0;
      if (!hasStyledSpans) {
        // No styled spans, clear relevant data
        styledSpanIndex.clear();
        styledSpans.clear();
      }

      // Int32 per index
      stringsStart =
          HEADER_SIZE
              + stringCount * 4 // String index
              + styledSpanIndex.size() * 4; // Styled span index
      int stringsSize = stringOffset + stringsPaddingSize;
      styledSpansStart = hasStyledSpans ? stringsStart + stringsSize : 0;
      chunkSize = stringsStart + stringsSize + (hasStyledSpans ? styledSpanOffset : 0);
      header = new ResChunkHeader(HEADER_TYPE_STRING_POOL, HEADER_SIZE, chunkSize);
    }

    void writeTo(ByteArrayOutputStream outputStream) throws IOException {
      header.writeTo(outputStream);
      outputStream.write(intToByteArray(stringCount));
      outputStream.write(intToByteArray(styledSpanCount));
      outputStream.write(intToByteArray(utf8Encode ? FLAG_UTF8 : 0));
      outputStream.write(intToByteArray(stringsStart));
      outputStream.write(intToByteArray(styledSpansStart));
      for (Integer index : stringIndex) {
        outputStream.write(intToByteArray(index));
      }
      for (Integer index : styledSpanIndex) {
        outputStream.write(intToByteArray(index));
      }
      for (byte[] string : strings) {
        outputStream.write(string);
      }
      if (stringsPaddingSize > 0) {
        outputStream.write(new byte[stringsPaddingSize]);
      }
      for (List<StringStyledSpan> styledSpanList : styledSpans) {
        for (StringStyledSpan styledSpan : styledSpanList) {
          styledSpan.writeTo(outputStream);
        }
        outputStream.write(intToByteArray(STYLED_SPAN_LIST_END));
      }
    }

    int getChunkSize() {
      return chunkSize;
    }

    private Pair<byte[], List<StringStyledSpan>> processString(String rawString) {
      // Ignore styled spans, won't be used in our scenario.
      return new Pair<>(
          utf8Encode ? stringToByteArrayUtf8(rawString) : stringToByteArray(rawString),
          Collections.<StringStyledSpan>emptyList());
    }
  }

  /** This structure defines a span of style information associated with a string in the pool. */
  private static class StringStyledSpan {

    private byte[] styleString;
    private int nameReference;
    private int firstCharacterIndex;
    private int lastCharacterIndex;

    void writeTo(ByteArrayOutputStream outputStream) throws IOException {
      outputStream.write(intToByteArray(nameReference));
      outputStream.write(intToByteArray(firstCharacterIndex));
      outputStream.write(intToByteArray(lastCharacterIndex));
    }
  }

  /**
   * A Package chunk contains a set of Resources and a set of strings associated with those
   * Resources. The Resources are grouped by type. For each of set of Resources of a given type that
   * the Package chunk contains there is a TypeSpec chunk and one or more Type chunks.
   *
   * <p>The strings are stored in two StringPool chunks: the typeStrings StringPool chunk which
   * contains the names of the types of the Resources defined in the Package; the keyStrings
   * StringPool chunk which contains the names (keys) of the Resources defined in the Package.
   */
  private static class PackageChunk {
    private static final short HEADER_SIZE = 0x0120;
    private static final int PACKAGE_NAME_MAX_LENGTH = 128;

    private final ResChunkHeader header;
    private final PackageInfo packageInfo;
    private final StringPoolChunk typeStrings;
    private final StringPoolChunk keyStrings;
    private final TypeSpecChunk typeSpecChunk;

    PackageChunk(PackageInfo packageInfo, List<ColorResource> colorResources) {
      this.packageInfo = packageInfo;

      typeStrings = new StringPoolChunk(false, generateTypeStrings(colorResources));
      keyStrings = new StringPoolChunk(true, generateKeyStrings(colorResources));
      typeSpecChunk = new TypeSpecChunk(colorResources);

      header = new ResChunkHeader(HEADER_TYPE_PACKAGE, HEADER_SIZE, getChunkSize());
    }

    private String[] generateTypeStrings(List<ColorResource> colorResources) {
      if (!colorResources.isEmpty()) {
        byte colorTypeId = colorResources.get(0).typeId;
        String[] types = new String[colorTypeId];

        // Placeholder String type, since only XML color resources will be replaced at runtime.
        for (int i = 0; i < colorTypeId - 1; i++) {
          types[i] = "?" + (i + 1);
        }

        types[colorTypeId - 1] = "color";

        return types;
      } else {
        return new String[0];
      }
    }

    private String[] generateKeyStrings(List<ColorResource> colorResources) {
      String[] keys = new String[colorResources.size()];
      for (int i = 0; i < colorResources.size(); i++) {
        keys[i] = colorResources.get(i).name;
      }
      return keys;
    }

    void writeTo(ByteArrayOutputStream outputStream) throws IOException {
      header.writeTo(outputStream);
      outputStream.write(intToByteArray(packageInfo.id));
      char[] packageName = packageInfo.name.toCharArray();
      for (int i = 0; i < PACKAGE_NAME_MAX_LENGTH; i++) {
        if (i < packageName.length) {
          outputStream.write(charToByteArray(packageName[i]));
        } else {
          outputStream.write(charToByteArray((char) 0));
        }
      }
      outputStream.write(intToByteArray(HEADER_SIZE)); // Type strings offset
      outputStream.write(intToByteArray(0)); // Last public type
      outputStream.write(
          intToByteArray(HEADER_SIZE + typeStrings.getChunkSize())); // Key strings offset
      outputStream.write(intToByteArray(0)); // Last public key
      outputStream.write(intToByteArray(0)); // Note
      typeStrings.writeTo(outputStream);
      keyStrings.writeTo(outputStream);
      typeSpecChunk.writeTo(outputStream);
    }

    int getChunkSize() {
      return HEADER_SIZE
          + typeStrings.getChunkSize()
          + keyStrings.getChunkSize()
          + typeSpecChunk.getChunkSizeWithTypeChunk();
    }
  }

  /**
   * A specification of the resources defined by a particular type.
   *
   * <p>There should be one of these chunks for each resource type.
   *
   * <p>This structure is followed by an array of integers providing the set of configuration change
   * flags (ResTable_config::CONFIG_*) that have multiple resources for that configuration. In
   * addition, the high bit is set if that resource has been made public.
   */
  private static class TypeSpecChunk {
    private static final short HEADER_SIZE = 0x0010;
    private static final int SPEC_PUBLIC = 0x40000000;

    private final ResChunkHeader header;
    private final int entryCount;
    private final int[] entryFlags;
    private final TypeChunk typeChunk;

    TypeSpecChunk(List<ColorResource> colorResources) {
      entryCount = colorResources.get(colorResources.size() - 1).entryId + 1;
      Set<Short> validEntryIds = new HashSet<>();
      for (ColorResource colorResource : colorResources) {
        validEntryIds.add(colorResource.entryId);
      }
      entryFlags = new int[entryCount];
      // All color resources in the table are marked as PUBLIC.
      for (short entryId = 0; entryId < entryCount; entryId++) {
        if (validEntryIds.contains(entryId)) {
          entryFlags[entryId] = SPEC_PUBLIC;
        }
      }

      header = new ResChunkHeader(HEADER_TYPE_TYPE_SPEC, HEADER_SIZE, getChunkSize());

      typeChunk = new TypeChunk(colorResources, validEntryIds, entryCount);
    }

    void writeTo(ByteArrayOutputStream outputStream) throws IOException {
      header.writeTo(outputStream);
      outputStream.write(new byte[] {typeIdColor, 0x00, 0x00, 0x00});
      outputStream.write(intToByteArray(entryCount));
      for (int entryFlag : entryFlags) {
        outputStream.write(intToByteArray(entryFlag));
      }
      typeChunk.writeTo(outputStream);
    }

    int getChunkSizeWithTypeChunk() {
      return getChunkSize() + typeChunk.getChunkSize();
    }

    private int getChunkSize() {
      return HEADER_SIZE + entryCount * 4; // Int32 per entry flag
    }
  }

  /**
   * A collection of resource entries for a particular resource data type.
   *
   * <p>There may be multiple of these chunks for a particular resource type, supply different
   * configuration variations for the resource values of that type.
   */
  private static class TypeChunk {
    private static final int OFFSET_NO_ENTRY = 0xFFFFFFFF;

    private static final short HEADER_SIZE = 0x0054;
    private static final byte CONFIG_SIZE = 0x40;

    private final ResChunkHeader header;
    private final int entryCount;
    private final byte[] config = new byte[CONFIG_SIZE];
    private final int[] offsetTable;
    private final ResEntry[] resEntries;

    TypeChunk(List<ColorResource> colorResources, Set<Short> entryIds, int entryCount) {
      this.entryCount = entryCount;
      this.config[0] = CONFIG_SIZE;

      this.resEntries = new ResEntry[colorResources.size()];

      for (int index = 0; index < colorResources.size(); index++) {
        ColorResource colorResource = colorResources.get(index);
        this.resEntries[index] = new ResEntry(index, colorResource.value);
      }

      this.offsetTable = new int[entryCount];
      int currentOffset = 0;
      for (short entryId = 0; entryId < entryCount; entryId++) {
        if (entryIds.contains(entryId)) {
          this.offsetTable[entryId] = currentOffset;
          currentOffset += ResEntry.SIZE;
        } else {
          this.offsetTable[entryId] = OFFSET_NO_ENTRY;
        }
      }

      this.header = new ResChunkHeader(HEADER_TYPE_TYPE, HEADER_SIZE, getChunkSize());
    }

    void writeTo(ByteArrayOutputStream outputStream) throws IOException {
      header.writeTo(outputStream);
      outputStream.write(new byte[] {typeIdColor, 0x00, 0x00, 0x00});
      outputStream.write(intToByteArray(entryCount));
      outputStream.write(intToByteArray(getEntryStart()));
      outputStream.write(config);
      for (int offset : offsetTable) {
        outputStream.write(intToByteArray(offset));
      }
      for (ResEntry entry : resEntries) {
        entry.writeTo(outputStream);
      }
    }

    int getChunkSize() {
      return getEntryStart() + resEntries.length * ResEntry.SIZE;
    }

    private int getEntryStart() {
      return HEADER_SIZE + getOffsetTableSize();
    }

    private int getOffsetTableSize() {
      return offsetTable.length * 4; // One int32 per entry
    }
  }

  /**
   * This is the beginning of information about an entry in the resource table. It holds the
   * reference to the name of this entry, and is immediately followed by one of: A Res_value
   * structure, if FLAG_COMPLEX is -not- set. An array of ResTable_map structures, if FLAG_COMPLEX
   * is set. These supply a set of name/value mappings of data.
   */
  private static class ResEntry {
    private static final short ENTRY_SIZE = 8;
    private static final short FLAG_PUBLIC = 0x0002; // Always set to "Public"
    private static final short VALUE_SIZE = 8;
    private static final byte DATA_TYPE_AARRGGBB = 0x1C; // Type #aarrggbb

    private static final int SIZE = ENTRY_SIZE + VALUE_SIZE;

    private final int keyStringIndex;
    private final int data;

    ResEntry(int keyStringIndex, @ColorInt int data) {
      this.keyStringIndex = keyStringIndex;
      this.data = data;
    }

    void writeTo(ByteArrayOutputStream outputStream) throws IOException {
      outputStream.write(shortToByteArray(ENTRY_SIZE));
      outputStream.write(shortToByteArray(FLAG_PUBLIC));
      outputStream.write(intToByteArray(keyStringIndex));
      outputStream.write(shortToByteArray(VALUE_SIZE));
      outputStream.write(new byte[] {0x00, DATA_TYPE_AARRGGBB});
      outputStream.write(intToByteArray(data));
    }
  }

  /** The basic info of a package, which consists of the id and the name of the package. */
  static class PackageInfo {
    private final int id;
    private final String name;

    PackageInfo(int id, String name) {
      this.id = id;
      this.name = name;
    }
  }

  /**
   * A Color Resource object, which consists of the id of the package that the resource belongs to;
   * the name and value of the color resource.
   */
  static class ColorResource {
    private final byte packageId;
    private final byte typeId;
    private final short entryId;

    private final String name;

    @ColorInt private final int value;

    ColorResource(int id, String name, int value) {
      this.name = name;
      this.value = value;

      this.entryId = (short) (id & 0xFFFF);
      this.typeId = (byte) ((id >> 16) & 0xFF);
      this.packageId = (byte) ((id >> 24) & 0xFF);
    }
  }

  private static byte[] shortToByteArray(short value) {
    return new byte[] {
      (byte) (value & 0xFF), (byte) ((value >> 8) & 0xFF),
    };
  }

  private static byte[] charToByteArray(char value) {
    return new byte[] {
      (byte) (value & 0xFF), (byte) ((value >> 8) & 0xFF),
    };
  }

  private static byte[] intToByteArray(int value) {
    return new byte[] {
      (byte) (value & 0xFF),
      (byte) ((value >> 8) & 0xFF),
      (byte) ((value >> 16) & 0xFF),
      (byte) ((value >> 24) & 0xFF),
    };
  }

  private static byte[] stringToByteArray(String value) {
    char[] chars = value.toCharArray();
    byte[] bytes = new byte[chars.length * 2 + 4];
    byte[] lengthBytes = shortToByteArray((short) chars.length);
    bytes[0] = lengthBytes[0];
    bytes[1] = lengthBytes[1];
    for (int i = 0; i < chars.length; i++) {
      byte[] charBytes = charToByteArray(chars[i]);
      bytes[i * 2 + 2] = charBytes[0];
      bytes[i * 2 + 3] = charBytes[1];
    }
    bytes[bytes.length - 2] = 0;
    bytes[bytes.length - 1] = 0; // EOS
    return bytes;
  }

  private static byte[] stringToByteArrayUtf8(String str) {
    byte[] strBytes = str.getBytes(UTF_8);
    byte[] strLengthBytes = encodeLengthUtf8((short) str.length());
    byte[] encStrLengthBytes = encodeLengthUtf8((short) strBytes.length);

    return concat(
        strLengthBytes,
        encStrLengthBytes,
        strBytes,
        new byte[] { 0 }  // EOS
    );
  }

  private static byte[] encodeLengthUtf8(short length) {
    return length > 0x7F
        ? new byte[] { (byte) (((length >> 8) & 0x7F) | 0x80), (byte) (length & 0xFF) }
        : new byte[] { (byte) (length & 0xFF) };
  }

  private static byte[] concat(byte[]... arrays) {
    int length = 0;
    for (byte[] array : arrays) {
      length += array.length;
    }
    byte[] result = new byte[length];
    int pos = 0;
    for (byte[] array : arrays) {
      System.arraycopy(array, 0, result, pos, array.length);
      pos += array.length;
    }
    return result;
  }
}
