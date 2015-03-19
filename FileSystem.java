//FileSystem.java
//Author:GKITZ & Luke Selbeck
//Date: 3/18/2015
//
//Description:
//User interface for the file system.  Can read/write, open/close, format,
//seek.

public class FileSystem 
{

  private SuperBlock superblock; 
  private Directory directory; 
  private FileTable filetable; 

  public FileSystem(int diskBlocks)
  {
    //create superblock and format disk with 64 iNodes in default
    superblock = new SuperBlock(diskBlocks); 

    //create directory and register '/' in directory entry 0
    directory = new Directory (superblock.totalInodes ); 

    //file table is created and store directory in the file table 
    filetable = new FileTable (directory); 

    //directory reconstruction
    FileTableEntry dirEnt = open("/", "r");
    int dirSize = fsize(dirEnt); 
    if (dirSize > 0)
    { 
      byte[] dirData = new byte[dirSize];
      read(dirEnt,dirData); 
      directory.bytes2directory(dirData); 
    } 
    close( dirEnt ); 
  }

  public void sync() 
  {
     FileTableEntry dirEnt = open("/", "w"); 

    //convert to byte array 
    byte[] dirByte = directory.directory2bytes(); 

     //write file table entry 
     write(dirEnt, dirByte); 
     close(dirEnt);  
     superblock.sync();//superblock writes the data to disk 
  }

  /*format(int files)
  //formats the disk, (i.e., Disk.java's data contents). The parameter files
  //specifies the maximum number of files to be created, (i.e., the number of
  //inodes to be allocated) in your file system. The return value is 0 on
  //success, otherwise -1.
  */
  public boolean format( int files ) 
  {
    if(!filetable.fempty())
    {
      Kernel.report("Format Error: Cannot format superblock while files are in use\n");
      return false;
    }
    superblock.format(files);
    directory = new Directory(SuperBlock.totalInodes);
    filetable = new FileTable(directory);
    return true;
  }


  /*open(String fileName, String mode)
  //opens the file specified by the fileName string in the given mode
  //(where "r" = ready only, "w" = write only, "w+" = read/write, "a" = append),
  //and allocates a new file descriptor, fd to this file. The file is created if
  //it does not exist in the mode "w", "w+" or "a". SysLib.open must return a
  //negative number as an error value if the file does not exist in the mode "r".
  //Note that the file descriptors 0, 1, and 2 are reserved as the standard input
  //output, and error, and therefore a newly opened file must receive a new
  //descriptor numbered in the range between 3 and 31. If the calling thread's
  //user file descriptor table is full, SysLib.open should return an error value.
  //The seek pointer is initialized to zero in the mode "r", "w", and "w+",
  //whereas initialized at the end of the file in the mode "a".
  */
  public FileTableEntry open( String filename, String mode ) 
  {
      FileTableEntry ftEnt;
      Inode iNode;

      if(filename == "" || mode == "")
      {
        return null;
      }

      ftEnt = filetable.falloc(filename, mode);
      iNode = ftEnt.iNode;
      //if ftEnt = null OR iNode = null OR ftEnt mode is invalid OR iNode is flagged 'to be deleted'
      if(ftEnt == null || iNode == null || ftEnt.mode == null 
         || iNode.flag == iNode.DELETE)
      {
        filetable.ffree(ftEnt);
        return null;
      }

      synchronized(ftEnt)
      {
        if(ftEnt.mode.equals("w") && !deallocAllBlocks(ftEnt))
        {
          filetable.ffree(ftEnt);
          Kernel.report("Open error: Could not deallocate all blocks");
          return null;
        }
      }
      return ftEnt;
  }

/*
  //closes the file corresponding to fd, commits all file transactions on this
  //file, and unregisters fd from the user file descriptor table of the calling
  //thread's TCB. The return value is 0 in success, otherwise -1.
  */
  public boolean close (FileTableEntry ftEnt ) 
  { 
    Inode iNode;
    if(ftEnt == null)
    {
      return false;
    }

    synchronized(ftEnt)
    {
      iNode = ftEnt.iNode;
      if(iNode == null)
      {
        return false;
      }

      if(iNode.flag == Inode.DELETE && ftEnt.count == 0)
      {
        deallocAllBlocks(ftEnt);
        if(!directory.ifree(ftEnt.iNumber))
        {
          return false;
        }

      }
      if(!filetable.ffree(ftEnt))
      {
        return false;
      }
    }
    return true;
  }

  //returns the size in bytes of the file indicated by fd.

