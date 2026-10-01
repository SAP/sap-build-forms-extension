package com.sap.bfx.p13n;

import com.sap.bfx.p13n.proto.P13nServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class PersonalizationConfig {

    public final static String CLIENT_NAME = "p13n";

    /**
     * Create a blocking stub for the P13nServiceGrpc service
     *
     * @param channels GrpcChannelFactory
     * @return P13nServiceGrpc.P13nServiceBlockingStub
     */
    @Bean
    P13nServiceGrpc.P13nServiceBlockingStub blockingStub(GrpcChannelFactory channels) {
        return P13nServiceGrpc.newBlockingStub(channels.createChannel(CLIENT_NAME));
    }
}
