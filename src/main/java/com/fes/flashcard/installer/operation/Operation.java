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

// TODO make process cancellable always
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

	protected void publishLn(String line) {
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

	protected void progress() {
		int newProgress = getProgress() + 1;
		if (newProgress <= 100) {
			setProgress(newProgress);
		}
	}

	public class CancellableWriter extends Writer {

		private final StringBuffer written = new StringBuffer();

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			if (isCancelled()) {
				throw new IOException("Cancelled"); // localize
			}
			var str = new String(cbuf, off, len);
			written.append(str);
			publish(str);
		}

		public String written() {
			return written.toString();
		}

		@Override
		public void flush() {}

		@Override
		public void close() {}
	}

	protected Result redirectOutputs(Process process) throws IOException, InterruptedException {
		var out = new CancellableWriter();
		var err = new CancellableWriter();
		try (var stdout = process.inputReader(); var stderr = process.errorReader()) {
			stdout.transferTo(out);
			stderr.transferTo(err);
		} catch (IOException e) {
			if (!isCancelled()) {
				throw e;
			}
			process.destroy();
			publishLn("Cancelled process with id " + process.pid()); // TODO localize
		}
		int exitCode = process.waitFor();
		return new Result(out, err, exitCode);
	}

	public static class Result {

		private final CancellableWriter output;
		private final CancellableWriter error;
		private final int               exitCode;

		Result(CancellableWriter output, CancellableWriter error, int exitCode) {
			this.output = output;
			this.error = error;
			this.exitCode = exitCode;
		}

		public String output() {
			return output.written();
		}

		public String error() {
			return error.written();
		}

		public int exitCode() {
			return exitCode;
		}
	}


}
