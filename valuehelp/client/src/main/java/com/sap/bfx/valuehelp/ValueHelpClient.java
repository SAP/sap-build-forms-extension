package com.sap.bfx.valuehelp;

import com.sap.bfx.utils.EnumUtils;
import com.sap.bfx.valuehelp.grpc.GetValueHelpDefsRequest;
import com.sap.bfx.valuehelp.grpc.GetValueHelpRequest;
import com.sap.bfx.valuehelp.grpc.GetValueHelpsVersionRequest;
import com.sap.bfx.valuehelp.grpc.ValueHelpsServiceGrpc;
import com.sap.bfx.valuehelp.model.ValueHelpDef;
import com.sap.bfx.valuehelp.model.ValueHelpType;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * A convenience class for accessing the value-help-service via gRPC. The service is used to retrieve information
 * about value-helps, including their versions and definitions.
 */
@Service
public class ValueHelpClient {

    public final static String CLIENT_NAME = "valueHelps";

    @GrpcClient(CLIENT_NAME)
    private ValueHelpsServiceGrpc.ValueHelpsServiceBlockingStub stub;

    /**
     * Returns the versions of the value-helps with the given ids.
     *
     * @param ids    A collection of ids of the value-helps to be computed
     * @param locale The locale for which the versions should be computed
     * @return a Map with an entry for each value-help (with its Id) and the version
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
     * Returns the values for the given id and lcoale
     *
     * @param id     The id of the value-help
     * @param locale The locale of the value-help
     * @return A structure with information about the value-help and the values for the given locale
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
        result.setType(EnumUtils.valueById(ValueHelpType.class, response.getType(), ValueHelpType.FREESTYLE));
        response.getValueKeyList().forEach(it -> result.getValueKeys().add(it));
        response.getValuesList().forEach(it -> {
            final var row = new HashMap<>(it.getItemsMap());
            result.getValues().add(row);
        });

        return result;
    }

    /**
     * Returns the value-help definitions for the given ids.
     *
     * @param ids A list/collection of ids to be computed
     * @return a list of value-help-definitions
     */
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
