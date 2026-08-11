package com.fes.flashcard.installer.operation;

import javax.swing.SwingWorker;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;

public abstract class Operation extends SwingWorker<String, String> implements PropertyChangeListener {

	protected final Logger log = getLogger(getClass().getName());

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

	public void println(String line) {
		publish(line + "\n");
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

			// This should prevent the exception type from being included
			var cause = e.getCause();
			process(List.of(cause != null ? cause.getLocalizedMessage() : e.getLocalizedMessage()));
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

	protected void progress() {
		int newProgress = getProgress() + 1;
		if (newProgress <= 100) {
			setProgress(newProgress);
		}
	}

	protected void progress(int by) {
		int newProgress = getProgress() + by;
		if (newProgress <= 100) {
			setProgress(newProgress);
		}
	}

	void print(String str) {
		publish(str);
	}

	/// execute the given process and send its outputs
	public Result execute(Process process) throws IOException, InterruptedException {
		var out = new CancellableWriter(this);
		var err = new CancellableWriter(this);
		try (var stdout = process.inputReader(); var stderr = process.errorReader()) {
			stdout.transferTo(out);
			stderr.transferTo(err);
		} catch (Cancelled e) {
			process.destroy();
			throw Cancelled.process(process, e);
		}
		return new Result(out, err, process.waitFor());
	}
}
