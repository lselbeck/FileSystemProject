//FileTableEntry.java
//Author: Luke Selbeck, Dallas Van Ess
//Date: 3/18/2015
//
//Description:
//The file system maintains the file (structure) table shared among all user
//threads



public class FileTableEntry             // Each table entry should have
{
	public int seekPtr;                 //    a file seek pointer
	public final Inode inode;           //    a reference to its inode
	public final short iNumber;         //    this inode number
	public int count;                   //    # threads sharing this entry
	public final String mode;           //    "r", "w", "w+", or "a"

	//modes
	public static final String READONLY = "r";
	public static final String WRITEONLY = "w";
	public static final String READWRITE = "w+";
	public static final String APPEND = "a";

	public FileTableEntry ( Inode i, short inumber, String m ) {
		seekPtr = 0;             // the seek pointer is set to the file top
		inode = i;
		iNumber = inumber;
		count = 1;               			// at least on thread is using this entry
		mode = getMode(m);                  // once access mode is set, it never changes
		if (mode.compareTo(APPEND) == 0)				// if mode is append,
		{
			seekPtr = inode.length;        // seekPtr points to the end of file
		} 				
			
	}

	public static String getMode(String mode)
	{
		mode = mode.toLowerCase();

		if(mode.compareTo("r") == 0)
		{
			return READONLY;
		}

		if(mode.compareTo("w") == 0)
		{
			return WRITEONLY;
		}

		if(mode.compareTo("w+") == 0)
		{
			return READWRITE;
		}

		if(mode.compareTo("a") == 0)
		{
			return APPEND;
		}

		return -1;
	}
}
