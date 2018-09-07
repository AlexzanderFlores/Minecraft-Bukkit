package network.server.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
    public static void zipFolder(String folderName, String zipFileName) {
        try {
        	ZipOutputStream zip = null;
            FileOutputStream out = null;
            out = new FileOutputStream(zipFileName);
            zip = new ZipOutputStream(out);
            addFolderToZip("", folderName, zip);
            zip.close();
            out.close();
        } catch(IOException e) {
        	e.printStackTrace();
        }
    }

    private static void addFolderToZip(String path, String sourceFolder, ZipOutputStream zip) {
        File folder = new File(sourceFolder);
        if(folder.list().length == 0) {
            addFileToZip(path , sourceFolder, zip, true);
        } else {
            for(String fileName : folder.list()) {
                if(path.equals("")) {
                    addFileToZip(folder.getName(), sourceFolder + "/" + fileName, zip, false);
                } else {
                	addFileToZip(path + "/" + folder.getName(), sourceFolder + "/" + fileName, zip, false);
                }
            }
        }
    }

    private static void addFileToZip(String path, String sourceFile, ZipOutputStream zip, boolean flag) {
        try {
        	File folder = new File(sourceFile);
            if(flag) {
                zip.putNextEntry(new ZipEntry(path + "/" + folder.getName() + "/"));
            } else {
                if(folder.isDirectory()) {
                    addFolderToZip(path, sourceFile, zip);
                } else {
                    byte [] buf = new byte[1024];
                    int len = 0;
                    FileInputStream in = new FileInputStream(sourceFile);
                    zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
                    while((len = in.read(buf)) > 0) {
                        zip.write(buf, 0, len);
                    }
                    in.close();
                }
            }
        } catch(IOException e) {
        	e.printStackTrace();
        }
    }
    
    public static void unZipIt(String zipFile, String outputFolder) {
    	byte [] buffer = new byte[1024];
       	try {
       		//create output directory is not exists
       		File folder = new File(outputFolder);
       		if(!folder.exists()){
       			folder.mkdir();
       		}
       		//get the zip file content
       		ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
       		//get the zipped file list entry
       		ZipEntry ze = zis.getNextEntry();
       		while(ze != null) {
       			String fileName = ze.getName();
       			File newFile = new File(outputFolder + File.separator + fileName);
       			//create all non exists folders
       			//else you will hit FileNotFoundException for compressed folder
       			new File(newFile.getParent()).mkdirs();
       			FileOutputStream fos = new FileOutputStream(newFile);
       			int len;
       			while((len = zis.read(buffer)) > 0) {
       				fos.write(buffer, 0, len);
       			}
       			fos.close();
       			ze = zis.getNextEntry();
       		}
       		zis.closeEntry();
       		zis.close();
       	} catch(IOException ex) {
       		ex.printStackTrace();
       	}
    }
}
