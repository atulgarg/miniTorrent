/**
* Class Upload Peer For Peer to act as Server in P2P network and upload chunks to requesting peers.
* Protocol so implemented is Upload Peer server runs on a predefined port which is passed as a part of Constructor.
* Next it accepts a connection from a Peer in form of FileName and chunkid.
* Upload peer checks if the Server already has chunks requested.
* If chunks are found then Server sends the chunks and waits for "CLOSE" message from Download Peer.
* If Chunks not found then Server puts the thread on sleep for the while it gets those chunks.
**/
import java.net.Socket;
import java.net.ServerSocket;
import java.util.HashMap;
import java.io.IOException;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileNotFoundException;
import java.lang.ClassNotFoundException;
public class UploadPeer extends PeerBase implements Runnable
{
	private PeerUtil peerUtilObject;
	public UploadPeer(PeerUtil peerUtilObject)
	{
		logFile = new File("upload.log");
		this.peerUtilObject = peerUtilObject;
		this.chunkSize = peerUtilObject.getChunkSize();
	}
	/**
	 * Method run implemented since Upload Peer will run as a thread from Peer class.Will serve as server to its neighbour.
	 **/
	public void run()
	{
		ServerSocket socket;
		Socket downloadPeerSocket;
		ObjectOutputStream out;         //stream write to the socket
		ObjectInputStream in;          //stream read from the socket
		boolean notCompleted = true;
		try
		{
			//Open a new Socket on the specified Port and wait till you get close from the peer.
			socket = new ServerSocket(peerUtilObject.getUploadPeerPort());
			System.out.println("[Uploading Thread]: Upload Server started at Peer at port : "+ peerUtilObject.getUploadPeerPort());
			downloadPeerSocket = socket.accept();
			out = new ObjectOutputStream(downloadPeerSocket.getOutputStream());
			in = new ObjectInputStream(downloadPeerSocket.getInputStream());
			System.out.println("[Uploading Thread]: Got Connection from Neighbour Peer.");
			String msg="DONTCLOSE";
			while(!msg.equals("CLOSE"))
			{			
				Thread.currentThread().sleep(500);
				int chunksrequiredByN = (int)in.readObject();

				System.out.println("[Uploading Thread]: Total Chunks Requested by neighbour " + chunksrequiredByN);
				//Concatenated String of chunks.
				msg = (String) in.readObject();
				
				String[] chunkList = splitMessage(msg);
				//Write the number of chunks Peer will send.
				System.out.println("[Uploading Thread]: Number of Chunks Server Will Send :" + peerUtilObject.numChunkSend);	
				out.writeObject(peerUtilObject.numChunkSend);
				out.flush();
				

				if(chunkList!=null && chunkList.length>0)
				{
					System.out.println("[Uploading Thread]: ChunkList length : " + chunkList.length);
					sendChunks(chunkList,out);
					msg = (String)in.readObject();
				}
				else
					msg = "DONTCLOSE";
			}
			in.close();
			out.close();
		}
		catch(InterruptedException ex)
		{
			System.out.println("[Uploading Thread]: Interrupt Exception: ");
		}
		catch(ClassNotFoundException ex)
		{
			System.out.println("[Uploading Thread]: Class Not Found Exception: " + ex.getMessage());
		}
		catch(IOException ex)
		{
			System.out.println("[Uploading Thread]: IOException: " + ex.getMessage());
		}
	}
	/**
	 * method splitMessage to split the message recieved from neighbour. As per protocol it expects message to be of comprised of chunkname
	 * separated by "^".
	 * @param msg String with chunkNames separated by "^"; 
	 **/
	private String[] splitMessage(String msg)
	{
		String[] chunkList;
		if(msg !=null)
		{
			chunkList = msg.split("\\^");	
		}
		else
			chunkList = null;
		//Determine the number of chunks peer has to send and initialise peerObjectUtil.
		int count =0;		
		for(int i=0;i<chunkList.length;i++)
		{
			if(peerUtilObject.getChunksMap().get(chunkList[i])!= null && peerUtilObject.getChunksMap().get(chunkList[i]))
			{
				count++;
			}
		}
		peerUtilObject.numChunkSend = count;
		return chunkList;
	}
	/**
	* method sendChunks to send chunks in String Array chunkList.
	* @param String[] chunkList
	**/
	private void sendChunks(String[] chunkList,ObjectOutputStream out)
	{
		try
		{
			int count =0;
			if(chunkList!=null && chunkList.length>0)
			{
				for(int i=0;i<chunkList.length && count < peerUtilObject.numChunkSend;i++)
				{
					//if chunkList[i] is there with the Peer.
					if(peerUtilObject.getChunksMap().get(chunkList[i])!= null && peerUtilObject.getChunksMap().get(chunkList[i]))
					{
						String msg = chunkList[i];
						out.writeObject(msg);
						out.flush();
						sendChunk(chunkList[i],out);
						count++;
					}
				}
			}
		}catch(IOException ex)
		{
			System.out.println("[Uploading Thread]: Exception while sendChunks:" + ex.getMessage());	
		}
	}
};
