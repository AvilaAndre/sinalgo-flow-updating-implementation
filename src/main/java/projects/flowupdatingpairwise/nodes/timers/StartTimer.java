package projects.flowupdatingpairwise.nodes.timers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import projects.flowupdatingpairwise.nodes.nodeImplementations.FlowUpdatingPairwiseNode;
import sinalgo.nodes.timers.Timer;

/**
 * A timer to call the start method.
 */
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class StartTimer extends Timer {

    private FlowUpdatingPairwiseNode node;
    private double delay;

    // Starts by itself when instantiated
    public StartTimer(double delay, FlowUpdatingPairwiseNode receiver) {
        this.setDelay(delay);
        this.setNode(receiver);
        this.startRelative(this.getDelay(), this.getNode());
    }

    @Override
    public void fire() {
        this.getNode().start();
    }
}
