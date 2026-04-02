package com.rusty.applistbackend.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeployConfig {
    private String sshHost;
    private Integer sshPort = 22;
    private String sshUser;
    private String sshKeyPath;
    private String sshKeyPassphrase;   // 키에 패스프레이즈가 있을 경우
    private String sshPassword;
    private String buildScriptPath = "./build.sh";
    private String dockerDeployCmd = "docker stack deploy -c docker-compose.yml myapp";
}
