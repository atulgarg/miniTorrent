import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.net.Socket;
import java.io.FileNotFoundException;
import java.io.IOException;
/**
* Class Connection Defined for each connection established with client. Initialised using port specified as accepting port and is used to send and
* recieve data to client. Implements Runnable to implement threads and method run is over ridden.
**/
class Connection implements Runnable
{
	private Socket clientSocket;
	private String fileName;
	private int totalChunks;
	private int initialChunkID =-1;
	private int lastChunkID =-1;
	private int chunkSize;
	/**
	 * Constructor for Connection object, initialised with socket client at which the client request is processed.
	 **/ 
	public Connection(Socket clientSocket,int initialChunkID,int lastChunkID,String fileName,int chunkSize,int totalChunks)
	{
		this.clientSocket = clientSocket;
		this.initialChunkID = initialChunkID;
		this.lastChunkID = lastChunkID;
		this.fileName = fileName;
		this.chunkSize = chunkSize;
		this.totalChunks= totalChunks;
	}
	/**
	 * method run() overridden to run as a thread for each client.
	 **/
	public void run()
	{
		ObjectOutputStream out;         //stream write to the socket
		ObjectInputStream in;          //stream read from the socket
		String msg;
		try
		{
			System.out.println("[Thread: "+ Thread.currentThread().getId() + "]: Got connection from client " + clientSocket.getPort());
			System.out.println("[Thread: "+ Thread.currentThread().getId() + "]: Address of Remote Client is " + clientSocket.getRemoteSocketAddress());
			out = new ObjectOutputStream(clientSocket.getOutputStream());
			out.flush();
			//As per the Protocol send filename first.
			msg = fileName;
			out.writeObject(fileName);
			out.flush();
			//Next send total number of chunks file is fragmented in.
			out.writeObject(totalChunks);
			out.flush();
			//Next initial Number of chunk File Owner will send the client.
			out.writeObject(initialChunkID);
			out.flush();
			//Next sent the last chunk File owner will send to peer.
			out.writeObject(lastChunkID-1);
			out.flush();
			//Next Send size of each chunk.
			out.writeObject(chunkSize);
			out.flush();
			//loop will iterate for sending chunk from index initialChunkID inclusive and lastChunkID exclusive.
			System.out.println("[Thread: "+Thread.currentThread().getId() + "]: Sending chunks from id " + initialChunkID + " to " + lastChunkID);
			for(int i=initialChunkID;i<lastChunkID;i++)
			{
			    //Send each chunk 
			    sendChunk(fileName+".chunk."+i,out);
			    out.flush();
			}
			System.out.println("[Thread: "+Thread.currentThread().getId() + "]: Chunks Sent. Closing Connection.");
			out.close();

		}
		catch(FileNotFoundException ex)
		{
			System.out.println(" File Not Found Exception : " + ex.getMessage());
		}
		catch(IOException ex)
		{
			System.out.println("Fatal Error: Please Try again");
		}
	}

	public void sendChunk(String fileName,ObjectOutputStream out)
	{
		FileInputStream fInput;
		BufferedInputStream bufInput;
		byte[] byteToSend = new byte[chunkSize];
		try
		{
			fInput = new FileInputStream(fileName);
			bufInput = new BufferedInputStream(fInput);
			int b;
			int i=0;
			b = bufInput.read(byteToSend,0,chunkSize);
			out.write(byteToSend,0,chunkSize);
			out.flush();
			bufInput.close();
		}
		catch(FileNotFoundException ex)
		{
			System.out.println("File Chunk Not found Exception: Some chunk missing.");
		}
		catch(IOException ex)
		{
			System.out.println("IOException while reading from the chunk file.");
		}	
	}
};
