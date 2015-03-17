//Inode.java
//Author: Luke Selbeck
//Date: 3/18/2015
//
//Description:
//Contains attributes to one file

public class Inode {
   private final static int iNodeSize = 32;       // fix to 32 bytes
   private final static int directSize = 11;      // # direct pointers
	private final static int maxFileSize =
			(11 + (Disk.blockSize / 2)) * Disk.blockSize;

   public int length; //4 bytes     // file size in bytes
   public short count;//2 bytes     // # file-table entries pointing to this
   public short flag; //2 bytes     // 0 = unused, 1 = used, ...
   public short direct[] = new short[directSize];//22 bytes; direct pointers
   public short indirect; //2 bytes              // a indirect pointer

   //flags
   public static final short UNUSED = 0;
   public static final short USED = 1;
   public static final short READ = 2;
   public static final short WRITE = 3;
   public static final short DELETE = 4;

   Inode( ) 
   {                                    //a default constructor
      initializeDefaults();
   }

   Inode( short iNumber ) //retrieving inode from disk
   {
      int blockNumber = 1 + iNumber / (Disk.blockSize / iNodeSize);
      int offset = (iNumber % (Disk.blockSize / iNodeSize)) * iNodeSize;
      byte[] nodeData = new byte[Disk.blockSize];
      SysLib.rawread(blockNumber, nodeData);

      length = SysLib.bytes2int(nodeData, offset);
      offset += 4;
      count = SysLib.bytes2short(nodeData, offset);
      offset += 2;
      flag = SysLib.bytes2short(nodeData, offset);
      offset += 2;
      for ( int i = 0; i < directSize; i++ )
      {
         direct[i] = SysLib.bytes2short(nodeData, offset);
         offset += 2;
      }
      indirect = SysLib.bytes2short(nodeData, offset);

      if (length < 1 || indirect < 2) //invalid node data; use default constructor
      {
         initializeDefaults();
      }
   }

   /*
   // Assumes that we are not overwriting an inode that has already been
   // changed -- need to put that check in place higher up with a controlled
   // access inode vector of all inodes
   */
   public void toDisk( short iNumber ) {    // save to disk as the i-th inode
      int blockNumber = 1 + iNumber / (Disk.blockSize / iNodeSize);
      int offset = (iNumber % (Disk.blockSize / iNodeSize)) * iNodeSize;
      byte buffer[] = new byte[Disk.blockSize];
      byte nodeData[] = new byte[iNodeSize];

      //write the inode data into buffer
      SysLib.int2bytes(length, nodeData, offset);
      offset += 4;
      SysLib.short2bytes(count, nodeData, offset);
      offset += 2;
      SysLib.short2bytes(flag, nodeData, offset);
      offset += 2;
      for ( int i = 0; i < directSize; i++ )
      {
         SysLib.short2bytes(direct[i], nodeData, offset);
         offset += 2;
      }
      SysLib.short2bytes(indirect, nodeData, offset);
      
      //write to disk
      SysLib.rawwrite(blockNumber, buffer);
   }

   public short getIndexBlockNumber()
   {
      return indirect;
   }

   public boolean setIndexBlock(short indexBlockNumber)
   {
      if (indexBlockNumber > 1)
      {
         indirect = indexBlockNumber;
         return true;
      }
      return false;
   }

   public short findTargetBlock(int offset)
   {
      if (offset > length)
      {
         return -1; //trying to access past the end of the file
      }

		int block = offset/512;

      if (block > 10)
      {
         byte[] pointerBlockData = new byte[Disk.blockSize];
         SysLib.rawread(indirect, pointerBlockData);
         return SysLib.bytes2short(pointerBlockData, (block-11)*2);
      }
      else if (block > -1)
      {
         return direct[block];
      }
      else
      {
         return -2; //negative pointer location
      }
   }
   
   public int maxFileSize()
   {
   	return maxFileSize;
   }

   private void initializeDefaults() 
   {
      length = 0;
      count = 0;
      flag = USED;
      for ( int i = 0; i < directSize; i++ )
         direct[i] = -1;
      indirect = -1;
   }
}

