package com.example.demodrive;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.DriveList;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@RestController
public class ControladorPrincipal {

    private final String APPLICATION_NAME = "Aplicación de Prueba";

    private final String TOKENS_DIRECTION_PATH = "tokens";

    private final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

    private final String CREDENTIALS_PATH = "C:/Users/rodri/Downloads/ANALIZAR/proyecto-prueba-infotec-94e210aed704.json";

    private final Drive INSTANCIA_DRIVE;

    public ControladorPrincipal() throws Exception {
        this.INSTANCIA_DRIVE = new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                this.httpRequestInitializer())
                .setApplicationName(this.APPLICATION_NAME)
                .build();
    }

    /**
     *
     * Métodos usados para la inicialización de instancias necesarias.
     * */
    private HttpRequestInitializer httpRequestInitializer() throws Exception{
        InputStream in = new FileInputStream(
                new FileSystemResource(this.CREDENTIALS_PATH).getFile()
        );

        return new HttpCredentialsAdapter(GoogleCredentials.fromStream(in)
                .createScoped(SCOPES));
    }

    @GetMapping("api/v1/drive/default")
    public String getAll() throws Exception{
        FileList results = this.INSTANCIA_DRIVE.files().list()
                .setPageSize(5)
                .setFields("nextPageToken, files(id, name)")
                .execute();

        List<File> files = results.getFiles();

        if(files == null || files.isEmpty()){
            return "No hay archivos";
        }else{
            StringBuilder msg = new StringBuilder("Archivos: \n");

            for(File archivo : files){
                msg.append(String.format("%s (%s)\n",archivo.getName(), archivo.getId()));
            }

            return msg.toString();
        }
    }

    @PostMapping(value = "api/v1/drive/subir", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String subir(@RequestPart String nombre, @RequestPart MultipartFile file) throws Exception{
        File upload = this.INSTANCIA_DRIVE.files()
                .create(new File()
                        .setName(nombre)
                        .setMimeType(file.getContentType()),
                        new InputStreamContent(
                                file.getContentType(),
                                new ByteArrayInputStream(file.getBytes())
                        ))
                .setUploadType("multipart")
                .setFields("id, webViewLink, permissions")
                .execute();

        return String.format("%s (%s)", upload.getName(), upload.getId());
    }

}
