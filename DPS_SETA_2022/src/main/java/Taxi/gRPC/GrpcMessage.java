package Taxi.gRPC;

import org.example.grpc.IPC;

/*
 * GrpcMessage
 * ------------------------------------------------------------------------------
 * Helper class for handling the different IPC objects for the services.
 */
public class GrpcMessage {
    private IPC.RideCharge rideCharge = null;
    private IPC.Infos infos = null;
    private IPC.RechargeProposal rechargeProposal = null;
    private IPC.ACK ackNotify = null;

    public IPC.RideCharge getRideCharge() {
        return rideCharge;
    }

    public void setRideCharge(IPC.RideCharge rideCharge) {
        this.rideCharge = rideCharge;
    }

    public IPC.Infos getInfos() {
        return infos;
    }

    public void setInfos(IPC.Infos infos) {
        this.infos = infos;
    }

    public IPC.RechargeProposal getRechargeProposal() {
        return rechargeProposal;
    }

    public void setRechargeProposal(IPC.RechargeProposal rechargeProposal) {
        this.rechargeProposal = rechargeProposal;
    }

    public IPC.ACK getACKNotify() {
        return ackNotify;
    }

    public void setAckNotify(IPC.ACK ackNotify) {
        this.ackNotify = ackNotify;
    }
}