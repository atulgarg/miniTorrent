/**
* Class PeerUtil to pass reference to data object to threads. Class Object is shared among DownloadPeer Thread and Upload Peer Thread.
**/
import java.util.HashMap;
import java.util.Iterator;
public class PeerUtil
{
	private HashMap<String,Boolean> chunksMap;
	private String fileName;
	private int totalChunks;
	private int downloadPeerPort;
	private int uploadPeerPort;
	private int chunkSize;
	public int chunksReqd;
	public int numChunkSend;		//Number of Chunks Peer will send.
	public int numChunkRecv;		//Number of chunks Peer will recieve.
	private String downloadPeerIP;
	public PeerUtil(String fileName, int totalChunks,String downloadPeerIP,int downloadPeerPort,int uploadPeerPort, int chunkSize)
	{
		this.fileName = fileName;
		this.totalChunks = totalChunks;
		this.downloadPeerIP = downloadPeerIP;
		this.downloadPeerPort = downloadPeerPort;
		this.uploadPeerPort = uploadPeerPort;
		this.chunkSize = chunkSize;
		initialiseChunkMap();
	}	
	/**
	* method initialiseChunkMap private method to initialise chunkMap to set all chunk value to false.
	**/
	private void initialiseChunkMap()
	{
		chunksMap = new HashMap<String,Boolean>();
                for(int i=0;i<totalChunks;i++)
                {
                        String chunkName = fileName+".chunk."+i;
                        chunksMap.put(chunkName,false);
                }
	}
	/**
	* method setChunkMap used to update chunkMap when any of the chunk Map is recieved. 
	**/
	public void setChunkMap(String chunkName,Boolean value)
	{
			chunksMap.put(chunkName, value);
	}
	/**
	 * method check to check if more chunks are required or the file has been completely downloaded.
	 **/
	public Boolean check()
	{
		Iterator<String> iter = chunksMap.keySet().iterator();
		Boolean chunkMoreReqd = false;
		while(iter.hasNext())
		{
			String chunkName = iter.next();
			chunkMoreReqd = chunksMap.get(chunkName);
			if(chunkMoreReqd)
				break;
			//System.out.println("ChunkName : " + chunkName + " Value : "  + chunksMap.get(chunkName));
		}
		return chunkMoreReqd;
	}
	public HashMap<String, Boolean> getChunksMap() {
		synchronized(this){
		return chunksMap;
		}
	}
	public void setChunksMap(HashMap<String, Boolean> chunksMap) {
		synchronized(this)
		{
		this.chunksMap = chunksMap;
		}
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public int getTotalChunks() {
		return totalChunks;
	}
	public void setTotalChunks(int totalChunks) {
		this.totalChunks = totalChunks;
	}
	public int getDownloadPeerPort() {
		return downloadPeerPort;
	}
	public void setDownloadPeerPort(int downloadPeerPort) {
		this.downloadPeerPort = downloadPeerPort;
	}
	public int getUploadPeerPort() {
		return uploadPeerPort;
	}
	public void setUploadPeerPort(int uploadPeerPort) {
		this.uploadPeerPort = uploadPeerPort;
	}
	public int getChunkSize() {
		return chunkSize;
	}
	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}
	public void setDownloadPeerIP(String downloadPeerIP)
	{
		this.downloadPeerIP = downloadPeerIP;
	}
	public String getDownloadPeerIP()
	{
		return downloadPeerIP;
	}

	
};
