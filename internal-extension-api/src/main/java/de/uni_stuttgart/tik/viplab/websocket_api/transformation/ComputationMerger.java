package de.uni_stuttgart.tik.viplab.websocket_api.transformation;

import de.uni_stuttgart.tik.viplab.websocket_api.model.Computation;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTask;
import de.uni_stuttgart.tik.viplab.websocket_api.model.ComputationTemplate;

public interface ComputationMerger {

	/**
	 * Merges the ComputationTemplate and the ComputationTask to a Computation.
	 * This also renders the template Parts of the ComputationTemplate files and
	 * substitutes all configuration parameter.
	 * 
	 * @param template
	 * @param task
	 * @return
	 */
	public Computation merge(ComputationTemplate template, ComputationTask task);

}
