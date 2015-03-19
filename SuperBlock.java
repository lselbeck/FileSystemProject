//Superblock.java
//Author: Luke Selbeck
//Date: 3/18/2015
//
//Description:
//Used to describe (1) the number of disk blocks, (2) the number of inodes, and
//(3) the block number of the head block of the free list

class SuperBlock {
   private final int defaultInodeBlocks = 64;
   public int totalBlocks; // the number of disk blocks
   public static int totalInodes; // the number of inodes
   public int freeList;    // the block number of the free list's head

   public SuperBlock(int diskSize)
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

   public boolean format(int totalFiles)
   {
      if (totalFiles < 1 || totalFiles > totalBlocks - 2)
      {
         return false;
      }
      
      //zero out disk
      byte zeroData[] = new byte[Disk.blockSize];
      for (int i = 0; i < totalBlocks; i++)
      {
         SysLib.rawwrite(i, zeroData);
      }

      //initialize inode/superblock data
      totalInodes = totalFiles;
      Inode init;
      for (short i = 0; i < totalInodes; i++)
      {
         init = new Inode(i);
         init.flag = 0;
         init.toDisk(i);
      }
      
      //add all the rest of the blocks to the free list
      freeList = 2 + totalFiles/16; //the first free block after inode blocks
      for (int i = freeList; i < totalBlocks; i++)
      {
      	returnBlock(i);
      }
      
      sync();

      return true;
   }

   public void sync() //write data members to buffer and then write to disk
   {
      byte superblock[] = new byte[Disk.blockSize];
      SysLib.int2bytes(totalBlocks, superblock, 0);
      SysLib.int2bytes(totalInodes, superblock, 4);
      SysLib.int2bytes(freeList, superblock, 8);
      SysLib.rawwrite(0, superblock);
   }

   public int getFreeBlock() //dequeue the top block from the free list
   {
      int retVal = freeList; //return freeList

      //reassign head to what the top block was pointing to
      byte[] topBlock = new byte[Disk.blockSize];
      SysLib.rawread(retVal, topBlock);
      freeList = SysLib.bytes2int(topBlock, 0);

      //zero out block to use
      SysLib.int2bytes(0, topBlock, 0);
      SysLib.rawwrite(retVal, topBlock);

      return retVal;
   }

   //enqueue a given block to the free list
   public boolean returnBlock(int blockNumber)
   {
      if (blockNumber < 2 || blockNumber >= totalBlocks)
      {
         return false;
      }

      //new block points to old head
      byte[] newBlock = new byte[Disk.blockSize];
      SysLib.int2bytes(freeList, newBlock, 0);
      SysLib.rawwrite(blockNumber, newBlock);

      //reassign head to point to new block
      freeList = blockNumber;

      return true;
   }
}
