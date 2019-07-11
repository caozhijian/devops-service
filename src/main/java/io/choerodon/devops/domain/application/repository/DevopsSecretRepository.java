package io.choerodon.devops.domain.application.repository;

import com.github.pagehelper.PageInfo;

import java.util.List;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.iam.entity.DevopsSecretE;

/**
 * Created by n!Ck
 * Date: 18-12-4
 * Time: 上午10:04
 * Description:
 */
public interface DevopsSecretRepository {

    DevopsSecretE create(DevopsSecretE devopsSecretE);

    void update(DevopsSecretE devopsSecretE);

    void deleteSecret(Long secretId);

    void checkName(String name, Long envId);

    DevopsSecretE queryBySecretId(Long secretId);

    DevopsSecretE selectByEnvIdAndName(Long envId, String name);

    PageInfo<DevopsSecretE> listByOption(Long envId, PageRequest pageRequest, String params, Long appId);

    List<DevopsSecretE> listByEnv(Long envId);
}
