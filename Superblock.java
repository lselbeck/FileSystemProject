//Superblock.java
//Author: Luke Selbeck
//Date: 3/18/2015
//
//Description:
//Used to describe (1) the number of disk blocks, (2) the number of inodes, and
//(3) the block number of the head block of the free list

class Superblock {
    public int totalBlocks; // the number of disk blocks
    public int totalInodes; // the number of inodes
	public int freeList;    // the block number of the free list's head
}
