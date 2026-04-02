package com.rusty.applistbackend.service;

import com.rusty.applistbackend.domain.dto.DeployConfig;
import com.rusty.applistbackend.domain.dto.InfraItem;
import com.rusty.applistbackend.repository.JsonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeployService {

    @Value("${app.deploy.default-key-path:}")
    private String defaultKeyPath;

    private final SimpMessagingTemplate messagingTemplate;
    private final JsonRepository jsonRepository;

    @Async
    public void deploy(String appId) {
        InfraItem item = findItemById(appId);
        if (item == null) {
            sendLog(appId, "[ERROR] ID에 해당하는 앱을 찾을 수 없습니다: " + appId);
            return;
        }
        DeployConfig config = item.getDeployConfig();
        if (config == null || config.getSshUser() == null || config.getSshUser().isBlank()) {
            sendLog(appId, "[ERROR] 배포 설정(SSH 정보)이 없습니다. [수정하기]에서 배포 설정을 입력하세요.");
            return;
        }

        String sshHost = notBlankOr(config.getSshHost(), item.getHost());
        int sshPort = config.getSshPort() != null ? config.getSshPort() : 22;
        String resolvedKeyPath = notBlankOr(config.getSshKeyPath(), defaultKeyPath);

        // WebSocket 구독 완료 대기
        try { Thread.sleep(500); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }

        SSHClient ssh = new SSHClient();
        try {
            sendLog(appId, String.format("[INFO] SSH 연결 중: %s@%s:%d", config.getSshUser(), sshHost, sshPort));
            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            ssh.connect(sshHost, sshPort);

            // 인증
            if (!resolvedKeyPath.isBlank()) {
                java.io.File keyFile = new java.io.File(resolvedKeyPath);
                if (!keyFile.exists()) {
                    sendLog(appId, "[ERROR] 키 파일을 찾을 수 없습니다: " + resolvedKeyPath);
                    return;
                }
                sendLog(appId, "[INFO] 키 파일 확인 OK (" + keyFile.length() + " bytes): " + resolvedKeyPath);
                net.schmizz.sshj.userauth.keyprovider.KeyProvider keyProvider =
                        (config.getSshKeyPassphrase() != null && !config.getSshKeyPassphrase().isBlank())
                                ? ssh.loadKeys(resolvedKeyPath, config.getSshKeyPassphrase())
                                : ssh.loadKeys(resolvedKeyPath);
                sendLog(appId, "[INFO] 키 타입: " + keyProvider.getType());
                ssh.authPublickey(config.getSshUser(), keyProvider);
                sendLog(appId, "[INFO] 키 인증 완료");
            } else if (config.getSshPassword() != null && !config.getSshPassword().isBlank()) {
                sendLog(appId, "[INFO] 비밀번호 인증 시도");
                ssh.authPassword(config.getSshUser(), config.getSshPassword());
                sendLog(appId, "[INFO] 비밀번호 인증 완료");
            } else {
                sendLog(appId, "[ERROR] SSH 인증 정보가 없습니다 (키 경로 또는 비밀번호를 설정하세요)");
                return;
            }

            // 1단계: 빌드
            String buildScript = notBlankOr(config.getBuildScriptPath(), "./build.sh");
            sendLog(appId, "");
            sendLog(appId, "━━━ [1/2] 빌드 시작: " + buildScript + " ━━━");
            int buildExit = execCommand(ssh, appId, buildScript);

            if (buildExit != 0) {
                sendLog(appId, "[FAILED] 빌드 실패 (exit code: " + buildExit + ") — 배포를 중단합니다.");
                return;
            }
            sendLog(appId, "[SUCCESS] 빌드 성공 (exit code: 0)");

            // 2단계: Docker 배포
            String deployCmd = notBlankOr(config.getDockerDeployCmd(), "docker stack deploy -c docker-compose.yml myapp");
            sendLog(appId, "");
            sendLog(appId, "━━━ [2/2] 컨테이너 배포: " + deployCmd + " ━━━");
            int deployExit = execCommand(ssh, appId, deployCmd);

            if (deployExit == 0) {
                sendLog(appId, "[SUCCESS] 배포 완료 (exit code: 0)");
            } else {
                sendLog(appId, "[FAILED] 배포 실패 (exit code: " + deployExit + ")");
            }

        } catch (Exception e) {
            log.error("배포 중 오류: {}", e.getMessage(), e);
            sendLog(appId, "[ERROR] " + e.getMessage());
        } finally {
            try { if (ssh.isConnected()) ssh.disconnect(); } catch (IOException ignored) {}
            sendLog(appId, "");
            sendLog(appId, "[DONE] 배포 프로세스 종료");
        }
    }

    private int execCommand(SSHClient ssh, String appId, String command) throws Exception {
        try (Session session = ssh.startSession()) {
            Session.Command cmd = session.exec(command);

            Thread stderrThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(cmd.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sendLog(appId, "[ERR] " + line);
                    }
                } catch (IOException ignored) {}
            });
            stderrThread.setDaemon(true);
            stderrThread.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(cmd.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sendLog(appId, line);
                }
            }
            stderrThread.join(5000);
            cmd.join(30, TimeUnit.SECONDS);

            Integer exitStatus = cmd.getExitStatus();
            return exitStatus != null ? exitStatus : -1;
        }
    }

    private void sendLog(String appId, String message) {
        messagingTemplate.convertAndSend("/topic/logs/" + appId, message);
    }

    private InfraItem findItemById(String appId) {
        return jsonRepository.getData().getInfraTierGroups().stream()
                .flatMap(g -> g.getItems().stream())
                .filter(item -> appId.equals(item.getId()))
                .findFirst()
                .orElse(null);
    }

    private String notBlankOr(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }
}
