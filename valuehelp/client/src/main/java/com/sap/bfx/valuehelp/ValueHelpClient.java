package com.sap.bfx.valuehelp;

import com.sap.bfx.valuehelp.grpc.GetValueHelpDefsRequest;
import com.sap.bfx.valuehelp.grpc.GetValueHelpRequest;
import com.sap.bfx.valuehelp.grpc.GetValueHelpsVersionRequest;
import com.sap.bfx.valuehelp.grpc.ValueHelpsServiceGrpc;
import com.sap.bfx.valuehelp.model.ValueHelpDef;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.*;

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
            final var response = stub.getValueHelpsVersion(
                    GetValueHelpsVersionRequest.newBuilder().addAllValueHelps(ids).setLocale(locale.toString())
                                               .build());
            response.getValuesList().forEach(it -> result.put(it.getName(), it.getVersion()));
        }
        return result;
    }

    /**
     * @param id
     * @param locale
     * @return
     */
    public GetValueHelpResponse findValues(String id, Locale locale) {
        final var response =
                stub.getValueHelp(GetValueHelpRequest.newBuilder().setId(id).setLocale(locale.toString()).build());

        final var result = new GetValueHelpResponse();
        result.setId(response.getId());
        result.setLocale(response.getLocale());
        result.setVersion(response.getVersion());
        result.setKeyKey(response.getKeyKey());
        result.setFormatTemplate(response.getFormatTemplate());
        response.getValueKeyList().forEach(it -> {
            result.getValueKeys().add(it);
        });
        response.getValuesList().forEach(it -> {
            final var row = new HashMap<String, String>();
            row.putAll(it.getItemsMap());
            result.getValues().add(row);
        });

        return result;
    }

    public List<ValueHelpDef> findValueHelpDefs(List<String> ids) {
        final var request = GetValueHelpDefsRequest.newBuilder();
        for (int i = 0; i < ids.size(); i++) {
            request.setIds(i, ids.get(i));
        }
        final var response = stub.getValueHelpDefs(request.build());

        final var result = new ArrayList<ValueHelpDef>();
        response.getValuesList().forEach(it -> {
            final var valueHelpDef = new ValueHelpDef();
            valueHelpDef.setId(it.getId());
            valueHelpDef.setDescription(it.getDescription());
            valueHelpDef.setLanguages(it.getLanguagesList().stream().toList());
            valueHelpDef.setKeyKey(it.getKeyKey());
            valueHelpDef.setValueKeys(it.getValueKeyList());
            result.add(valueHelpDef);
        });

        return result;
    }
}
