// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: Taxi.proto

public final class TaxiOuterClass {
  private TaxiOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface TaxiOrBuilder extends
      // @@protoc_insertion_point(interface_extends:Taxi)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>required int32 id = 1;</code>
     * @return Whether the id field is set.
     */
    boolean hasId();
    /**
     * <code>required int32 id = 1;</code>
     * @return The id.
     */
    int getId();

    /**
     * <code>required float distance = 2;</code>
     * @return Whether the distance field is set.
     */
    boolean hasDistance();
    /**
     * <code>required float distance = 2;</code>
     * @return The distance.
     */
    float getDistance();

    /**
     * <code>repeated int32 position = 3;</code>
     * @return A list containing the position.
     */
    java.util.List<java.lang.Integer> getPositionList();
    /**
     * <code>repeated int32 position = 3;</code>
     * @return The count of position.
     */
    int getPositionCount();
    /**
     * <code>repeated int32 position = 3;</code>
     * @param index The index of the element to return.
     * @return The position at the given index.
     */
    int getPosition(int index);
  }
  /**
   * Protobuf type {@code Taxi}
   */
  public  static final class Taxi extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:Taxi)
      TaxiOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use Taxi.newBuilder() to construct.
    private Taxi(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private Taxi() {
      position_ = emptyIntList();
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
        UnusedPrivateParameter unused) {
      return new Taxi();
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private Taxi(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
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
            case 8: {
              bitField0_ |= 0x00000001;
              id_ = input.readInt32();
              break;
            }
            case 21: {
              bitField0_ |= 0x00000002;
              distance_ = input.readFloat();
              break;
            }
            case 24: {
              if (!((mutable_bitField0_ & 0x00000004) != 0)) {
                position_ = newIntList();
                mutable_bitField0_ |= 0x00000004;
              }
              position_.addInt(input.readInt32());
              break;
            }
            case 26: {
              int length = input.readRawVarint32();
              int limit = input.pushLimit(length);
              if (!((mutable_bitField0_ & 0x00000004) != 0) && input.getBytesUntilLimit() > 0) {
                position_ = newIntList();
                mutable_bitField0_ |= 0x00000004;
              }
              while (input.getBytesUntilLimit() > 0) {
                position_.addInt(input.readInt32());
              }
              input.popLimit(limit);
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
        if (((mutable_bitField0_ & 0x00000004) != 0)) {
          position_.makeImmutable(); // C
        }
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return TaxiOuterClass.internal_static_Taxi_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return TaxiOuterClass.internal_static_Taxi_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              TaxiOuterClass.Taxi.class, TaxiOuterClass.Taxi.Builder.class);
    }

    private int bitField0_;
    public static final int ID_FIELD_NUMBER = 1;
    private int id_;
    /**
     * <code>required int32 id = 1;</code>
     * @return Whether the id field is set.
     */
    public boolean hasId() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>required int32 id = 1;</code>
     * @return The id.
     */
    public int getId() {
      return id_;
    }

    public static final int DISTANCE_FIELD_NUMBER = 2;
    private float distance_;
    /**
     * <code>required float distance = 2;</code>
     * @return Whether the distance field is set.
     */
    public boolean hasDistance() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <code>required float distance = 2;</code>
     * @return The distance.
     */
    public float getDistance() {
      return distance_;
    }

    public static final int POSITION_FIELD_NUMBER = 3;
    private com.google.protobuf.Internal.IntList position_;
    /**
     * <code>repeated int32 position = 3;</code>
     * @return A list containing the position.
     */
    public java.util.List<java.lang.Integer>
        getPositionList() {
      return position_;
    }
    /**
     * <code>repeated int32 position = 3;</code>
     * @return The count of position.
     */
    public int getPositionCount() {
      return position_.size();
    }
    /**
     * <code>repeated int32 position = 3;</code>
     * @param index The index of the element to return.
     * @return The position at the given index.
     */
    public int getPosition(int index) {
      return position_.getInt(index);
    }

    private byte memoizedIsInitialized = -1;
    @java.lang.Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      if (!hasId()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasDistance()) {
        memoizedIsInitialized = 0;
        return false;
      }
      memoizedIsInitialized = 1;
      return true;
    }

    @java.lang.Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (((bitField0_ & 0x00000001) != 0)) {
        output.writeInt32(1, id_);
      }
      if (((bitField0_ & 0x00000002) != 0)) {
        output.writeFloat(2, distance_);
      }
      for (int i = 0; i < position_.size(); i++) {
        output.writeInt32(3, position_.getInt(i));
      }
      unknownFields.writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) != 0)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(1, id_);
      }
      if (((bitField0_ & 0x00000002) != 0)) {
        size += com.google.protobuf.CodedOutputStream
          .computeFloatSize(2, distance_);
      }
      {
        int dataSize = 0;
        for (int i = 0; i < position_.size(); i++) {
          dataSize += com.google.protobuf.CodedOutputStream
            .computeInt32SizeNoTag(position_.getInt(i));
        }
        size += dataSize;
        size += 1 * getPositionList().size();
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof TaxiOuterClass.Taxi)) {
        return super.equals(obj);
      }
      TaxiOuterClass.Taxi other = (TaxiOuterClass.Taxi) obj;

      if (hasId() != other.hasId()) return false;
      if (hasId()) {
        if (getId()
            != other.getId()) return false;
      }
      if (hasDistance() != other.hasDistance()) return false;
      if (hasDistance()) {
        if (java.lang.Float.floatToIntBits(getDistance())
            != java.lang.Float.floatToIntBits(
                other.getDistance())) return false;
      }
      if (!getPositionList()
          .equals(other.getPositionList())) return false;
      if (!unknownFields.equals(other.unknownFields)) return false;
      return true;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      if (hasId()) {
        hash = (37 * hash) + ID_FIELD_NUMBER;
        hash = (53 * hash) + getId();
      }
      if (hasDistance()) {
        hash = (37 * hash) + DISTANCE_FIELD_NUMBER;
        hash = (53 * hash) + java.lang.Float.floatToIntBits(
            getDistance());
      }
      if (getPositionCount() > 0) {
        hash = (37 * hash) + POSITION_FIELD_NUMBER;
        hash = (53 * hash) + getPositionList().hashCode();
      }
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static TaxiOuterClass.Taxi parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static TaxiOuterClass.Taxi parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static TaxiOuterClass.Taxi parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static TaxiOuterClass.Taxi parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static TaxiOuterClass.Taxi parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static TaxiOuterClass.Taxi parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static TaxiOuterClass.Taxi parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static TaxiOuterClass.Taxi parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static TaxiOuterClass.Taxi parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static TaxiOuterClass.Taxi parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static TaxiOuterClass.Taxi parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static TaxiOuterClass.Taxi parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @java.lang.Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(TaxiOuterClass.Taxi prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @java.lang.Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code Taxi}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:Taxi)
        TaxiOuterClass.TaxiOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return TaxiOuterClass.internal_static_Taxi_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return TaxiOuterClass.internal_static_Taxi_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                TaxiOuterClass.Taxi.class, TaxiOuterClass.Taxi.Builder.class);
      }

      // Construct using TaxiOuterClass.Taxi.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        id_ = 0;
        bitField0_ = (bitField0_ & ~0x00000001);
        distance_ = 0F;
        bitField0_ = (bitField0_ & ~0x00000002);
        position_ = emptyIntList();
        bitField0_ = (bitField0_ & ~0x00000004);
        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return TaxiOuterClass.internal_static_Taxi_descriptor;
      }

      @java.lang.Override
      public TaxiOuterClass.Taxi getDefaultInstanceForType() {
        return TaxiOuterClass.Taxi.getDefaultInstance();
      }

      @java.lang.Override
      public TaxiOuterClass.Taxi build() {
        TaxiOuterClass.Taxi result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public TaxiOuterClass.Taxi buildPartial() {
        TaxiOuterClass.Taxi result = new TaxiOuterClass.Taxi(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) != 0)) {
          result.id_ = id_;
          to_bitField0_ |= 0x00000001;
        }
        if (((from_bitField0_ & 0x00000002) != 0)) {
          result.distance_ = distance_;
          to_bitField0_ |= 0x00000002;
        }
        if (((bitField0_ & 0x00000004) != 0)) {
          position_.makeImmutable();
          bitField0_ = (bitField0_ & ~0x00000004);
        }
        result.position_ = position_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      @java.lang.Override
      public Builder clone() {
        return super.clone();
      }
      @java.lang.Override
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.setField(field, value);
      }
      @java.lang.Override
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return super.clearField(field);
      }
      @java.lang.Override
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return super.clearOneof(oneof);
      }
      @java.lang.Override
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return super.setRepeatedField(field, index, value);
      }
      @java.lang.Override
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.addRepeatedField(field, value);
      }
      @java.lang.Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof TaxiOuterClass.Taxi) {
          return mergeFrom((TaxiOuterClass.Taxi)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(TaxiOuterClass.Taxi other) {
        if (other == TaxiOuterClass.Taxi.getDefaultInstance()) return this;
        if (other.hasId()) {
          setId(other.getId());
        }
        if (other.hasDistance()) {
          setDistance(other.getDistance());
        }
        if (!other.position_.isEmpty()) {
          if (position_.isEmpty()) {
            position_ = other.position_;
            bitField0_ = (bitField0_ & ~0x00000004);
          } else {
            ensurePositionIsMutable();
            position_.addAll(other.position_);
          }
          onChanged();
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      @java.lang.Override
      public final boolean isInitialized() {
        if (!hasId()) {
          return false;
        }
        if (!hasDistance()) {
          return false;
        }
        return true;
      }

      @java.lang.Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        TaxiOuterClass.Taxi parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (TaxiOuterClass.Taxi) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private int id_ ;
      /**
       * <code>required int32 id = 1;</code>
       * @return Whether the id field is set.
       */
      public boolean hasId() {
        return ((bitField0_ & 0x00000001) != 0);
      }
      /**
       * <code>required int32 id = 1;</code>
       * @return The id.
       */
      public int getId() {
        return id_;
      }
      /**
       * <code>required int32 id = 1;</code>
       * @param value The id to set.
       * @return This builder for chaining.
       */
      public Builder setId(int value) {
        bitField0_ |= 0x00000001;
        id_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required int32 id = 1;</code>
       * @return This builder for chaining.
       */
      public Builder clearId() {
        bitField0_ = (bitField0_ & ~0x00000001);
        id_ = 0;
        onChanged();
        return this;
      }

      private float distance_ ;
      /**
       * <code>required float distance = 2;</code>
       * @return Whether the distance field is set.
       */
      public boolean hasDistance() {
        return ((bitField0_ & 0x00000002) != 0);
      }
      /**
       * <code>required float distance = 2;</code>
       * @return The distance.
       */
      public float getDistance() {
        return distance_;
      }
      /**
       * <code>required float distance = 2;</code>
       * @param value The distance to set.
       * @return This builder for chaining.
       */
      public Builder setDistance(float value) {
        bitField0_ |= 0x00000002;
        distance_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required float distance = 2;</code>
       * @return This builder for chaining.
       */
      public Builder clearDistance() {
        bitField0_ = (bitField0_ & ~0x00000002);
        distance_ = 0F;
        onChanged();
        return this;
      }

      private com.google.protobuf.Internal.IntList position_ = emptyIntList();
      private void ensurePositionIsMutable() {
        if (!((bitField0_ & 0x00000004) != 0)) {
          position_ = mutableCopy(position_);
          bitField0_ |= 0x00000004;
         }
      }
      /**
       * <code>repeated int32 position = 3;</code>
       * @return A list containing the position.
       */
      public java.util.List<java.lang.Integer>
          getPositionList() {
        return ((bitField0_ & 0x00000004) != 0) ?
                 java.util.Collections.unmodifiableList(position_) : position_;
      }
      /**
       * <code>repeated int32 position = 3;</code>
       * @return The count of position.
       */
      public int getPositionCount() {
        return position_.size();
      }
      /**
       * <code>repeated int32 position = 3;</code>
       * @param index The index of the element to return.
       * @return The position at the given index.
       */
      public int getPosition(int index) {
        return position_.getInt(index);
      }
      /**
       * <code>repeated int32 position = 3;</code>
       * @param index The index to set the value at.
       * @param value The position to set.
       * @return This builder for chaining.
       */
      public Builder setPosition(
          int index, int value) {
        ensurePositionIsMutable();
        position_.setInt(index, value);
        onChanged();
        return this;
      }
      /**
       * <code>repeated int32 position = 3;</code>
       * @param value The position to add.
       * @return This builder for chaining.
       */
      public Builder addPosition(int value) {
        ensurePositionIsMutable();
        position_.addInt(value);
        onChanged();
        return this;
      }
      /**
       * <code>repeated int32 position = 3;</code>
       * @param values The position to add.
       * @return This builder for chaining.
       */
      public Builder addAllPosition(
          java.lang.Iterable<? extends java.lang.Integer> values) {
        ensurePositionIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, position_);
        onChanged();
        return this;
      }
      /**
       * <code>repeated int32 position = 3;</code>
       * @return This builder for chaining.
       */
      public Builder clearPosition() {
        position_ = emptyIntList();
        bitField0_ = (bitField0_ & ~0x00000004);
        onChanged();
        return this;
      }
      @java.lang.Override
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      @java.lang.Override
      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:Taxi)
    }

    // @@protoc_insertion_point(class_scope:Taxi)
    private static final TaxiOuterClass.Taxi DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new TaxiOuterClass.Taxi();
    }

    public static TaxiOuterClass.Taxi getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    @java.lang.Deprecated public static final com.google.protobuf.Parser<Taxi>
        PARSER = new com.google.protobuf.AbstractParser<Taxi>() {
      @java.lang.Override
      public Taxi parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new Taxi(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<Taxi> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<Taxi> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public TaxiOuterClass.Taxi getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_Taxi_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_Taxi_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\nTaxi.proto\"6\n\004Taxi\022\n\n\002id\030\001 \002(\005\022\020\n\010dist" +
      "ance\030\002 \002(\002\022\020\n\010position\030\003 \003(\005"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_Taxi_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_Taxi_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_Taxi_descriptor,
        new java.lang.String[] { "Id", "Distance", "Position", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
