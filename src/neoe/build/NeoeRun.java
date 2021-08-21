package neoe . build ;
import java . io . * ;
import java . util . * ;
import neoe . build . util . * ;

public class NeoeRun {
	public static void main( String [ ] args ) throws Exception {
		new NeoeRun( ) . run( args ) ;
	}
	public int run( String [ ] args ) throws Exception {
		assert( args [ 0 ] . equals( "-run" ) ) ;
		String base = System . getenv( "neoebuild_base" ) ;
		if ( base == null || base . length( ) == 0 ) {
			System . out . println( "cannot find env neoebuild_base" ) ;
			return 1 ;
		}
		File conf = new File( base , "livepool.cache" ) ;
		if ( ! conf . isFile( ) ) {
			System . out . println( "cannot find file " + conf . getAbsolutePath( ) ) ;
			return 1 ;
		}

		String prjname = null ;
		if ( args . length >= 2 ) { prjname = args [ 1 ] ; }
		String prjPath = null ;
		Map m =( Map ) PyData . parseAll( FileUtil . readString( new FileInputStream( conf ) , null ) ) ;
		//Map m2 =new HashMap();
		for ( Object k : m . keySet( ) ) {
			String path = ( String ) k ;
			int p1 = path . lastIndexOf( "/" ) ;
			String name = path . substring( p1 + 1 ) ;
			if ( prjname == null ) {
				System . out . println( "name:" +  name ) ;
			} else {
				if ( name . equalsIgnoreCase( prjname ) ) {
					if ( prjPath == null ) {
						prjPath = path ;
					} else {
						System . out . printf( "Ambiguities path [%s] and [%s]\n" , prjPath , path ) ;
						return 1 ;
					}
				}
			}
		}
		if ( prjname == null ) {
			return 0 ;
		}
		if ( prjPath == null ) {
			System . out . println( "cannot find name:" + prjname ) ;
			return 1 ;
		}
		File dir ;
		if ( prjPath . charAt( 0 ) == '/' ) {
			dir = new File( prjPath ) ;
		} else {
			dir = new File( base , prjPath ) ;
		}
		File mybuild = new File( dir , "mybuild" ) ;
		if ( ! mybuild . isFile( ) ) {
			System . out . println( "cannot find :" + mybuild . getAbsolutePath( ) ) ;
			return 1 ;
		}
		try {
			new BuildMain( ) . main( new String [ ] { mybuild . getAbsolutePath( ) } ) ;
		} catch( Exception ex ) {
			ex . printStackTrace( ) ;
			System . out . println( "build failed" ) ;
			return 1 ;
		}
		String mainClass = args [ 2 ] ;
		String [ ] args2 = new String [ args . length - 3 ] ;
		if ( args2 . length > 0 )
		System . arraycopy( args , 3 , args2 , 0 , args2 . length ) ;
		Loader . load( new File( dir , "dist" ) . getAbsolutePath( ) , mainClass , args2 ) ;
		return 0 ;
	}
}

