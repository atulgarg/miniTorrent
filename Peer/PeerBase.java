/**
* Class PeerBase comprising basic utility functions of Peer Object functions. Class used for extending.
**/
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
public class PeerBase{
	public int chunkSize;
	public File logFile;
	public PeerBase()
	{
		
	}
	/**
	* method sendChunk to send Chunks to outputStream specified.
	**/
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
	/**
         *method saveChunk to save each chunk as a separate file.
         **/
        public void saveChunk(String chunkName,ObjectInputStream in)
        {
                BufferedOutputStream bufOutput;
                try
                {
                        bufOutput = new BufferedOutputStream(new FileOutputStream(chunkName));
                        int b;
                        int bytesRead=0;
                        while(bytesRead < chunkSize && (b=in.read())!=-1)
                        {
                                bufOutput.write(b);
                                bytesRead++;
                        }
                        bufOutput.close();
                }
                catch(IOException ex)
                {
                        System.out.println("Exception While Saving Chunk:" + ex.getMessage());
                }
        }
	/**
         * method mergeChunks to merge all the given chunks. Method takes file name and number of chunks as input and expects all chunks to be of the name
         * filename.chunk.i where i represents the number of chunk.Method creates a new file with specified file name and writes all chunks in the file.
         * @param fileName String 
         * @param int numChunks Total # of chunks to merge.
         **/
        public void mergeChunks(String fileName,int numChunks)
        {
                FileInputStream fInput;
                BufferedInputStream bufInput;
                BufferedOutputStream bufOutput;
                FileOutputStream fOutput;
                try
                { 
                        fOutput = new FileOutputStream(fileName);
                        bufOutput = new BufferedOutputStream(fOutput);                  
                        for(int i=0;i<numChunks;i++)
                        {
                                fInput = new FileInputStream(fileName+ ".chunk." + i);
                                bufInput = new BufferedInputStream(fInput);
                                int b;
                                while((b=bufInput.read())!= -1)
                                {
                                        bufOutput.write(b);
                                }

                        }
                        bufOutput.close();
                }
		catch(FileNotFoundException ex)
                {
                        System.out.println("File Not Found");
                }
                catch(IOException ex)
                {
                        System.out.println("Some IOException occurred");
                }
        }
	/**
	 * @method deleteChunks to delete all chunk files after the merge is done.Method sticks to nomenclature pattern of naming all the chunks with
	 * format of filename.chunk.numChunk.
	 * @param String fileName  name of the file 
	 * @param int numChunks number of chunks present
	 **/
	public void deleteChunks(String filename,int numChunks)
	{
		File file;
		try
		{
		for(int i=0;i<numChunks;i++)
		{
			file = new File(filename+".chunk."+i);
			if(file.exists())
			{
				file.delete();
			}
		}
		}catch(SecurityException ex)
		{
			System.out.println("Some error occurred during Clean up. Please delete the chunk files manually in the directory");
		}
	}

};
