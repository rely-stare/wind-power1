package com.tc.common.utils.poi;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.merge.AbstractMergeStrategy;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;

import java.util.List;

/**
 * @author admin
 * @version 1.0
 * @description: TODO
 * @date 2022/6/6 10:43
 */
public class ExcelHandler extends AbstractMergeStrategy implements SheetWriteHandler {

    private String sheetName;

    private Integer sheetNo;

    public ExcelHandler(String sheetName,int sheetNo) {
        this.sheetName = sheetName;
        this.sheetNo = sheetNo;
    }

    public ExcelHandler() {
    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        if (sheetName == null) {
            return;
        }
        if (sheetNo == null) {
            sheetNo = 0;
        }
        //往哪个sheet设置名称
        System.out.println("======"+sheetNo+",sheetName="+sheetName);
        writeWorkbookHolder.getCachedWorkbook().setSheetName(sheetNo, sheetName);
    }

    @Override
    protected void merge(Sheet sheet, Cell cell, Head head, Integer relativeRowIndex) {
        if(relativeRowIndex==null ||relativeRowIndex==0){
            return;
        }
        int rowIndex = cell.getRowIndex();
        int colIndex = cell.getColumnIndex();
        sheet=cell.getSheet();
        Row preRow = sheet.getRow(rowIndex - 1);
        Cell preCell = preRow.getCell(colIndex);//获取上一行的该格
        List<CellRangeAddress> list = sheet.getMergedRegions();
        CellStyle cs = cell.getCellStyle();
        cell.setCellStyle(cs);
        for (int i = 0; i < list.size(); i++) {
            CellRangeAddress cellRangeAddress = list.get(i);
            if (cellRangeAddress.containsRow(preCell.getRowIndex()) && cellRangeAddress.containsColumn(preCell.getColumnIndex())) {
                int lastColIndex = cellRangeAddress.getLastColumn();
                int firstColIndex = cellRangeAddress.getFirstColumn();
                CellRangeAddress cra = new CellRangeAddress(cell.getRowIndex(), cell.getRowIndex(), firstColIndex, lastColIndex);
                sheet.addMergedRegion(cra);
                RegionUtil.setBorderBottom(BorderStyle.THIN, cra, sheet);
                RegionUtil.setBorderLeft(BorderStyle.THIN, cra, sheet);
                RegionUtil.setBorderRight(BorderStyle.THIN, cra, sheet);
                RegionUtil.setBorderTop(BorderStyle.THIN, cra, sheet);
                return;
            }
        }

    }

}
