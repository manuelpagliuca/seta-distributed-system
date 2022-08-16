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
      org.example.grpc.IPC.Response> getPresentMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "present",
      requestType = org.example.grpc.IPC.Infos.class,
      responseType = org.example.grpc.IPC.Response.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.example.grpc.IPC.Infos,
      org.example.grpc.IPC.Response> getPresentMethod() {
    io.grpc.MethodDescriptor<org.example.grpc.IPC.Infos, org.example.grpc.IPC.Response> getPresentMethod;
    if ((getPresentMethod = IPCServiceGrpc.getPresentMethod) == null) {
      synchronized (IPCServiceGrpc.class) {
        if ((getPresentMethod = IPCServiceGrpc.getPresentMethod) == null) {
          IPCServiceGrpc.getPresentMethod = getPresentMethod =
              io.grpc.MethodDescriptor.<org.example.grpc.IPC.Infos, org.example.grpc.IPC.Response>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "present"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.IPC.Infos.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.IPC.Response.getDefaultInstance()))
              .setSchemaDescriptor(new IPCServiceMethodDescriptorSupplier("present"))
              .build();
        }
      }
    }
    return getPresentMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.example.grpc.IPC.RideCharge,
      org.example.grpc.IPC.ACK> getCompareDistancesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "compareDistances",
      requestType = org.example.grpc.IPC.RideCharge.class,
      responseType = org.example.grpc.IPC.ACK.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.example.grpc.IPC.RideCharge,
      org.example.grpc.IPC.ACK> getCompareDistancesMethod() {
    io.grpc.MethodDescriptor<org.example.grpc.IPC.RideCharge, org.example.grpc.IPC.ACK> getCompareDistancesMethod;
    if ((getCompareDistancesMethod = IPCServiceGrpc.getCompareDistancesMethod) == null) {
      synchronized (IPCServiceGrpc.class) {
        if ((getCompareDistancesMethod = IPCServiceGrpc.getCompareDistancesMethod) == null) {
          IPCServiceGrpc.getCompareDistancesMethod = getCompareDistancesMethod =
              io.grpc.MethodDescriptor.<org.example.grpc.IPC.RideCharge, org.example.grpc.IPC.ACK>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "compareDistances"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.IPC.RideCharge.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.IPC.ACK.getDefaultInstance()))
              .setSchemaDescriptor(new IPCServiceMethodDescriptorSupplier("compareDistances"))
              .build();
        }
      }
    }
    return getCompareDistancesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.example.grpc.IPC.Infos,
      org.example.grpc.IPC.ACK> getChangedPositionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "changedPosition",
      requestType = org.example.grpc.IPC.Infos.class,
      responseType = org.example.grpc.IPC.ACK.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.example.grpc.IPC.Infos,
      org.example.grpc.IPC.ACK> getChangedPositionMethod() {
    io.grpc.MethodDescriptor<org.example.grpc.IPC.Infos, org.example.grpc.IPC.ACK> getChangedPositionMethod;
    if ((getChangedPositionMethod = IPCServiceGrpc.getChangedPositionMethod) == null) {
      synchronized (IPCServiceGrpc.class) {
        if ((getChangedPositionMethod = IPCServiceGrpc.getChangedPositionMethod) == null) {
          IPCServiceGrpc.getChangedPositionMethod = getChangedPositionMethod =
              io.grpc.MethodDescriptor.<org.example.grpc.IPC.Infos, org.example.grpc.IPC.ACK>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "changedPosition"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.IPC.Infos.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.IPC.ACK.getDefaultInstance()))
              .setSchemaDescriptor(new IPCServiceMethodDescriptorSupplier("changedPosition"))
              .build();
        }
      }
    }
    return getChangedPositionMethod;
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
        io.grpc.stub.StreamObserver<org.example.grpc.IPC.Response> responseObserver) {
      asyncUnimplementedUnaryCall(getPresentMethod(), responseObserver);
    }

    /**
     */
    public void compareDistances(org.example.grpc.IPC.RideCharge request,
        io.grpc.stub.StreamObserver<org.example.grpc.IPC.ACK> responseObserver) {
      asyncUnimplementedUnaryCall(getCompareDistancesMethod(), responseObserver);
    }

    /**
     */
    public void changedPosition(org.example.grpc.IPC.Infos request,
        io.grpc.stub.StreamObserver<org.example.grpc.IPC.ACK> responseObserver) {
      asyncUnimplementedUnaryCall(getChangedPositionMethod(), responseObserver);
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
                org.example.grpc.IPC.Response>(
                  this, METHODID_PRESENT)))
          .addMethod(
            getCompareDistancesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.example.grpc.IPC.RideCharge,
                org.example.grpc.IPC.ACK>(
                  this, METHODID_COMPARE_DISTANCES)))
          .addMethod(
            getChangedPositionMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.example.grpc.IPC.Infos,
                org.example.grpc.IPC.ACK>(
                  this, METHODID_CHANGED_POSITION)))
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
        io.grpc.stub.StreamObserver<org.example.grpc.IPC.Response> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPresentMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void compareDistances(org.example.grpc.IPC.RideCharge request,
        io.grpc.stub.StreamObserver<org.example.grpc.IPC.ACK> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCompareDistancesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void changedPosition(org.example.grpc.IPC.Infos request,
        io.grpc.stub.StreamObserver<org.example.grpc.IPC.ACK> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getChangedPositionMethod(), getCallOptions()), request, responseObserver);
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
    public org.example.grpc.IPC.Response present(org.example.grpc.IPC.Infos request) {
      return blockingUnaryCall(
          getChannel(), getPresentMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.example.grpc.IPC.ACK compareDistances(org.example.grpc.IPC.RideCharge request) {
      return blockingUnaryCall(
          getChannel(), getCompareDistancesMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.example.grpc.IPC.ACK changedPosition(org.example.grpc.IPC.Infos request) {
      return blockingUnaryCall(
          getChannel(), getChangedPositionMethod(), getCallOptions(), request);
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
    public com.google.common.util.concurrent.ListenableFuture<org.example.grpc.IPC.Response> present(
        org.example.grpc.IPC.Infos request) {
      return futureUnaryCall(
          getChannel().newCall(getPresentMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.example.grpc.IPC.ACK> compareDistances(
        org.example.grpc.IPC.RideCharge request) {
      return futureUnaryCall(
          getChannel().newCall(getCompareDistancesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.example.grpc.IPC.ACK> changedPosition(
        org.example.grpc.IPC.Infos request) {
      return futureUnaryCall(
          getChannel().newCall(getChangedPositionMethod(), getCallOptions()), request);
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
  private static final int METHODID_COMPARE_DISTANCES = 1;
  private static final int METHODID_CHANGED_POSITION = 2;
  private static final int METHODID_GOODBYE = 3;

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
              (io.grpc.stub.StreamObserver<org.example.grpc.IPC.Response>) responseObserver);
          break;
        case METHODID_COMPARE_DISTANCES:
          serviceImpl.compareDistances((org.example.grpc.IPC.RideCharge) request,
              (io.grpc.stub.StreamObserver<org.example.grpc.IPC.ACK>) responseObserver);
          break;
        case METHODID_CHANGED_POSITION:
          serviceImpl.changedPosition((org.example.grpc.IPC.Infos) request,
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
              .addMethod(getCompareDistancesMethod())
              .addMethod(getChangedPositionMethod())
              .addMethod(getGoodbyeMethod())
              .build();
        }
      }
    }
    return result;
  }
}
