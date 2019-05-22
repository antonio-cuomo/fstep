package com.cgi.eoss.fstep.clouds.ipt;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
final class ProvisioningConfig {
    private final String defaultNodeFlavor;
    private final String nodeImageId;
    private final String sshUser;
    private final String securityGroupName;
    private final String floatingIpPool;
    private final String networks;
    private final String nfsHost;
    private final  String additionalNfsMounts;
    private boolean provisionFloatingIp;
    private String insecureRegistries;
}
