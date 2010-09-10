package org.dllearner.tools.ore.ui.rendering;

import org.semanticweb.owlapi.model.OWLEntity;

public class OWLEntityRenderer {
	public String render(OWLEntity entity) {
		try {
			String rendering = entity.getIRI().getFragment();
			if (rendering == null) {
				// Get last bit of path
				String path = entity.getIRI().toURI().getPath();
				if (path == null) {
					return entity.getIRI().toString();
				}
				return entity.getIRI().toURI().getPath().substring(
						path.lastIndexOf("/") + 1);
			}
			if (rendering.indexOf(' ') != -1 || rendering.indexOf('(') != -1
					|| rendering.indexOf(')') != -1) {
				return "'" + rendering + "'";
			} else {
				return rendering;
			}
		} catch (Exception e) {
			return "<Error! " + e.getMessage() + ">";
		}
	}
}
