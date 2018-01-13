package mainTests;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainT {
    public static void main(String[] args) {
        String dst = "C:\\Users\\uatil\\OneDrive\\programming\\java\\java5\\ru.uatilman.fileRepository\\serverFiles\\";
        File dstFile = new File(dst);
        System.out.println("dstFile.exists() " + dstFile.exists());
        File file = new File(dst + "temp");
        createDirectory(file);
    }

    public static void createDirectory(File file) {
        System.out.println("create dir result " + file.mkdir());
    }

    private boolean copyDir(final String src, final String dst) {
        System.out.println("Копируем каталог: " + src);
        final File srcFile = new File(src);
        final File dstFile = new File(dst);
        if (srcFile.exists() && srcFile.isDirectory() && !dstFile.exists()) {

            dstFile.mkdir();
            File nextSrcFile;
            String nextSrcFilename, nextDstFilename;
            for (String filename : srcFile.list()) {
                nextSrcFilename = srcFile.getAbsolutePath()
                        + File.separator + filename;
                nextDstFilename = dstFile.getAbsolutePath()
                        + File.separator + filename;
                nextSrcFile = new File(nextSrcFilename);
                if (nextSrcFile.isDirectory()) {
                    copyDir(nextSrcFilename, nextDstFilename);
                } else {
                    copyFile(nextSrcFilename, nextDstFilename);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private static final int BUFFER_SIZE = 1024;

    private boolean copyFile(final String src, final String dst) {
        System.out.println("Копируем файл: " + src);
        final File srcFile = new File(src);
        final File dstFile = new File(dst);
        if (srcFile.exists() && srcFile.isFile() && !dstFile.exists()) {
            try (InputStream in = new FileInputStream(srcFile);
                 OutputStream out = new FileOutputStream(dstFile)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytes;
                while ((bytes = in.read(buffer)) > 0) {
                    out.write(buffer, 0, bytes);
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(this.getClass().getName())
                        .log(Level.SEVERE, null, ex);
                return false;
            } catch (IOException ex) {
                Logger.getLogger(this.getClass().getName())
                        .log(Level.SEVERE, null, ex);
                return false;
            }
            return true;
        } else {
            return false;
        }
    }
}
