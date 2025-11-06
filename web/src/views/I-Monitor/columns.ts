const columns = [
    {
      title: '序号',
      dataIndex: 'index',
      key: 'index',
      width: 50,
      slots: { customRender: 'index' },
    },
    {
      title: '风机号',
      dataIndex: 'siteName',
      key: 'siteName',
      width: 60,
      align: 'left',
      ellipsis: true,
    },
    {
      title: '监控设备',
      dataIndex: 'monitorLocation',
      key: 'monitorLocation',
      width: 120,
      align: 'left',
      ellipsis: true,
    },
    {
      title: '监控信息',
      dataIndex: 'alarmContent',
      key: 'alarmContent',
      width: 80,
      align: 'left',
      ellipsis: true,
    },
    {
      title: '触发时间',
      dataIndex: 'alarmTime',
      key: 'alarmTime',
      width: 130,
      align: 'left',
      ellipsis: true,
    },
    {
      title: '操作',
      dataIndex: 'action',
      key: 'action',
      width: 50,
      slots: { customRender: 'action' },
      align: 'left'
    },
  
  ]
  export { columns };