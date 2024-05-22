import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

public class CBZMaker {

	private static final String TMP_GRAYSCALE_JPG = "tmpGrayScale.jpg";
	private static final String MAX_PAGES_ARG = "-MaxPages";
	private static final String GROUP_VOLUMES_ARG = "-GroupVolumes";
	private static final String CONVERT_TO_GRAYSCALE_ARG = "-GrayScale";

	public static void main(String[] args) {
		
		System.out.println("Welcome to CBZ Maker");
		
		List<String> help = Arrays.asList("h", "-h", "--help", "?", "/h", "/help");
        int maxNumberOfPages = 0;
        int groupVolumes = 1;
        boolean convertToGrayScale = false;
		
        boolean exportAsPDF = false;
        
        if(args.length == 0 ){
			displayHelp();
			return;
		}
        
		for(int i = 0; i < args.length; i++ ){
			if(help.contains(args[i])){
				displayHelp();
				return;
			}else if("-pdf".equalsIgnoreCase(args[i])){
				exportAsPDF = true;
			}else if(MAX_PAGES_ARG.equalsIgnoreCase(args[i])){
				if(i == args.length-1){
					System.err.println("Missing value after " + MAX_PAGES_ARG + " argument");
					return;
				}else{
					try{
						maxNumberOfPages = Integer.valueOf(args[++i]);
						maxNumberOfPages = Math.max(0, maxNumberOfPages);
					}catch(Exception e){
						System.err.println("Missing integer value after " + MAX_PAGES_ARG + " argument");
						return;
					}
					System.out.println("File will be truncated to maximum " + maxNumberOfPages + " pages");
				}
			}else if(GROUP_VOLUMES_ARG.equalsIgnoreCase(args[i])){
				if(i == args.length-1){
					System.err.println("Missing value after " + GROUP_VOLUMES_ARG + " argument");
					return;
				}else{
					try{
						groupVolumes = Integer.valueOf(args[++i]);
						groupVolumes = Math.max(1, groupVolumes);
					}catch(Exception e){
						System.err.println("Missing integer value after " + GROUP_VOLUMES_ARG + " argument");
						return;
					}
					System.out.println("Volumes will be grouped by " + groupVolumes);
				}
			}else if(CONVERT_TO_GRAYSCALE_ARG.equalsIgnoreCase(args[i])){
				convertToGrayScale = true;
			}
		}
		
		if(maxNumberOfPages > 0 && groupVolumes > 1){
			System.err.println("Please, do not use both " + MAX_PAGES_ARG + " and " + GROUP_VOLUMES_ARG + " arguments.");
			return;
		}
		
		String currentLocation = args[0];
		File currentLocationFile = new File(currentLocation);
		
		String mangaName = currentLocationFile.getName();
		
		//from a root, go inside all folders and create a cbz file per folder
		int volumeCount = 0;
		File cbz = null;
		List<File> volumes = Arrays.asList(currentLocationFile.listFiles());
		volumes.sort(Util.getFileComparator());
		ZipOutputStream out = null;
		for(File folder : volumes){
			if(!folder.isDirectory()){
				continue;
			}
			
			if(exportAsPDF){
				new Images2PDF(maxNumberOfPages).generatePDFFromImage(folder.getAbsolutePath());
				continue;
			}
			
			volumeCount += 1;
			
			try {
				// zip all jpg files
				if(cbz == null || volumeCount%groupVolumes == 1){
					cbz = createFile(maxNumberOfPages, groupVolumes, currentLocation, mangaName, volumeCount, folder);
					System.out.println("Creating " + cbz.getAbsolutePath() + "...");
					out = new ZipOutputStream(new FileOutputStream(cbz));
				}

				List<File> pages = Arrays.asList(folder.listFiles());
				pages.sort(Util.getFileComparator());
				
				int currentPage = 0;
				for(File page : pages){
					if(!page.getName().endsWith(".jpg") && !page.getName().endsWith(".jpeg")){
						continue;
					}
					currentPage += 1;
					if(maxNumberOfPages > 0 && currentPage%maxNumberOfPages == 0){
						out.close();
						System.out.println("Done!");
						System.out.print("Creating " + cbz.getAbsolutePath() + "...");
						cbz = new File(currentLocation, mangaName + "_" + folder.getName() + "_Part" + Integer.valueOf(currentPage/maxNumberOfPages + 1).toString() + ".cbz");
						out = new ZipOutputStream(new FileOutputStream(cbz));
					}
					
					
					String zipEntryName = (groupVolumes > 1 ? "Volume" + volumeCount : "") + page.getName();
					//System.out.println("puting " + zipEntryName);
					ZipEntry e = new ZipEntry(zipEntryName);
					out.putNextEntry(e);
					File converted = convertToGrayScale ? convertIntoBlackAndWhite(page) : page;
					FileInputStream fis = new FileInputStream(converted);
					byte[] bytes = new byte[1024];
		            int length;
		            while((length = fis.read(bytes)) >= 0) {
		                out.write(bytes, 0, length);
		            }
		            
		            fis.close();
					out.closeEntry();
					
					System.out.println(cbz.getName() + ": Volume \"" + page.getParentFile().getName() + "\" Page: " + currentPage + "/" + pages.size());
				}
				
				if(groupVolumes == 1 || groupVolumes > 1 && volumeCount%groupVolumes == 0){
					out.close();
					cbz = null;
					System.out.println("Done!");
				}
				
				if(convertToGrayScale){
					Files.delete(new File(folder, TMP_GRAYSCALE_JPG).toPath());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Finished! Thank you for using this tool");
	}

	private static File createFile(int maxNumberOfPages, int groupVolumes, String currentLocation, String mangaName,
			int volumeCount, File folder) {
		File cbz;
		String cbzName = mangaName + " ";
		if(groupVolumes > 1){
			String volumes = String.valueOf(volumeCount);
			int i = volumeCount;
			while(i%groupVolumes != 0){
				i++;
			}
			volumes += " to " +  (i);
			cbzName += volumes +(maxNumberOfPages > 0 ? "_Part1" : "") + ".cbz";
		}else{
			cbzName += "_" + folder.getName() +(maxNumberOfPages > 0 ? "_Part1" : "") + ".cbz";
		}
		cbz = new File(currentLocation, cbzName);
		return cbz;
	}
	
	//conversion credit goes to https://www.tutorialspoint.com/java_dip/grayscale_conversion.htm#:~:text=File%20input%20%3D%20new%20File(%22,GrayScale()%20method%20on%20it.
	protected static File convertIntoBlackAndWhite(File inputFile){
		try {
			BufferedImage   image = ImageIO.read(inputFile);
	         int width = image.getWidth();
	         int height = image.getHeight();
	         
	         for(int i=0; i<height; i++) {
	         
	            for(int j=0; j<width; j++) {
	            
	               Color c = new Color(image.getRGB(j, i));
	               int red = (int)(c.getRed() * 0.299);
	               int green = (int)(c.getGreen() * 0.587);
	               int blue = (int)(c.getBlue() *0.114);
	               Color newColor = new Color(red+green+blue,
	               
	               red+green+blue,red+green+blue);
	               
	               image.setRGB(j,i,newColor.getRGB());
	            }
	         }
	         
	         File output = new File(inputFile.getParentFile(), TMP_GRAYSCALE_JPG);
	         ImageIO.write(image, "jpg", output);
	         return inputFile;
	         
	    } catch (Exception e) {e.printStackTrace();}
		return null;
	    
	}
	
	protected static void displayHelp(){
		System.out.println("<<< CBZ Maker >>>"); 
		System.out.println("Please provide a path to a serie containing folders containing jpg or jpeg files.");
		System.out.println("For instace use: CBZMaker \"c:\\temp\\Made in Abyss\" [" + MAX_PAGES_ARG + " 100]");
		System.out.println("optional argument: " + MAX_PAGES_ARG + " : specifiy max number of page per volume");
		System.out.println("optional argument: " + GROUP_VOLUMES_ARG + " : specifiy number of volumes to group");
		System.out.println("optional argument: -pdf : create pdf instead of cbz files");
		System.out.println("optional argument: " + CONVERT_TO_GRAYSCALE_ARG + " : convert all images in gray scale. Usefull for e-reader (decrease drasticaly memory size)");
	}
}
