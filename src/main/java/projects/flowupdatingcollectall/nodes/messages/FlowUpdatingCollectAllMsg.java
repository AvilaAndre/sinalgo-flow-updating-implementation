package projects.flowupdatingcollectall.nodes.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import projects.flowupdatingcollectall.nodes.nodeImplementations.FlowUpdatingCollectAllNode;
import sinalgo.nodes.messages.Message;

/**
 * The Messages that are sent by the S1Nodes in the Sample1 projects. They
 * contain one int as payload.
 */
@Getter
@Setter
@AllArgsConstructor
public class FlowUpdatingCollectAllMsg extends Message {

    private FlowUpdatingCollectAllNode sender;
    private double flow;
    private double estimate;

    @Override
    public Message clone() {
        return new FlowUpdatingCollectAllMsg(this.sender, this.flow, this.estimate);
    }

    @Override
    public String toString() {
        return "(" + this.sender + ", " + this.flow + ", " + this.estimate + ")";
    }
}
