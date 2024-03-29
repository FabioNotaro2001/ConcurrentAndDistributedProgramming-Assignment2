package pcd.ass01.simtrafficbase;

public interface Barrier {

	/**
	 * 
	 */
	void waitBeforeActing();
	/**
	 * 
	 * @param onAllWaiting
	 */
	void waitBeforeActing(Runnable onAllWaiting);
}
