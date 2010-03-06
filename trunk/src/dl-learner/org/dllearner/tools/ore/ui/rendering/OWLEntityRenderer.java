package org.dllearner.tools.ore.ui.rendering;

import org.semanticweb.owl.model.OWLEntity;

public class OWLEntityRenderer {
	public String render(OWLEntity entity) {
		try {
			String rendering = entity.getURI().getFragment();
			if (rendering == null) {
				// Get last bit of path
				String path = entity.getURI().getPath();
				if (path == null) {
					return entity.getURI().toString();
				}
				return entity.getURI().getPath().substring(
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
