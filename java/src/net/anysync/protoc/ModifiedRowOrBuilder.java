// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: rescan.proto

package net.anysync.protoc;

public interface ModifiedRowOrBuilder extends
    // @@protoc_insertion_point(interface_extends:utils.ModifiedRow)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int32 OperationMode = 1;</code>
   */
  int getOperationMode();

  /**
   * <code>bytes Row = 2;</code>
   */
  com.google.protobuf.ByteString getRow();

  /**
   * <code>string FileName = 3;</code>
   */
  java.lang.String getFileName();
  /**
   * <code>string FileName = 3;</code>
   */
  com.google.protobuf.ByteString
      getFileNameBytes();

  /**
   * <code>int32 Conflict = 4;</code>
   */
  int getConflict();

  /**
   * <code>bool SendBackToClient = 5;</code>
   */
  boolean getSendBackToClient();

  /**
   * <code>uint32 PreviousIndex = 6;</code>
   */
  int getPreviousIndex();

  /**
   * <code>string OldFolderHashAndIndex = 7;</code>
   */
  java.lang.String getOldFolderHashAndIndex();
  /**
   * <code>string OldFolderHashAndIndex = 7;</code>
   */
  com.google.protobuf.ByteString
      getOldFolderHashAndIndexBytes();

  /**
   * <code>map&lt;string, bytes&gt; Attribs = 8;</code>
   */
  int getAttribsCount();
  /**
   * <code>map&lt;string, bytes&gt; Attribs = 8;</code>
   */
  boolean containsAttribs(
      java.lang.String key);
  /**
   * Use {@link #getAttribsMap()} instead.
   */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, com.google.protobuf.ByteString>
  getAttribs();
  /**
   * <code>map&lt;string, bytes&gt; Attribs = 8;</code>
   */
  java.util.Map<java.lang.String, com.google.protobuf.ByteString>
  getAttribsMap();
  /**
   * <code>map&lt;string, bytes&gt; Attribs = 8;</code>
   */

  com.google.protobuf.ByteString getAttribsOrDefault(
      java.lang.String key,
      com.google.protobuf.ByteString defaultValue);
  /**
   * <code>map&lt;string, bytes&gt; Attribs = 8;</code>
   */

  com.google.protobuf.ByteString getAttribsOrThrow(
      java.lang.String key);

  /**
   * <code>bool IsDir = 9;</code>
   */
  boolean getIsDir();
}