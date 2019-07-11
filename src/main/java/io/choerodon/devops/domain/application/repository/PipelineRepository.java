package io.choerodon.devops.domain.application.repository;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.iam.entity.PipelineE;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:19 2019/4/4
 * Description:
 */
public interface PipelineRepository {
    PageInfo<PipelineE> listByOptions(Long projectId, PageRequest pageRequest, String params, Map<String, Object> classifyParam);

    PipelineE create(Long projectId, PipelineE pipelineE);

    PipelineE update(Long projectId, PipelineE pipelineE);

    PipelineE updateIsEnabled(Long pipelineId, Integer isEnabled);

    PipelineE queryById(Long pipelineId);

    void delete(Long pipelineId);

    void checkName(Long projectId, String name);

    List<PipelineE> queryByProjectId(Long projectId);
}
