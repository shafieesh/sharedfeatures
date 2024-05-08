package com.chainedminds.utilities;

import com.chainedminds._Classes;
import com.chainedminds._Config;
import com.chainedminds.models._FileData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class _File {

    private static final String TAG = _File.class.getSimpleName();

    public static final int MODE_ARTICLE_COVER = 2;

    public static final int SECTION_LEAGUES = 1;
    public static final int SECTION_PROFILES = 2;
    public static final int SECTION_ASSETS = 3;
    public static final int SECTION_NEWS = 4;
    public static final int SECTION_LOG_ERRORS = 5;
    public static final int SECTION_LOG_INFO = 6;
    public static final int SECTION_ARTICLES = 7;


    public final String DIRECTORY_APP = "cafegame";
    public String BASE_DIRECTORY;
    public String DIRECTORY_LEAGUES = "leagues";
    public String DIRECTORY_PROFILES = "profiles";
    public String DIRECTORY_ASSETS = "assets";
    public String DIRECTORY_NEWS = "news";
    public String DIRECTORY_LOGS = "Logs";
    public String DIRECTORY_LOGS_ERRORS = "errors";
    public String DIRECTORY_LOGS_INFO = "info";
    public String DIRECTORY_BANNERS = "banners";
    public String DIRECTORY_GAMES = "games";
    public String DIRECTORY_ARTICLES = "articles";

    public final String[] FILE_IMAGE = {".jpg", ".jpeg", ".png"};
    public final String[] FILE_AUDIO = {".ogg", ".amr", ".mp3"};
    public final String[] FILE_APK = {".apk"};

    public _File() {

        //init();
    }

    protected void init() {

        String sep = File.separator;

        String workingDirectory = Utilities.OS.getWorkingDirectory();

        BASE_DIRECTORY = workingDirectory + "WebServer" + sep + "cloud.fandoghapps.com";

        DIRECTORY_LOGS_ERRORS = BASE_DIRECTORY + sep + DIRECTORY_LOGS + sep + DIRECTORY_LOGS_ERRORS;
        DIRECTORY_LOGS_INFO = BASE_DIRECTORY + sep + DIRECTORY_LOGS + sep + DIRECTORY_LOGS_INFO;
        DIRECTORY_LEAGUES = BASE_DIRECTORY + sep + DIRECTORY_APP + sep + DIRECTORY_LEAGUES;
        DIRECTORY_PROFILES = BASE_DIRECTORY + sep + DIRECTORY_APP + sep + DIRECTORY_PROFILES;
        DIRECTORY_ASSETS = BASE_DIRECTORY + sep + DIRECTORY_APP + sep + DIRECTORY_ASSETS;
        DIRECTORY_NEWS = BASE_DIRECTORY + sep + DIRECTORY_APP + sep + DIRECTORY_NEWS;
        DIRECTORY_BANNERS = BASE_DIRECTORY + sep + DIRECTORY_APP + sep + DIRECTORY_BANNERS;
        DIRECTORY_GAMES = BASE_DIRECTORY + sep + DIRECTORY_APP + sep + DIRECTORY_GAMES;
        DIRECTORY_ARTICLES = BASE_DIRECTORY + sep + DIRECTORY_APP + sep + DIRECTORY_ARTICLES;
    }

    private String getPath(int section) {

        if (section == SECTION_PROFILES) {
            return DIRECTORY_PROFILES;
        }
        if (section == SECTION_LEAGUES) {
            return DIRECTORY_LEAGUES;
        }
        if (section == SECTION_ASSETS) {
            return DIRECTORY_ASSETS;
        }
        if (section == SECTION_NEWS) {
            return DIRECTORY_NEWS;
        }
        if (section == SECTION_LOG_ERRORS) {
            return DIRECTORY_LOGS_ERRORS;
        }
        if (section == SECTION_LOG_INFO) {
            return DIRECTORY_LOGS_INFO;
        }
        if (section == SECTION_ARTICLES) {
            return DIRECTORY_ARTICLES;
        }
        return null;
    }

    public String saveFile(int section, int fileType, byte[] fileBytes) {

        String filePath = getPath(section);

        File file = new File(filePath);

        if (!file.exists()) {

            file.mkdirs();
        }

        List<String> filesList = new ArrayList<>();

        Collections.addAll(filesList, file.list());

        String fileName;

        do {

            fileName = generateFileName();

        } while (filesList.contains(fileName));

        filesList.clear();

        boolean saved = saveFile(section, fileName, fileType, fileBytes);

        return saved ? fileName : null;
    }

    private boolean saveFile(int section, String fileName, int fileType, byte[] fileBytes) {

        String filePath = getPath(section);

        String fileExtension = getFileExtension(fileType)[0];

        return saveFile(filePath, fileName + fileExtension, fileBytes);
    }

    public boolean saveFile(int section, String fileNameAndExtension, byte[] fileBytes) {

        String filePath = getPath(section);

        return saveFile(filePath, fileNameAndExtension, fileBytes);
    }

    public void createDirectories(int section, String folder) {

        String filePath = getPath(section);

        File file = new File(filePath, folder);

        if (!file.exists()) {

            file.mkdirs();
        }
    }

    public boolean saveFile(String filePath, String fileName, byte[] fileBytes) {

        if (fileName == null || fileBytes == null) {

            return false;
        }

        try {

            File file = new File(filePath);

            if (!file.exists()) {

                file.mkdirs();
            }

            file = new File(filePath, fileName);

            if (!file.exists()) {

                file.createNewFile();
            }

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(fileBytes);
            outputStream.close();

            return true;

        } catch (Exception e) {

            _Log.error(TAG, e);
        }

        return false;
    }

    public void deleteFile(int section, String fileName, int fileType) {

        String filePath = getPath(section);

        for (String fileExtension : getFileExtension(fileType)) {

            deleteFile(filePath, fileName + fileExtension);
        }
    }

    public void deleteFile(String filePath, String fileName) {

        try {

            File file = new File(filePath, fileName);

            if (file.exists()) {

                file.delete();
            }

        } catch (Exception e) {

            _Log.error(TAG, e);
        }
    }

    public <FileData extends _FileData> FileData getHash(int section, String fileName) {

        return getHash(getFile(section, fileName));
    }

    public byte[] getHashValue(int section, String fileName) {

        return getHashValue(getFile(section, fileName));
    }

    public <FileData extends _FileData> FileData getHash(File file) {

        FileData fileData = null;

        byte[] hash = getHashValue(file);

        if (hash != null) {

            fileData = (FileData) _Classes.construct(_Classes.getInstance().fileClass);
            fileData.md5 = hash;
        }

        return fileData;
    }

    private byte[] getHashValue(File file) {

        byte[] hash = null;

        byte[] fileBytes = readFile(file);

        if (fileBytes != null) {

            try {

                MessageDigest messageDigest = MessageDigest.getInstance("MD5");

                messageDigest.update(fileBytes);

                hash = messageDigest.digest();

            } catch (Exception e) {

                _Log.error(TAG, e);
            }
        }

        return hash;
    }

    private String generateFileName() {

        String[] chars = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
                "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

        String fileName = "";

        int length = 0;

        while (length == 0) {

            length = new Random().nextInt(10);
        }

        for (int i = 0; i < length; i++) {

            fileName += chars[new Random().nextInt(chars.length)];
        }

        return fileName;
    }

    private String[] getFileExtension(int fileType) {

        if (fileType == _Config.MEDIA_IMAGE) {

            return new String[]{".jpg", ".jpeg", ".png"};
        }

        if (fileType == _Config.MEDIA_AUDIO) {

            return new String[]{".ogg", ".amr", ".mp3"};
        }

        if (fileType == _Config.MEDIA_APK) {

            return new String[]{".apk"};
        }

        return new String[]{""};
    }

    public boolean isFileMedia(int fileType) {

        return fileType != _Config.MEDIA_STRING;
    }

    @Deprecated
    public byte[] loadFile(int section, String fileName, int fileType) {

        String filePath = getPath(section);

        return loadFile(filePath, fileName, fileType);
    }

    public byte[] loadFile(String filePath, String fileName) {

        try {

            File file = new File(filePath);

            if (!file.exists()) {

                file.mkdirs();
            }

            file = new File(filePath, fileName);

            if (file.exists()) {

                try {

                    return readFileUnsafe(file);

                } catch (Exception ignore) {

                }
            }

            throw new FileNotFoundException("Could not find file: " + filePath + " : " + fileName);

        } catch (Exception e) {

            _Log.error(TAG, e);
        }

        return null;
    }

    public byte[] loadFile(String filePath, String fileName, String[] extensions) {

        try {

            File file = new File(filePath);

            if (!file.exists()) {

                file.mkdirs();
            }

            for (String fileExtension : extensions) {

                file = new File(filePath, fileName + fileExtension);

                if (file.exists()) {

                    try {

                        return readFileUnsafe(file);

                    } catch (Exception ignore) {

                    }
                }
            }

            throw new FileNotFoundException("Could not find file: " + filePath + " : " + fileName);

        } catch (Exception e) {

            _Log.error(TAG, e);
        }

        return null;
    }

    @Deprecated
    public byte[] loadFile(String filePath, String fileName, int fileType) {

        try {

            File file = new File(filePath);

            if (!file.exists()) {

                file.mkdirs();
            }

            for (String fileExtension : getFileExtension(fileType)) {

                file = new File(filePath, fileName + fileExtension);

                if (file.exists()) {

                    try {

                        return readFileUnsafe(file);

                    } catch (Exception ignore) {

                    }
                }
            }

            throw new FileNotFoundException("Could not find file: " + filePath + " : " + fileName);

        } catch (Exception e) {

            _Log.error(TAG, e);
        }

        return null;
    }

    @Deprecated
    public byte[] loadFile(int section, String fileNameAndExtension) {

        String filePath = getPath(section);

        File file = new File(filePath);

        file.mkdirs();

        file = new File(filePath, fileNameAndExtension);

        return readFile(file);
    }

    private byte[] readFileUnsafe(File file) throws Exception {

        int availableBytes = (int) file.length();
        byte[] fileBytes = new byte[availableBytes];
        int totalBytesRead = 0;
        int bytesRead = 0;

        final FileInputStream inputStream = new FileInputStream(file);

        while (bytesRead != -1 && totalBytesRead < availableBytes) {

            bytesRead = inputStream.read(fileBytes, totalBytesRead, availableBytes - totalBytesRead);

            totalBytesRead += bytesRead;
        }

        inputStream.close();

        return fileBytes;
    }

    private byte[] readFile(File file) {

        if (!file.exists()) {

            return null;
        }

        try {

            return readFileUnsafe(file);

        } catch (Exception e) {

            _Log.error(TAG, e);
        }

        return null;
    }

    private File getFile(int section, String fileName) {

        String filePath = getPath(section);

        return getFile(filePath, fileName);
    }

    private File getFile(String filePath, String fileName) {

        File file = new File(filePath);

        if (!file.exists()) {

            file.mkdirs();
        }

        file = new File(filePath, fileName);

        return file;
    }

    public boolean exists(int section, String fileNameAndExtension) {

        String filePath = getPath(section);

        if (filePath != null) {

            File file = new File(filePath, fileNameAndExtension);

            return file.exists();
        }

        return false;
    }
}