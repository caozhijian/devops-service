import getTablePostData from '../../../../../../utils/getTablePostData';

export default ({ formatMessage, intlPrefix, projectId, id }) => ({
  selection: false,
  pageSize: 10,
  transport: {
    read: ({ data }) => {
      const postData = getTablePostData(data);

      return ({
        url: `/devops/v1/projects/${projectId}/service/page_by_instance?app_service_id=${id}`,
        method: 'post',
        data: postData,
      });
    },
    destroy: ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/service/${data.id}`,
      method: 'delete',
    }),
  },
  fields: [
    { name: 'id', type: 'number' },
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.application.net.name` }) },
    { name: 'error', type: 'string' },
    { name: 'status', type: 'string' },
    { name: 'config', type: 'object' },
    { name: 'type', type: 'string', label: formatMessage({ id: `${intlPrefix}.application.net.configType` }) },
    { name: 'loadBalanceIp', type: 'string' },
    { name: 'target', type: 'object' },
    { name: 'appId', type: 'number' },
    { name: 'devopsIngressVOS', type: 'object' },
  ],
});
