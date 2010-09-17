package org.dllearner.tools.ore.ui.editor;

public interface VerifiedInputEditor {

	void addStatusChangedListener(
			InputVerificationStatusChangedListener listener);

	void removeStatusChangedListener(
			InputVerificationStatusChangedListener listener);
}
