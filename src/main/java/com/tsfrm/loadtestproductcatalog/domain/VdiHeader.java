package com.tsfrm.loadtestproductcatalog.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VdiHeader {
    private String vdiVersion;
    private String vdiType;
    private String providerName;
    private String partnerUid;
    private String applicationId;
    private String operatorId;
    private String requestId;
    private String correlationId;
    private String transactionId;
    private OffsetDateTime transactionDtm;
}


