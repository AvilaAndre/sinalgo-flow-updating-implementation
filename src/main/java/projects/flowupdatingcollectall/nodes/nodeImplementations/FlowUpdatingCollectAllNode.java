package projects.flowupdatingcollectall.nodes.nodeImplementations;

import lombok.Getter;
import lombok.Setter;
import projects.flowupdatingcollectall.nodes.messages.FlowUpdatingCollectAllMsg;
import projects.flowupdatingcollectall.nodes.timers.StartTimer;
import projects.flowupdatingcollectall.nodes.timers.TickTimer;
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
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The Node of the second Flow Updating algorithm in an asynchronous network
 * model
 */
@Getter
@Setter
public class FlowUpdatingCollectAllNode extends Node {
    @Getter
    private double value;
    private int TIMEOUT_VAL = 50;
    private HashMap<Long, Double> flows = new HashMap<>();
    private HashMap<Long, Double> neighborsEstimates = new HashMap<>();
    private HashSet<Long> idOfMsgsReceived = new HashSet<>();
    private int ticksSinceLastAvg = 0;

    private double lastAverage = 0.0;

    Logging log = Logging.getLogger("flow_updating_asynchronous_1_log");

    private ArrayList<FlowUpdatingCollectAllNode> getNeighbors() {
        ArrayList<FlowUpdatingCollectAllNode> neighbors = new ArrayList<>();
        for (Edge e : this.getOutgoingConnections()) {
            if (e.getEndNode().getClass() == FlowUpdatingCollectAllNode.class)
                neighbors.add((FlowUpdatingCollectAllNode) e.getEndNode());
        }
        return neighbors;
    }

    private void averageAndSend() {

        ArrayList<FlowUpdatingCollectAllNode> neighbors = getNeighbors();

        double flowsSum = neighbors
                .stream()
                .map((n) -> this.flows.getOrDefault(n.getID(), 0.0))
                .reduce(0.0, (acc, flow) -> acc + flow);

        double estimate = this.value - flowsSum;

        double avgSum = neighbors
                .stream()
                .map((n) -> this.neighborsEstimates.getOrDefault(n.getID(), 0.0))
                .reduce(0.0, (acc, flow) -> acc + flow);

        double avg = (estimate + avgSum) / (neighbors.size() + 1);
        this.lastAverage = avg;

        for (FlowUpdatingCollectAllNode node : neighbors) {
            double newFlow = this.flows.getOrDefault(node.getID(), 0.0)
                    + avg
                    - this.neighborsEstimates.getOrDefault(node.getID(), 0.0);
            this.flows.put(node.getID(), newFlow);
            this.neighborsEstimates.put(node.getID(), avg);

            this.send(new FlowUpdatingCollectAllMsg(this, newFlow, avg), node);
        }

        idOfMsgsReceived = new HashSet<>();
        ticksSinceLastAvg = 0;
    }

    public void tick() {
        this.ticksSinceLastAvg += 1;

        if (this.ticksSinceLastAvg >= this.TIMEOUT_VAL) {
            this.averageAndSend();
        }
    }

    private void onReceive(FlowUpdatingCollectAllNode node, double flow, double estimate) {
        this.neighborsEstimates.put(node.getID(), estimate);
        this.flows.put(node.getID(), -flow);
        this.idOfMsgsReceived.add(node.getID());

        Set<Long> neighborsIdsSet = this.getNeighbors().stream().map((n) -> n.getID()).collect(Collectors.toSet());
        if (idOfMsgsReceived.containsAll(neighborsIdsSet)) {
            this.averageAndSend();
        }
    }

    @Override
    public void handleMessages(Inbox inbox) {
        while (inbox.hasNext()) {
            Message msg = inbox.next();
            if (msg instanceof FlowUpdatingCollectAllMsg) {
                FlowUpdatingCollectAllMsg flowMsg = (FlowUpdatingCollectAllMsg) msg;
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
        for (FlowUpdatingCollectAllNode neigh : getNeighbors()) {
            this.send(new FlowUpdatingCollectAllMsg(this, 0, this.value), neigh);
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
