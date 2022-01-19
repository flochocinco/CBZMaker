import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CBZMaker {

	private static final String MAX_PAGES_ARG = "-MaxPages";

	public static void main(String[] args) {
		
		List<String> help = Arrays.asList("h", "-h", "--help", "?", "/h", "/help");
        int maxNumberOfPages = 0;
		
        boolean exportAsPDF = false;
        
        if(args.length == 0 ){
			displayHelp();
			return;
		}
        
		for(int i = 0; i < args.length; i++ ){
			if(help.contains(args[i])){
				displayHelp();
				return;
			}
			if("-pdf".equalsIgnoreCase(args[i])){
				exportAsPDF = true;
			}
			if(MAX_PAGES_ARG.equalsIgnoreCase(args[i])){
				if(i == args.length-1){
					System.err.println("Missing value after MaxPages argument");
					return;
				}else{
					try{
						maxNumberOfPages = Integer.valueOf(args[++i]);
						maxNumberOfPages = Math.max(0, maxNumberOfPages);
					}catch(Exception e){
						System.err.println("Missing integer value after MaxPages argument");
						return;
					}
				}
			}
		}
		
		String currentLocation = args[0];
		File currentLocationFile = new File(currentLocation);
		
		String mangaName = currentLocationFile.getName();
		
		//from a root, go inside all folders and create a cbz file per folder
		for(File folder : currentLocationFile.listFiles()){
			if(!folder.isDirectory()){
				continue;
			}
			
			if(exportAsPDF){
				new Images2PDF(maxNumberOfPages).generatePDFFromImage(folder.getAbsolutePath());
				continue;
			}
			
			// zip all jpg files
			File cbz = new File(currentLocation, mangaName + "_" + folder.getName() +(maxNumberOfPages > 0 ? "_1" : "") + (exportAsPDF ? ".pdf" : ".cbz"));
			System.out.print("Creating " + cbz.getAbsolutePath() + "...");

			try {
				ZipOutputStream out = new ZipOutputStream(new FileOutputStream(cbz));

				List<File> pages = Arrays.asList(folder.listFiles());
				pages.sort(Util.getFileComparator());
				
				int currentPage = 0;
				for(File page : pages){
					if(maxNumberOfPages > 0 && ++currentPage%maxNumberOfPages == 0){
						out.close();
						System.out.println("Done!");
						System.out.print("Creating " + cbz.getAbsolutePath() + "...");
						cbz = new File(currentLocation, mangaName + "_" + folder.getName() + "_" + Integer.valueOf(currentPage/maxNumberOfPages + 1).toString() + ".cbz");
						out = new ZipOutputStream(new FileOutputStream(cbz));
					}
					if(!page.getName().endsWith(".jpg") && !page.getName().endsWith(".jpeg")){
						continue;
					}
					
					ZipEntry e = new ZipEntry(page.getName());
					out.putNextEntry(e);
					FileInputStream fis = new FileInputStream(page);
					byte[] bytes = new byte[1024];
		            int length;
		            while((length = fis.read(bytes)) >= 0) {
		                out.write(bytes, 0, length);
		            }
		            
		            fis.close();
					out.closeEntry();
				}
				
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("Done!");
		}
		System.out.println("Finished! Thank you for using this tool");
	}
	
	protected static void displayHelp(){
		System.out.println("<<< CBZ Maker >>>"); 
		System.out.println("Please provide a path to a serie containing folders containing jpg or jpeg files.");
		System.out.println("For instace use: CBZMaker \"c:\\temp\\Made in Abyss\" [" + MAX_PAGES_ARG + " 100]");
		System.out.println("optional argument: " + MAX_PAGES_ARG + " : specifiy max number of page per volume ");
	}
}
