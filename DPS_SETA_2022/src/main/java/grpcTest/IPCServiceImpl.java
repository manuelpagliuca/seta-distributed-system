package grpcTest;

import io.grpc.stub.StreamObserver;
import org.example.grpc.IPC;
import org.example.grpc.IPCServiceGrpc;

public class IPCServiceImpl extends IPCServiceGrpc.IPCServiceImplBase {
    @Override
    public void present(IPC.Infos request, StreamObserver<IPC.Response> responseObserver) {
        System.out.println("ACK, " + request.toString());
        responseObserver.onNext(IPC.Response.newBuilder().setStringResponse(request.toString()).build());
    }

    @Override
    public StreamObserver<IPC.Proposal> coordinate(StreamObserver<IPC.Response> responseObserver) {
        //it returns the stream that will be used by the clients to send messages. The client will write on this stream
        return new StreamObserver<>() {
            //receiving a message from the client
            public void onNext(IPC.Proposal clientRequest) {
                System.out.println("Something that the client reads");
                //String clientStringRequest = clientRequest.getStringRequest();
                //System.out.println("[From taxi client] " + clientStringRequest);

                // sending the response to the client
                //System.out.println("Sending the response to the client...\n");
                responseObserver.onNext(IPC.Response.newBuilder().setStringResponse("I've received this message: x").build());
            }

            public void onError(Throwable throwable) {
            }

            public void onCompleted() {
            }
        };

    }
}
