package projects.flowupdatingsync.nodes.nodeImplementations;

import lombok.Getter;
import lombok.Setter;
import projects.flowupdatingsync.nodes.messages.FlowUpdatingSyncMsg;
import sinalgo.exception.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.logging.Logging;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * The Node of the Flow Updating algorithm in a synchronous network model
 */
@Getter
@Setter
public class FlowUpdatingSyncNode extends Node {

    @Getter
    private double value;
    private HashMap<Long, Double> initialFlows = new HashMap<>();
    private HashMap<Long, FlowUpdatingSyncMsg> rcvdMsgs = new HashMap<>();

    Logging log = Logging.getLogger("flow_updating_synchronous_log");

    private FlowUpdatingSyncMsg generateMessage(long idx) {
        return new FlowUpdatingSyncMsg(
                this.getID(),
                this.getInitialFlows().getOrDefault(idx, 0.0),
                this.estimate(this.initialFlows));
    }

    private double estimate(HashMap<Long, Double> flows) {
        return this.value - flows.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    private void stateTransition() {
        ArrayList<Long> neighborIds = new ArrayList<>();
        for (Edge e : this.getOutgoingConnections()) {
            neighborIds.add(e.getEndNode().getID());
        }

        HashMap<Long, Double> stateF = new HashMap<>();

        for (Long neighID : neighborIds) {
            if (this.rcvdMsgs.containsKey(neighID)) {
                FlowUpdatingSyncMsg msg = rcvdMsgs.get(neighID);
                stateF.put(neighID, msg.getFlow() * -1);
            } else if (this.initialFlows.containsKey(neighID)) {
                stateF.put(neighID, this.initialFlows.get(neighID));
            }
        }

        HashMap<Long, Double> stateE = new HashMap<>();
        // using the newly updated flows in F - using F
        stateE.put(this.getID(), this.estimate(stateF));

        for (Long neighID : neighborIds) {
            if (this.rcvdMsgs.containsKey(neighID)) {
                FlowUpdatingSyncMsg msg = rcvdMsgs.get(neighID);
                stateE.put(neighID, msg.getEstimate());
            } else {
                // using Fi
                stateE.put(neighID, this.estimate(initialFlows));
            }
        }

        Double avg = stateE.size() > 0
                ? stateE.values().stream().reduce(0.0, (acc, val) -> acc + val) / stateE.size()
                : 0.0;

        HashMap<Long, Double> fPrime = new HashMap<>();
        for (Long key : stateF.keySet()) {
            fPrime.put(key, stateF.get(key) + avg - stateE.get(key));
        }

        // Set flows for next round
        this.setInitialFlows(fPrime);
    }

    @Override
    public void handleMessages(Inbox inbox) {
        while (inbox.hasNext()) {
            Message msg = inbox.next();
            if (msg instanceof FlowUpdatingSyncMsg) {
                FlowUpdatingSyncMsg flowMsg = (FlowUpdatingSyncMsg) msg;
                rcvdMsgs.put(flowMsg.getSenderId(), flowMsg);
            }
        }
    }

    @Override
    public void preStep() {
        ArrayList<Long> neighborIds = new ArrayList<>();
        for (Edge e : this.getOutgoingConnections()) {
            neighborIds.add(e.getEndNode().getID());
        }

        this.rcvdMsgs = new HashMap<>();

        for (Edge e : this.getOutgoingConnections()) {
            if (e.getEndNode().getClass() != FlowUpdatingSyncNode.class)
                continue;

            FlowUpdatingSyncNode node = (FlowUpdatingSyncNode) e.getEndNode();

            this.send(this.generateMessage(node.getID()), node);
        }
    }

    @Override
    public void init() {
        // initialize the node
        this.value = new Random().nextInt(40) + 10;
    }

    @Override
    public void neighborhoodChange() {
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        // set the color of this node
        String text = Double.toString(this.value) + "|"
                + Double.parseDouble(new DecimalFormat("##.##").format(this.estimate(initialFlows)));

        // values
        // draw the node as a circle with the text inside
        super.drawNodeAsDiskWithText(g, pt, highlight, text, 10, Color.YELLOW);
    }

    // State Transition
    @Override
    public void postStep() {
        this.stateTransition();
    }

    @Override
    public String toString() {
        return "The value is: "
                + this.value
                + "\nThe estimate is: "
                + this.estimate(initialFlows);
    }

    @Override
    public void checkRequirements() throws WrongConfigurationException {
    }
}
