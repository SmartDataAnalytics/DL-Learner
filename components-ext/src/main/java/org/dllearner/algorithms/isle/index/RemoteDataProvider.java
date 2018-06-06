package org.dllearner.algorithms.isle.index;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.springframework.util.FileSystemUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Provides methods to download zipped zipped files from remote locations and extracts and stores them locally.
 * @author Daniel Fleischhacker
 */
public class RemoteDataProvider {
    private final static Logger log = org.slf4j.LoggerFactory.getLogger(RemoteDataProvider.class);

    public static String DATA_DIRECTORY = "tmp/";
    private URL url;
    private File localDirectory;

    private File lastModifiedCache;

    /**
     * Initializes this downloader to fetch data from the given URL. The download process is started
     * immediately.
     * @param url URL to download data from
     * @throws IOException on errors downloading or extracting the file
     */
    public RemoteDataProvider(URL url) throws IOException {
        this.url = url;

        log.debug("Initializing for URL '{}'", url);

        log.debug("Data directory is '{}'", DATA_DIRECTORY);
        File dataDir = new File(DATA_DIRECTORY);
        if (!dataDir.exists()) {
            log.debug("Data directory not yet existing, trying to create");
            if (!dataDir.mkdirs()) {
                throw new RuntimeException(
                        "Unable to create temporary file directory: " + dataDir.getAbsoluteFile());
            }
        }

        this.localDirectory = new File(DATA_DIRECTORY + DigestUtils.md5Hex(url.toString()));
        log.debug("'{}' --> '{}'", url, localDirectory.getAbsolutePath());
        this.lastModifiedCache = new File(DATA_DIRECTORY + DigestUtils.md5Hex(url.toString()) + ".last");

        downloadData();
    }

    /**
     * Downloads the file from the URL assigned to this RemoteDataProvider and extracts it into
     * the tmp subdirectory of the current working directory. The actual path to access the data
     * can be retrieved using {@link #getLocalDirectory()}.
     *
     * @throws IOException on errors downloading or extracting the file
     */
    private void downloadData() throws IOException {
        String localModified = getLocalLastModified();

        log.debug("Local last modified: {}", localModified);
        boolean triggerDownload = false;

        if (localModified == null) {
            log.debug("No local last modified date found, triggering download");
            triggerDownload = true;
        }
        else {
            URLConnection conn = url.openConnection();
            long lastModified = conn.getLastModified();
            log.debug("Remote last modified: {}", lastModified);
            if (!Long.valueOf(localModified).equals(lastModified)) {
                log.debug("Last modified dates do not match, triggering download");
                triggerDownload = true;
            }
        }

        if (triggerDownload) {
            deleteData();
            if (!this.localDirectory.mkdir()) {
                throw new RuntimeException(
                        "Unable to create temporary file directory: " + localDirectory.getAbsoluteFile());
            }
            ZipInputStream zin = new ZipInputStream(this.url.openStream());

            ZipEntry ze;
            byte[] buffer = new byte[2048];
            while ((ze = zin.getNextEntry()) != null) {
                final String base = localDirectory.getCanonicalPath();
                File outpath = new File(base, ze.getName());
                if (!outpath.getCanonicalPath().startsWith(base)) {
                    log.error("Not extracting {} because it is outside of {}", ze.getName(), base);
                    continue;
                }
                if (!outpath.getParentFile().exists()) {
                    outpath.getParentFile().mkdirs();
                }
                if (ze.isDirectory()) {
                    outpath.mkdirs();
                }
                else {
                    FileOutputStream output = null;
                    try {
                        output = new FileOutputStream(outpath);
                        int len = 0;
                        while ((len = zin.read(buffer)) > 0) {
                            output.write(buffer, 0, len);
                        }
                    }
                    finally {
                        if (output != null) {
                            output.close();
                        }
                    }
                }
            }
            zin.close();

            BufferedWriter writer = new BufferedWriter(new FileWriter(lastModifiedCache));
            long lastModified = url.openConnection().getLastModified();
            log.debug("Writing local last modified date: '{}'", lastModified);
            writer.write(String.valueOf(lastModified));
            writer.close();
        }
        else {
            log.debug("Local data is up to date, skipping download");
        }
    }

    /**
     * Forces a redownload of the data. The data directory is first deleted and then recreated.
     */
    public void redownload() throws IOException {
        deleteData();
        downloadData();
    }

    /**
     * Deletes the data downloaded.
     */
    public void deleteData() {
        FileSystemUtils.deleteRecursively(localDirectory);
        lastModifiedCache.delete();
    }

    /**
     * Returns the folder to access the downloaded data. The returned File object points to the directory
     * created for the downloaded data.
     * @return file pointing to the downloaded data's directory
     */
    public File getLocalDirectory() {
        return localDirectory;
    }

    /**
     * Returns the URL assigned to this RemoteDataProvider
     * @return the URL assigned to this downloader
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Returns the content of the local last modified cache for this URL. If no such file exists, null is returned
     * @return content of local last modified cache, if not existing null
     */
    private String getLocalLastModified() {
        if (!lastModifiedCache.exists()) {
            return null;
        }
        String res;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(lastModifiedCache));
            res = reader.readLine();
            reader.close();
            return res;
        }
        catch (FileNotFoundException e) {
            return null;
        }
        catch (IOException e) {
            return null;
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException e) {
                    log.error("Unable to close last modified cache property", e);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        RemoteDataProvider rid = new RemoteDataProvider(
                new URL("http://gold.linkeddata.org/data/bible/verse_index.zip"));
        System.out.println(rid.getLocalDirectory().getAbsolutePath());
        RemoteDataProvider rid2 = new RemoteDataProvider(
                new URL("http://gold.linkeddata.org/data/bible/chapter_index.zip"));
        System.out.println(rid2.getLocalDirectory().getAbsolutePath());
    }
}
