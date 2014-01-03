import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.lang.InterruptedException;
import java.io.File;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;
/**
* Class FileOwner for Server to upload file to peers.Classs implements Runnable so to execute each client request as a separate thread.
**/
class FileOwner{
	private int serverPort; 			// TO be used for passing to thread instance.
	private int[] chunks;
	public static final int CHUNK_SIZE= 1024*100;
	public int totalChunks;
	public int offset;
	public static final int TOTAL_PEER= 5;
	/**
	 * Constructor triggered by passing client Socket. The new instance so created will run as a thread and will be executed parallely to upload a chunk
	 * to client.
	 **/
	public FileOwner(int ServerPort)
	{
		//Set Server Port Default to 5000
		serverPort = 5000;
	}
	public int getServerPort()
	{
		return serverPort;
	}
	/**
	 * method splitFile to divide the file in chunks. File is minimum divided in 5 parts. Each part is minimum of 100KB. Method reads file and divides it* in chunks of size each equal to 100kb with last chunk less than or equal to specified value.
	 **/
	public void splitFile(File file,String fileName)
	{
		BufferedOutputStream bufOutput;
		BufferedInputStream bufInput;
		try
		{
			long fileSize = file.length();
			totalChunks =(int) (Math.ceil((double)fileSize/(double)CHUNK_SIZE));
			bufInput = new BufferedInputStream(new FileInputStream(fileName));
			int i =0;
			while(i < totalChunks)
			{

				bufOutput = new BufferedOutputStream(new FileOutputStream(fileName + ".chunk." + i++));
				int j=0;
				int b;
				while(j < CHUNK_SIZE && ((b=bufInput.read())!= -1))
				{
					bufOutput.write(b);
					j++;
				}
				bufOutput.close();

			}
			offset = 0;
			bufInput.close();
			System.out.println("Total Chunks: "+ totalChunks);
		}
		catch(IOException ex)
		{
			System.out.println("One or more chunks are missing.");
		}
	}
public void deleteChunks(String fileName,int numChunks)
{
	File file;
	try
	{
		for(int i=0;i<numChunks;i++)
		{
			file = new File(fileName+".chunk."+i);
			if(file.exists())
				file.delete();
		}
	}
	catch(SecurityException ex)
	{
		System.out.println("Unable to delete temporary File Chunks due to security reasons. Please delete the temporary files manually from download directory");
	}
}

public static void main(String[] args)
{
	try
	{
		if(args.length != 2)
		{
			System.out.println("Incorrect Usage. Specify Proper parameters.\nUsage: java FileOwner <Port_Number> <FileName>");
			return;
		}
		FileOwner fServer = new FileOwner(Integer.parseInt(args[0]));
		ArrayList<Thread> threads = new ArrayList<Thread>();
		ServerSocket server = new ServerSocket(fServer.getServerPort());
		System.out.println("Server Listening at Port" + fServer.getServerPort());
		String fileName = args[1];
		//Split the file in chunks of Size CHUNK_SIZE if the file exists.;
		File file = new File(fileName);
		if(file.exists())
			fServer.splitFile(file,fileName);
		else
			throw (new FileNotFoundException());
		int totalPeers =0;
		while(totalPeers<TOTAL_PEER)
		{
			//For each connection start a new thread for distributing the chunks of the file.
			Socket clientSocket = server.accept();
			int nextChunkID = fServer.offset + (int)Math.ceil(((double)fServer.totalChunks/(double)TOTAL_PEER));
			if(nextChunkID > fServer.totalChunks)
				nextChunkID = fServer.totalChunks;
			Connection conn = new Connection(clientSocket,fServer.offset,nextChunkID,fileName,CHUNK_SIZE,fServer.totalChunks);
			fServer.offset = nextChunkID;
			Thread connectionThread = new Thread(conn);
			threads.add(connectionThread);
			connectionThread.start();
			totalPeers++;
		}
		//Wait till all threads end.
		for(Thread thread:threads)
		{
			thread.join();
		}
		fServer.deleteChunks(fileName,fServer.totalChunks);
	}catch(NumberFormatException ex)
	{
		System.out.println("Specify a proper Port Number between 1024-65536");
	}
	catch(FileNotFoundException ex)
	{
		System.out.println("File Specified Does Not exist. Please make sure if the specified file exists in the same directory of Program");
	}
	catch(IOException ex){
		System.out.println("Exception : "+ ex.getMessage());
	}
	catch(InterruptedException ex)
	{
		System.out.println("Interrupted Exception : " + ex.getMessage());
	}
}
};
