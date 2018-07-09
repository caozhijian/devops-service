package io.choerodon.devops.infra.dataobject;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/9
 * Time: 14:23
 * Description:
 */
@ModifyAudit
@Table(name = "devops_merge_request")
public class DevopsMergeRequestDO {

    @Id
    @GeneratedValue
    private Long id;

    private Long applicationId;

    private Long gitlabMergeRequestId;

    private Long authorId;

    private Long assigneeId;

    private String sourceBranch;

    private String targetBranch;

    private String state;

    public DevopsMergeRequestDO() {
    }

    /**
     * constructor a new merge request item
     *
     * @param applicationId devops application ID
     * @param sourceBranch  source branch to merge
     * @param targetBranch  target merge branch
     */
    public DevopsMergeRequestDO(Long applicationId, String sourceBranch, String targetBranch) {
        this.applicationId = applicationId;
        this.sourceBranch = sourceBranch;
        this.targetBranch = targetBranch;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getGitlabMergeRequestId() {
        return gitlabMergeRequestId;
    }

    public void setGitlabMergeRequestId(Long gitlabMergeRequestId) {
        this.gitlabMergeRequestId = gitlabMergeRequestId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getSourceBranch() {
        return sourceBranch;
    }

    public void setSourceBranch(String sourceBranch) {
        this.sourceBranch = sourceBranch;
    }

    public String getTargetBranch() {
        return targetBranch;
    }

    public void setTargetBranch(String targetBranch) {
        this.targetBranch = targetBranch;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
