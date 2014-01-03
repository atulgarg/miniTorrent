/**
* Class DownloadPeer responsible for downloading all the chunks of the file not yet downloaded.
* Download Peer will act as a client and connect to port specified. It will then try to fetch all chunks it can download from the Peer.
**/
import java.util.HashMap;
import java.util.Iterator;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.io.IOException;
import java.io.File;
public class DownloadPeer extends PeerBase implements Runnable
{
	private PeerUtil peerUtilObject;
	
	public DownloadPeer(PeerUtil peerUtilObject)
	{
		logFile = new File("download.log");
		this.peerUtilObject = peerUtilObject;
		this.chunkSize = peerUtilObject.getChunkSize();
	}
	/**
	 * Method run implemented since Download peer will be invoked as a thread from Peer Class.
	 **/
	public void run()
	{
		Socket peerSocket =null;
		ObjectInputStream in;
		ObjectOutputStream out;         //stream write to the socket
                FileOutputStream chunkOutputStream;
		try
		{
			String msg;
			while(peerSocket == null)
			{
				try{
					peerSocket = new Socket("127.0.0.1",peerUtilObject.getDownloadPeerPort());
				}catch(IOException ex)
				{
					//System.out.println("Neighbour Node not up");	
				}
			}
			if(peerSocket!=null)
			{
				System.out.println("[Downloading Thread]: Peer connected to Other Neighbouring Peer listening at port: "+ peerUtilObject.getDownloadPeerPort());
				in = new ObjectInputStream(peerSocket.getInputStream());
				out = new ObjectOutputStream(peerSocket.getOutputStream());
				msg = "DONTCLOSE";
				while(!msg.equals("CLOSE"))
				{
					Thread.currentThread().sleep(500);
					msg = chunksToRecieve();
					//Send how many chunks required.				
					out.writeObject(peerUtilObject.chunksReqd);
					out.flush();
					
					//Send single string for all chunks required.
					out.writeObject(msg);
					out.flush();
					
					//read the name of the chunk next save the next stream as chunk and update map.
				
					peerUtilObject.numChunkRecv = (int)in.readObject();
					System.out.println("[Downloading Thread]: Recieving "+ peerUtilObject.numChunkRecv + " chunks from Peer.");

					int j=0;
					while(j<peerUtilObject.numChunkRecv)
					{				
						String chunkName = (String)in.readObject();
						System.out.println("[Downloading Thread]: Server is sending chunk with name : " + chunkName); 
						j++;
						saveChunk(chunkName,in);
						peerUtilObject.setChunkMap(chunkName,true);
					}
					if(checkMoreChunkNeeded())
					{
						System.out.println("[Downloading Thread]: More Chunks Required : DontClose");
						msg ="DONTCLOSE";
					}else
					{
						System.out.println("[Downloading Thread]: No More Chunks required: Close");
						msg = "CLOSE";
					}
					out.writeObject(msg);
					out.flush();
				}
				out.close();
				in.close();
			}
		}
		catch(InterruptedException ex)
		{
			System.out.println("[Downloading Thread]: InterruptedException in Download Thread");
		}
		catch(ClassNotFoundException ex)
		{
			System.out.println("[Downloading Thread]: Exception in DownloadPeer : " + ex.getMessage());
		}
		catch(IOException ex)
		{
			System.out.println("[Downloading Thread]: IOException at run in Peer :" + ex.getMessage());
		}
		
	}
	/**
	* method chunksToRecieve private to create String of not recieved Chunks.
	* @returns a String of all chunkid separated by "^"
	**/
	private String chunksToRecieve()
	{
		String chunks = new String();
		int chunksReqd =0;
		Iterator<String> iter = peerUtilObject.getChunksMap().keySet().iterator();
		while(iter.hasNext())
		{
			String chunkName = iter.next();
			if(!peerUtilObject.getChunksMap().get(chunkName))
			{
				chunks = chunks + "^" +chunkName;
				chunksReqd++;
			}
		}
		peerUtilObject.chunksReqd = chunksReqd;
		chunks = chunks.substring(1,chunks.length());
		return chunks;
	}
	/**
	* method checkMoreChunkNeeded to check if any more chunks are required or not.
	* @returns true if for any chunk value in map is set to false else returns true;
	**/
	private boolean checkMoreChunkNeeded()
	{
		boolean chunksNeeded = true;
		Iterator<String> iter = peerUtilObject.getChunksMap().keySet().iterator();
		while(iter.hasNext())
		{
			boolean chunkPresent = peerUtilObject.getChunksMap().get(iter.next());
			chunksNeeded = chunksNeeded && chunkPresent;
		}
		return !chunksNeeded;
	}
};
