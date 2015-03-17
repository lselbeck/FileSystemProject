//FileSystem.java
//Author:GKITZ & Luke Selbeck
//Date: 3/18/2015
//
//Description:
//User interface for the file system.  Can read/write, open/close, format,
//seek.

public class FileSystem {

  private SuperBlock superblock; 
  private Directory directory; 
  private Filetable filetable; 


 private final int SEEK_SET = 0; 
 private final int SEEK_CUR = 1; 
 private final int SEEK_END = 2; 
 private final int ERROR = -1; 
 private final int OK = 0; 

  public FileSystem(int diskBlocks){
    //create superblock and format disk with 64 iNodes in default
    superblock = new SuperBlock(diskBlocks); 

    //create directory and register '/' in directory entry 0
    directory = new Directory (superblock.inodeBlocks ); 

    //file table is created and store directory in the file table 
    filetable = new Filetable (directory); 

    //directory reconstruction
    FileTableEntry dirEnt = ("/", "r");
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
   close(dirByte);  
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
  superblock.format(files);
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
    FileTableEntry ftEnt = filetable.falloc (filename, mode); 
    if (mode.equals( "w" ) 
    {

     
        if (deallocAllBlocks (ftEnt ) == false ) // need to implement
           return null; 
    }
   return ftEnt;  
}

public boolean close (FileTableEntry ftEnt ) 
{ 

}

//returns the size in bytes of the file indicated by fd.

public int fsize ( FileTableEntry ftEnt ) 
{
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


public int read (FileTableEntry ftEnt, byte[] buffer)
{
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
	
	//assumption: if there are enough free blocks,
	//the buffer can be written to the file
	int offsetInBlock
	
}

private boolean deallocAllBlocks( FileTableEntry ftEnt ) 
{
}

boolean delete( String filename )
{
}

/*
//Updates the seek pointer corresponding to fd as follows:
//  If whence is SEEK_SET (= 0), the file's seek pointer is set to offset bytes
//     from the beginning of the file
//  If whence is SEEK_CUR (= 1), the file's seek pointer is set to its current
//     value plus the offset. The offset can be positive or negative.
//  If whence is SEEK_END (= 2), the file's seek pointer is set to the size of
//     the file plus the offset. The offset can be positive or negative.
//If the user attempts to set the seek pointer to a negative number you must
//clamp it to zero. If the user attempts to set the pointer to beyond the
//file size, you must set the seek pointer to the end of the file. In both
//cases, you should return success.
*/
int seek(FileTableEntry ftEnt, int offset, int whence)
{
}


int write( int fd, byte buffer[] )
{

}

/*
//closes the file corresponding to fd, commits all file transactions on this
//file, and unregisters fd from the user file descriptor table of the calling
//thread's TCB. The return value is 0 in success, otherwise -1.
*/

int close( int fd )
{

}


/*
//destroys the file specified by fileName. If the file is currently open, it is
//not destroyed until the last open on it is closed, but new attempts to open
//it will fail.
*/
int delete( String fileName )
{

}

