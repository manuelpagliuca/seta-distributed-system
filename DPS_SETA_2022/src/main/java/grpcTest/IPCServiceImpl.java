package grpcTest;

import io.grpc.stub.StreamObserver;
import org.example.grpc.IPC;
import org.example.grpc.IPCServiceGrpc;

public class IPCServiceImpl extends IPCServiceGrpc.IPCServiceImplBase {
    @Override
    public void rideProposal(IPC.Proposal request, StreamObserver<IPC.Response> responseObserver) {
        System.out.println("Stub/String request" + request.getStringRequest());
    }

}
