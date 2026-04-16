package com.ruoyi.business.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import com.ruoyi.business.domain.BizDataBackup;
import com.ruoyi.business.mapper.BizDataBackupMapper;
import com.ruoyi.business.service.IBizDataBackupService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizDataBackupServiceImpl implements IBizDataBackupService
{
    /** 恢复数据批次大小 */
    private static final int RESTORE_BATCH_SIZE = 500;

    private static final String BACKUP_TYPE_MANUAL = "manual";

    private static final String BACKUP_TYPE_AUTO = "auto";

    private static final String BACKUP_STATUS_SUCCESS = "success";

    private static final String BACKUP_STATUS_FAILED = "failed";

    private static final String RESTORE_STATUS_SUCCESS = "success";

    private static final String RESTORE_STATUS_FAILED = "failed";

    private static final String SYSTEM_OPERATOR = "system";

    @Autowired
    private BizDataBackupMapper bizDataBackupMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Override
    public BizDataBackup selectBizDataBackupById(Long backupId)
    {
        return bizDataBackupMapper.selectBizDataBackupById(backupId);
    }

    @Override
    public List<BizDataBackup> selectBizDataBackupList(BizDataBackup bizDataBackup)
    {
        return bizDataBackupMapper.selectBizDataBackupList(bizDataBackup);
    }

    @Override
    public int createManualBackup(String operatorName)
    {
        return createBackup(BACKUP_TYPE_MANUAL, operatorName);
    }

    @Override
    public int createAutoBackup()
    {
        return createBackup(BACKUP_TYPE_AUTO, SYSTEM_OPERATOR);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int restoreByBackupId(Long backupId, String operatorName)
    {
        BizDataBackup backupRecord = bizDataBackupMapper.selectBizDataBackupById(backupId);
        if (backupRecord == null)
        {
            throw new ServiceException("备份记录不存在");
        }
        if (StringUtils.isEmpty(backupRecord.getBackupContent()))
        {
            throw new ServiceException("备份内容为空，无法恢复");
        }
        try
        {
            TypeReference<LinkedHashMap<String, List<LinkedHashMap<String, Object>>>> backupTypeReference =
                new TypeReference<LinkedHashMap<String, List<LinkedHashMap<String, Object>>>>()
                {
                };
            LinkedHashMap<String, List<LinkedHashMap<String, Object>>> backupContentMap =
                objectMapper.readValue(backupRecord.getBackupContent(), backupTypeReference);
            for (Map.Entry<String, List<LinkedHashMap<String, Object>>> tableEntry : backupContentMap.entrySet())
            {
                String tableName = tableEntry.getKey();
                if (!isSupportedBackupTable(tableName))
                {
                    continue;
                }
                jdbcTemplate.update("delete from " + tableName);
                List<LinkedHashMap<String, Object>> tableDataList = tableEntry.getValue();
                if (tableDataList == null || tableDataList.isEmpty())
                {
                    continue;
                }
                insertTableData(tableName, tableDataList);
            }
            BizDataBackup updateRecord = new BizDataBackup();
            updateRecord.setBackupId(backupId);
            updateRecord.setRestoreStatus(RESTORE_STATUS_SUCCESS);
            updateRecord.setRestoreBy(operatorName);
            updateRecord.setRestoreTime(new Date());
            updateRecord.setUpdateBy(operatorName);
            return bizDataBackupMapper.updateBizDataBackup(updateRecord);
        }
        catch (Exception exception)
        {
            updateRestoreStatusInNewTransaction(backupId, RESTORE_STATUS_FAILED, operatorName);
            throw new ServiceException("恢复失败：" + exception.getMessage());
        }
    }

    @Override
    public int deleteBizDataBackupByIds(Long[] backupIds)
    {
        return bizDataBackupMapper.deleteBizDataBackupByIds(backupIds);
    }

    private int createBackup(String backupType, String operatorName)
    {
        BizDataBackup backupRecord = new BizDataBackup();
        backupRecord.setBackupType(backupType);
        backupRecord.setBackupName(generateBackupName(backupType));
        backupRecord.setCreateBy(operatorName);
        backupRecord.setUpdateBy(operatorName);
        try
        {
            List<String> backupTableNameList = listBackupTableNameList();
            LinkedHashMap<String, List<Map<String, Object>>> backupContentMap = new LinkedHashMap<String, List<Map<String, Object>>>();
            long totalRecordCount = 0L;
            for (String tableName : backupTableNameList)
            {
                List<Map<String, Object>> tableDataList = jdbcTemplate.queryForList("select * from " + tableName);
                backupContentMap.put(tableName, tableDataList);
                totalRecordCount += tableDataList.size();
            }
            backupRecord.setTableCount(backupTableNameList.size());
            backupRecord.setRecordCount(totalRecordCount);
            backupRecord.setBackupContent(objectMapper.writeValueAsString(backupContentMap));
            backupRecord.setBackupStatus(BACKUP_STATUS_SUCCESS);
            return bizDataBackupMapper.insertBizDataBackup(backupRecord);
        }
        catch (Exception exception)
        {
            backupRecord.setBackupStatus(BACKUP_STATUS_FAILED);
            backupRecord.setRemark(exception.getMessage());
            bizDataBackupMapper.insertBizDataBackup(backupRecord);
            throw new ServiceException("创建备份失败：" + exception.getMessage());
        }
    }

    /**
     * 使用独立事务更新恢复状态，避免主恢复事务回滚时丢失失败标记。
     *
     * @param backupId 备份编号
     * @param restoreStatus 恢复状态
     * @param operatorName 操作人
     */
    private void updateRestoreStatusInNewTransaction(Long backupId, String restoreStatus, String operatorName)
    {
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            BizDataBackup updateRecord = new BizDataBackup();
            updateRecord.setBackupId(backupId);
            updateRecord.setRestoreStatus(restoreStatus);
            updateRecord.setRestoreBy(operatorName);
            updateRecord.setRestoreTime(new Date());
            updateRecord.setUpdateBy(operatorName);
            bizDataBackupMapper.updateBizDataBackup(updateRecord);
        });
    }

    /**
     * 分批写入表数据，降低恢复时逐条插入带来的执行耗时。
     *
     * @param tableName 表名
     * @param tableDataList 表数据列表
     */
    private void insertTableData(String tableName, List<LinkedHashMap<String, Object>> tableDataList)
    {
        LinkedHashMap<String, Object> firstRecord = tableDataList.get(0);
        List<String> columnNameList = new ArrayList<String>(firstRecord.keySet());
        StringBuilder columnNameSqlBuilder = new StringBuilder();
        StringBuilder placeHolderSqlBuilder = new StringBuilder();
        for (int columnIndex = 0; columnIndex < columnNameList.size(); columnIndex++)
        {
            if (columnIndex > 0)
            {
                columnNameSqlBuilder.append(",");
                placeHolderSqlBuilder.append(",");
            }
            columnNameSqlBuilder.append(columnNameList.get(columnIndex));
            placeHolderSqlBuilder.append("?");
        }
        String insertSql = "insert into " + tableName + " (" + columnNameSqlBuilder + ") values (" + placeHolderSqlBuilder + ")";
        List<Object[]> batchParameterValueList = new ArrayList<Object[]>();
        for (LinkedHashMap<String, Object> rowData : tableDataList)
        {
            Object[] parameterValueArray = new Object[columnNameList.size()];
            for (int columnIndex = 0; columnIndex < columnNameList.size(); columnIndex++)
            {
                parameterValueArray[columnIndex] = rowData.get(columnNameList.get(columnIndex));
            }
            batchParameterValueList.add(parameterValueArray);
            if (batchParameterValueList.size() >= RESTORE_BATCH_SIZE)
            {
                jdbcTemplate.batchUpdate(insertSql, batchParameterValueList);
                batchParameterValueList.clear();
            }
        }
        if (!batchParameterValueList.isEmpty())
        {
            jdbcTemplate.batchUpdate(insertSql, batchParameterValueList);
        }
    }

    private List<String> listBackupTableNameList()
    {
        String listTableSql =
            "select table_name from information_schema.tables where table_schema = (select database()) and table_type = 'base table' order by table_name";
        List<String> allTableNameList = jdbcTemplate.queryForList(listTableSql, String.class);
        List<String> backupTableNameList = new ArrayList<String>();
        for (String tableName : allTableNameList)
        {
            if (isSupportedBackupTable(tableName))
            {
                backupTableNameList.add(tableName);
            }
        }
        return backupTableNameList;
    }

    private boolean isSupportedBackupTable(String tableName)
    {
        if (StringUtils.isEmpty(tableName))
        {
            return false;
        }
        if (tableName.startsWith("wms_") || tableName.startsWith("fin_"))
        {
            return true;
        }
        return "biz_message".equals(tableName)
            || "biz_message_read".equals(tableName)
            || "biz_decision_budget_plan".equals(tableName)
            || "biz_executive_brief_record".equals(tableName);
    }

    private String generateBackupName(String backupType)
    {
        SimpleDateFormat datetimeFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
        return backupType + "_" + datetimeFormatter.format(new Date());
    }
}