  public int fsize ( FileTableEntry ftEnt ) 
  {
    Inode iNode = ftEnt.iNode;
    if(iNode == null || ftEnt == null)
    {
      return ERROR;
    }
    return iNode.length;
  }

   /*
  //reads up to buffer.length bytes from the file indicated by fd, starting at
  //the position currently pointed to by the seek pointer. If bytes remaining
  //between the current seek pointer and the end of file are less than
  //buffer.length, SysLib.read reads as many bytes as possible, putting them into
  //the beginning of buffer. It increments the seek pointer by the number of
  //bytes to have been read. The return value is the number of bytes that have
  //been read, or a negative value upon an error.
  */


  public synchronized int read (FileTableEntry ftEnt, byte[] buffer)
  {
        int bufferPointer = 0; 
	      int fileSize =  ftEnt.iNode.length; //size of the file
	      int bytesLeft = fileSize - ftEnt.seekPtr; 
        int offset = 0; 
        byte[] readBlock = new byte[512];  
 
        
        if(ftEnt != null && buffer != null)
        {
	         while (ftEnt.seekPtr < ftEnt.iNode.length && bufferPointer < buffer.length)
           { 
               int readLength = Math.min(Disk.blockSize-offset, buffer.length - bufferPointer); 
               //either read to the end of the block or read the space left in the buffer
               short currentBlock = ftEnt.iNode.findTargetBlock(ftEnt.seekPtr);
 	             offset = ftEnt.seekPtr % Disk.blockSize; //block offset           
               SysLib.rawread(currentBlock, readBlock); 
               System.arraycopy(readBlock, offset, buffer, bufferPointer, readLength); //
               //copy block buffer into the parameter buffer
               bufferPointer += readLength; 
               ftEnt.seekPtr+= readLength;   
           }  
        	 return bufferPointer; 
        }
else 
{
    return -1; 
}
 
  }

  /*write (int fd, byte buffer[])
  //writes the contents of buffer to the file indicated by fd, starting at the
  //position indicated by the seek pointer. The operation may overwrite existing
  //data in the file and/or append to the end of the file. SysLib.write
  //increments the seek pointer by the number of bytes to have been written. The
  //return value is the number of bytes that have been written, or a negative
  //value upon an error.
  */

  public synchronized int write( FileTableEntry ftEnt, byte[] buffer)
  {
   	int bufferLength = buffer.length;
   	int writeBlock = ftEnt.iNode.findTargetBlock(ftEnt.seekPtr);
   	//error handling
  	if (writeBlock < 0)
  	{
  		if (writeBlock == -1)
  		{
  			SysLib.cerr("Pointer beyond end of file");
  		}
  		else if (writeBlock == -2)
  		{
  			SysLib.cerr("Negative pointer");
  		}
  		return -1;
  	}
  	else if (writeBlock + bufferLength > ftEnt.iNode.maxFileSize())
  	{
  		SysLib.cerr("Cannot write beyond maximum file length");
  		return -1;
  	}
    else if (ftEnt.mode == "r")
    {
      SysLib.cerr("Cannot write to read only file");
      return -1;
    }
	  	
  	//assumption: if there are enough free blocks,
  	//the buffer can be written to the file  

   	//if empty file
  	if (fsize(ftEnt) == 0)
  	{
  		//copy buffer into disk blocks until direct space runs out
  		for (int i = 0; i < 11 && i < (bufferLength / Disk.blockSize); i++)
  		{
  			byte[] blockOfData = new byte[Disk.blockSize];
  			System.arraycopy
  					(buffer, i*Disk.blockSize, blockOfData, 0, Disk.blockSize);
  					
  			ftEnt.iNode.direct[i] = (short)superblock.getFreeBlock();
  			SysLib.rawwrite(ftEnt.iNode.direct[i], blockOfData);
  		}
  		
  		if (bufferLength / Disk.blockSize + 1 > 11)
  		{
  			byte[] blockNumBytes = new byte[Disk.blockSize];
  			ftEnt.iNode.indirect = (short)superblock.getFreeBlock();
  			for (int i = 11; i < Disk.blockSize / 2 ||
  					i < (bufferLength/Disk.blockSize);	i++)
	  		{
	  			//put buffer into 512 byte chunk
	  			byte[] blockOfData = new byte[Disk.blockSize];
	  			System.arraycopy
	  					(buffer, i*Disk.blockSize, blockOfData, 0, Disk.blockSize);
	  			
	  			//put new block in indirect list
	  			int newBlockNum = superblock.getFreeBlock();
	  			SysLib.int2bytes(newBlockNum, blockNumBytes, (i-11)*2);
	  			SysLib.rawwrite(ftEnt.iNode.indirect, blockNumBytes);
	  			
	  			//write data to block
	  			SysLib.rawwrite(newBlockNum, blockOfData);
	  		}
  		}
  		return bufferLength;
  	}
	  	
  	//file already has stuff in it   	
  	int offsetInBlock = ftEnt.seekPtr % Disk.blockSize;
    int offsetInverse = Disk.blockSize - offsetInBlock;
  	byte[] blockOfData = new byte[Disk.blockSize];

		for (int i = 0; i < (bufferLength / Disk.blockSize); i++)
	  {				
			//put buffer into block chunk, taking into consideration the offset that
      //previous data puts on the block
      int remainingBytes = bufferLength - i*Disk.blockSize;
      if (offsetInBlock < remainingBytes)
      {
        System.arraycopy(buffer, i*Disk.blockSize,
          blockOfData, offsetInBlock, offsetInverse);
      }
      else
      {
        System.arraycopy(buffer, i*Disk.blockSize,
          blockOfData, offsetInBlock, remainingBytes);
      }

      //write said chunk to the block
      SysLib.rawwrite(writeBlock, blockOfData);
		}
    //update inode
    ftEnt.iNode.toDisk(ftEnt.iNumber);
    return bufferLength;
  }

