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


         
      }

      /*
      // Assumes that we are not overwriting an inode that has already been
      // changed -- need to put that check in place higher up with a controlled
      // access inode vector of all inodes
      */
      void toDisk( short iNumber ) {    // save to disk as the i-th inode
         byte buffer[] = new byte[512];
         byte iNodeData[] = new byte[iNodeSize];
         SysLib.int2bytes(length, iNodeData, 0);
         SysLib.short2bytes(count, iNodeData, 4);
         SysLib.short2bytes(flag, iNodeData, 6);
         for ( int i = 0; i < directSize; i++ )
         {
            SysLib.short2bytes(direct[i], iNodeData, 8 + i*2);
         }
         SysLib.short2bytes(indirect, iNodeData, 30);

         //write to correct space on disk
         System.arraycopy(iNodeData, 0, buffer, iNumber*iNodeSize, iNodeSize);
         System.out.println("buffer = " + buffer);
         SysLib.rawwrite()


      }
   }
