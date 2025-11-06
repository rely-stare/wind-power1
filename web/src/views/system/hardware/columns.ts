const columns = [
    {
      title: '序号',
      dataIndex: 'index',
      key: 'index',
      width: 40,
      align: 'center',
      slots: { customRender: 'index' },
    },
    {
      title: '硬件类型',
      dataIndex: 'hardwareType',
      key: 'hardwareType',
      width: 80,
      align: 'center',
      customCell: (record, index) => ({
        rowSpan: record.rowSpan, // 通过 rowSpan 控制合并
      }),
    },
    {
      title: '监控位置',
      dataIndex: 'monitorLocation',
      key: 'monitorLocation',
      width: 80,
      align: 'center'
    },
    {
      title: '硬件编码',
      dataIndex: 'hardwareCode',
      key: 'hardwareCode',
      width: 80,
      align: 'center',
      slots: { customRender: 'hardwareCode' },

    },
    {
      title: '添加时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 120,
      align: 'center'
    }
  ]
  export { columns };