import java.util.HashMap;
import java.net.Socket;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.lang.InterruptedException;
public class Peer extends PeerBase
{
	public final int BUFFER_SIZE = 10240;
	private volatile PeerUtil peerUtilObject;
	/**
	 * method start to establish connection with server and recieve chunks from server and store it as a files. 
	 * Later start with two new threads one responsible for accumulating other chunks for download neighbour and
	 * other for uploading chunks that peer has but upload neighbour does not have.
	 * @param serverIP String format Server IP
	 * @param serverPort Port number for FileOwner.
	 * @param peerListeningPort port at which this peer will listen. 
	 * @param downloadPeerPort port at which this peer will connect to download.
	 **/	
	public void start(String serverIP,int serverPort,int peerListeningPort,String downloadPeerIP,int downloadPeerPort)
	{
		Socket peerSocket;
		ObjectInputStream in;
		FileOutputStream chunkOutputStream;
		byte[] buffer;
		try
		{
			peerSocket = new Socket(serverIP,serverPort);
			if(peerSocket != null)
			{
				System.out.println("Client Connected to Server Running on Port :"+ serverPort);
				in = new ObjectInputStream(peerSocket.getInputStream());
				//As per the protocol read the name of file first.
				String fileName = (String)in.readObject();
				System.out.println("FileName recieved : " + fileName);
				//Get the total number of chunks for the file.
				int totalChunks = (int)in.readObject();
				System.out.println("Total Chunks for file: " + totalChunks);
				//initialise Boolean array with size of chunks.
				//Next read number of initial chunkID the peer will recieve.
				int initialChunkID =(int)in.readObject();
				System.out.println("Peer will recive Chunks from ID : " + initialChunkID);
				//Next read last Chunk id peer will recieve.
				int lastChunkID = (int)in.readObject();
				System.out.println("Peer will recieve Chunks till : " + lastChunkID);
				//Next read size of chunks peer will recieve.
				chunkSize = (int)in.readObject();
				System.out.println("Size of each of chunks:" + chunkSize);
				//Now iteratively read each chunk and store each chunk as a temp file.
			       peerUtilObject = new PeerUtil(fileName,totalChunks,downloadPeerIP,downloadPeerPort,peerListeningPort,chunkSize);
			       for(int chunkRead=initialChunkID;chunkRead<=lastChunkID;chunkRead++)
				{
					
					//SetCHunkMap
					String chunkName = fileName +".chunk." + chunkRead;
					saveChunk(chunkName,in);
					peerUtilObject.setChunkMap(chunkName,true);
				}
				peerSocket.close();
				//Now start two threads one for Upload Peer and Download Peer.
				UploadPeer uploadPeer = new UploadPeer(peerUtilObject);
				Thread uploadThread = new Thread(uploadPeer);
				DownloadPeer downloadPeer = new DownloadPeer(peerUtilObject);
				Thread downloadThread = new Thread(downloadPeer);
				//Start upload and download peer part
				uploadThread.start();
				downloadThread.start();
				//wait till both thread complete.
				uploadThread.join();
				downloadThread.join();
				//When all threads stop Merge all the files.
				if(peerUtilObject.check())
				{
					System.out.println("[Peer]: All chunks downloaded. Merging chunks.");
					mergeChunks(fileName,totalChunks);
					System.out.println("[Peer]: Chunks Merged. File Download Finished. Clean up starting.");
					deleteChunks(fileName,totalChunks);
					System.out.println("[Peer]: Temporary Files deleted. Clean up Finished.");
				}
				else
				{
					System.out.println("Some Chunks Missing.");
				}
			}
		}
		catch(ClassNotFoundException ex)
		{
			System.out.println(ex.getMessage());
		}
		catch(IOException io)
		{
			System.out.println(io.getMessage());
		}
		catch(InterruptedException ex)
		{
			System.out.println(ex.getMessage());
		}
		finally
		{
		}

	}
	public static void main(String[] args)
	{
		if(args.length !=5 && args.length !=4)
		{
			System.out.println("Incorrect Usage. Please specify proper parameters. Check usage.\nUsage: java Peer <Server_IP> <Server_Port> <Self_Server_Port> <Download peer IP> <Downloading_Peer_Port>");
			return;
		}
		String downloadPeerIP;
		int downloadPeerPort;
		try
		{
			Peer peer = new Peer();
			String serverIP = args[0];
			int serverPort = Integer.parseInt(args[1]);
			int peerListeningPort = Integer.parseInt(args[2]);
			if(args.length == 5)
			{
			downloadPeerIP = args[3];
			downloadPeerPort = Integer.parseInt(args[4]);
			}
			else
			{
				downloadPeerPort = Integer.parseInt(args[3]);
				downloadPeerIP = "127.0.0.1";

			}
			peer.start(serverIP,serverPort,peerListeningPort,downloadPeerIP,downloadPeerPort);
		}catch(NumberFormatException ex)
		{
			System.out.println("Please Enter Valid Port Values");
		}
	}
};
