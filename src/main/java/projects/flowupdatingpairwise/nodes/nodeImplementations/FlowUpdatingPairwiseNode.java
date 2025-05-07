package projects.flowupdatingpairwise.nodes.nodeImplementations;

import lombok.Getter;
import lombok.Setter;
import projects.flowupdatingpairwise.nodes.messages.FlowUpdatingPairwiseMsg;
import projects.flowupdatingpairwise.nodes.timers.StartTimer;
import projects.flowupdatingpairwise.nodes.timers.TickTimer;
import sinalgo.exception.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;
import sinalgo.tools.logging.Logging;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * The Node of the first Flow Updating algorithm in an asynchronous network
 * model
 */
@Getter
@Setter
public class FlowUpdatingPairwiseNode extends Node {

    @Getter
    private double value;
    private int TIMEOUT_VAL = 50;
    private HashMap<Long, Double> flows = new HashMap<>();
    private HashMap<Long, Double> neighborsEstimates = new HashMap<>();
    private HashMap<Long, Integer> ticksSinceLastAvg = new HashMap<>();
    private double lastAverage = 0.0;

    Logging log = Logging.getLogger("flow_updating_asynchronous_1_log");

    private ArrayList<FlowUpdatingPairwiseNode> getFlowUpdatingPairwiseNodeNeighbors() {
        ArrayList<FlowUpdatingPairwiseNode> neighbors = new ArrayList<>();
        for (Edge e : this.getOutgoingConnections()) {
            if (e.getEndNode().getClass() == FlowUpdatingPairwiseNode.class)
                neighbors.add((FlowUpdatingPairwiseNode) e.getEndNode());
        }
        return neighbors;
    }

    private void averageAndSend(FlowUpdatingPairwiseNode node) {
        double flowsSum = getFlowUpdatingPairwiseNodeNeighbors()
                .stream()
                .map((n) -> this.flows.getOrDefault(n.getID(), 0.0))
                .reduce(0.0, (acc, flow) -> acc + flow);

        double estimate = this.value - flowsSum;

        double avg = (this.neighborsEstimates.getOrDefault(node.getID(), 0.0) + estimate) / 2.0;

        this.lastAverage = avg;

        this.flows.put(
                node.getID(),
                this.flows.getOrDefault(node.getID(), 0.0)
                        + avg
                        - this.neighborsEstimates.getOrDefault(node.getID(), 0.0));

        this.neighborsEstimates.put(node.getID(), avg);

        this.ticksSinceLastAvg.put(node.getID(), 0);

        FlowUpdatingPairwiseMsg msg = new FlowUpdatingPairwiseMsg(
                this,
                this.flows.getOrDefault(node.getID(), 0.0),
                avg);
        this.send(msg, node);
    }

    public void tick() {
        for (FlowUpdatingPairwiseNode neigh : getFlowUpdatingPairwiseNodeNeighbors()) {
            this.ticksSinceLastAvg.put(neigh.getID(), this.ticksSinceLastAvg.getOrDefault(neigh.getID(), 0) + 1);

            if (this.ticksSinceLastAvg.get(neigh.getID()) > TIMEOUT_VAL) {
                this.averageAndSend(neigh);
            }
        }

    }

    private void onReceive(FlowUpdatingPairwiseNode node, double flow, double estimate) {
        this.neighborsEstimates.put(node.getID(), estimate);
        this.flows.put(node.getID(), -flow);
        this.averageAndSend(node);
    }

    @Override
    public void handleMessages(Inbox inbox) {
        while (inbox.hasNext()) {
            Message msg = inbox.next();
            if (msg instanceof FlowUpdatingPairwiseMsg) {
                FlowUpdatingPairwiseMsg flowMsg = (FlowUpdatingPairwiseMsg) msg;
                this.onReceive(flowMsg.getSender(), flowMsg.getFlow(), flowMsg.getEstimate());
            }
        }
    }

    @Override
    public void preStep() {
        tick();
    }

    @Override
    public void init() {
        // initialize the node
        this.value = new Random().nextInt(40) + 10;
        new TickTimer(10.0, this);

        // An asynchornous simulation requires events
        // to start, so sending messages right away will
        // not work.
        if (Global.isAsynchronousMode()) {
            new StartTimer(0.1, this);
        } else {
            start();
        }
    }

    public void start() {
        for (FlowUpdatingPairwiseNode neigh : getFlowUpdatingPairwiseNodeNeighbors()) {
            this.send(new FlowUpdatingPairwiseMsg(this, 0, this.value), neigh);
        }
    }

    @Override
    public void neighborhoodChange() {
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        // set the color of this node
        String text = Double.toString(this.value) + "|"
                + Double.parseDouble(new DecimalFormat("##.##").format(this.lastAverage));

        // values
        // draw the node as a circle with the text inside
        super.drawNodeAsDiskWithText(g, pt, highlight, text, 10, Color.YELLOW);
    }

    // State Transition
    @Override
    public void postStep() {
    }

    @Override
    public String toString() {
        return "The value is: "
                + this.value
                + "\nThe estimate is: "
                + Double.parseDouble(new DecimalFormat("##.##").format(this.lastAverage));
    }

    @Override
    public void checkRequirements() throws WrongConfigurationException {
    }
}
