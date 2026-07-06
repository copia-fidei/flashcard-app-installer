package com.fes.flashcard.installer.page;

import com.fes.flashcard.installer.toast.StatusBar;
import com.fes.flashcard.installer.toast.Toast;
import com.fes.flashcard.installer.validation.Severity;
import com.fes.flashcard.installer.validation.ValidationResult;
import com.fes.flashcard.installer.validation.ValidationResults;

import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.util.stream.Stream;

import static com.fes.flashcard.installer.validation.Severity.ERROR;
import static com.fes.flashcard.installer.validation.Severity.INFO;
import static com.fes.flashcard.installer.validation.Severity.WARNING;
import static java.lang.IO.println;
import static java.util.Comparator.comparing;

public abstract class Page {

	protected final PageData pageData;
	//	protected final ButtonBar buttonBar;

	protected final JPanel content = new JPanel(new GridBagLayout());

	private final StatusBar      statusBar      = new StatusBar();
	private final JLayer<JPanel> statusBarLayer = new JLayer<>(content, new ToastLayerUI(statusBar.toast()));

	private boolean isValid;

	private final Runnable onValidationChanged;

	public Page(PageData pageData, Runnable onValidationChanged) {
		this.pageData = pageData;
		this.onValidationChanged = onValidationChanged;

		statusBar.toast().setVisible(false);
	}

	public boolean isValid() {
		return isValid;
	}

	protected final void pageChanged() {
		println("pageChanged");
		removeListeners();
		updatePageData();
		evaluateValidationResults();
		SwingUtilities.invokeLater(() -> {
			updateGUI();
			updateDependantValues();
			addListeners();
		});
	}

	private void evaluateValidationResults() {
		ValidationResults results = pageData.validate();
		ValidationSummary summary = new ValidationSummary(results);

		if (results.contains(ERROR)) {
			statusBar.displayError(summary.mostImportantError());
		} else if (results.contains(WARNING)) {
			statusBar.displayWarning(summary.first(WARNING));
		} else if (results.contains(INFO)) {
			statusBar.displayInfo(summary.first(INFO));
		} else {
			statusBar.toast().setVisible(false);
		}
		statusBarLayer.repaint();

		// TODO ValidationDialog

		isValid = !results.contains(ERROR);
		onValidationChanged.run();
	}

	record ValidationSummary(ValidationResults results) {

		String mostImportantError() {
			//noinspection OptionalGetWithoutIsPresent
			return filter(ERROR).min(comparing(ValidationResult::getPriority)).get().getDescription();
		}

		String first(Severity severity) {
			//noinspection OptionalGetWithoutIsPresent
			return filter(severity).findFirst().get().getDescription();
		}

		private Stream<ValidationResult> filter(Severity severity) {
			return results.list().stream().filter(result -> result.getSeverity() == severity);
		}
	}


	public abstract void build();

	protected abstract void addListeners();

	protected abstract void removeListeners();


	public void willBecomeVisible() {
		pageData.load();
		fillGUI();
		addListeners();
		evaluateValidationResults();
	}

	public void willBecomeInvisible() {
		removeListeners();
		pageData.save();
	}

	public void restoreDefaults() {
		removeListeners();
		pageData.loadDefaults();
		fillGUI();
		evaluateValidationResults();
		addListeners();
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

		private static final int GAP = 20;

		private final Toast toast;

		ToastLayerUI(Toast toast) { this.toast = toast; }

		@Override
		public void paint(Graphics g, JComponent c) {
			super.paint(g, c);

			if (!toast.isVisible()) {
				return;
			}

			var g2   = (Graphics2D) g.create();
			var size = toast.getPreferredSize();
			int x    = (c.getWidth() - size.width) / 2;
			int y    = c.getHeight() - size.height - GAP;

			toast.setBounds(x, y, size.width, size.height);
			g2.translate(x, y);
			toast.doLayout();
			toast.print(g2);

			g2.dispose();
		}
	}

	@Override
	public String toString() {
		return getTitle();
	}
}
