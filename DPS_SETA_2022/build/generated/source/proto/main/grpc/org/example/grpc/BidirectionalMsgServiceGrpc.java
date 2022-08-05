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
    comments = "Source: BidirectionalMsgService.proto")
public final class BidirectionalMsgServiceGrpc {

  private BidirectionalMsgServiceGrpc() {}

  public static final String SERVICE_NAME = "org.example.grpc.BidirectionalMsgService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.example.grpc.BidirectionalMsgServiceOuterClass.ClientRequest,
      org.example.grpc.BidirectionalMsgServiceOuterClass.ServerResponse> getRideProposalMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "rideProposal",
      requestType = org.example.grpc.BidirectionalMsgServiceOuterClass.ClientRequest.class,
      responseType = org.example.grpc.BidirectionalMsgServiceOuterClass.ServerResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.example.grpc.BidirectionalMsgServiceOuterClass.ClientRequest,
      org.example.grpc.BidirectionalMsgServiceOuterClass.ServerResponse> getRideProposalMethod() {
    io.grpc.MethodDescriptor<org.example.grpc.BidirectionalMsgServiceOuterClass.ClientRequest, org.example.grpc.BidirectionalMsgServiceOuterClass.ServerResponse> getRideProposalMethod;
    if ((getRideProposalMethod = BidirectionalMsgServiceGrpc.getRideProposalMethod) == null) {
      synchronized (BidirectionalMsgServiceGrpc.class) {
        if ((getRideProposalMethod = BidirectionalMsgServiceGrpc.getRideProposalMethod) == null) {
          BidirectionalMsgServiceGrpc.getRideProposalMethod = getRideProposalMethod =
              io.grpc.MethodDescriptor.<org.example.grpc.BidirectionalMsgServiceOuterClass.ClientRequest, org.example.grpc.BidirectionalMsgServiceOuterClass.ServerResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "rideProposal"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.BidirectionalMsgServiceOuterClass.ClientRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.BidirectionalMsgServiceOuterClass.ServerResponse.getDefaultInstance()))
              .setSchemaDescriptor(new BidirectionalMsgServiceMethodDescriptorSupplier("rideProposal"))
              .build();
        }
      }
    }
    return getRideProposalMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.example.grpc.BidirectionalMsgServiceOuterClass.ClientRequest,
      org.example.grpc.BidirectionalMsgServiceOuterClass.ServerResponse> getStreamRideProposalMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "streamRideProposal",
      requestType = org.example.grpc.BidirectionalMsgServiceOuterClass.ClientRequest.class,
      responseType = org.example.grpc.BidirectionalMsgServiceOuterClass.ServerResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<org.example.grpc.BidirectionalMsgServiceOuterClass.ClientRequest,
      org.example.grpc.BidirectionalMsgServiceOuterClass.ServerResponse> getStreamRideProposalMethod() {
    io.grpc.MethodDescriptor<org.example.grpc.BidirectionalMsgServiceOuterClass.ClientRequest, org.example.grpc.BidirectionalMsgServiceOuterClass.ServerResponse> getStreamRideProposalMethod;
    if ((getStreamRideProposalMethod = BidirectionalMsgServiceGrpc.getStreamRideProposalMethod) == null) {
      synchronized (BidirectionalMsgServiceGrpc.class) {
        if ((getStreamRideProposalMethod = BidirectionalMsgServiceGrpc.getStreamRideProposalMethod) == null) {
          BidirectionalMsgServiceGrpc.getStreamRideProposalMethod = getStreamRideProposalMethod =
              io.grpc.MethodDescriptor.<org.example.grpc.BidirectionalMsgServiceOuterClass.ClientRequest, org.example.grpc.BidirectionalMsgServiceOuterClass.ServerResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "streamRideProposal"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.BidirectionalMsgServiceOuterClass.ClientRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.BidirectionalMsgServiceOuterClass.ServerResponse.getDefaultInstance()))
              .setSchemaDescriptor(new BidirectionalMsgServiceMethodDescriptorSupplier("streamRideProposal"))
              .build();
        }
      }
    }
    return getStreamRideProposalMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static BidirectionalMsgServiceStub newStub(io.grpc.Channel channel) {
    return new BidirectionalMsgServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static BidirectionalMsgServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new BidirectionalMsgServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static BidirectionalMsgServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new BidirectionalMsgServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class BidirectionalMsgServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void rideProposal(org.example.grpc.BidirectionalMsgServiceOuterClass.ClientRequest request,
        io.grpc.stub.StreamObserver<org.example.grpc.BidirectionalMsgServiceOuterClass.ServerResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getRideProposalMethod(), responseObserver);
    }

    /**
     */
    public void streamRideProposal(org.example.grpc.BidirectionalMsgServiceOuterClass.ClientRequest request,
        io.grpc.stub.StreamObserver<org.example.grpc.BidirectionalMsgServiceOuterClass.ServerResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getStreamRideProposalMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getRideProposalMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.example.grpc.BidirectionalMsgServiceOuterClass.ClientRequest,
                org.example.grpc.BidirectionalMsgServiceOuterClass.ServerResponse>(
                  this, METHODID_RIDE_PROPOSAL)))
          .addMethod(
            getStreamRideProposalMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                org.example.grpc.BidirectionalMsgServiceOuterClass.ClientRequest,
                org.example.grpc.BidirectionalMsgServiceOuterClass.ServerResponse>(
                  this, METHODID_STREAM_RIDE_PROPOSAL)))
          .build();
    }
  }

  /**
   */
  public static final class BidirectionalMsgServiceStub extends io.grpc.stub.AbstractStub<BidirectionalMsgServiceStub> {
    private BidirectionalMsgServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private BidirectionalMsgServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BidirectionalMsgServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new BidirectionalMsgServiceStub(channel, callOptions);
    }

    /**
     */
    public void rideProposal(org.example.grpc.BidirectionalMsgServiceOuterClass.ClientRequest request,
        io.grpc.stub.StreamObserver<org.example.grpc.BidirectionalMsgServiceOuterClass.ServerResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getRideProposalMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void streamRideProposal(org.example.grpc.BidirectionalMsgServiceOuterClass.ClientRequest request,
        io.grpc.stub.StreamObserver<org.example.grpc.BidirectionalMsgServiceOuterClass.ServerResponse> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getStreamRideProposalMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class BidirectionalMsgServiceBlockingStub extends io.grpc.stub.AbstractStub<BidirectionalMsgServiceBlockingStub> {
    private BidirectionalMsgServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private BidirectionalMsgServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BidirectionalMsgServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new BidirectionalMsgServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public org.example.grpc.BidirectionalMsgServiceOuterClass.ServerResponse rideProposal(org.example.grpc.BidirectionalMsgServiceOuterClass.ClientRequest request) {
      return blockingUnaryCall(
          getChannel(), getRideProposalMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<org.example.grpc.BidirectionalMsgServiceOuterClass.ServerResponse> streamRideProposal(
        org.example.grpc.BidirectionalMsgServiceOuterClass.ClientRequest request) {
      return blockingServerStreamingCall(
          getChannel(), getStreamRideProposalMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class BidirectionalMsgServiceFutureStub extends io.grpc.stub.AbstractStub<BidirectionalMsgServiceFutureStub> {
    private BidirectionalMsgServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private BidirectionalMsgServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BidirectionalMsgServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new BidirectionalMsgServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.example.grpc.BidirectionalMsgServiceOuterClass.ServerResponse> rideProposal(
        org.example.grpc.BidirectionalMsgServiceOuterClass.ClientRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getRideProposalMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_RIDE_PROPOSAL = 0;
  private static final int METHODID_STREAM_RIDE_PROPOSAL = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final BidirectionalMsgServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(BidirectionalMsgServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_RIDE_PROPOSAL:
          serviceImpl.rideProposal((org.example.grpc.BidirectionalMsgServiceOuterClass.ClientRequest) request,
              (io.grpc.stub.StreamObserver<org.example.grpc.BidirectionalMsgServiceOuterClass.ServerResponse>) responseObserver);
          break;
        case METHODID_STREAM_RIDE_PROPOSAL:
          serviceImpl.streamRideProposal((org.example.grpc.BidirectionalMsgServiceOuterClass.ClientRequest) request,
              (io.grpc.stub.StreamObserver<org.example.grpc.BidirectionalMsgServiceOuterClass.ServerResponse>) responseObserver);
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

  private static abstract class BidirectionalMsgServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    BidirectionalMsgServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.example.grpc.BidirectionalMsgServiceOuterClass.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("BidirectionalMsgService");
    }
  }

  private static final class BidirectionalMsgServiceFileDescriptorSupplier
      extends BidirectionalMsgServiceBaseDescriptorSupplier {
    BidirectionalMsgServiceFileDescriptorSupplier() {}
  }

  private static final class BidirectionalMsgServiceMethodDescriptorSupplier
      extends BidirectionalMsgServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    BidirectionalMsgServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (BidirectionalMsgServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new BidirectionalMsgServiceFileDescriptorSupplier())
              .addMethod(getRideProposalMethod())
              .addMethod(getStreamRideProposalMethod())
              .build();
        }
      }
    }
    return result;
  }
}
