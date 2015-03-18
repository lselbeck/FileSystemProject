//Directory.java
//Author: Luke Selbeck, Greg Kitzmiller, Dallas Van Ess 
//Date: 3/18/2015
//
//Description:
//Start of the file system; root

public class Directory {
	private static int maxChars = 30; // max characters of each file name

	// Directory entries
	private int fsize[];        // each element stores a different file size.
	private char fnames[][];    // each element stores a different file name.

	public Directory( int maxInumber )
         { // directory constructor
		fsize = new int[maxInumber];     // maxInumber = max files
		for ( int i = 0; i < maxInumber; i++ ) 
		 fsize[i] = 0;                 // all file size initialized to 0
		fnames = new char[maxInumber][maxChars];
		String root = "/";                // entry(inode) 0 is "/"
		fsize[0] = root.length( );        // fsize[0] is the size of "/".
		root.getChars( 0, fsize[0], fnames[0], 0 ); // fnames[0] includes "/"
	}

	public void bytes2directory( byte data[] )
        {
		// assumes data[] received directory information from disk
		// initializes the Directory instance with this data[]
                // gets the size of the file from the offset
                // the name of the file getting the characters from the array 
            int offset = 0; 
             for (int i=0; i<fsize.length; i++, offset += 4) 
                 { 
                  fsize[i] = SysLib.bytes2int(data, offset); //stores file sizes 
                 }
             for (int i =0; i < fnames.length; i++, offset += maxChars *2)
                 {
                   String fname = new String(data, offset, maxChars *2); 
                   fname.getChars(0, fsize[i], fnames[i], 0);  
                  
                 }
	}

	public byte[] directory2bytes( )
        {
		// converts and return Directory information into a plain byte array
		// this byte array will be written back to disk
		// note: only meaningfull directory information should be converted
		// into bytes.

             byte[] byteDirectory = new byte[(fsize.length * 4) + (fnames.length * maxChars * 2)]; 
             int offset = 0; 
            
            for(int i = 0; i < fsize.length; i++, offset += 4) //get sizes & stores into dir first
               { 
                 SysLib.int2bytes(fsize[i], byteDirectory, offset); 
               } 

            for (int i =0; i <fsize.length; i++) //second, stores the names 
               { 
                  for (int j = 0; j < maxChars * 2; j++)
                  { 
                    byteDirectory[offset] = (byte) fnames[i][j]; 
                    offset++;
                  } 
               }
             return byteDirectory; 
	}

	public short ialloc( String filename ) {
		// filename is the one of a file to be created.
		// allocates a new inode number for this filename
		int sizeLength = fsize.length;
		for(int i = 1; i < sizeLength; i++)
		{
			if(fsize[i] == 0)
			{
				fsize[i] = Math.min(filename.length(), maxChars);
				filename.getChars(0, fsize[i], fnames[i], 0);
				return (short) i;
			}
		}
		return -1;
	}

	// deallocates this inumber (inode number)
	// the corresponding file will be deleted.
	public boolean ifree( short iNumber ) 
	{
		if(fsize[iNumber] < 0)
		{
			return false;
		}
		fsize[iNumber] = 0;
		return true;
	}

	// returns the inumber corresponding to this filename
	public short namei( String filename ) 
	{
		
		String tmp;
		int strLen = filename.length();
		int sizeLength = fsize.length;
		for(int i = 1; i < sizeLength; i++)
		{
			if(fsize[i] == strLen)
			{
				tmp = new String(fnames[i], 0, fsize[i]);
				if(tmp.compareTo(filename) == 0)
				{
					return (short) i;
				}
			}
		}
		return -1;
	}
}
