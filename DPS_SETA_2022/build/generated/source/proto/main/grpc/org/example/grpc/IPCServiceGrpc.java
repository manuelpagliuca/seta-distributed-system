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
  private static volatile io.grpc.MethodDescriptor<org.example.grpc.IPC.Proposal,
      org.example.grpc.IPC.Response> getRideProposalMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "rideProposal",
      requestType = org.example.grpc.IPC.Proposal.class,
      responseType = org.example.grpc.IPC.Response.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.example.grpc.IPC.Proposal,
      org.example.grpc.IPC.Response> getRideProposalMethod() {
    io.grpc.MethodDescriptor<org.example.grpc.IPC.Proposal, org.example.grpc.IPC.Response> getRideProposalMethod;
    if ((getRideProposalMethod = IPCServiceGrpc.getRideProposalMethod) == null) {
      synchronized (IPCServiceGrpc.class) {
        if ((getRideProposalMethod = IPCServiceGrpc.getRideProposalMethod) == null) {
          IPCServiceGrpc.getRideProposalMethod = getRideProposalMethod =
              io.grpc.MethodDescriptor.<org.example.grpc.IPC.Proposal, org.example.grpc.IPC.Response>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "rideProposal"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.IPC.Proposal.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.IPC.Response.getDefaultInstance()))
              .setSchemaDescriptor(new IPCServiceMethodDescriptorSupplier("rideProposal"))
              .build();
        }
      }
    }
    return getRideProposalMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.example.grpc.IPC.Distance,
      org.example.grpc.IPC.Permission> getCheckDistanceMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "checkDistance",
      requestType = org.example.grpc.IPC.Distance.class,
      responseType = org.example.grpc.IPC.Permission.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.example.grpc.IPC.Distance,
      org.example.grpc.IPC.Permission> getCheckDistanceMethod() {
    io.grpc.MethodDescriptor<org.example.grpc.IPC.Distance, org.example.grpc.IPC.Permission> getCheckDistanceMethod;
    if ((getCheckDistanceMethod = IPCServiceGrpc.getCheckDistanceMethod) == null) {
      synchronized (IPCServiceGrpc.class) {
        if ((getCheckDistanceMethod = IPCServiceGrpc.getCheckDistanceMethod) == null) {
          IPCServiceGrpc.getCheckDistanceMethod = getCheckDistanceMethod =
              io.grpc.MethodDescriptor.<org.example.grpc.IPC.Distance, org.example.grpc.IPC.Permission>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "checkDistance"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.IPC.Distance.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.grpc.IPC.Permission.getDefaultInstance()))
              .setSchemaDescriptor(new IPCServiceMethodDescriptorSupplier("checkDistance"))
              .build();
        }
      }
    }
    return getCheckDistanceMethod;
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
    public void rideProposal(org.example.grpc.IPC.Proposal request,
        io.grpc.stub.StreamObserver<org.example.grpc.IPC.Response> responseObserver) {
      asyncUnimplementedUnaryCall(getRideProposalMethod(), responseObserver);
    }

    /**
     */
    public void checkDistance(org.example.grpc.IPC.Distance request,
        io.grpc.stub.StreamObserver<org.example.grpc.IPC.Permission> responseObserver) {
      asyncUnimplementedUnaryCall(getCheckDistanceMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getRideProposalMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.example.grpc.IPC.Proposal,
                org.example.grpc.IPC.Response>(
                  this, METHODID_RIDE_PROPOSAL)))
          .addMethod(
            getCheckDistanceMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.example.grpc.IPC.Distance,
                org.example.grpc.IPC.Permission>(
                  this, METHODID_CHECK_DISTANCE)))
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
    public void rideProposal(org.example.grpc.IPC.Proposal request,
        io.grpc.stub.StreamObserver<org.example.grpc.IPC.Response> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getRideProposalMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void checkDistance(org.example.grpc.IPC.Distance request,
        io.grpc.stub.StreamObserver<org.example.grpc.IPC.Permission> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCheckDistanceMethod(), getCallOptions()), request, responseObserver);
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
    public org.example.grpc.IPC.Response rideProposal(org.example.grpc.IPC.Proposal request) {
      return blockingUnaryCall(
          getChannel(), getRideProposalMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.example.grpc.IPC.Permission checkDistance(org.example.grpc.IPC.Distance request) {
      return blockingUnaryCall(
          getChannel(), getCheckDistanceMethod(), getCallOptions(), request);
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
    public com.google.common.util.concurrent.ListenableFuture<org.example.grpc.IPC.Response> rideProposal(
        org.example.grpc.IPC.Proposal request) {
      return futureUnaryCall(
          getChannel().newCall(getRideProposalMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.example.grpc.IPC.Permission> checkDistance(
        org.example.grpc.IPC.Distance request) {
      return futureUnaryCall(
          getChannel().newCall(getCheckDistanceMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_RIDE_PROPOSAL = 0;
  private static final int METHODID_CHECK_DISTANCE = 1;

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
        case METHODID_RIDE_PROPOSAL:
          serviceImpl.rideProposal((org.example.grpc.IPC.Proposal) request,
              (io.grpc.stub.StreamObserver<org.example.grpc.IPC.Response>) responseObserver);
          break;
        case METHODID_CHECK_DISTANCE:
          serviceImpl.checkDistance((org.example.grpc.IPC.Distance) request,
              (io.grpc.stub.StreamObserver<org.example.grpc.IPC.Permission>) responseObserver);
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
              .addMethod(getRideProposalMethod())
              .addMethod(getCheckDistanceMethod())
              .build();
        }
      }
    }
    return result;
  }
}
