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
         byte buffer[] = new byte[32];
         SysLib.int2bytes(length, buffer, 0);
         SysLib.short2bytes(count, buffer, 4);
         SysLib.short2bytes(flag, buffer, 6);
         for ( int i = 0; i < directSize; i++ )
         {
            SysLib.short2bytes(direct[i], buffer, 8 + i*2);
         }
         SysLib.short2bytes(indirect, buffer, 30);

         //write to correct space on disk
      }

      int toDisk( short iNumber ) {    // save to disk as the i-th inode
         // design it by yourself.
      }
   }
