package adhoc.setup;

import java.util.Random;

import player.cacheManager.CacheManager;
import player.configuration.CacheConfiguration;
import player.model.URI;
import player.taskManager.TaskManager;
import android.widget.VideoView;

public class FilePrefetching {
	private VideoView videoView = null;
	private Prefetch prefetch = null;
	private boolean isStop = false;

	private class Prefetch extends Thread{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(!isStop){
				if(isPrefetch()){
					int position = videoView.getCurrentPosition();
					URI uri = parsePosition(position);
					if(uri != null){
						if(!CacheManager.sharedInstance().query(uri)){
							TaskManager.sharedInstance().addTask(uri, null);
						}
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	public FilePrefetching(VideoView view){
		this.videoView = view;
		prefetch = new Prefetch();
	}

	public void startPrefetch(){
		isStop = false;
		prefetch.start();
	}

	public void stopPrefetch(){
		isStop = true;
	}

	private URI parsePosition(int position){
		Random random = new Random();
		URI uri = null;
		do{
			String identifier = "test";
			long offset = Math.abs(random.nextLong() % 5001);
			uri = new URI(identifier, offset);
		}while(CacheManager.sharedInstance().uriSet.contains(uri) && (uri != null));
		
		return uri;
	}

	private boolean isPrefetch(){
		if(CacheManager.sharedInstance().getCountOfNotUsed() < CacheConfiguration.maxNotUsedNumber)
			return true;
		System.out.println("FilePrefetching.isPrefetch(): stop prefetch...");
		return false;
	}
}
