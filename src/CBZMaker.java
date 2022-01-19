import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CBZMaker {

	public static void main(String[] args) {
		
		List<String> help = Arrays.asList("h", "-h", "--help", "?", "/h", "/help");
		if(args.length == 0 || args.length > 1 || help.contains(args[0])){
			System.out.println("<<< CBZ Maker >>>"); 
			System.out.println("Please provide a path to a serie containing folders containing jpg or jpeg files.");
			System.out.println("For instace use: CBZMaker \"c:\\temp\\Made in Abyss\"");
			return;
		}
		
		String currentLocation = args[0];
		File currentLocationFile = new File(currentLocation);
		
		String mangaName = currentLocationFile.getName();
		
		//from a root, go inside all folders and create a cbz file per folder
		for(File folder : currentLocationFile.listFiles()){
			if(!folder.isDirectory()){
				continue;
			}
			// zip all jpg files
			File cbz = new File(currentLocation, mangaName + "_" + folder.getName() + ".cbz");
			System.out.print("Creating " + cbz.getAbsolutePath() + "...");
			ZipOutputStream out;
			try {
				out = new ZipOutputStream(new FileOutputStream(cbz));

				List<File> pages = Arrays.asList(folder.listFiles());
				pages.sort(new Comparator<File>() {

					@Override
					public int compare(File o1, File o2) {
						if(o1 == null){
							return o2 == null ? 0 : 1;
						}
						if(o2 == null){
							return -1;
						}
						return o1.getName().compareTo(o2.getName());
					}
				});
				
				for(File page : pages){
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
}
