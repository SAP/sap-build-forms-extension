package com.sap.bfx.p13n;

import com.sap.bfx.p13n.grpc.ChangeSettingsRequest;
import com.sap.bfx.p13n.grpc.DeleteSettingsRequest;
import com.sap.bfx.p13n.grpc.GetSettingsRequest;
import com.sap.bfx.p13n.grpc.P13nServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @see <a href="https://yidongnan.github.io/grpc-spring-boot-starter/en/"/>
 */
@Service
public class PersonalizationClient {

    public final static String CLIENT_NAME = "p13n";

    @GrpcClient(CLIENT_NAME)
    private P13nServiceGrpc.P13nServiceBlockingStub stub;

    /**
     * Get settings for a specific app and user
     *
     * @param app  application name
     * @param user user name
     * @return List of Settings
     */
    public List<Settings> getSettings(String app, String user) {
        var response = stub.getSettings(GetSettingsRequest.newBuilder()
                .setApp(app)
                .setUser(user)
                .build());

        return fromGrpc(response.getValuesList());
    }

    /**
     * Change settings
     *
     * @param settings settings to change
     * @return List of changed Settings
     */
    public List<Settings> changeSettings(ArrayList<Settings> settings) {
        var response = stub.changeSettings(
                ChangeSettingsRequest.newBuilder().addAllSettings(toGrpc(settings)).build());
        return fromGrpc(response.getSettingsList());
    }

    /**
     * Delete settings
     *
     * @param settings settings to delete
     * @return List of deleted Settings
     */
    public List<Settings> deleteSettings(ArrayList<Settings> settings) {
        var response = stub.deleteSettings(
                DeleteSettingsRequest.newBuilder().addAllSettings(toGrpc(settings)).build());
        return fromGrpc(response.getSettingsList());
    }

    /**
     * Convert gRPC Settings to local Settings
     *
     * @param grpcSettings gRPC Settings
     * @return Local Settings
     */
    private List<Settings> fromGrpc(List<com.sap.bfx.p13n.grpc.Settings> grpcSettings) {
        return new ArrayList<>(grpcSettings.stream().map(it ->
                        new Settings(it.getId(), it.getUser(), it.getKey(), it.getApp(), it.getEncoding(), it.getValue()))
                .toList());
    }

    /**
     * Convert local Settings to gRPC Settings
     *
     * @param settings local Settings
     * @return gRPC Settings
     */
    private List<com.sap.bfx.p13n.grpc.Settings> toGrpc(List<Settings> settings) {
        return new ArrayList<>(settings.stream().map(it ->
                        com.sap.bfx.p13n.grpc.Settings.newBuilder()
                                                      .setId(it.getId())
                                                      .setUser(it.getUser())
                                                      .setKey(it.getKey())
                                                      .setApp(it.getApp())
                                                      .setEncoding(it.getEncoding())
                                                      .setValue(it.getValue())
                                                      .build())
                .toList());
    }

}
