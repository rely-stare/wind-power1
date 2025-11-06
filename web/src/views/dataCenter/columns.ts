const columns = [
    {
      title: '序号',
      dataIndex: 'index',
      key: 'index',
      width: 40,
      slots: { customRender: 'index' },
    },
    {
      title: '风机号',
      dataIndex: 'siteName',
      key: 'siteName',
      width: 80,
      align: 'left'
    },
    {
      title: '监控设备',
      dataIndex: 'monitorLocation',
      key: 'monitorLocation',
      width: 80,
      align: 'left'
    },
    {
      title: '监控信息',
      dataIndex: 'monitorInfo',
      key: 'monitorInfo',
      width: 80,
      align: 'left'
    },
    {
      title: '触发时间',
      dataIndex: 'time',
      key: 'time',
      width: 120,
      align: 'left'
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