package projects.flowupdatingsync.nodes.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import sinalgo.nodes.messages.Message;

/**
 * The Messages that are sent by the S1Nodes in the Sample1 projects. They
 * contain one int as payload.
 */
@Getter
@Setter
@AllArgsConstructor
public class FlowUpdatingSyncMsg extends Message {

    private long senderId;
    private double flow;
    private double estimate;

    @Override
    public Message clone() {
        return new FlowUpdatingSyncMsg(this.senderId, this.flow, this.estimate);
    }

    @Override
    public String toString() {
        return "(" + this.senderId + ", " + this.flow + ", " + this.estimate + ")";
    }
}
