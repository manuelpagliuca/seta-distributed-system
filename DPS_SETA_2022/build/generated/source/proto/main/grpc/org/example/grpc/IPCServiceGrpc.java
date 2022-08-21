package org.example.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.25.0)",
    comments = "Source: IPC.proto")
public final class IPCServiceGrpc {

  private IPCServiceGrpc() {}

  public static final String SERVICE_NAME = "org.example.grpc.IPCService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.example.grpc.IPC.Infos,
      org.example.grpc.IPC.ACK> getPresentMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "present",
      requestType = org.example.grpc.IPC.Infos.class,
      responseType = org.example.grpc.IPC.ACK.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.example.grpc.IPC.Infos,
      org.example.grpc.IPC.ACK> getPresentMethod() {
    io.grpc.MethodDescriptor<org.example.grpc.IPC.Infos, org.example.grpc.IPC.ACK> getPresentMethod;
    if ((getPresentMethod = IPCServiceGrpc.getPresentMethod) == null) {
      synchronized (IPCServiceGrpc.class) {
        if ((getPresentMethod = IPCServiceGrpc.getPresentMethod) == null) {
          IPCServiceGrpc.getPresentMethod = getPresentMethod =
              io.grpc.MethodDescriptor.<org.example.grpc.IPC.Infos, org.example.grpc.IPC.ACK>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "present"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.IPC.Infos.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.IPC.ACK.getDefaultInstance()))
              .setSchemaDescriptor(new IPCServiceMethodDescriptorSupplier("present"))
              .build();
        }
      }
    }
    return getPresentMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.example.grpc.IPC.Infos,
      org.example.grpc.IPC.ACK> getRemoveMeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "removeMe",
      requestType = org.example.grpc.IPC.Infos.class,
      responseType = org.example.grpc.IPC.ACK.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<org.example.grpc.IPC.Infos,
      org.example.grpc.IPC.ACK> getRemoveMeMethod() {
    io.grpc.MethodDescriptor<org.example.grpc.IPC.Infos, org.example.grpc.IPC.ACK> getRemoveMeMethod;
    if ((getRemoveMeMethod = IPCServiceGrpc.getRemoveMeMethod) == null) {
      synchronized (IPCServiceGrpc.class) {
        if ((getRemoveMeMethod = IPCServiceGrpc.getRemoveMeMethod) == null) {
          IPCServiceGrpc.getRemoveMeMethod = getRemoveMeMethod =
              io.grpc.MethodDescriptor.<org.example.grpc.IPC.Infos, org.example.grpc.IPC.ACK>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "removeMe"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.IPC.Infos.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.IPC.ACK.getDefaultInstance()))
              .setSchemaDescriptor(new IPCServiceMethodDescriptorSupplier("removeMe"))
              .build();
        }
      }
    }
    return getRemoveMeMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.example.grpc.IPC.RideCharge,
      org.example.grpc.IPC.ACK> getCoordinateRideStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "coordinateRideStream",
      requestType = org.example.grpc.IPC.RideCharge.class,
      responseType = org.example.grpc.IPC.ACK.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<org.example.grpc.IPC.RideCharge,
      org.example.grpc.IPC.ACK> getCoordinateRideStreamMethod() {
    io.grpc.MethodDescriptor<org.example.grpc.IPC.RideCharge, org.example.grpc.IPC.ACK> getCoordinateRideStreamMethod;
    if ((getCoordinateRideStreamMethod = IPCServiceGrpc.getCoordinateRideStreamMethod) == null) {
      synchronized (IPCServiceGrpc.class) {
        if ((getCoordinateRideStreamMethod = IPCServiceGrpc.getCoordinateRideStreamMethod) == null) {
          IPCServiceGrpc.getCoordinateRideStreamMethod = getCoordinateRideStreamMethod =
              io.grpc.MethodDescriptor.<org.example.grpc.IPC.RideCharge, org.example.grpc.IPC.ACK>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "coordinateRideStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.IPC.RideCharge.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.IPC.ACK.getDefaultInstance()))
              .setSchemaDescriptor(new IPCServiceMethodDescriptorSupplier("coordinateRideStream"))
              .build();
        }
      }
    }
    return getCoordinateRideStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.example.grpc.IPC.Infos,
      org.example.grpc.IPC.ACK> getChangedPositionStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "changedPositionStream",
      requestType = org.example.grpc.IPC.Infos.class,
      responseType = org.example.grpc.IPC.ACK.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<org.example.grpc.IPC.Infos,
      org.example.grpc.IPC.ACK> getChangedPositionStreamMethod() {
    io.grpc.MethodDescriptor<org.example.grpc.IPC.Infos, org.example.grpc.IPC.ACK> getChangedPositionStreamMethod;
    if ((getChangedPositionStreamMethod = IPCServiceGrpc.getChangedPositionStreamMethod) == null) {
      synchronized (IPCServiceGrpc.class) {
        if ((getChangedPositionStreamMethod = IPCServiceGrpc.getChangedPositionStreamMethod) == null) {
          IPCServiceGrpc.getChangedPositionStreamMethod = getChangedPositionStreamMethod =
              io.grpc.MethodDescriptor.<org.example.grpc.IPC.Infos, org.example.grpc.IPC.ACK>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "changedPositionStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.IPC.Infos.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.IPC.ACK.getDefaultInstance()))
              .setSchemaDescriptor(new IPCServiceMethodDescriptorSupplier("changedPositionStream"))
              .build();
        }
      }
    }
    return getChangedPositionStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.example.grpc.IPC.Goodbye,
      org.example.grpc.IPC.Response> getGoodbyeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "goodbye",
      requestType = org.example.grpc.IPC.Goodbye.class,
      responseType = org.example.grpc.IPC.Response.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.example.grpc.IPC.Goodbye,
      org.example.grpc.IPC.Response> getGoodbyeMethod() {
    io.grpc.MethodDescriptor<org.example.grpc.IPC.Goodbye, org.example.grpc.IPC.Response> getGoodbyeMethod;
    if ((getGoodbyeMethod = IPCServiceGrpc.getGoodbyeMethod) == null) {
      synchronized (IPCServiceGrpc.class) {
        if ((getGoodbyeMethod = IPCServiceGrpc.getGoodbyeMethod) == null) {
          IPCServiceGrpc.getGoodbyeMethod = getGoodbyeMethod =
              io.grpc.MethodDescriptor.<org.example.grpc.IPC.Goodbye, org.example.grpc.IPC.Response>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "goodbye"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.IPC.Goodbye.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.IPC.Response.getDefaultInstance()))
              .setSchemaDescriptor(new IPCServiceMethodDescriptorSupplier("goodbye"))
              .build();
        }
      }
    }
    return getGoodbyeMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static IPCServiceStub newStub(io.grpc.Channel channel) {
    return new IPCServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static IPCServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new IPCServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static IPCServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new IPCServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class IPCServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void present(org.example.grpc.IPC.Infos request,
        io.grpc.stub.StreamObserver<org.example.grpc.IPC.ACK> responseObserver) {
      asyncUnimplementedUnaryCall(getPresentMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<org.example.grpc.IPC.Infos> removeMe(
        io.grpc.stub.StreamObserver<org.example.grpc.IPC.ACK> responseObserver) {
      return asyncUnimplementedStreamingCall(getRemoveMeMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<org.example.grpc.IPC.RideCharge> coordinateRideStream(
        io.grpc.stub.StreamObserver<org.example.grpc.IPC.ACK> responseObserver) {
      return asyncUnimplementedStreamingCall(getCoordinateRideStreamMethod(), responseObserver);
    }

    /**
     * <pre>
     * todo: rpc per comunicare il fatto che sta
     * </pre>
     */
    public io.grpc.stub.StreamObserver<org.example.grpc.IPC.Infos> changedPositionStream(
        io.grpc.stub.StreamObserver<org.example.grpc.IPC.ACK> responseObserver) {
      return asyncUnimplementedStreamingCall(getChangedPositionStreamMethod(), responseObserver);
    }

    /**
     */
    public void goodbye(org.example.grpc.IPC.Goodbye request,
        io.grpc.stub.StreamObserver<org.example.grpc.IPC.Response> responseObserver) {
      asyncUnimplementedUnaryCall(getGoodbyeMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getPresentMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.example.grpc.IPC.Infos,
                org.example.grpc.IPC.ACK>(
                  this, METHODID_PRESENT)))
          .addMethod(
            getRemoveMeMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                org.example.grpc.IPC.Infos,
                org.example.grpc.IPC.ACK>(
                  this, METHODID_REMOVE_ME)))
          .addMethod(
            getCoordinateRideStreamMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                org.example.grpc.IPC.RideCharge,
                org.example.grpc.IPC.ACK>(
                  this, METHODID_COORDINATE_RIDE_STREAM)))
          .addMethod(
            getChangedPositionStreamMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                org.example.grpc.IPC.Infos,
                org.example.grpc.IPC.ACK>(
                  this, METHODID_CHANGED_POSITION_STREAM)))
          .addMethod(
            getGoodbyeMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.example.grpc.IPC.Goodbye,
                org.example.grpc.IPC.Response>(
                  this, METHODID_GOODBYE)))
          .build();
    }
  }

  /**
   */
  public static final class IPCServiceStub extends io.grpc.stub.AbstractStub<IPCServiceStub> {
    private IPCServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private IPCServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected IPCServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new IPCServiceStub(channel, callOptions);
    }

    /**
     */
    public void present(org.example.grpc.IPC.Infos request,
        io.grpc.stub.StreamObserver<org.example.grpc.IPC.ACK> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPresentMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<org.example.grpc.IPC.Infos> removeMe(
        io.grpc.stub.StreamObserver<org.example.grpc.IPC.ACK> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getRemoveMeMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<org.example.grpc.IPC.RideCharge> coordinateRideStream(
        io.grpc.stub.StreamObserver<org.example.grpc.IPC.ACK> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getCoordinateRideStreamMethod(), getCallOptions()), responseObserver);
    }

    /**
     * <pre>
     * todo: rpc per comunicare il fatto che sta
     * </pre>
     */
    public io.grpc.stub.StreamObserver<org.example.grpc.IPC.Infos> changedPositionStream(
        io.grpc.stub.StreamObserver<org.example.grpc.IPC.ACK> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getChangedPositionStreamMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public void goodbye(org.example.grpc.IPC.Goodbye request,
        io.grpc.stub.StreamObserver<org.example.grpc.IPC.Response> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGoodbyeMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class IPCServiceBlockingStub extends io.grpc.stub.AbstractStub<IPCServiceBlockingStub> {
    private IPCServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private IPCServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected IPCServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new IPCServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public org.example.grpc.IPC.ACK present(org.example.grpc.IPC.Infos request) {
      return blockingUnaryCall(
          getChannel(), getPresentMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.example.grpc.IPC.Response goodbye(org.example.grpc.IPC.Goodbye request) {
      return blockingUnaryCall(
          getChannel(), getGoodbyeMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class IPCServiceFutureStub extends io.grpc.stub.AbstractStub<IPCServiceFutureStub> {
    private IPCServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private IPCServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected IPCServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new IPCServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.example.grpc.IPC.ACK> present(
        org.example.grpc.IPC.Infos request) {
      return futureUnaryCall(
          getChannel().newCall(getPresentMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.example.grpc.IPC.Response> goodbye(
        org.example.grpc.IPC.Goodbye request) {
      return futureUnaryCall(
          getChannel().newCall(getGoodbyeMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_PRESENT = 0;
  private static final int METHODID_GOODBYE = 1;
  private static final int METHODID_REMOVE_ME = 2;
  private static final int METHODID_COORDINATE_RIDE_STREAM = 3;
  private static final int METHODID_CHANGED_POSITION_STREAM = 4;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final IPCServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(IPCServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_PRESENT:
          serviceImpl.present((org.example.grpc.IPC.Infos) request,
              (io.grpc.stub.StreamObserver<org.example.grpc.IPC.ACK>) responseObserver);
          break;
        case METHODID_GOODBYE:
          serviceImpl.goodbye((org.example.grpc.IPC.Goodbye) request,
              (io.grpc.stub.StreamObserver<org.example.grpc.IPC.Response>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_REMOVE_ME:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.removeMe(
              (io.grpc.stub.StreamObserver<org.example.grpc.IPC.ACK>) responseObserver);
        case METHODID_COORDINATE_RIDE_STREAM:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.coordinateRideStream(
              (io.grpc.stub.StreamObserver<org.example.grpc.IPC.ACK>) responseObserver);
        case METHODID_CHANGED_POSITION_STREAM:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.changedPositionStream(
              (io.grpc.stub.StreamObserver<org.example.grpc.IPC.ACK>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class IPCServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    IPCServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.example.grpc.IPC.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("IPCService");
    }
  }

  private static final class IPCServiceFileDescriptorSupplier
      extends IPCServiceBaseDescriptorSupplier {
    IPCServiceFileDescriptorSupplier() {}
  }

  private static final class IPCServiceMethodDescriptorSupplier
      extends IPCServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    IPCServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (IPCServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new IPCServiceFileDescriptorSupplier())
              .addMethod(getPresentMethod())
              .addMethod(getRemoveMeMethod())
              .addMethod(getCoordinateRideStreamMethod())
              .addMethod(getChangedPositionStreamMethod())
              .addMethod(getGoodbyeMethod())
              .build();
        }
      }
    }
    return result;
  }
}
