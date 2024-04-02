package pcd.ass01.simtrafficbase;

public interface Barrier {
	void waitBeforeActing();

	void waitBeforeActing(Runnable onAllWaiting);
}
