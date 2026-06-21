package apply;

import javax.swing.SwingWorker;
import java.util.ArrayList;
import java.util.List;

public abstract class Operation extends SwingWorker<String, String> {

	private       OperationStatus         status    = OperationStatus.NOT_STARTED;
	private final String                  description;
	private       int                     progress  = 0;
	private final List<String>            logOutput = new ArrayList<>();
	private final List<OperationListener> listeners = new ArrayList<>();

	protected ApplyDialog applyDialog;



	public Operation(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public OperationStatus getStatus() {
		return status;
	}

	public void setStatus(OperationStatus status) {
		this.status = status;
		notifyStatusChanged();
	}

	public int getOperationProgress() {
		return progress;
	}

	public void setOperationProgress(int progress) {
		this.progress = progress;
		notifyProgressChanged();
	}

	public List<String> getLogOutput() {
		return new ArrayList<>(logOutput);
	}

	public void addLogOutput(String message) {
		logOutput.add(message);
		publish(message);
	}

	public void addListener(OperationListener listener) {
		listeners.add(listener);
	}

	public void removeListener(OperationListener listener) {
		listeners.remove(listener);
	}

	private void notifyStatusChanged() {
		for (OperationListener listener : listeners) {
			listener.statusChanged(status);
		}
	}

	private void notifyProgressChanged() {
		for (OperationListener listener : listeners) {
			listener.progressChanged(progress);
		}
	}

	@Override
	protected void done() {
		if (isCancelled()) {
			setStatus(OperationStatus.CANCELLED_BY_USER);
		} else {
			try {
				get();


				setStatus(OperationStatus.COMPLETED);
			} catch (Exception e) {
				setStatus(OperationStatus.ERROR);
				addLogOutput("Fehler: " + e.getMessage());
			}
		}
	}

	@Override
	protected void process(List<String> chunks) {
		for (String chunk : chunks) {
			addLogOutput(chunk);
		}
	}
}
