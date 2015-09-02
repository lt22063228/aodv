// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: message.proto

package player.model;

public final class MessageProtos {
  private MessageProtos() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface MessageOrBuilder
      extends com.google.protobuf.MessageOrBuilder {
    
    // required int32 type = 1;
    boolean hasType();
    int getType();
    
    // required string identifier = 2;
    boolean hasIdentifier();
    String getIdentifier();
    
    // required int64 offset = 3;
    boolean hasOffset();
    long getOffset();
    
    // required int32 ip = 4;
    boolean hasIp();
    int getIp();
    
    // optional bytes payload = 5;
    boolean hasPayload();
    com.google.protobuf.ByteString getPayload();
    
    // optional string checksum = 6;
    boolean hasChecksum();
    String getChecksum();
  }
  public static final class Message extends
      com.google.protobuf.GeneratedMessage
      implements MessageOrBuilder {
    // Use Message.newBuilder() to construct.
    private Message(Builder builder) {
      super(builder);
    }
    private Message(boolean noInit) {}
    
    private static final Message defaultInstance;
    public static Message getDefaultInstance() {
      return defaultInstance;
    }
    
    public Message getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return player.model.MessageProtos.internal_static_Message_Message_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return player.model.MessageProtos.internal_static_Message_Message_fieldAccessorTable;
    }
    
    private int bitField0_;
    // required int32 type = 1;
    public static final int TYPE_FIELD_NUMBER = 1;
    private int type_;
    public boolean hasType() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    public int getType() {
      return type_;
    }
    
