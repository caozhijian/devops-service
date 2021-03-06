package io.choerodon.devops.infra.feign.fallback;


import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.dto.agile.IssueDTO;
import io.choerodon.devops.infra.dto.agile.ProjectInfoDTO;
import io.choerodon.devops.infra.dto.agile.SprintDTO;
import io.choerodon.devops.infra.feign.AgileServiceClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgileServiceClientFallback implements AgileServiceClient {
    @Override
    public ResponseEntity<IssueDTO> queryIssue(Long projectId, Long issueId, Long organizationId) {
        return new ResponseEntity("error.issue.get", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<ProjectInfoDTO> queryProjectInfo(Long projectId) {
        throw new CommonException("error.projectinfo.get");
    }

    @Override
    public ResponseEntity<List<IssueDTO>> queryIssues(Long projectId, List<Long> issueIds) {
        throw new CommonException("error.issue.get");
    }

    @Override
    public ResponseEntity<SprintDTO> getActiveSprint(Long projectId, Long organizationId) {
        throw new CommonException("error.active.sprint.get");
    }
}
