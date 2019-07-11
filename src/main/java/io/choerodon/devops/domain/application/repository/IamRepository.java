package io.choerodon.devops.domain.application.repository;

import java.util.List;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.ProjectReqVO;
import io.choerodon.devops.api.vo.RoleAssignmentSearchDTO;
import io.choerodon.devops.api.vo.iam.ProjectWithRoleDTO;
import io.choerodon.devops.api.vo.iam.UserDTO;
import io.choerodon.devops.api.vo.iam.UserWithRoleDTO;
import io.choerodon.devops.api.vo.ProjectVO;
import io.choerodon.devops.api.vo.iam.entity.iam.UserE;
import io.choerodon.devops.app.eventhandler.payload.IamAppPayLoad;
import io.choerodon.devops.domain.application.valueobject.OrganizationVO;
import io.choerodon.devops.domain.application.valueobject.OrganizationSimplifyDTO;
import io.choerodon.devops.domain.application.valueobject.ProjectCreateDTO;

/**
 * Created by younger on 2018/3/29.
 */
public interface IamRepository {

    ProjectVO queryIamProject(Long projectId);

    OrganizationVO queryOrganization();

    OrganizationVO queryOrganizationById(Long organizationId);

    UserE queryByLoginName(String userName);

    List<ProjectVO> listIamProjectByOrgId(Long organizationId, String name, String[] params);

    PageInfo<ProjectVO> queryProjectByOrgId(Long organizationId, int page, int size, String name, String[] params);

    List<UserE> listUsersByIds(List<Long> ids);

    UserE queryUserByUserId(Long id);

    UserE queryByEmail(Long projectId, String email);

    PageInfo<UserDTO> pagingQueryUsersByRoleIdOnProjectLevel(PageRequest pageRequest,
                                                             RoleAssignmentSearchDTO roleAssignmentSearchDTO, Long roleId,
                                                             Long projectId, Boolean doPage);

    PageInfo<UserWithRoleDTO> queryUserPermissionByProjectId(Long projectId, PageRequest pageRequest, Boolean doPage);

    List<ProjectWithRoleDTO> listProjectWithRoleDTO(Long userId);

    Long queryRoleIdByCode(String roleCode);

    List<Long> getAllMemberIdsWithoutOwner(Long projectId);

    List<UserDTO> getAllMember(Long projectId);

    Boolean isProjectOwner(Long userId, ProjectVO projectE);

    IamAppPayLoad createIamApp(Long organizationId, IamAppPayLoad iamAppPayLoad);

    IamAppPayLoad updateIamApp(Long organizationId, Long id, IamAppPayLoad iamAppPayLoad);

    IamAppPayLoad queryIamAppByCode(Long organizationId, String code);

    ProjectReqVO createProject(Long organizationId, ProjectCreateDTO projectCreateDTO);

    PageInfo<OrganizationSimplifyDTO> getAllOrgs(Integer page, Integer size);
}
