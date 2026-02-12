package com.sap.bfx.p13n;

import com.sap.bfx.exception.NotAuthorizedException;
import com.sap.bfx.p13n.grpc.*;
import com.sap.bfx.p13n.grpc.Settings;
import com.sap.bfx.p13n.model.Personalization;
import com.sap.bfx.p13n.service.PersonalizationService;
import com.sap.bfx.security.SecurityService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * @see <a href="https://yidongnan.github.io/grpc-spring-boot-starter/en/"/>
 */
@GrpcService
@Slf4j
public class PersonalizationServer extends P13nServiceGrpc.P13nServiceImplBase {

    private final PersonalizationService service;
    private final SecurityService securityService;

    /**
     * @param service - personalization service
     */
    @Autowired
    public PersonalizationServer(final PersonalizationService service, final SecurityService securityService) {
        super();
        this.service = service;
        this.securityService = securityService;
    }

    /**
     * Test method
     *
     * @param request          - test request
     * @param responseObserver - response observer
     */
    @Override
    public void test(TestRequest request, StreamObserver<TestResponse> responseObserver) {

        TestResponse response = TestResponse.newBuilder()
                .setReply("Hallo, you received a message via gRPC")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Get personalization settings
     *
     * @param request          - request with app and user
     * @param responseObserver - response observer
     */
    @Override
    public void getSettings(GetSettingsRequest request, StreamObserver<GetSettingsResponse> responseObserver/*, AbstractAuthenticationToken token*/) {
        /*securityService.ensureAuthorized(token, P13NRoles.P13NUsage);
        String requestDataUsername = null;
        if (!securityService.isAuthorized(token, P13NRoles.P13NEdit.getValue()) || (!(securityService.isAuthorized(token, P13NRoles.P13NEnduser.getValue())) && checkOnYourOwnUsername(token, requestDataUsername))) {
            throw new NotAuthorizedException(null, new String[]{"Not authorized to modify users other than your own"}, getTokenUsername(token));
        }*/

        var app = request.getApp();
        var user = request.getUser();

        log.debug("getSettings is called with app='{}' and user='{}'", app, user);

        var result = service.findPersonalizationsByUserAndAppForAdmin(user, app);

        Collection<Settings> settings = new ArrayList<>();
        for (var element : result) {
            settings.add(Settings.newBuilder()
                                 .setId(element.getId().toString())
                                 .setUser(element.getUser())
                                 .setKey(element.getKey())
                                 .setApp(element.getApp())
                                 .setEncoding(element.getEncoding())
                                 .setValue(element.getValue()).build());
        }

        var responseBuilder = GetSettingsResponse.newBuilder();
        responseBuilder.addAllValues(settings);

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    /**
     * Change personalization settings
     *
     * @param request          - request with settings to change
     * @param responseObserver - response observer
     */
    public void changeSettings(ChangeSettingsRequest request, StreamObserver<ChangeSettingsResponse> responseObserver/*, AbstractAuthenticationToken token*/) {
        /*securityService.ensureAuthorized(token, P13NRoles.P13NUsage);
        String requestDataUsername = null;
        if (!securityService.isAuthorized(token, P13NRoles.P13NEdit.getValue()) || (!(securityService.isAuthorized(token, P13NRoles.P13NEnduser.getValue())) && checkOnYourOwnUsername(token, requestDataUsername))) {
            throw new NotAuthorizedException(null, new String[]{"Not authorized to modify users other than your own"}, getTokenUsername(token));
        }*/
        Collection<Settings> settings = new ArrayList<>();

        for (Settings setting : request.getSettingsList()) {
            Personalization p = new Personalization();
            p.setValue(setting.getValue());
            p.setUser(setting.getUser());
            p.setApp(setting.getApp());
            p.setEncoding(setting.getEncoding());
            p.setKey(setting.getKey());
            p.setId(UUID.fromString(setting.getId()));

            var resultOpt = service.findPersonalizationById(p.getId());
            if (resultOpt.isEmpty()) {
                //get last suitable personalization
                Personalization lastSuitablePersonalization = service.findPersonalizationByKeyUserAndApp(
                        p.getKey(),
                        p.getUser(),
                        p.getApp());

                //if personalization is existent at different id --> continue
                if (lastSuitablePersonalization != null &&
                        Objects.equals(lastSuitablePersonalization.getKey(), p.getKey())
                        && Objects.equals(lastSuitablePersonalization.getUser(), p.getUser())
                        && Objects.equals(lastSuitablePersonalization.getApp(),
                        p.getApp())) {
                    continue;
                }
                //create personalization
                if (lastSuitablePersonalization == null ||
                        (lastSuitablePersonalization.isEditable() && lastSuitablePersonalization.isVisible())) {
                    if (lastSuitablePersonalization != null) {
                        p.setVisible(lastSuitablePersonalization.isVisible());
                        p.setEditable(lastSuitablePersonalization.isEditable());
                    } else {
                        p.setVisible(true);
                        p.setEditable(true);
                    }
                    p.setId(UUID.randomUUID());
                    service.addPersonalization(p);
                    settings.add(Settings.newBuilder()
                                         .setId(p.getId().toString())
                                         .setUser(p.getUser())
                                         .setKey(p.getKey())
                                         .setApp(p.getApp())
                                         .setEncoding(p.getEncoding())
                                         .setValue(p.getValue()).build());
                }
                // update personalization
            } else if (resultOpt.get() != p
                    && resultOpt.get().isEditable()
                    && resultOpt.get().isVisible()) {
                service.updatePersonalizationUser(p);
                settings.add(Settings.newBuilder()
                                     .setId(p.getId().toString())
                                     .setUser(p.getUser())
                                     .setKey(p.getKey())
                                     .setApp(p.getApp())
                                     .setEncoding(p.getEncoding())
                                     .setValue(p.getValue()).build());
            }
        }

        var responseBuilder = ChangeSettingsResponse.newBuilder();
        responseBuilder.addAllSettings(settings);
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    /**
     * Delete personalization settings
     *
     * @param request          - request with settings to delete
     * @param responseObserver - response observer
     */
    public void deleteSettings(DeleteSettingsRequest request, StreamObserver<DeleteSettingsResponse> responseObserver/*, AbstractAuthenticationToken token*/) {
        /*securityService.ensureAuthorized(token, P13NRoles.P13NUsage);
        String requestDataUsername = null;
        if (!securityService.isAuthorized(token, P13NRoles.P13NEdit.getValue()) || (!(securityService.isAuthorized(token, P13NRoles.P13NEnduser.getValue())) && checkOnYourOwnUsername(token, requestDataUsername))) {
            throw new NotAuthorizedException(null, new String[]{"Not authorized to modify users other than your own"}, getTokenUsername(token));
        }*/
        Collection<Settings> settings = new ArrayList<>();

        for (Settings setting : request.getSettingsList()) {
            if ((setting.getUser().equals("_") || setting.getUser().isBlank())
                    && (setting.getApp().equals("_") || setting.getApp().isBlank())) {
                log.debug("default value connot be deleted");
            } else {
                service.deletePersonalizationById(UUID.fromString(setting.getId()));
                settings.add(setting);
            }
        }

        var responseBuilder = DeleteSettingsResponse.newBuilder();
        responseBuilder.addAllSettings(settings);
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    private String getTokenUsername(AbstractAuthenticationToken token) {
        return token.getName().substring((-1 != token.getName().lastIndexOf("/")) ? token.getName().lastIndexOf("/") + 1 : 0);
    }

    private boolean checkOnYourOwnUsername(AbstractAuthenticationToken token, String requestUsername) {
        String tokenUsername = getTokenUsername(token);
        if (!requestUsername.matches(tokenUsername)) {
            throw new NotAuthorizedException(null, new String[]{"Not authorized to modify users other than your own"}, tokenUsername);
        }
        return true;
    }
}
