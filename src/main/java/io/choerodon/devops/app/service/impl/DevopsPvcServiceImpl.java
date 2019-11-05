package io.choerodon.devops.app.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsPvcReqVO;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.app.service.DevopsPvcService;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.dto.DevopsPvcDTO;
import io.choerodon.devops.infra.mapper.DevopsPvcMapper;

@Service
public class DevopsPvcServiceImpl implements DevopsPvcService {

    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private DevopsPvcMapper devopsPvcMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Long projectId, DevopsPvcReqVO devopsPvcReqVO) {
    }

    private DevopsPvcDTO handlePvc(DevopsPvcReqVO devopsPvcReqVO) {
        return null;
    }

    @Override
    public void baseCheckName(String pvcName, Long envId) {
        if (queryByEnvIdAndName(envId, pvcName) != null) {
            throw new CommonException("error.pvc.name.already.exists");
        }
    }

    @Override
    public DevopsPvcDTO queryByEnvIdAndName(Long envId, String name) {
        DevopsPvcDTO devopsPvcDTO = new DevopsPvcDTO();
        devopsPvcDTO.setName(name);
        devopsPvcDTO.setEnvId(envId);
        return devopsPvcMapper.selectOne(devopsPvcDTO);
    }

    @Override
    public DevopsPvcDTO createOrUpdateByGitOps(Long envId, DevopsPvcReqVO devopsPvcReqVO) {
        // TODO by zmf
        return null;
    }

    @Override
    public void deleteByGitOps(Long pvcId) {
        // TODO by zmf
    }

    private static DevopsPvcDTO voToDTO(DevopsPvcReqVO devopsPvcReqVO) {
        DevopsPvcDTO devopsPvcDTO = new DevopsPvcDTO();
        BeanUtils.copyProperties(devopsPvcReqVO, devopsPvcDTO);
        return devopsPvcDTO;
    }

}