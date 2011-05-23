/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.insalyon.creatis.vip.portal.server.rpc;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import fr.insalyon.creatis.vip.common.server.ServerConfiguration;
import fr.insalyon.creatis.vip.portal.client.rpc.ModelService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author glatard
 */
public class ModelServiceImpl extends RemoteServiceServlet implements ModelService  {

    public List<String> getFiles(String modelZipFile)  {
        System.out.println("Calling getFiles " + modelZipFile);
        if(modelZipFile.lastIndexOf(".zip")==-1)
            this.doUnexpectedFailure(new Exception("This is not a zip file"));
            
        
        String rootDirectory = ServerConfiguration.getInstance().getDataManagerPath() + "/uploads/";

        ArrayList<String> files = new ArrayList<String>();
        byte[] buf = new byte[1024];
        try {
        // TODO discuss with Rafael how to fix
            if (!(new File(rootDirectory + modelZipFile)).exists()) {
                try {
                    Thread.currentThread().sleep(3000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ModelServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            ZipInputStream zipinputstream = null;
            ZipEntry zipentry;
            zipinputstream = new ZipInputStream(new FileInputStream(rootDirectory + modelZipFile));
            zipentry = zipinputstream.getNextEntry();
            while (zipentry != null) {
                //for each entry to be extracted
//                String entryName = zipentry.getName();
//
//                FileOutputStream fileoutputstream;
//                File newFile = new File(entryName);
//                String directory = newFile.getParent();
//
//                if (directory == null) {
//                    if (newFile.isDirectory()) {
//                        break;
//                    }
//                }
//                int n;
//                fileoutputstream = new FileOutputStream(
//                        rootDirectory + entryName);
//
//                while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
//                    fileoutputstream.write(buf, 0, n);
//                }
//
//                fileoutputstream.close();
//                zipinputstream.closeEntry();

                files.add(zipentry.getName());
                zipentry = zipinputstream.getNextEntry();


            }
            zipinputstream.close();
        } catch (IOException ex) {
            Logger.getLogger(ModelServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return files;
    }
}