//FileTable.java
//Author: Dallas Van Ess
//Date: 3/18/2015
//
//Description:
//The file system maintains the file (structure) table shared among all user
//threads

import java.util.Vector;

public class FileTable {

	private Vector<FileTableEntry> table;         // the actual entity of this file table
	private Directory root;        // the root directory 

	public FileTable( Directory directory ) 
	{ 
		table = new Vector<FileTableEntry>();     // instantiate a file (structure) table
		root = directory;           // receive a reference to the Director
	}                             // from the file system

	// major public methods

	// allocate a new file (structure) table entry for this file name
	// allocate/retrieve and register the corresponding inode using dir
	// increment this inode's count
	// immediately write back this inode to the disk
	// return a reference to this file (structure) table entry
	public synchronized FileTableEntry falloc( String filename, String m ) 
	{
		String mode = FileTableEntry.getMode(m);
		if(mode == null)
		{
			return null;
		}
		short iNumber = -1;
		Inode iNode = null;

		FileTableEntry fte;

		while(true)
		{
			//check for root
			if(filename.equals("/"))
			{
				iNumber = 0;
			}
			else
			{
				iNumber = root.namei(filename);
			}

			if(iNumber < 0)
			{
				if(mode.compareTo(FileTableEntry.READONLY) == 0)
				{
					return null;
				}

				iNumber = root.ialloc(filename);
				if(iNumber < 0)
				{
					return null;
				}
				iNode = new Inode();
				break;
			}
			iNode = new Inode(iNumber);
			if(iNode.flag == Inode.DELETE)
			{
				return null;
			}

			if(iNode.flag == Inode.UNUSED || iNode.flag == Inode.USED)
			{
				break;
			}
			if(mode.compareTo(FileTableEntry.READONLY) == 0 && iNode.flag == Inode.READ)
			{
				break;
			}
			try
			{
				wait();
			}
			catch(InterruptedException e) { }
		}	
		iNode.count++;
		iNode.toDisk(iNumber);
		fte = new FileTableEntry(iNode, iNumber, m);
		table.add(fte);
		return fte;
	}

	// receive a file table entry reference
	// save the corresponding inode to the disk
	// free this file table entry.
	// return true if this file table entry found in my table
	public synchronized boolean ffree( FileTableEntry e ) 
	{
		if(e == null)
		{
			return true;
		}

		Inode iNode = e.iNode;
		short iNumber = e.iNumber;

		if(!table.removeElement(e))
		{
			return false;
		}

		if(iNode.count > 0)
		{
			iNode.count--;
		}
		if(iNode.count == 0)
		{
			iNode.flag = Inode.UNUSED;
		}

		iNode.toDisk(iNumber);

		if(iNode.flag == Inode.READ || iNode.flag == Inode.WRITE)
		{
			notify();
		}

		e = null;
		return true;
	}

	// return if table is empty 
	// should be called before starting a format
	public synchronized boolean fempty( ) 
	{
		return table.isEmpty( );  
	}                           
}
