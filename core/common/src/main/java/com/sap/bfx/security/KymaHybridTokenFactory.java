package com.sap.bfx.security;

import com.sap.cloud.security.json.JsonParsingException;
import com.sap.cloud.security.token.*;
import com.sap.cloud.security.xsuaa.Assertions;
import com.sap.cloud.security.xsuaa.jwt.Base64JwtDecoder;
import com.sap.cloud.security.xsuaa.jwt.DecodedJwt;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;


@Slf4j
public class KymaHybridTokenFactory implements TokenFactory {

    static Optional<String> xsAppId;
    static ScopeConverter xsScopeConverter;

    static void withXsuaaAppId(@NonNull String xsAppId) {
        log.debug("XSUAA app id = {}", xsAppId);
        KymaHybridTokenFactory.xsAppId = Optional.of(xsAppId);
        getOrCreateScopeConverter();
    }

    private static ScopeConverter getOrCreateScopeConverter() {
        if (xsScopeConverter == null && getXsAppId().isPresent()) {
            xsScopeConverter = (ScopeConverter) new XsuaaScopeConverter(getXsAppId().get());
        }
        return xsScopeConverter;
    }

    private static Optional<String> getXsAppId() {
//        if (Objects.nonNull(xsAppId)) {
//            return xsAppId;
//        }
//        OAuth2ServiceConfiguration serviceConfiguration = Environments.getCurrent().getXsuaaConfiguration();
//        if (serviceConfiguration != null) {
//            xsAppId = Optional.of(serviceConfiguration.getProperty("xsappname"));
//        } else {
//            log.warn("There is no xsuaa service configuration with 'xsappname' property: no local scope check possible.");
//
//            xsAppId = Optional.empty();
//        }
//        return xsAppId;

        if (Objects.nonNull(xsAppId)) {
            return xsAppId;
        }

        return Optional.of("forms-scenario-test!t156912");
    }

    private static boolean isXsuaaToken(DecodedJwt decodedJwt) {
        String jwtPayload = decodedJwt.getPayload().toLowerCase();
        return ((jwtPayload.contains("ext_attr") && jwtPayload
                .contains("enhancer") && jwtPayload
                .contains("xsuaa")) || jwtPayload
                .contains("\"zid\":\"uaa\","));
    }

    private static String removeBearer(@NonNull String jwtToken) {
        Assertions.assertHasText(jwtToken, "jwtToken must not be null / empty");
        Pattern bearerPattern = Pattern.compile("[B|b]earer ");
        return bearerPattern.matcher(jwtToken).replaceFirst("");
    }

    public Token create(String jwtToken) {
        try {
            Objects.requireNonNull(jwtToken, "Requires encoded jwtToken to create a Token instance.");
            DecodedJwt decodedJwt = Base64JwtDecoder.getInstance().decode(removeBearer(jwtToken));

            if (isXsuaaToken(decodedJwt)) {
                return (Token) (new XsuaaToken(decodedJwt)).withScopeConverter(getOrCreateScopeConverter());
            }
            return (Token) new SapIdToken(decodedJwt);
        } catch (JsonParsingException e) {
            throw new JsonParsingException(String.format("Issue with Jwt parsing. Authorization header: %s - %s", new Object[]{jwtToken
                    .substring(0, 20), e.getMessage()}), (Throwable) e);
        }
    }
}

