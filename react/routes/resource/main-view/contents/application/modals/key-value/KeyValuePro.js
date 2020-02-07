/* eslint-disable */
import React, { Component, Fragment, useState, useEffect, useCallback, useRef } from 'react';
import { observer } from 'mobx-react-lite';
import { inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import _ from 'lodash';
import { Choerodon } from '@choerodon/boot';
import { Button, Form, Input, Modal, Icon, Table, Tooltip } from 'choerodon-ui';
import { EditableCell, EditableFormRow } from './editableTable';
import { objToYaml, yamlToObj, takeObject, ConfigNode, makePostData } from '../utils';
import YamlEditor from '../../../../../../../components/yamlEditor';
import { handlePromptError } from '../../../../../../../utils';

import '../../../../../../main.less';
import './index.less';

const { Sidebar } = Modal;
const { Item: FormItem } = Form;
const { TextArea } = Input;

const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 100 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 26 },
  },
};

const FormView = injectIntl(inject('AppState')(observer((props) => {

  const [dataSource, setDataSource] = useState([new ConfigNode()]);
  const [dataYaml, setDataYaml] = useState('');
  const [counter, setCounter] = useState(1);
  const [submitting, setSubmitting] = useState(false);
  const [hasItemError, setHasItemError] = useState(false);
  const [warningMes, setWarningMes] = useState('');
  const [data, setData] = useState(false);
  const [isYamlEdit, setIsYamlEdit] = useState(false);
  const [hasYamlError, setHasYamlError] = useState(false);
  const [hasValueError, setHasValueError] = useState(false);
  const [valueErrorMsg, setValueErrorMsg] = useState('');

  const [isSubmit, setIsSubmit] = useState(false);

  /**
   * 检查名称唯一性
   * @param rule
   * @param value
   * @param callback
   */
  const checkName = _.debounce((rule, value, callback) => {
    const {
      intl: {
        formatMessage,
      },
      AppState: {
        currentMenuType: {
          projectId,
        },
      },
      envId,
      store,
    } = props;

    const pattern = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;
    if (value && !pattern.test(value)) {
      callback(formatMessage({ id: 'network.name.check.failed' }));
    } else if (value && pattern.test(value)) {
      store.checkName(projectId, envId, value)
        .then(res => {
          if (handlePromptError(res, false)) {
            callback();
          } else {
            callback(formatMessage({ id: 'checkNameExist' }));
          }
        })
        .catch(error => {
          callback(formatMessage({ id: 'checkNameFailed' }));

        });
    } else {
      callback();
    }
  }, 1000);

  useEffect(() => {
    async function callBack() {
      const {
        id,
        store,
        AppState: {
          currentMenuType: {
            projectId,
          },
        },
        modal,
      } = props;
      if (typeof id === 'number') {
        try {
          const res = await store.loadSingleData(projectId, id);
          if (handlePromptError(res)) {
            let counter = 1;

            if (!_.isEmpty(res.value)) {
              const dataSource = _.map(res.value, (value, key) => new ConfigNode(key, value, counter++));

              setDataSource(dataSource);
              setCounter(counter);
            }

            setData(res);
          }
        } catch (e) {
          Choerodon.handleResponseError(e);
        }
      }
      modal.handleOk(handleSubmit);
    }
    callBack();
  }, []);

  useEffect(() => {
    checkButtonDisabled(isSubmit);
  }, [hasValueError, hasItemError, hasYamlError]);

  function useAvoidClosure(fn) {
    const ref = useRef(fn);
    ref.current = fn;
    const callback = (...args) => ref.current(...args);
    return callback;
  }

  /**
   * 删除key-value
   * @param key
   */
  const handleDelete = useAvoidClosure((key) => {
    console.log(dataSource);
    const dataSource = [...dataSource].filter(item => item.index !== key);

    asyncCheckErrorData(dataSource);

    setDataSource(dataSource);
  });

  /**
   * 添加一组 key/value
   * @param data
   */
  const handleAdd = (data) => {
    let _data = data;

    if (!Array.isArray(data)) {
      _data = [[null, null]];
    }

    let _counter = counter;
    const newData = _.map(_data, ([key, value]) => new ConfigNode(key, value, ++_counter));

    if (!newData.length) {
      const initConfig = new ConfigNode();
      newData.push(initConfig);
    }

    const uniqData = _.uniqBy([...dataSource.filter(item => item.index !== ''), ...newData], 'index');
    setDataSource([...uniqData]);
    setCounter(_counter);
  };

  /**
   * 保存输入
   * @param row
   */
  const handleSave = (row) => {
    const newData = [...dataSource];
    const index = _.findIndex(newData, ['index', row.index]);

    newData.splice(index, 1, {
      ...newData[index],
      ...row,
    });

    checkErrorData(newData);

    setDataSource(newData);
  };

  /**
   * configMap 规则中value只能是字符串
   * @param data
   */
  const checkConfigRuleError = (data = '') => {
    const yaml = data || dataYaml;
    const yamlObj = yamlToObj(yaml) || {};
    const values = Object.values(yamlObj);

    let error = false;
    for (let i = 0, len = values.length; i < len; i++) {
      if (typeof values[i] !== 'string' || values[i] === '') {
        error = true;
        break;
      }
    }

    //TODO 此处存疑
    setHasValueError(error);

    // this.setState({ hasValueError: error }, () => this.checkButtonDisabled);
    return error;
  };

  const asyncCheckConfigRuleError = _.debounce(checkConfigRuleError, 600);

  /**
   * 同步校验键值对
   * @param data
   * @returns {boolean}
   */
  const checkErrorData = (data = null, isSubmit = false) => {
    const {
      title,
      intl: {
        formatMessage,
      },
      modal,
    } = props;

    const _data = data || dataSource;
    const hasKey = _data.filter(({ key }) => !_.isEmpty(key));
    const onlyHasValue = _data.filter(({ key, value }) => _.isEmpty(key) && !_.isEmpty(value));
    const onlyHasKey = hasKey.filter(({ value }) => _.isEmpty(value));
    const hasErrorItem = onlyHasKey.length || onlyHasValue.length;
    const hasRepeatKey = hasKey.length !== _.uniqBy(hasKey, 'key').length;
    const hasEmptyKey = title === 'cipher' && (_.isEmpty(hasKey) || hasKey.length !== _data.length);

    let hasErrorKey;
    for (const { key } of hasKey) {
      const pattern = /[^0-9A-Za-z\.\-\_]/;
      if (pattern.test(key)) {
        hasErrorKey = true;
        break;
      }
    }

    if (!(hasErrorItem || hasErrorKey || hasRepeatKey || hasEmptyKey)) {
      setWarningMes('');
      setIsSubmit(isSubmit);
      setHasItemError(false);

      // this.setState({
      //   warningMes: '',
      //   hasItemError: false,
      // }, () => this.checkButtonDisabled(isSubmit));
      return false;
    }

    const errorMsg = formatMessage({
      id: hasRepeatKey ? 'configMap.keyRepeat' : `${title}.keyValueSpan`,
    });

    setConfigError(errorMsg);

    return true;
  };

  /**
   * 校验键值对
   * @param data
   * @returns {boolean}
   */
  const asyncCheckErrorData = _.debounce(checkErrorData, 500);

  /**
   * 设置键值对模式下的错误提示
   * @param msg
   */
  function setConfigError(msg) {
    const { modal } = props;
    setWarningMes(msg);
    setHasItemError(true);
    // this.setState({
    //   warningMes: msg,
    //   hasItemError: true,
    // });
    modal.update({ okProps: { disabled: true }});
  }

  const formValidate = () => {
    const {
      id,
      envId,
      appId,
      form: { validateFields },
    } = props;

    let configData = [];
    let hasKVError = false;
    let hasConfigRuleError = false;

    return new Promise((resolve) => {
      validateFields((err, { name, description }) => {
        if (!isYamlEdit) {
          hasKVError = checkErrorData(null, true);
          const allData = [...dataSource.filter(item => !_.isEmpty(item.key))];
          configData = _.uniqBy(allData, 'index');
        } else {
          hasConfigRuleError = checkConfigRuleError();
          configData = yamlToObj(dataYaml);
        }

        if (hasYamlError || hasKVError || hasConfigRuleError) {
          resolve(false);
        } else {
          setSubmitting(true);
          setHasItemError(false)

          if (!err) {
            const _value = takeObject(configData);
            const dto = {
              name,
              description,
              envId,
              appServiceId: appId,
              type: id ? 'update' : 'create',
              id: id || undefined,
              value: _value,
            };
            resolve(dto);
          }
          resolve(false);
        }
      });
    });
  };

  /**
   * form提交函数
   * 添加粘贴后key-value校验
   * @param e
   */
  const handleSubmit = async () => {
    const {
      store,
      AppState: {
        currentMenuType: {
          id: projectId,
        },
      },
      refresh,
    } = props;
    const postData = await formValidate();

    if (!postData) {
      return false;
    }
    try {
      const res = await store.postKV(projectId, postData);
      if (handlePromptError(res)) {
        refresh();
      } else {
        return false;
      }
    } catch (error) {
      Choerodon.handleResponseError(error);
      return false;
    }
  };

  /**
   * 配置信息的名称描述等常规表单项
   * @returns {*}
   */
  const getFormContent = () => {
    const {
      intl: { formatMessage },
      form: { getFieldDecorator },
      id,
      title,
    } = props;
    return (<Form className='c7n-sidebar-form' layout="vertical">
      <FormItem
        {...formItemLayout}
      >
        {getFieldDecorator('name', {
          initialValue: data ? data.name : null,
          rules: [{
            required: true,
            message: formatMessage({ id: 'required' }),
          }, {
            validator: id ? null : checkName,
          }],
        })(
          <Input
            autoFocus={!id}
            disabled={!!id}
            maxLength={100}
            label={<FormattedMessage id="app.name" />}
          />,
        )}
      </FormItem>
      <FormItem
        {...formItemLayout}
      >
        {getFieldDecorator('description', {
          initialValue: data ? data.description : null,
        })(
          <TextArea
            autoFocus={!!id}
            autosize={{ minRows: 2 }}
            maxLength={30}
            label={<FormattedMessage id="configMap.des" />}
          />,
        )}
      </FormItem>
    </Form>);
  };

  /**
   * 编辑 configMap 组件节点
   * 有两种模式：key/value编辑模式、YAML代码编辑模式
   * @returns {*}
   */
  const getConfigMap = () => {
    const { title, intlPrefix } = props;

    let configMap = null;
    if (!isYamlEdit) {
      const components = {
        body: {
          row: EditableFormRow,
          cell: EditableCell,
        },
      };
      const baseColumns = [{
        title: 'key',
        dataIndex: 'key',
        width: '25%',
        editable: true,
      }, {
        title: '',
        width: '5%',
        className: 'icon-equal',
        align: 'center',
        dataIndex: 'temp',
      }, {
        title,
        width: '100%',
        dataIndex: 'value',
        editable: true,
      }, {
        title: '',
        dataIndex: 'operation',
        render: (text, { index }) => (
          dataSource.length >= 1 ? (
            <Icon
              className="del-btn"
              type="delete"
              onClick={() => handleDelete(index)}
            />
          ) : null),
      }];

      const columns = baseColumns.map((col) => {
        if (!col.editable) return col;

        return {
          ...col,
          onCell: record => ({
            record,
            editable: col.editable,
            dataIndex: col.dataIndex,
            title: col.title,
            save: handleSave,
            add: handleAdd,
          }),
        };
      });

      configMap = <Fragment>
        <Table
          filterBar={false}
          showHeader={false}
          pagination={false}
          components={components}
          className="c7n-editable-table"
          dataSource={dataSource}
          columns={columns}
          rowKey={record => record.index}
        />
        <Button icon="add" onClick={handleAdd} type="primary">
          <FormattedMessage id={`${intlPrefix}.${title}.add`} />
        </Button>
        {hasItemError ? <div className="c7n-cm-warning">{warningMes}</div> : null}
      </Fragment>;
    } else {
      configMap = <Fragment>
        <YamlEditor
          readOnly={false}
          modeChange={false}
          value={dataYaml}
          onValueChange={changeYamlValue}
          handleEnableNext={checkYamlError}
        />
        <div className="c7ncd-config-yaml-tip">{hasValueError && (valueErrorMsg
          || <FormattedMessage id="configMap.yaml.error" />)}</div>
      </Fragment>;
    }

    return configMap;
  };

  /**
   * yaml 值改变
   * @param value
   */
  const changeYamlValue = (value) => {
    asyncCheckConfigRuleError(value);

    setDataYaml(value);
  };

  /**
   * 校验yaml格式
   * @param flag
   */
  const checkYamlError = (flag) => {
    setHasYamlError(flag);
    // this.setState({ hasYamlError: flag }, () => this.checkButtonDisabled());
  };

  /**
   * 切换配置映射的编辑模式
   */
  const changeEditMode = () => {
    const { modal } = props;

    if (hasYamlError || hasValueError || hasItemError) return;

    if (!isYamlEdit) {
      const result = checkErrorData(dataSource);

      if (result) return;

      const yamlValue = objToYaml(dataSource);

      checkConfigRuleError(yamlValue);

      setCounter(1);
      setHasItemError(false);
      setIsYamlEdit(true);
      setWarningMes('');
      setDataSource([]);
      setDataYaml(yamlValue);

      // this.setState({
      //   counter: 1,
      //   hasItemError: false,
      //   isYamlEdit: true,
      //   warningMes: '',
      //   dataSource: [],
      //   dataYaml: yamlValue,
      // }, () => this.checkButtonDisabled());
    } else {
      const result = checkConfigRuleError(dataYaml);

      if (result) return;

      try {
        const kvValue = yamlToObj(dataYaml);
        const postData = makePostData(kvValue);

        const counter = postData.length;
        setDataSource(postData);
        setHasYamlError(false);
        setIsYamlEdit(false);
        setDataYaml('');
        setCounter(counter);
        // this.setState({
        //   dataSource: postData,
        //   hasYamlError: false,
        //   isYamlEdit: false,
        //   dataYaml: '',
        //   counter,
        // }, () => this.checkButtonDisabled());
      } catch (e) {
        setHasValueError(true);
        setValueErrorMsg(e.message);
        // this.setState({
        //   hasValueError: true,
        //   valueErrorMsg: e.message,
        // });
        modal.update({ okProps: { disabled: true }})
      }
    }
  };

  const checkButtonDisabled = (isSubmit = false) => {
    const { modal } = props;
    !isSubmit && modal.update({ okProps: { disabled: hasYamlError || hasValueError || hasItemError }});
    setIsSubmit(false);
  };

  const {
    intl: { formatMessage },
    visible,
    id,
    title,
    modeSwitch,
    AppState: {
      currentMenuType: {
        name: menuName,
      },
    },
    intlPrefix,
  } = props;

  const titleName = id ? data.name : menuName;
  const titleCode = `${intlPrefix}.${title}.${id ? 'edit' : 'create'}`;
  const disableBtn = hasYamlError || hasValueError || hasItemError;

  return (
    <div className="c7n-region">
      <div>
        {getFormContent()}
        <div className="c7n-sidebar-from-title">
          <FormattedMessage id={`${intlPrefix}.${title}.head`} />
          {!isYamlEdit && <Tooltip
            overlayStyle={{ maxWidth: 350 }}
            title={formatMessage({ id: `${intlPrefix}.${title}.help.tooltip` })}
          >
            <Icon type="help" />
          </Tooltip>}
          {modeSwitch ? <Button
            className="c7n-config-mode-btn"
            type="primary"
            funcType="flat"
            disabled={disableBtn}
            onClick={changeEditMode}
          >
            <FormattedMessage id={isYamlEdit ? 'configMap.mode.yaml' : 'configMap.mode.kv'} />
          </Button> : null}
        </div>
        <div className="c7n-config-editor">
          {getConfigMap()}
        </div>
      </div>
    </div>
  );
})));
export default Form.create()(FormView);
