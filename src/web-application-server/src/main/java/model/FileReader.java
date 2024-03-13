package model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileReader {

    private static final String DEFAULT_ROOT = Paths.get("").toAbsolutePath() + "/src/web-application-server/webapp";

    private final int contentLength;

    private final byte[] content;
    /**
     * 파일의 내용을 반환
     * @param filePath 경로가 포함된 파일명 (ex, /user/create/index.html)
     * @throws IOException 파일이 없는경우 발생
     */
    public FileReader(String filePath) throws IOException{
        content = Files.readAllBytes(new File(DEFAULT_ROOT + filePath).toPath());
        contentLength = content.length;
    }


    public byte[] getContent(){
        return content;
    }
    public int getContentLength(){
        return contentLength;
    }
}
