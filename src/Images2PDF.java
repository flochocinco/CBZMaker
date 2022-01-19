
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

public class Images2PDF {
	
	int max_pages = 0;
	
	public Images2PDF() {
		this(0);
	}
	
	public Images2PDF(int max_pages) {
		this.max_pages = max_pages;
	}

	public void generatePDFFromImage(String folderName) {
		Document document = new Document();
		FileOutputStream fos;
		PdfWriter writer = null;
		try {
			fos = new FileOutputStream(getOutputFileName(folderName, 0));
			writer = PdfWriter.getInstance(document, fos);
			writer.open();
			document.open();
			int currentPage = 0;
			List<File> images = new ArrayList<>(Arrays.asList(new File(folderName).listFiles()));
			images.sort(Util.getFileComparator());
			
			for(File f : images){
				if(f.getName().endsWith(".jpg")){
					if(max_pages > 0 && ++currentPage%max_pages == 0){
						System.out.println("done!");
						document.close();
						if(writer != null){
							writer.close();
						}
						document = new Document();
						fos = new FileOutputStream(getOutputFileName(folderName, currentPage));
						writer = PdfWriter.getInstance(document, fos);
						writer.open();
						document.open();
					}
					document.add(Image.getInstance(f.getAbsolutePath()));
				}
			}
			System.out.println("done!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			document.close();
			if(writer != null){
				writer.close();
			}
		}
	}
	
	/**
	 * 
	 * @param folderName folder containing images
	 * @param currentPage
	 * @return
	 */
	public String getOutputFileName(String folderName, int currentPage){
		File MangaFile = new File(folderName).getParentFile();
		String new_file_name = new File(MangaFile, MangaFile.getName() + "_" + new File(folderName).getName() + (max_pages > 0 ? ("_" + Integer.valueOf(currentPage/max_pages + 1).toString()) : "") + ".pdf").getAbsolutePath();
		System.out.print("Creating " + new_file_name + "...");
		return new_file_name;
	}

}
