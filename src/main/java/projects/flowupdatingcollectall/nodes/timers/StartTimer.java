package projects.flowupdatingcollectall.nodes.timers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import projects.flowupdatingcollectall.nodes.nodeImplementations.FlowUpdatingCollectAllNode;
import sinalgo.nodes.timers.Timer;

/**
 * A timer to call the start method.
 */
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class StartTimer extends Timer {

    private FlowUpdatingCollectAllNode node;
    private double delay;

    // Starts by itself when instantiated
    public StartTimer(double delay, FlowUpdatingCollectAllNode receiver) {
        this.setDelay(delay);
        this.setNode(receiver);
        this.startRelative(this.getDelay(), this.getNode());
    }

    @Override
    public void fire() {
        this.getNode().start();
    }
}