  private boolean deallocAllBlocks( FileTableEntry ftEnt ) 
  {
    Inode iNode;
    int blockNumber;
    byte[] data;

    iNode = ftEnt.iNode;
    if(iNode == null || ftEnt == null)
    {
      return false;
    }

    if(iNode.count > 1)
    {
      return false;
    }

    //deallocate direct blocks
    for(int i = 0; i < iNode.length; i += Disk.blockSize)
    {
      blockNumber = iNode.findTargetBlock(i);
      //skip unallocated blocks
      if(blockNumber == ERROR)
      {
        continue;
      }
      superblock.returnBlock(blockNumber);
      iNode.setTargetBlock(blockNumber, (short) -1);
    }

    //deallocate indirect blocks
    data = iNode.deleteIndexBlock();
    if(data != null)
    {
      for(int i = 0; i < (Disk.blockSize / 2); i+= 2)
      {
        blockNumber = SysLib.bytes2int(data, i);
        if(blockNumber == ERROR)
        {
          continue;
        }
        superblock.returnBlock(blockNumber);
      }
    }
    iNode.toDisk(ftEnt.iNumber);
    return true;
  }

  /*
  //destroys the file specified by fileName. If the file is currently open, it
  //is not destroyed until the last open on it is closed, but new attempts to
  //open it will fail.
  */
  public boolean delete( String filename )
  {
    short iNumber;
    if(filename == "")
    {
      return false;
    }

    iNumber = directory.namei(filename);
    //file doesn't exist
    if(iNumber == ERROR)
    {
      return false;
    }
    return directory.ifree(iNumber);
  }

  /*
  //Updates the seek pointer corresponding to fd as follows:
  //  If whence is SEEK_SET (= 0), the file's seek pointer is set to offset
  //     bytes from the beginning of the file
  //  If whence is SEEK_CUR (= 1), the file's seek pointer is set to its current
  //     value plus the offset. The offset can be positive or negative.
  //  If whence is SEEK_END (= 2), the file's seek pointer is set to the size of
  //     the file plus the offset. The offset can be positive or negative.
  //If the user attempts to set the seek pointer to a negative number you must
  //clamp it to zero. If the user attempts to set the pointer to beyond the
  //file size, you must set the seek pointer to the end of the file. In both
  //cases, you should return success.
  */
  //cases
  private final int SEEK_SET = 0; 
  private final int SEEK_CUR = 1; 
  private final int SEEK_END = 2; 
  private final int ERROR = -1; 
  private final int OK = 0; 

  public synchronized int seek(FileTableEntry ftEnt, int offset, int whence)
  {
    switch (whence)
    {
      case SEEK_SET:
        ftEnt.seekPtr = seekClamper(ftEnt, offset);
        break;
      case SEEK_CUR:
        ftEnt.seekPtr = seekClamper(ftEnt, ftEnt.seekPtr + offset);
        break;
      case SEEK_END:
        ftEnt.seekPtr = seekClamper(ftEnt, fsize(ftEnt) + offset);
        break;
    }
    return ftEnt.seekPtr;
  }

  private synchronized int seekClamper(FileTableEntry ftEnt, int offset)
  {
    if (offset < 0) 
    {
      return 0;
    }
    else if (offset > fsize(ftEnt))
    {
      return fsize(ftEnt);
    }
    return offset;
  }
}
