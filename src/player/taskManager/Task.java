package player.taskManager;

import player.cacheManager.CacheInputStream;
import player.configuration.NetworkConfiguration;
import player.model.URI;

public class Task {
	public static int defaultTTL = NetworkConfiguration.TTL;
	
	public URI uri;
	public TaskStatus status;
	public int[] hosts;
	public int TTL;
	public CacheInputStream callback;
	
	public Task() {
		this.TTL = Task.defaultTTL;
		this.status = TaskStatus.HANDSHAKE_MESSAGE_SENT;
	}
}