    // required string identifier = 2;
    public static final int IDENTIFIER_FIELD_NUMBER = 2;
    private java.lang.Object identifier_;
    public boolean hasIdentifier() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    public String getIdentifier() {
      java.lang.Object ref = identifier_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (com.google.protobuf.Internal.isValidUtf8(bs)) {
          identifier_ = s;
        }
        return s;
      }
    }
    private com.google.protobuf.ByteString getIdentifierBytes() {
      java.lang.Object ref = identifier_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8((String) ref);
        identifier_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    
    // required int64 offset = 3;
    public static final int OFFSET_FIELD_NUMBER = 3;
    private long offset_;
    public boolean hasOffset() {
      return ((bitField0_ & 0x00000004) == 0x00000004);
    }
    public long getOffset() {
      return offset_;
    }
    
    // required int32 ip = 4;
    public static final int IP_FIELD_NUMBER = 4;
    private int ip_;
    public boolean hasIp() {
      return ((bitField0_ & 0x00000008) == 0x00000008);
    }
    public int getIp() {
      return ip_;
    }
    
    // optional bytes payload = 5;
    public static final int PAYLOAD_FIELD_NUMBER = 5;
    private com.google.protobuf.ByteString payload_;
    public boolean hasPayload() {
      return ((bitField0_ & 0x00000010) == 0x00000010);
    }
    public com.google.protobuf.ByteString getPayload() {
      return payload_;
    }
    
    // optional string checksum = 6;
    public static final int CHECKSUM_FIELD_NUMBER = 6;
    private java.lang.Object checksum_;
    public boolean hasChecksum() {
      return ((bitField0_ & 0x00000020) == 0x00000020);
    }
    public String getChecksum() {
      java.lang.Object ref = checksum_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (com.google.protobuf.Internal.isValidUtf8(bs)) {
          checksum_ = s;
        }
        return s;
      }
    }
    private com.google.protobuf.ByteString getChecksumBytes() {
      java.lang.Object ref = checksum_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8((String) ref);
        checksum_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    
    private void initFields() {
      type_ = 0;
      identifier_ = "";
      offset_ = 0L;
      ip_ = 0;
      payload_ = com.google.protobuf.ByteString.EMPTY;
      checksum_ = "";
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;
      
      if (!hasType()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasIdentifier()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasOffset()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasIp()) {
        memoizedIsInitialized = 0;
        return false;
      }
      memoizedIsInitialized = 1;
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeInt32(1, type_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeBytes(2, getIdentifierBytes());
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        output.writeInt64(3, offset_);
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        output.writeInt32(4, ip_);
      }
      if (((bitField0_ & 0x00000010) == 0x00000010)) {
        output.writeBytes(5, payload_);
      }
      if (((bitField0_ & 0x00000020) == 0x00000020)) {
        output.writeBytes(6, getChecksumBytes());
      }
      getUnknownFields().writeTo(output);
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(1, type_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(2, getIdentifierBytes());
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt64Size(3, offset_);
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(4, ip_);
      }
      if (((bitField0_ & 0x00000010) == 0x00000010)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(5, payload_);
      }
      if (((bitField0_ & 0x00000020) == 0x00000020)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(6, getChecksumBytes());
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }
    
    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }
    
    public static player.model.MessageProtos.Message parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static player.model.MessageProtos.Message parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static player.model.MessageProtos.Message parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static player.model.MessageProtos.Message parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static player.model.MessageProtos.Message parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static player.model.MessageProtos.Message parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static player.model.MessageProtos.Message parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static player.model.MessageProtos.Message parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static player.model.MessageProtos.Message parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static player.model.MessageProtos.Message parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(player.model.MessageProtos.Message prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }
    
    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements player.model.MessageProtos.MessageOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return player.model.MessageProtos.internal_static_Message_Message_descriptor;
      }
      
      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return player.model.MessageProtos.internal_static_Message_Message_fieldAccessorTable;
      }
      
      // Construct using Model.MessageProtos.Message.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }
      
      private Builder(BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }
      
      public Builder clear() {
        super.clear();
        type_ = 0;
        bitField0_ = (bitField0_ & ~0x00000001);
        identifier_ = "";
        bitField0_ = (bitField0_ & ~0x00000002);
        offset_ = 0L;
        bitField0_ = (bitField0_ & ~0x00000004);
        ip_ = 0;
        bitField0_ = (bitField0_ & ~0x00000008);
        payload_ = com.google.protobuf.ByteString.EMPTY;
        bitField0_ = (bitField0_ & ~0x00000010);
        checksum_ = "";
        bitField0_ = (bitField0_ & ~0x00000020);
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return player.model.MessageProtos.Message.getDescriptor();
      }
      
      public player.model.MessageProtos.Message getDefaultInstanceForType() {
        return player.model.MessageProtos.Message.getDefaultInstance();
      }
      
      public player.model.MessageProtos.Message build() {
        player.model.MessageProtos.Message result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }
      
      private player.model.MessageProtos.Message buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        player.model.MessageProtos.Message result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return result;
      }
      
      public player.model.MessageProtos.Message buildPartial() {
        player.model.MessageProtos.Message result = new player.model.MessageProtos.Message(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.type_ = type_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.identifier_ = identifier_;
        if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
          to_bitField0_ |= 0x00000004;
        }
        result.offset_ = offset_;
        if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
          to_bitField0_ |= 0x00000008;
        }
        result.ip_ = ip_;
        if (((from_bitField0_ & 0x00000010) == 0x00000010)) {
          to_bitField0_ |= 0x00000010;
        }
        result.payload_ = payload_;
        if (((from_bitField0_ & 0x00000020) == 0x00000020)) {
          to_bitField0_ |= 0x00000020;
        }
        result.checksum_ = checksum_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }
      
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof player.model.MessageProtos.Message) {
          return mergeFrom((player.model.MessageProtos.Message)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }
      
      public Builder mergeFrom(player.model.MessageProtos.Message other) {
        if (other == player.model.MessageProtos.Message.getDefaultInstance()) return this;
        if (other.hasType()) {
          setType(other.getType());
        }
        if (other.hasIdentifier()) {
          setIdentifier(other.getIdentifier());
        }
        if (other.hasOffset()) {
          setOffset(other.getOffset());
        }
        if (other.hasIp()) {
          setIp(other.getIp());
        }
        if (other.hasPayload()) {
          setPayload(other.getPayload());
        }
        if (other.hasChecksum()) {
          setChecksum(other.getChecksum());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }
      
      public final boolean isInitialized() {
        if (!hasType()) {
          
          return false;
        }
        if (!hasIdentifier()) {
          
          return false;
        }
        if (!hasOffset()) {
          
          return false;
        }
        if (!hasIp()) {
          
          return false;
        }
        return true;
      }
      
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder(
            this.getUnknownFields());
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              this.setUnknownFields(unknownFields.build());
              onChanged();
              return this;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                this.setUnknownFields(unknownFields.build());
                onChanged();
                return this;
              }
              break;
            }
            case 8: {
              bitField0_ |= 0x00000001;
              type_ = input.readInt32();
              break;
            }
            case 18: {
              bitField0_ |= 0x00000002;
              identifier_ = input.readBytes();
              break;
            }
            case 24: {
              bitField0_ |= 0x00000004;
              offset_ = input.readInt64();
              break;
            }
            case 32: {
              bitField0_ |= 0x00000008;
              ip_ = input.readInt32();
              break;
            }
            case 42: {
              bitField0_ |= 0x00000010;
              payload_ = input.readBytes();
              break;
            }
            case 50: {
              bitField0_ |= 0x00000020;
              checksum_ = input.readBytes();
              break;
            }
          }
        }
      }
      
      private int bitField0_;
      
      // required int32 type = 1;
      private int type_ ;
      public boolean hasType() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      public int getType() {
        return type_;
      }
      public Builder setType(int value) {
        bitField0_ |= 0x00000001;
        type_ = value;
        onChanged();
        return this;
      }
      public Builder clearType() {
        bitField0_ = (bitField0_ & ~0x00000001);
        type_ = 0;
        onChanged();
        return this;
      }
      
      // required string identifier = 2;
      private java.lang.Object identifier_ = "";
      public boolean hasIdentifier() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      public String getIdentifier() {
        java.lang.Object ref = identifier_;
        if (!(ref instanceof String)) {
          String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
          identifier_ = s;
          return s;
        } else {
          return (String) ref;
        }
      }
      public Builder setIdentifier(String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
        identifier_ = value;
        onChanged();
        return this;
      }
      public Builder clearIdentifier() {
        bitField0_ = (bitField0_ & ~0x00000002);
        identifier_ = getDefaultInstance().getIdentifier();
        onChanged();
        return this;
      }
      void setIdentifier(com.google.protobuf.ByteString value) {
        bitField0_ |= 0x00000002;
        identifier_ = value;
        onChanged();
      }
      
      // required int64 offset = 3;
      private long offset_ ;
      public boolean hasOffset() {
        return ((bitField0_ & 0x00000004) == 0x00000004);
      }
      public long getOffset() {
        return offset_;
      }
      public Builder setOffset(long value) {
        bitField0_ |= 0x00000004;
        offset_ = value;
        onChanged();
        return this;
      }
      public Builder clearOffset() {
        bitField0_ = (bitField0_ & ~0x00000004);
        offset_ = 0L;
        onChanged();
        return this;
      }
      
      // required int32 ip = 4;
      private int ip_ ;
      public boolean hasIp() {
        return ((bitField0_ & 0x00000008) == 0x00000008);
      }
      public int getIp() {
        return ip_;
      }
      public Builder setIp(int value) {
        bitField0_ |= 0x00000008;
        ip_ = value;
        onChanged();
        return this;
      }
      public Builder clearIp() {
        bitField0_ = (bitField0_ & ~0x00000008);
        ip_ = 0;
        onChanged();
        return this;
      }
      
      // optional bytes payload = 5;
      private com.google.protobuf.ByteString payload_ = com.google.protobuf.ByteString.EMPTY;
      public boolean hasPayload() {
        return ((bitField0_ & 0x00000010) == 0x00000010);
      }
      public com.google.protobuf.ByteString getPayload() {
        return payload_;
      }
      public Builder setPayload(com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000010;
        payload_ = value;
        onChanged();
        return this;
      }
      public Builder clearPayload() {
        bitField0_ = (bitField0_ & ~0x00000010);
        payload_ = getDefaultInstance().getPayload();
        onChanged();
        return this;
      }
      
      // optional string checksum = 6;
      private java.lang.Object checksum_ = "";
      public boolean hasChecksum() {
        return ((bitField0_ & 0x00000020) == 0x00000020);
      }
      public String getChecksum() {
        java.lang.Object ref = checksum_;
        if (!(ref instanceof String)) {
          String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
          checksum_ = s;
          return s;
        } else {
          return (String) ref;
        }
      }
      public Builder setChecksum(String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000020;
        checksum_ = value;
        onChanged();
        return this;
      }
      public Builder clearChecksum() {
        bitField0_ = (bitField0_ & ~0x00000020);
        checksum_ = getDefaultInstance().getChecksum();
        onChanged();
        return this;
      }
      void setChecksum(com.google.protobuf.ByteString value) {
        bitField0_ |= 0x00000020;
        checksum_ = value;
        onChanged();
      }
      
      // @@protoc_insertion_point(builder_scope:Message.Message)
    }
    
    static {
      defaultInstance = new Message(true);
      defaultInstance.initFields();
    }
    
    // @@protoc_insertion_point(class_scope:Message.Message)
  }
  
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_Message_Message_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_Message_Message_fieldAccessorTable;
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\rmessage.proto\022\007Message\"j\n\007Message\022\014\n\004t" +
      "ype\030\001 \002(\005\022\022\n\nidentifier\030\002 \002(\t\022\016\n\006offset\030" +
      "\003 \002(\003\022\n\n\002ip\030\004 \002(\005\022\017\n\007payload\030\005 \001(\014\022\020\n\010ch" +
      "ecksum\030\006 \001(\tB\026\n\005ModelB\rMessageProtos"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_Message_Message_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_Message_Message_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_Message_Message_descriptor,
              new java.lang.String[] { "Type", "Identifier", "Offset", "Ip", "Payload", "Checksum", },
              player.model.MessageProtos.Message.class,
              player.model.MessageProtos.Message.Builder.class);
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }
  
  // @@protoc_insertion_point(outer_class_scope)
}
