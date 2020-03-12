package com.perkins.mongodb;

import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.async.client.MongoClientSettings;
import com.mongodb.client.model.vault.DataKeyOptions;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;
import org.bson.BsonBinary;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ClientTest {


    @Test
    public void testFile() {
        byte[] localMasterKey = new byte[96];
        new SecureRandom().nextBytes(localMasterKey);

        try (FileOutputStream stream = new FileOutputStream("master-key.txt")) {
            stream.write(localMasterKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testDemo(){
        String path = "master-key.txt";

        byte[] localMasterKey= new byte[96];

        try (FileInputStream fis = new FileInputStream(path)) {
            fis.read(localMasterKey, 0, 96);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, Object> keyMap = new HashMap<String, Object>();
        keyMap.put("key", localMasterKey);

        Map<String, Map<String, Object>> kmsProviders = new HashMap<String, Map<String, Object>>();
        kmsProviders.put("local", keyMap);



        String connectionString = "mongodb://localhost:27017";
        String keyVaultNamespace = "encryption.__keyVault";

        ClientEncryptionSettings clientEncryptionSettings = ClientEncryptionSettings.builder()
                .keyVaultMongoClientSettings(com.mongodb.MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(connectionString))
                        .build())
                .keyVaultNamespace(keyVaultNamespace)
                .kmsProviders(kmsProviders)
                .build();

        ClientEncryption clientEncryption = ClientEncryptions.create(clientEncryptionSettings);
        BsonBinary dataKeyId = clientEncryption.createDataKey("kmsProvider", new DataKeyOptions());
        System.out.println("DataKeyId [UUID]: " + dataKeyId.asUuid());

        String base64DataKeyId = Base64.getEncoder().encodeToString(dataKeyId.getData());
        System.out.println("DataKeyId [base64]: " + base64DataKeyId);
    }

}
