//Superblock.java
//Author: Luke Selbeck
//Date: 3/18/2015
//
//Description:
//Used to describe (1) the number of disk blocks, (2) the number of inodes, and
//(3) the block number of the head block of the free list

class Superblock {
   private final int defaultInodeBlocks = 64;
   public int totalBlocks; // the number of disk blocks
   public int totalInodes; // the number of inodes
   public int freeList;    // the block number of the free list's head

   public Superblock(int diskSize)
   {
      //attempt to get existing superblock
      byte[] superblock = new byte[Disk.blockSize];
      SysLib.rawread(0, superblock);
      totalBlocks = SysLib.bytes2int(superblock, 0);
      totalInodes = SysLib.bytes2int(superblock, 4);
      freeList = SysLib.bytes2int(superblock, 8);

      if (totalBlocks != diskSize || totalInodes <= 0 || freeList < 2)
      {
         //need to format disk
         totalBlocks = diskSize;
         format(defaultInodeBlocks);
      }
   }

   public format()
   {
      //TODO
   }

   public sync()
   {
      //TODO
   }

   public getFreeBlock()
   {
      //TODO
   }

   public returnBlock(int blockNumber)
   {
      //TODO
   }
}
