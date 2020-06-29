// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: dex.proto

package com.tangem.blockchain.blockchains.binance.proto;

/**
 * <pre>
 * please note the field name is the JSON name.
 * </pre>
 *
 * Protobuf type {@code transaction.StdTx}
 */
public  final class StdTx extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:transaction.StdTx)
    StdTxOrBuilder {
private static final long serialVersionUID = 0L;
  // Use StdTx.newBuilder() to construct.
  private StdTx(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private StdTx() {
    msgs_ = java.util.Collections.emptyList();
    signatures_ = java.util.Collections.emptyList();
    memo_ = "";
    data_ = com.google.protobuf.ByteString.EMPTY;
  }

  @Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private StdTx(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new NullPointerException();
    }
    int mutable_bitField0_ = 0;
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          case 10: {
            if (!((mutable_bitField0_ & 0x00000001) != 0)) {
              msgs_ = new java.util.ArrayList<com.google.protobuf.ByteString>();
              mutable_bitField0_ |= 0x00000001;
            }
            msgs_.add(input.readBytes());
            break;
          }
          case 18: {
            if (!((mutable_bitField0_ & 0x00000002) != 0)) {
              signatures_ = new java.util.ArrayList<com.google.protobuf.ByteString>();
              mutable_bitField0_ |= 0x00000002;
            }
            signatures_.add(input.readBytes());
            break;
          }
          case 26: {
            String s = input.readStringRequireUtf8();

            memo_ = s;
            break;
          }
          case 32: {

            source_ = input.readInt64();
            break;
          }
          case 42: {

            data_ = input.readBytes();
            break;
          }
          default: {
            if (!parseUnknownField(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      if (((mutable_bitField0_ & 0x00000001) != 0)) {
        msgs_ = java.util.Collections.unmodifiableList(msgs_); // C
      }
      if (((mutable_bitField0_ & 0x00000002) != 0)) {
        signatures_ = java.util.Collections.unmodifiableList(signatures_); // C
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return Transaction.internal_static_transaction_StdTx_descriptor;
  }

  @Override
  protected FieldAccessorTable
      internalGetFieldAccessorTable() {
    return Transaction.internal_static_transaction_StdTx_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            StdTx.class, Builder.class);
  }

  private int bitField0_;
  public static final int MSGS_FIELD_NUMBER = 1;
  private java.util.List<com.google.protobuf.ByteString> msgs_;
  /**
   * <pre>
   *    uint64 SIZE-OF-ENCODED // varint encoded length of the structure after encoding
   *    0xF0625DEE   // hardcoded, object type prefix in 4 bytes
   * </pre>
   *
   * <code>repeated bytes msgs = 1;</code>
   */
  public java.util.List<com.google.protobuf.ByteString>
      getMsgsList() {
    return msgs_;
  }
  /**
   * <pre>
   *    uint64 SIZE-OF-ENCODED // varint encoded length of the structure after encoding
   *    0xF0625DEE   // hardcoded, object type prefix in 4 bytes
   * </pre>
   *
   * <code>repeated bytes msgs = 1;</code>
   */
  public int getMsgsCount() {
    return msgs_.size();
  }
  /**
   * <pre>
   *    uint64 SIZE-OF-ENCODED // varint encoded length of the structure after encoding
   *    0xF0625DEE   // hardcoded, object type prefix in 4 bytes
   * </pre>
   *
   * <code>repeated bytes msgs = 1;</code>
   */
  public com.google.protobuf.ByteString getMsgs(int index) {
    return msgs_.get(index);
  }

  public static final int SIGNATURES_FIELD_NUMBER = 2;
  private java.util.List<com.google.protobuf.ByteString> signatures_;
  /**
   * <pre>
   * array of size 1, containing the standard signature structure of the transaction sender
   * </pre>
   *
   * <code>repeated bytes signatures = 2;</code>
   */
  public java.util.List<com.google.protobuf.ByteString>
      getSignaturesList() {
    return signatures_;
  }
  /**
   * <pre>
   * array of size 1, containing the standard signature structure of the transaction sender
   * </pre>
   *
   * <code>repeated bytes signatures = 2;</code>
   */
  public int getSignaturesCount() {
    return signatures_.size();
  }
  /**
   * <pre>
   * array of size 1, containing the standard signature structure of the transaction sender
   * </pre>
   *
   * <code>repeated bytes signatures = 2;</code>
   */
  public com.google.protobuf.ByteString getSignatures(int index) {
    return signatures_.get(index);
  }

  public static final int MEMO_FIELD_NUMBER = 3;
  private volatile Object memo_;
  /**
   * <pre>
   * a short sentence of remark for the transaction. Please only `Transfer` transaction allows 'memo' input, and other transactions with non-empty `Memo` would be rejected.
   * </pre>
   *
   * <code>string memo = 3;</code>
   */
  public String getMemo() {
    Object ref = memo_;
    if (ref instanceof String) {
      return (String) ref;
    } else {
      com.google.protobuf.ByteString bs =
          (com.google.protobuf.ByteString) ref;
      String s = bs.toStringUtf8();
      memo_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * a short sentence of remark for the transaction. Please only `Transfer` transaction allows 'memo' input, and other transactions with non-empty `Memo` would be rejected.
   * </pre>
   *
   * <code>string memo = 3;</code>
   */
  public com.google.protobuf.ByteString
      getMemoBytes() {
    Object ref = memo_;
    if (ref instanceof String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8(
              (String) ref);
      memo_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int SOURCE_FIELD_NUMBER = 4;
  private long source_;
  /**
   * <pre>
   * an identifier for tools triggerring this transaction, set to zero if unwilling to disclose.
   * </pre>
   *
   * <code>int64 source = 4;</code>
   */
  public long getSource() {
    return source_;
  }

  public static final int DATA_FIELD_NUMBER = 5;
  private com.google.protobuf.ByteString data_;
  /**
   * <pre>
   *byte array, reserved for future use
   * </pre>
   *
   * <code>bytes data = 5;</code>
   */
  public com.google.protobuf.ByteString getData() {
    return data_;
  }

  private byte memoizedIsInitialized = -1;
  @Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    for (int i = 0; i < msgs_.size(); i++) {
      output.writeBytes(1, msgs_.get(i));
    }
    for (int i = 0; i < signatures_.size(); i++) {
      output.writeBytes(2, signatures_.get(i));
    }
    if (!getMemoBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, memo_);
    }
    if (source_ != 0L) {
      output.writeInt64(4, source_);
    }
    if (!data_.isEmpty()) {
      output.writeBytes(5, data_);
    }
    unknownFields.writeTo(output);
  }

  @Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    {
      int dataSize = 0;
      for (int i = 0; i < msgs_.size(); i++) {
        dataSize += com.google.protobuf.CodedOutputStream
          .computeBytesSizeNoTag(msgs_.get(i));
      }
      size += dataSize;
      size += 1 * getMsgsList().size();
    }
    {
      int dataSize = 0;
      for (int i = 0; i < signatures_.size(); i++) {
        dataSize += com.google.protobuf.CodedOutputStream
          .computeBytesSizeNoTag(signatures_.get(i));
      }
      size += dataSize;
      size += 1 * getSignaturesList().size();
    }
    if (!getMemoBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, memo_);
    }
    if (source_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt64Size(4, source_);
    }
    if (!data_.isEmpty()) {
      size += com.google.protobuf.CodedOutputStream
        .computeBytesSize(5, data_);
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof StdTx)) {
      return super.equals(obj);
    }
    StdTx other = (StdTx) obj;

    if (!getMsgsList()
        .equals(other.getMsgsList())) return false;
    if (!getSignaturesList()
        .equals(other.getSignaturesList())) return false;
    if (!getMemo()
        .equals(other.getMemo())) return false;
    if (getSource()
        != other.getSource()) return false;
    if (!getData()
        .equals(other.getData())) return false;
    if (!unknownFields.equals(other.unknownFields)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    if (getMsgsCount() > 0) {
      hash = (37 * hash) + MSGS_FIELD_NUMBER;
      hash = (53 * hash) + getMsgsList().hashCode();
    }
    if (getSignaturesCount() > 0) {
      hash = (37 * hash) + SIGNATURES_FIELD_NUMBER;
      hash = (53 * hash) + getSignaturesList().hashCode();
    }
    hash = (37 * hash) + MEMO_FIELD_NUMBER;
    hash = (53 * hash) + getMemo().hashCode();
    hash = (37 * hash) + SOURCE_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getSource());
    hash = (37 * hash) + DATA_FIELD_NUMBER;
    hash = (53 * hash) + getData().hashCode();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static StdTx parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static StdTx parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static StdTx parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static StdTx parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static StdTx parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static StdTx parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static StdTx parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static StdTx parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static StdTx parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static StdTx parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static StdTx parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static StdTx parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(StdTx prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @Override
  protected Builder newBuilderForType(
      BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * <pre>
   * please note the field name is the JSON name.
   * </pre>
   *
   * Protobuf type {@code transaction.StdTx}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:transaction.StdTx)
      StdTxOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return Transaction.internal_static_transaction_StdTx_descriptor;
    }

    @Override
    protected FieldAccessorTable
        internalGetFieldAccessorTable() {
      return Transaction.internal_static_transaction_StdTx_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              StdTx.class, Builder.class);
    }

    // Construct using com.tangem.blockchain.blockchains.binance.proto.StdTx.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    @Override
    public Builder clear() {
      super.clear();
      msgs_ = java.util.Collections.emptyList();
      bitField0_ = (bitField0_ & ~0x00000001);
      signatures_ = java.util.Collections.emptyList();
      bitField0_ = (bitField0_ & ~0x00000002);
      memo_ = "";

      source_ = 0L;

      data_ = com.google.protobuf.ByteString.EMPTY;

      return this;
    }

    @Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return Transaction.internal_static_transaction_StdTx_descriptor;
    }

    @Override
    public StdTx getDefaultInstanceForType() {
      return StdTx.getDefaultInstance();
    }

    @Override
    public StdTx build() {
      StdTx result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @Override
    public StdTx buildPartial() {
      StdTx result = new StdTx(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((bitField0_ & 0x00000001) != 0)) {
        msgs_ = java.util.Collections.unmodifiableList(msgs_);
        bitField0_ = (bitField0_ & ~0x00000001);
      }
      result.msgs_ = msgs_;
      if (((bitField0_ & 0x00000002) != 0)) {
        signatures_ = java.util.Collections.unmodifiableList(signatures_);
        bitField0_ = (bitField0_ & ~0x00000002);
      }
      result.signatures_ = signatures_;
      result.memo_ = memo_;
      result.source_ = source_;
      result.data_ = data_;
      result.bitField0_ = to_bitField0_;
      onBuilt();
      return result;
    }

    @Override
    public Builder clone() {
      return super.clone();
    }
    @Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return super.setField(field, value);
    }
    @Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return super.addRepeatedField(field, value);
    }
    @Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof StdTx) {
        return mergeFrom((StdTx)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(StdTx other) {
      if (other == StdTx.getDefaultInstance()) return this;
      if (!other.msgs_.isEmpty()) {
        if (msgs_.isEmpty()) {
          msgs_ = other.msgs_;
          bitField0_ = (bitField0_ & ~0x00000001);
        } else {
          ensureMsgsIsMutable();
          msgs_.addAll(other.msgs_);
        }
        onChanged();
      }
      if (!other.signatures_.isEmpty()) {
        if (signatures_.isEmpty()) {
          signatures_ = other.signatures_;
          bitField0_ = (bitField0_ & ~0x00000002);
        } else {
          ensureSignaturesIsMutable();
          signatures_.addAll(other.signatures_);
        }
        onChanged();
      }
      if (!other.getMemo().isEmpty()) {
        memo_ = other.memo_;
        onChanged();
      }
      if (other.getSource() != 0L) {
        setSource(other.getSource());
      }
      if (other.getData() != com.google.protobuf.ByteString.EMPTY) {
        setData(other.getData());
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    @Override
    public final boolean isInitialized() {
      return true;
    }

    @Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      StdTx parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (StdTx) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private java.util.List<com.google.protobuf.ByteString> msgs_ = java.util.Collections.emptyList();
    private void ensureMsgsIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        msgs_ = new java.util.ArrayList<com.google.protobuf.ByteString>(msgs_);
        bitField0_ |= 0x00000001;
       }
    }
    /**
     * <pre>
     *    uint64 SIZE-OF-ENCODED // varint encoded length of the structure after encoding
     *    0xF0625DEE   // hardcoded, object type prefix in 4 bytes
     * </pre>
     *
     * <code>repeated bytes msgs = 1;</code>
     */
    public java.util.List<com.google.protobuf.ByteString>
        getMsgsList() {
      return ((bitField0_ & 0x00000001) != 0) ?
               java.util.Collections.unmodifiableList(msgs_) : msgs_;
    }
    /**
     * <pre>
     *    uint64 SIZE-OF-ENCODED // varint encoded length of the structure after encoding
     *    0xF0625DEE   // hardcoded, object type prefix in 4 bytes
     * </pre>
     *
     * <code>repeated bytes msgs = 1;</code>
     */
    public int getMsgsCount() {
      return msgs_.size();
    }
    /**
     * <pre>
     *    uint64 SIZE-OF-ENCODED // varint encoded length of the structure after encoding
     *    0xF0625DEE   // hardcoded, object type prefix in 4 bytes
     * </pre>
     *
     * <code>repeated bytes msgs = 1;</code>
     */
    public com.google.protobuf.ByteString getMsgs(int index) {
      return msgs_.get(index);
    }
    /**
     * <pre>
     *    uint64 SIZE-OF-ENCODED // varint encoded length of the structure after encoding
     *    0xF0625DEE   // hardcoded, object type prefix in 4 bytes
     * </pre>
     *
     * <code>repeated bytes msgs = 1;</code>
     */
    public Builder setMsgs(
        int index, com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  ensureMsgsIsMutable();
      msgs_.set(index, value);
      onChanged();
      return this;
    }
    /**
     * <pre>
     *    uint64 SIZE-OF-ENCODED // varint encoded length of the structure after encoding
     *    0xF0625DEE   // hardcoded, object type prefix in 4 bytes
     * </pre>
     *
     * <code>repeated bytes msgs = 1;</code>
     */
    public Builder addMsgs(com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  ensureMsgsIsMutable();
      msgs_.add(value);
      onChanged();
      return this;
    }
    /**
     * <pre>
     *    uint64 SIZE-OF-ENCODED // varint encoded length of the structure after encoding
     *    0xF0625DEE   // hardcoded, object type prefix in 4 bytes
     * </pre>
     *
     * <code>repeated bytes msgs = 1;</code>
     */
    public Builder addAllMsgs(
        Iterable<? extends com.google.protobuf.ByteString> values) {
      ensureMsgsIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(
          values, msgs_);
      onChanged();
      return this;
    }
    /**
     * <pre>
     *    uint64 SIZE-OF-ENCODED // varint encoded length of the structure after encoding
     *    0xF0625DEE   // hardcoded, object type prefix in 4 bytes
     * </pre>
     *
     * <code>repeated bytes msgs = 1;</code>
     */
    public Builder clearMsgs() {
      msgs_ = java.util.Collections.emptyList();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }

    private java.util.List<com.google.protobuf.ByteString> signatures_ = java.util.Collections.emptyList();
    private void ensureSignaturesIsMutable() {
      if (!((bitField0_ & 0x00000002) != 0)) {
        signatures_ = new java.util.ArrayList<com.google.protobuf.ByteString>(signatures_);
        bitField0_ |= 0x00000002;
       }
    }
    /**
     * <pre>
     * array of size 1, containing the standard signature structure of the transaction sender
     * </pre>
     *
     * <code>repeated bytes signatures = 2;</code>
     */
    public java.util.List<com.google.protobuf.ByteString>
        getSignaturesList() {
      return ((bitField0_ & 0x00000002) != 0) ?
               java.util.Collections.unmodifiableList(signatures_) : signatures_;
    }
    /**
     * <pre>
     * array of size 1, containing the standard signature structure of the transaction sender
     * </pre>
     *
     * <code>repeated bytes signatures = 2;</code>
     */
    public int getSignaturesCount() {
      return signatures_.size();
    }
    /**
     * <pre>
     * array of size 1, containing the standard signature structure of the transaction sender
     * </pre>
     *
     * <code>repeated bytes signatures = 2;</code>
     */
    public com.google.protobuf.ByteString getSignatures(int index) {
      return signatures_.get(index);
    }
    /**
     * <pre>
     * array of size 1, containing the standard signature structure of the transaction sender
     * </pre>
     *
     * <code>repeated bytes signatures = 2;</code>
     */
    public Builder setSignatures(
        int index, com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  ensureSignaturesIsMutable();
      signatures_.set(index, value);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * array of size 1, containing the standard signature structure of the transaction sender
     * </pre>
     *
     * <code>repeated bytes signatures = 2;</code>
     */
    public Builder addSignatures(com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  ensureSignaturesIsMutable();
      signatures_.add(value);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * array of size 1, containing the standard signature structure of the transaction sender
     * </pre>
     *
     * <code>repeated bytes signatures = 2;</code>
     */
    public Builder addAllSignatures(
        Iterable<? extends com.google.protobuf.ByteString> values) {
      ensureSignaturesIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(
          values, signatures_);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * array of size 1, containing the standard signature structure of the transaction sender
     * </pre>
     *
     * <code>repeated bytes signatures = 2;</code>
     */
    public Builder clearSignatures() {
      signatures_ = java.util.Collections.emptyList();
      bitField0_ = (bitField0_ & ~0x00000002);
      onChanged();
      return this;
    }

    private Object memo_ = "";
    /**
     * <pre>
     * a short sentence of remark for the transaction. Please only `Transfer` transaction allows 'memo' input, and other transactions with non-empty `Memo` would be rejected.
     * </pre>
     *
     * <code>string memo = 3;</code>
     */
    public String getMemo() {
      Object ref = memo_;
      if (!(ref instanceof String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        memo_ = s;
        return s;
      } else {
        return (String) ref;
      }
    }
    /**
     * <pre>
     * a short sentence of remark for the transaction. Please only `Transfer` transaction allows 'memo' input, and other transactions with non-empty `Memo` would be rejected.
     * </pre>
     *
     * <code>string memo = 3;</code>
     */
    public com.google.protobuf.ByteString
        getMemoBytes() {
      Object ref = memo_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        memo_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * a short sentence of remark for the transaction. Please only `Transfer` transaction allows 'memo' input, and other transactions with non-empty `Memo` would be rejected.
     * </pre>
     *
     * <code>string memo = 3;</code>
     */
    public Builder setMemo(
        String value) {
      if (value == null) {
    throw new NullPointerException();
  }

      memo_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * a short sentence of remark for the transaction. Please only `Transfer` transaction allows 'memo' input, and other transactions with non-empty `Memo` would be rejected.
     * </pre>
     *
     * <code>string memo = 3;</code>
     */
    public Builder clearMemo() {

      memo_ = getDefaultInstance().getMemo();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * a short sentence of remark for the transaction. Please only `Transfer` transaction allows 'memo' input, and other transactions with non-empty `Memo` would be rejected.
     * </pre>
     *
     * <code>string memo = 3;</code>
     */
    public Builder setMemoBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);

      memo_ = value;
      onChanged();
      return this;
    }

    private long source_ ;
    /**
     * <pre>
     * an identifier for tools triggerring this transaction, set to zero if unwilling to disclose.
     * </pre>
     *
     * <code>int64 source = 4;</code>
     */
    public long getSource() {
      return source_;
    }
    /**
     * <pre>
     * an identifier for tools triggerring this transaction, set to zero if unwilling to disclose.
     * </pre>
     *
     * <code>int64 source = 4;</code>
     */
    public Builder setSource(long value) {

      source_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * an identifier for tools triggerring this transaction, set to zero if unwilling to disclose.
     * </pre>
     *
     * <code>int64 source = 4;</code>
     */
    public Builder clearSource() {

      source_ = 0L;
      onChanged();
      return this;
    }

    private com.google.protobuf.ByteString data_ = com.google.protobuf.ByteString.EMPTY;
    /**
     * <pre>
     *byte array, reserved for future use
     * </pre>
     *
     * <code>bytes data = 5;</code>
     */
    public com.google.protobuf.ByteString getData() {
      return data_;
    }
    /**
     * <pre>
     *byte array, reserved for future use
     * </pre>
     *
     * <code>bytes data = 5;</code>
     */
    public Builder setData(com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }

      data_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     *byte array, reserved for future use
     * </pre>
     *
     * <code>bytes data = 5;</code>
     */
    public Builder clearData() {

      data_ = getDefaultInstance().getData();
      onChanged();
      return this;
    }
    @Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:transaction.StdTx)
  }

  // @@protoc_insertion_point(class_scope:transaction.StdTx)
  private static final StdTx DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new StdTx();
  }

  public static StdTx getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<StdTx>
      PARSER = new com.google.protobuf.AbstractParser<StdTx>() {
    @Override
    public StdTx parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new StdTx(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<StdTx> parser() {
    return PARSER;
  }

  @Override
  public com.google.protobuf.Parser<StdTx> getParserForType() {
    return PARSER;
  }

  @Override
  public StdTx getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

