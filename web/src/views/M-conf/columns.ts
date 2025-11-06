
const columns = [
    {
      title: '功能',
      dataIndex: 'funcName',
      key: 'funcName',
      align: 'left'
    },
    {
      title: '显示信息',
      dataIndex: 'alarmName',
      key: 'alarmName',
      align: 'left'
    },
    {
      title: '设定值',
      dataIndex: 'setting',
      width:400,
      key: 'setting',
      align: 'left',
      slots: { customRender: 'setting' },
    },
    {
      
      title: '操作',
      dataIndex: 'operation',
      key: 'operation',
      width:100,
      align: 'left',
      slots: { customRender: 'operation' },
    }
  ]
  export { columns };