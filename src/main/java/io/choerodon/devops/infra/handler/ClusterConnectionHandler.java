package io.choerodon.devops.infra.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.choerodon.devops.infra.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ClusterSessionVO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * Creator: Runge
 * Date: 2018/6/1
 * Time: 15:47
 * Description:
 */

@Service
public class ClusterConnectionHandler {

    private static final String CLUSTER_SESSION = "cluster-sessions-catch";

    Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
    @Value("${agent.version}")
    private String agentExpectVersion;
    @Value("${services.gitlab.sshUrl}")
    private String gitlabSshUrl;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private GitUtil gitUtil;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public static int compareVersion(String a, String b) {
        if (!a.contains("-") && !b.contains("-")) {
            return compareTag(a, b);
        } else if (a.contains("-") && b.contains("-")) {
            String[] a1 = a.split("-");
            String[] b1 = b.split("-");
            int compareResult = compareTag(a1[0], b1[0]);
            if (compareResult == 0) {
                if (TypeUtil.objToLong(b1[1]) > TypeUtil.objToLong(a1[1])) {
                    return 1;
                } else if (TypeUtil.objToLong(b1[1]) < TypeUtil.objToLong(a1[1])) {
                    return -1;
                } else {
                    return 0;
                }
            } else {
                return compareResult;
            }
        }
        return 1;
    }

    public static int compareTag(String a, String b) {
        String[] a1 = a.split("\\.");
        String[] b1 = b.split("\\.");
        if (TypeUtil.objToLong(b1[0]) > TypeUtil.objToLong(a1[0])) {
            return 1;
        } else if (TypeUtil.objToLong(b1[0]) < TypeUtil.objToLong(a1[0])) {
            return -1;
        } else {
            if (TypeUtil.objToLong(b1[1]) > TypeUtil.objToLong(a1[1])) {
                return 1;
            } else if (TypeUtil.objToLong(b1[1]) < TypeUtil.objToLong(a1[1])) {
                return -1;
            } else {
                if (TypeUtil.objToLong(b1[2]) > TypeUtil.objToLong(a1[2])) {
                    return 1;
                } else if (TypeUtil.objToLong(b1[2]) < TypeUtil.objToLong(a1[2])) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }

    /**
     * 检查集群的环境是否链接
     *
     * @param clusterId 环境ID
     */
    public void checkEnvConnection(Long clusterId) {
        Map<String, ClusterSessionVO> clusterSessions = (Map<String, ClusterSessionVO>) (Map) redisTemplate.opsForHash().entries(CLUSTER_SESSION);

        boolean envConnected = clusterSessions.entrySet().stream()
                .anyMatch(t -> clusterId.equals(t.getValue().getClusterId())
                        && compareVersion(t.getValue().getVersion() == null ? "0" : t.getValue().getVersion(), agentExpectVersion) != 1);
        if (!envConnected) {
            throw new CommonException("error.env.disconnect");
        }
    }

    /**
     * 已连接的集群列表
     *
     * @return 已连接的集群列表
     */
    public List<Long> getConnectedClusterList() {
        Map<String, ClusterSessionVO> clusterSessions = (Map<String, ClusterSessionVO>) (Map) redisTemplate.opsForHash().entries(CLUSTER_SESSION);
        return clusterSessions.entrySet().stream()
                .map(t -> t.getValue().getClusterId())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 不需要进行升级的已连接的集群 up-to-date
     *
     * @return 环境更新列表
     */
    public List<Long> getUpdatedClusterList() {
        Map<String, ClusterSessionVO> clusterSessions = (Map<String, ClusterSessionVO>) (Map) redisTemplate.opsForHash().entries(CLUSTER_SESSION);
        return clusterSessions.entrySet().stream()
                .filter(t -> compareVersion(t.getValue().getVersion() == null ? "0" : t.getValue().getVersion(), agentExpectVersion) != 1)
                .map(t -> t.getValue().getClusterId())
                .collect(Collectors.toCollection(ArrayList::new));
    }


    public String handDevopsEnvGitRepository(Long projectId, String envCode, String envRsa) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        //本地路径
        String path = String.format("gitops/%s/%s/%s",
                organizationDTO.getCode(), projectDTO.getCode(), envCode);
        //生成环境git仓库ssh地址
        String url = GitUtil.getGitlabSshUrl(pattern, gitlabSshUrl, organizationDTO.getCode(),
                projectDTO.getCode(), envCode);

        File file = new File(path);
        gitUtil.setSshKey(envRsa);
        if (!file.exists()) {
            gitUtil.cloneBySsh(path, url);
        } else {
            FileUtil.deleteDirectory(file);
            gitUtil.cloneBySsh(path, url);
        }
        return path;
    }
}