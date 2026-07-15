package com.fes.flashcard.installer.operation;

import javax.swing.SwingWorker;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class Operation extends SwingWorker<String, String> implements PropertyChangeListener {

	private OperationStatus status = OperationStatus.NOT_STARTED;

	private final List<OperationListener> listeners = new ArrayList<>();

	private final String title;
	private final String description;

	private final List<String> logs = Collections.synchronizedList(new ArrayList<>());

	public Operation(String title, String description) {
		this.title = title;
		this.description = description;

		this.addPropertyChangeListener(this);
	}

	public OperationStatus getStatus() {
		return status;
	}

	public List<String> getLogs() {
		return new ArrayList<>(logs);
	}

	@Override
	protected void process(List<String> chunks) {
		logs.addAll(chunks);

		notifyAboutIntermediateResults(chunks);
	}

	private void notifyAboutIntermediateResults(List<String> chunks) {
		listeners.forEach(listener -> listener.intermediateResults(chunks));
	}

	public void addListener(OperationListener listener) {
		listeners.add(listener);
	}

	public void removeListener(OperationListener listener) {
		listeners.remove(listener);
	}

	private void notifyProgressChanged() {
		listeners.forEach(l -> l.progressChanged(getProgress()));
	}

	@Override
	protected void done() {
		if (isCancelled()) {
			fireOperationStatusChanged(OperationStatus.CANCELLED_BY_USER);
			return;
		}
		try {
			get();
			fireOperationStatusChanged(OperationStatus.COMPLETED);
		} catch (InterruptedException e) {
			fireOperationStatusChanged(OperationStatus.CANCELLED_BY_USER);
		} catch (ExecutionException e) {
			fireOperationStatusChanged(OperationStatus.ERROR);

			process(List.of(e.getLocalizedMessage()));
		}
	}

	protected void fireOperationStatusChanged(OperationStatus status) {
		this.status = status;

		listeners.forEach(listener -> listener.statusChanged(status));
	}

	/**
	 * Transform PropertyChangeEvents to OperationStatus and progress events.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object newValue = evt.getNewValue();
		switch (evt.getPropertyName()) {
			case "state" -> {
				if (newValue == SwingWorker.StateValue.STARTED) {
					fireOperationStatusChanged(OperationStatus.RUNNING);
				}
				// other statuses are fired in done()
			}
			case "progress" -> {
				notifyProgressChanged();
			}
		}
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public class CancellableWriter extends Writer {

		private final StringBuffer buffer = new StringBuffer();

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			if (isCancelled()) {
				throw new IOException("Cancelled");
			}
			var str = new String(cbuf, off, len);
			buffer.append(str);
			publish(str);
		}

		@Override
		public void flush() {}

		@Override
		public void close() {}

		@Override
		public String toString() {
			return buffer.toString();
		}
	}
}
