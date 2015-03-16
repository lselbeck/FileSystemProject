//Inode.java
//Author: Luke Selbeck
//Date: 3/18/2015
//
//Description:
//Contains attributes to one file

public class Inode {
      private final static int iNodeSize = 32;       // fix to 32 bytes
      private final static int directSize = 11;      // # direct pointers

      public int length; //4 bytes     // file size in bytes
      public short count;//2 bytes     // # file-table entries pointing to this
      public short flag; //2 bytes     // 0 = unused, 1 = used, ...
      public short direct[] = new short[directSize];//22 bytes; direct pointers
      public short indirect; //2 bytes              // a indirect pointer

      Inode( ) {                                    // a default constructor
         length = 0;
         count = 0;
         flag = 1;
         for ( int i = 0; i < directSize; i++ )
            direct[i] = -1;
         indirect = -1;
      }

      Inode( short iNumber ) {         // retrieving inode from disk
         int blockNumber = 1 + iNumber / (Disk.blockSize / iNodeSize);
         int offset = (iNumber % (Disk.blockSize / iNodeSize)) * iNodeSize;
         byte[] nodeData = new byte[Disk.blockSize];
         SysLib.rawread(blockNumber, nodeData);

         length = SysLib.bytes2int(nodeData, offset);
         offset += 4;
         count = SysLib.bytes2short(data, offset);
         offset +=
      }

      /*
      // Assumes that we are not overwriting an inode that has already been
      // changed -- need to put that check in place higher up with a controlled
      // access inode vector of all inodes
      */
      int toDisk( short iNumber ) {    // save to disk as the i-th inode
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

         return 1; //TODO find out what to return
      }
   }
