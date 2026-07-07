package com.fes.flashcard.installer.page;

import com.fes.flashcard.installer.toast.StatusBar;
import com.fes.flashcard.installer.toast.Toast;
import com.fes.flashcard.installer.validation.ValidationResults;
import com.fes.flashcard.installer.validation.ValidationResultsDialog;

import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.LayerUI;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.List;

import static com.fes.flashcard.installer.validation.Severity.ERROR;
import static com.fes.flashcard.installer.validation.Severity.INFO;
import static com.fes.flashcard.installer.validation.Severity.WARNING;
import static java.awt.AWTEvent.MOUSE_EVENT_MASK;
import static java.awt.AWTEvent.MOUSE_MOTION_EVENT_MASK;
import static java.awt.Cursor.HAND_CURSOR;
import static java.awt.Cursor.getDefaultCursor;
import static java.awt.event.MouseEvent.MOUSE_CLICKED;
import static javax.swing.SwingUtilities.windowForComponent;

public abstract class Page {

	protected final PageData pageData;
	protected final JPanel   content = new JPanel(new GridBagLayout());

	private final StatusBar      statusBar      = new StatusBar();
	private final JLayer<JPanel> statusBarLayer = new JLayer<>(content, new ToastLayerUI());

	private boolean           isValid;
	private ValidationResults latestValidationResults = new ValidationResults(List.of());

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
		latestValidationResults = pageData.validate();
		ValidationSummary summary = new ValidationSummary(latestValidationResults);

		if (pageData.validate().contains(ERROR)) {
			statusBar.displayError(summary.mostImportantError());
		} else if (pageData.validate().contains(WARNING)) {
			statusBar.displayWarning(summary.first(WARNING));
		} else if (pageData.validate().contains(INFO)) {
			statusBar.displayInfo(summary.first(INFO));
		} else {
			statusBar.toast().setVisible(false);
		}
		statusBarLayer.repaint();

		isValid = !pageData.validate().contains(ERROR);
		onValidationChanged.run();
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

	@SuppressWarnings("rawtypes")
	class ToastLayerUI extends LayerUI<JPanel> {

		private static final int GAP = 20;

		private boolean mouseIsInsideToast = false;

		@Override
		protected void processMouseEvent(MouseEvent e, JLayer<? extends JPanel> l) {
			if (!mouseIsInsideToast) {
				return;
			}
			if (e.getID() == MOUSE_CLICKED) {
				new ValidationResultsDialog(windowForComponent(statusBarLayer), latestValidationResults).setVisible(true);
			}
		}


		@Override
		protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JPanel> layer) {
			Rectangle bounds          = statusBar.toast().getBounds();
			boolean   currentlyInside = bounds.contains(e.getPoint());
			if (currentlyInside != mouseIsInsideToast) {
				mouseIsInsideToast = currentlyInside;
				layer.setCursor(currentlyInside ? new Cursor(HAND_CURSOR) : getDefaultCursor());
			}
		}


		@Override
		public void paint(Graphics g, JComponent c) {
			super.paint(g, c);

			Toast toast = statusBar.toast();

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

		@Override
		public void installUI(JComponent c) {
			super.installUI(c);
			var layer = (JLayer) c;
			layer.setLayerEventMask(MOUSE_EVENT_MASK | MOUSE_MOTION_EVENT_MASK);
		}

		@Override
		public void uninstallUI(JComponent c) {
			var layer = (JLayer) c;
			layer.setLayerEventMask(0);
			super.uninstallUI(c);
		}
	}


	@Override
	public String toString() {
		return getTitle();
	}
}
