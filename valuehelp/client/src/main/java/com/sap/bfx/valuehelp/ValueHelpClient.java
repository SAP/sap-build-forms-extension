package com.sap.bfx.valuehelp;

import com.sap.bfx.valuehelp.grpc.GetValueHelpRequest;
import com.sap.bfx.valuehelp.grpc.GetValueHelpsVersionRequest;
import com.sap.bfx.valuehelp.grpc.ValueHelpsServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @see <a href="https://yidongnan.github.io/grpc-spring-boot-starter/en/"/>
 */
@Service
public class ValueHelpClient {

    public final static String CLIENT_NAME = "valueHelps";

    @GrpcClient(CLIENT_NAME)
    private ValueHelpsServiceGrpc.ValueHelpsServiceBlockingStub stub;

    /**
     * @param ids
     * @param locale
     * @return
     */
    public Map<String, Long> findValuesVersion(Collection<String> ids, Locale locale) {
        final var result = new HashMap<String, Long>();

        if (stub == null) {
            throw new RuntimeException("ValueHelpsServiceGrpc.ValueHelpsServiceBlockingStub is null!!");
        } else {
            final var response = stub.getValueHelpsVersion(GetValueHelpsVersionRequest.newBuilder()
                    .addAllValueHelps(ids)
                    .setLocale(locale.toString()
                    ).build());
            response.getValuesList().forEach(it -> result.put(it.getName(), it.getVersion()));
        }
        return result;
    }

    /**
     * @param id
     * @param locale
     * @return
     */
    public Pair<String, Long> findValues(String id, Locale locale) {
        var response = stub.getValueHelp(GetValueHelpRequest.newBuilder()
                .setId(id)
                .setLocale(locale.toString()
                ).build());

        return new ImmutablePair<String, Long>(response.getValues(), response.getVersion());
    }
}
