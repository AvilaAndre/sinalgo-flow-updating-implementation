package projects.flowupdatingpairwise.nodes.timers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import projects.flowupdatingpairwise.nodes.nodeImplementations.FlowUpdatingPairwiseNode;
import sinalgo.nodes.timers.Timer;

/**
 * A timer to call the tick method.
 */
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class TickTimer extends Timer {

    private FlowUpdatingPairwiseNode node;
    private double interval;

    // Starts by itself when instantiated
    public TickTimer(double interval, FlowUpdatingPairwiseNode receiver) {
        this.setInterval(interval);
        this.setNode(receiver);
        this.startRelative(this.getInterval(), this.getNode());
    }

    @Override
    public void fire() {
        this.getNode().tick();
        new TickTimer(this.getInterval(), this.getNode());
    }

}
