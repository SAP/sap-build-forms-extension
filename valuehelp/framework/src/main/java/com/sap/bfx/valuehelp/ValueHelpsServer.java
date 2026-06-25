package com.sap.bfx.valuehelp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.bfx.valuehelp.grpc.*;
import com.sap.bfx.valuehelp.service.ValueHelpService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
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
        final var response = TestResponse.newBuilder().setReply("Hallo, you received a message via gRPC").build();
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
        try {
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
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL.asRuntimeException());
        }
    }

    /**
     * @param request
     * @param responseObserver
     */
    @Override
    public void getValueHelp(GetValueHelpRequest request,
                             StreamObserver<GetValueHelpResponse> responseObserver/*, AbstractAuthenticationToken token*/) {
        try {
            var def = service.findDefById(request.getId());
            if (def.isEmpty()) {
                log.error("cannot find def with id='{}'", request.getId());
                responseObserver.onError(Status.INVALID_ARGUMENT.asRuntimeException());
            }
            var value = service.findValueById(request.getId(), request.getLocale());
            if (value == null) {
                log.error("cannot find value with id='{}', locale='{}'", request.getId(), request.getLocale());
                responseObserver.onError(Status.INVALID_ARGUMENT.asRuntimeException());
            }

            var response = GetValueHelpResponse.newBuilder().setId(request.getId()).setLocale(request.getLocale())
                                               .setVersion(value.getRight()).setKeyKey(def.get().getKeyKey())
                                               .setFormatTemplate(def.get().getFormatTemplate())
                                               .setType(def.get().getValueHelpType().getIdentifier());
            response.addAllValueKey(def.get().getValueKeys());
            new ObjectMapper().readValue(value.getLeft(), List.class).forEach(it -> {
                response.addValues(ValueHelpRow.newBuilder().putAllItems((Map<String, String>) it).build());
            });

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL.asRuntimeException());
        }
    }
}
