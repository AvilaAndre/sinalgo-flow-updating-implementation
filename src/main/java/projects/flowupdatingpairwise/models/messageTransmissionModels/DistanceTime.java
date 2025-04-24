package projects.flowupdatingpairwise.models.messageTransmissionModels;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import sinalgo.models.MessageTransmissionModel;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

/**
 * A message transmission model implementation that delivers all messages in the
 * following round, which corresponds to a constant time delay of 1. <br>
 * This model expects a configuration entry of the form
 * <code>&lt;MessageTransmission ConstantTime="..."&gt;</code> where
 * ConstantTime specifies the time a message needs to arrive.
 */
public class DistanceTime extends MessageTransmissionModel {

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private double timeMultiplier = 0.1;

    @Override
    public double timeToReach(Node startNode, Node endNode, Message msg) {
        double dist = startNode.getPosition().distanceTo(endNode.getPosition());
        return dist * this.getTimeMultiplier();
    }

}
