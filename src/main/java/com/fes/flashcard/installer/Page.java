package com.fes.flashcard.installer;

import com.fes.flashcard.installer.toast.StatusBar;
import com.fes.flashcard.installer.toast.Toast;
import com.fes.flashcard.installer.validation.Severity;
import com.fes.flashcard.installer.validation.ValidationResult;
import com.fes.flashcard.installer.validation.ValidationResults;

import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.util.stream.Stream;

import static com.fes.flashcard.installer.validation.Severity.ERROR;
import static com.fes.flashcard.installer.validation.Severity.INFO;
import static com.fes.flashcard.installer.validation.Severity.WARNING;
import static java.util.Comparator.comparing;
import static javax.swing.SwingUtilities.invokeLater;

public abstract class Page {

	protected final PageData  pageData;
	protected final ButtonBar buttonBar;

	protected final JPanel content = new JPanel(new GridBagLayout());

	private final StatusBar      statusBar      = new StatusBar();
	private final JLayer<JPanel> statusBarLayer = new JLayer<>(content, new ToastLayerUI(statusBar.toast()));

	public Page(PageData pageData, ButtonBar buttonBar) {
		this.pageData = pageData;
		this.buttonBar = buttonBar;

		statusBar.toast().setVisible(false);
	}

	private boolean updating;

	protected final void pageChanged() {
		if (updating) return;

		updating = true;
		updatePageData();

		evaluateValidationResults();

		invokeLater(() -> {
			updateGUI();
			updateDependantValues();
			updating = false;
		});
	}

	private void evaluateValidationResults() {
		ValidationResults results = pageData.validate();
		if (results.contains(ERROR)) {
			statusBar.displayError(filter(results, ERROR).min(comparing(ValidationResult::getPriority)).get().getDescription());
		} else if (results.contains(WARNING)) {
			statusBar.displayWarning(filter(results, WARNING).findFirst().get().getDescription());
		} else if (results.contains(INFO)) {
			statusBar.displayInfo(filter(results, INFO).findFirst().get().getDescription());
		} else {
			statusBar.toast().setVisible(false);
		}
		statusBarLayer.repaint();

		buttonBar.getNextButton().setEnabled(!results.contains(ERROR));
	}

	private static Stream<ValidationResult> filter(ValidationResults results, Severity severity) {
		return results.list().stream().filter(result -> result.getSeverity() == severity);
	}


	public abstract void build();

	protected abstract void addListeners();


	public void willBecomeVisible() {
		pageData.load();
		fillGUI();
		addListeners();
		evaluateValidationResults();
	}

	public void willBecomeInvisible() {
		pageData.save();
	}

	protected abstract void fillGUI();

	protected abstract void updatePageData();

	public abstract void updateGUI();

	public abstract void updateDependantValues();

	public abstract String getTitle();

	public abstract String getDescription();

	public JComponent getContent() {
		return statusBarLayer;
	}

	static class ToastLayerUI extends LayerUI<JPanel> {

		private final Toast toast;

		ToastLayerUI(Toast toast) { this.toast = toast; }

		@Override
		public void paint(Graphics g, JComponent c) {
			super.paint(g, c);

			Graphics2D g2 = (Graphics2D) g.create();

			if (!toast.isVisible()) {
				return;
			}
			var size = toast.getPreferredSize();
			int x    = (c.getWidth() - size.width) / 2;
			int y    = c.getHeight() - size.height - 20;

			toast.setBounds(x, y, size.width, size.height);
			g2.translate(x, y);
			toast.doLayout();
			toast.print(g2);

			g2.dispose();
		}
	}

}
