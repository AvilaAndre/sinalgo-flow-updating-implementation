package projects.flowupdatingpairwise.nodes.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import projects.flowupdatingpairwise.nodes.nodeImplementations.FlowUpdatingPairwiseNode;
import sinalgo.nodes.messages.Message;

/**
 * The Messages that are sent by the S1Nodes in the Sample1 projects. They
 * contain one int as payload.
 */
@Getter
@Setter
@AllArgsConstructor
public class FlowUpdatingPairwiseMsg extends Message {

    private FlowUpdatingPairwiseNode sender;
    private double flow;
    private double estimate;

    @Override
    public Message clone() {
        return new FlowUpdatingPairwiseMsg(this.sender, this.flow, this.estimate);
    }

    @Override
    public String toString() {
        return "(" + this.sender + ", " + this.flow + ", " + this.estimate + ")";
    }
}
