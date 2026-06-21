package apply;

public interface OperationListener {

	void statusChanged(OperationStatus status);

	void progressChanged(int progress);
}
