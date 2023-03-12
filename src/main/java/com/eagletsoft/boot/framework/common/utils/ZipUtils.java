package com.eagletsoft.boot.framework.common.utils;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    public static class ZipBuilder {
        private ZipOutputStream zip;

        public ZipBuilder(OutputStream os) {
            zip = new ZipOutputStream(os);
        }

        public void addCompress(String name, InputStream is) {
            try {
                ZipEntry ze = new ZipEntry(name);
                zip.putNextEntry(ze);

                int length = 0;
                byte[] buffer = new byte[4096];
                while ((length = is.read(buffer)) != -1) {
                    zip.write(buffer, 0, length);
                }
                zip.closeEntry();
                is.close();
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            finally {
                IOUtils.closeQuietly(is);
            }
        }

        public void close() {
            IOUtils.closeQuietly(zip);
        }
    }

    public static void main(String[] args) throws Exception {
        File zipFile = new File("/Users/markkk/Desktop/test.zip");

        File sourceFile = new File("/Users/markkk/Desktop/test.txt");
        File sourceFile2 = new File("/Users/markkk/Desktop/book.jpeg");

        FileOutputStream os = new FileOutputStream(zipFile);

        FileInputStream is = new FileInputStream(sourceFile);
        FileInputStream is2 = new FileInputStream(sourceFile2);

        ZipUtils.ZipBuilder zb = new ZipUtils.ZipBuilder(os);
        try{
            zb.addCompress(sourceFile.getName(), is);
            zb.addCompress(sourceFile2.getName(), is2);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            zb.close();
        }
    }
}
