package com.sap.bfx.valuehelp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.bfx.valuehelp.grpc.*;
import com.sap.bfx.valuehelp.service.ValueHelpService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @see <a href="https://yidongnan.github.io/grpc-spring-boot-starter/en/"/>
 */
@GrpcService
@Slf4j
public class ValueHelpsServer extends ValueHelpsServiceGrpc.ValueHelpsServiceImplBase {

    private final ValueHelpService service;

    @Autowired
    public ValueHelpsServer(final ValueHelpService service) {
        super();
        this.service = service;
    }

    @Override
    public void test(TestRequest request, StreamObserver<TestResponse> responseObserver) {

        TestResponse response = TestResponse.newBuilder().setReply("Hallo, you received a message via gRPC").build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * @param request
     * @param responseObserver
     */
    @Override
    public void getValueHelpsVersion(GetValueHelpsVersionRequest request,
                                     StreamObserver<GetValueHelpsVersionResponse> responseObserver/*, AbstractAuthenticationToken token*/) {
        var ids = new ArrayList<String>();
        for (var i = 0; i < request.getValueHelpsCount(); i++) {
            ids.add(request.getValueHelps(i));
        }
        var locale = request.getLocale();

        log.debug("getValueHelpsVersion is called with ids='{}' and locale='{}'",
                StringUtils.join(ids.toArray(new String[0]), ","), locale);

        var result =
                ids.size() == 0 ? new HashMap<String, Long>() : service.findValuesVersion(ids, request.getLocale());

        var responseBuilder = GetValueHelpsVersionResponse.newBuilder();
        responseBuilder.addAllValues(result.keySet().stream().map(it -> ValueHelpsVersion.newBuilder().setName(it)
                                                                                         .setVersion(result.get(it))
                                                                                         .build())
                                           .collect(Collectors.toList()));

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    /**
     * @param request
     * @param responseObserver
     */
    @Override
    public void getValueHelp(GetValueHelpRequest request,
                             StreamObserver<GetValueHelpResponse> responseObserver/*, AbstractAuthenticationToken token*/) {
        try {
            var result = service.findValueById(request.getId(), request.getLocale());

            var response = GetValueHelpResponse.newBuilder().setId(request.getId()).setLocale(request.getLocale())
                                               .setVersion(result.getRight());

            // Transform the values to correct type for gRPC response
            final var repsonseValues = ValueHelpValues.newBuilder();
            final var persistetValues = new ObjectMapper().readValue(result.getLeft(), List.class);
            var i = 0;
            IterableUtils.forEach(persistetValues, it -> {
                if (it instanceof Map row) {
                    row.forEach((k, v) -> {
                        repsonseValues.addValue(
                                ValueHelpValue.newBuilder().setKey((String) k).setValue((String) v).build());
                    });
                }
            });
            response.addValues(repsonseValues);

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            responseObserver.onError(e);
        }
    }
}
