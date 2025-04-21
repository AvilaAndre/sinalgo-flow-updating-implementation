package projects.flowupdatingsync.models.connectivityModels;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import sinalgo.models.ConnectivityModelHelper;
import sinalgo.nodes.Node;
import sinalgo.runtime.SinalgoRuntime;

public class CircleConnectivity extends ConnectivityModelHelper {

	@Override
	protected boolean isConnected(Node from, Node to) {
		int nNodes = SinalgoRuntime.getNodes().size();

		if ((from.getID() == 1 && to.getID() == nNodes) || (from.getID() == nNodes && to.getID() == 1))
			return true;

		return Math.abs(from.getID() - to.getID()) == 1;
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	// -
	// Code to initialize the static variables of this class
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	// -

	@Getter(AccessLevel.PRIVATE)
	@Setter(AccessLevel.PRIVATE)
	private static boolean initialized; // indicates whether the static fields of this class have already been
	// initialized

	public CircleConnectivity() {
		if (!isInitialized()) {
			setInitialized(true);
		}
	}

}
