package com.ruoyi.business.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysDictTypeService;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import com.fasterxml.jackson.core.JsonGenerator;
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
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizDataBackupServiceImpl implements IBizDataBackupService
{
    /** 恢复数据批次大小 */
    private static final int RESTORE_BATCH_SIZE = 500;

    /** 流式读取分页大小 */
    private static final int STREAM_FETCH_SIZE = 1000;

    private static final String BACKUP_TYPE_MANUAL = "manual";

    private static final String BACKUP_TYPE_AUTO = "auto";

    private static final String BACKUP_TYPE_PRE_RESTORE = "pre_restore";

    private static final String BACKUP_STATUS_SUCCESS = "success";

    private static final String BACKUP_STATUS_FAILED = "failed";

    private static final String RESTORE_STATUS_SUCCESS = "success";

    private static final String RESTORE_STATUS_FAILED = "failed";

    private static final String SYSTEM_OPERATOR = "system";

    @Autowired(required = false)
    private ISysConfigService sysConfigService;

    @Autowired(required = false)
    private ISysDictTypeService sysDictTypeService;

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
        if (!BACKUP_STATUS_SUCCESS.equals(backupRecord.getBackupStatus()))
        {
            throw new ServiceException("仅备份成功的记录允许执行恢复");
        }

        // 恢复前在独立事务中自动生成一条保护性快照，确保误恢复可回退
        createPreRestoreSnapshotInNewTransaction(operatorName, backupRecord.getBackupName());

        try
        {
            LinkedHashMap<String, List<LinkedHashMap<String, Object>>> backupContentMap = loadBackupPayloadMap(backupRecord);
            if (backupContentMap == null || backupContentMap.isEmpty())
            {
                throw new ServiceException("备份数据包无有效业务表内容，无法恢复");
            }
            applyBackupPayloadMap(backupContentMap);

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
    @Transactional(rollbackFor = Exception.class)
    public int restoreFromUploadedFile(InputStream inputStream, String fileName, String operatorName)
    {
        if (inputStream == null)
        {
            throw new ServiceException("请上传有效的备份文件（.json.gz 或 .json）");
        }
        String safeFileName = StringUtils.isEmpty(fileName) ? "uploaded_backup.json.gz" : fileName.trim();
        LinkedHashMap<String, List<LinkedHashMap<String, Object>>> payloadMap;
        try
        {
            TypeReference<LinkedHashMap<String, List<LinkedHashMap<String, Object>>>> typeRef =
                new TypeReference<LinkedHashMap<String, List<LinkedHashMap<String, Object>>>>() {};
            PushbackInputStream pb = new PushbackInputStream(inputStream, 2);
            byte[] header = new byte[2];
            int readLen = pb.read(header);
            if (readLen > 0)
            {
                pb.unread(header, 0, readLen);
            }
            boolean isGzip = readLen == 2 && (header[0] == (byte) 0x1f) && (header[1] == (byte) 0x8b);
            if (isGzip)
            {
                try (GZIPInputStream gzis = new GZIPInputStream(pb))
                {
                    payloadMap = objectMapper.readValue(gzis, typeRef);
                }
            }
            else
            {
                payloadMap = objectMapper.readValue(pb, typeRef);
            }
        }
        catch (Exception e)
        {
            throw new ServiceException("解析上传的备份文件失败，请确认文件为合法的 SaleERP .json.gz 或 .json 离线备份包：" + e.getMessage());
        }
        if (payloadMap == null || payloadMap.isEmpty())
        {
            throw new ServiceException("上传的备份文件不包含任何业务数据表");
        }
        int validTableCount = 0;
        long totalRecordCount = 0L;
        for (Map.Entry<String, List<LinkedHashMap<String, Object>>> entry : payloadMap.entrySet())
        {
            if (isSupportedBackupTable(entry.getKey()))
            {
                validTableCount++;
                if (entry.getValue() != null)
                {
                    totalRecordCount += entry.getValue().size();
                }
            }
        }
        if (validTableCount == 0)
        {
            throw new ServiceException("上传的文件未匹配到任何受管业务表，已拦截恢复");
        }

        createPreRestoreSnapshotInNewTransaction(operatorName, safeFileName);
        applyBackupPayloadMap(payloadMap);

        BizDataBackup auditRecord = new BizDataBackup();
        auditRecord.setBackupName("upload_restore_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        auditRecord.setBackupType("upload_restore");
        auditRecord.setBackupStatus(BACKUP_STATUS_SUCCESS);
        auditRecord.setTableCount(validTableCount);
        auditRecord.setRecordCount(totalRecordCount);
        auditRecord.setRestoreStatus(RESTORE_STATUS_SUCCESS);
        auditRecord.setRestoreBy(operatorName);
        auditRecord.setRestoreTime(new Date());
        auditRecord.setCreateBy(operatorName);
        auditRecord.setUpdateBy(operatorName);
        auditRecord.setRemark("通过离线上传包恢复：" + safeFileName);
        return bizDataBackupMapper.insertBizDataBackup(auditRecord);
    }

    private void applyBackupPayloadMap(LinkedHashMap<String, List<LinkedHashMap<String, Object>>> backupContentMap)
    {
        List<String> managedTables = listBackupTableNameList();
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        try
        {
            // 先清空全部当前受管业务表，避免旧版备份恢复后在新上线业务表留下孤立脏数据
            for (String managedTable : managedTables)
            {
                jdbcTemplate.update("DELETE FROM `" + managedTable + "`");
            }
            for (Map.Entry<String, List<LinkedHashMap<String, Object>>> tableEntry : backupContentMap.entrySet())
            {
                String tableName = tableEntry.getKey();
                if (!isSupportedBackupTable(tableName))
                {
                    continue;
                }
                if (!managedTables.contains(tableName.toLowerCase(Locale.ROOT)))
                {
                    continue;
                }
                List<LinkedHashMap<String, Object>> tableDataList = tableEntry.getValue();
                if (tableDataList == null || tableDataList.isEmpty())
                {
                    continue;
                }
                insertTableData(tableName, tableDataList);
            }
        }
        finally
        {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
        refreshSystemCachesAfterRestore();
    }

    private void refreshSystemCachesAfterRestore()
    {
        try
        {
            if (sysConfigService != null)
            {
                sysConfigService.resetConfigCache();
            }
            if (sysDictTypeService != null)
            {
                sysDictTypeService.resetDictCache();
            }
        }
        catch (Exception ignored)
        {
            // 非缓存环境忽略
        }
    }

    @Override
    public File resolveBackupDownloadFile(Long backupId)
    {
        BizDataBackup backupRecord = bizDataBackupMapper.selectBizDataBackupById(backupId);
        if (backupRecord == null)
        {
            throw new ServiceException("备份记录不存在");
        }
        if (StringUtils.isNotEmpty(backupRecord.getFilePath()))
        {
            File diskFile = new File(getBackupBaseDir(), backupRecord.getFilePath());
            if (diskFile.exists() && diskFile.isFile())
            {
                return diskFile;
            }
        }
        if (StringUtils.isEmpty(backupRecord.getBackupContent()))
        {
            throw new ServiceException("该记录不存在可下载的备份文件");
        }
        try
        {
            File dir = getBackupBaseDir();
            if (!dir.exists())
            {
                dir.mkdirs();
            }
            String fileName = backupRecord.getBackupName() + ".json.gz";
            File tempGzFile = new File(dir, fileName);
            try (OutputStream fos = new FileOutputStream(tempGzFile);
                 GZIPOutputStream gzos = new GZIPOutputStream(fos))
            {
                gzos.write(backupRecord.getBackupContent().getBytes(StandardCharsets.UTF_8));
            }
            return tempGzFile;
        }
        catch (Exception e)
        {
            throw new ServiceException("生成备份下载包失败：" + e.getMessage());
        }
    }

    @Override
    public int deleteBizDataBackupByIds(Long[] backupIds)
    {
        if (backupIds != null)
        {
            for (Long id : backupIds)
            {
                BizDataBackup record = bizDataBackupMapper.selectBizDataBackupById(id);
                if (record != null && StringUtils.isNotEmpty(record.getFilePath()))
                {
                    File diskFile = new File(getBackupBaseDir(), record.getFilePath());
                    if (diskFile.exists())
                    {
                        diskFile.delete();
                    }
                }
            }
        }
        return bizDataBackupMapper.deleteBizDataBackupByIds(backupIds);
    }

    private int createBackup(String backupType, String operatorName)
    {
        return createBackupWithRemark(backupType, operatorName, null);
    }

    private int createBackupWithRemark(String backupType, String operatorName, String customRemark)
    {
        BizDataBackup backupRecord = new BizDataBackup();
        backupRecord.setBackupType(backupType);
        String backupName = generateBackupName(backupType);
        backupRecord.setBackupName(backupName);
        backupRecord.setCreateBy(operatorName);
        backupRecord.setUpdateBy(operatorName);
        if (StringUtils.isNotEmpty(customRemark))
        {
            backupRecord.setRemark(customRemark);
        }
        try
        {
            List<String> backupTableNameList = listBackupTableNameList();
            File baseDir = getBackupBaseDir();
            if (!baseDir.exists())
            {
                baseDir.mkdirs();
            }
            String relativeFileName = backupName + ".json.gz";
            File targetFile = new File(baseDir, relativeFileName);

            LinkedHashMap<String, Integer> tableRowSummary = new LinkedHashMap<>();
            long[] totalRecordHolder = new long[] { 0L };

            Runnable snapshotDumpTask = () -> {
                try (OutputStream fos = new FileOutputStream(targetFile);
                     GZIPOutputStream gzos = new GZIPOutputStream(fos);
                     JsonGenerator generator = objectMapper.getFactory().createGenerator(gzos))
                {
                    generator.writeStartObject();
                    for (String tableName : backupTableNameList)
                    {
                        generator.writeArrayFieldStart(tableName);
                        int tableRows = streamWriteTableRows(tableName, generator);
                        generator.writeEndArray();
                        tableRowSummary.put(tableName, tableRows);
                        totalRecordHolder[0] += tableRows;
                    }
                    generator.writeEndObject();
                    generator.flush();
                }
                catch (Exception ex)
                {
                    throw new RuntimeException(ex);
                }
            };
            if (platformTransactionManager != null)
            {
                // 在单次 REPEATABLE_READ 只读事务内读取全部业务表，保证跨表 MVCC 快照一致性
                TransactionTemplate readSnapshotTx = new TransactionTemplate(platformTransactionManager);
                readSnapshotTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                readSnapshotTx.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
                readSnapshotTx.setReadOnly(true);
                readSnapshotTx.executeWithoutResult(status -> snapshotDumpTask.run());
            }
            else
            {
                snapshotDumpTask.run();
            }
            long totalRecordCount = totalRecordHolder[0];

            Map<String, Object> metaMap = new LinkedHashMap<>();
            metaMap.put("format", "gzip-json-v2");
            metaMap.put("fileName", relativeFileName);
            metaMap.put("tableRowSummary", tableRowSummary);

            backupRecord.setTableCount(backupTableNameList.size());
            backupRecord.setRecordCount(totalRecordCount);
            backupRecord.setFilePath(relativeFileName);
            backupRecord.setFileSize(targetFile.length());
            backupRecord.setBackupContent(objectMapper.writeValueAsString(metaMap));
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

    private int streamWriteTableRows(String tableName, JsonGenerator generator) throws Exception
    {
        String orderByClause = resolveTablePrimaryKeyOrderBy(tableName);
        int offset = 0;
        int writtenCount = 0;
        while (true)
        {
            String pageSql = "SELECT * FROM `" + tableName + "`" + orderByClause + " LIMIT " + STREAM_FETCH_SIZE + " OFFSET " + offset;
            List<Map<String, Object>> pageRows = jdbcTemplate.queryForList(pageSql);
            if (pageRows == null || pageRows.isEmpty())
            {
                break;
            }
            for (Map<String, Object> row : pageRows)
            {
                generator.writeObject(row);
                writtenCount++;
            }
            if (pageRows.size() < STREAM_FETCH_SIZE)
            {
                break;
            }
            offset += STREAM_FETCH_SIZE;
        }
        return writtenCount;
    }

    private String resolveTablePrimaryKeyOrderBy(String tableName)
    {
        try
        {
            List<String> pkCols = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.key_column_usage " +
                "WHERE table_schema = DATABASE() AND table_name = ? AND constraint_name = 'PRIMARY' " +
                "ORDER BY ordinal_position ASC",
                String.class,
                tableName
            );
            if (pkCols != null && !pkCols.isEmpty())
            {
                StringBuilder sb = new StringBuilder(" ORDER BY ");
                for (int i = 0; i < pkCols.size(); i++)
                {
                    if (i > 0) sb.append(", ");
                    sb.append("`").append(pkCols.get(i)).append("` ASC");
                }
                return sb.toString();
            }
        }
        catch (Exception ignored)
        {
            // fallback without order by
        }
        return "";
    }

    private LinkedHashMap<String, List<LinkedHashMap<String, Object>>> loadBackupPayloadMap(BizDataBackup backupRecord) throws Exception
    {
        TypeReference<LinkedHashMap<String, List<LinkedHashMap<String, Object>>>> typeRef =
            new TypeReference<LinkedHashMap<String, List<LinkedHashMap<String, Object>>>>() {};

        if (StringUtils.isNotEmpty(backupRecord.getFilePath()))
        {
            File diskFile = new File(getBackupBaseDir(), backupRecord.getFilePath());
            if (!diskFile.exists() || !diskFile.isFile())
            {
                throw new ServiceException("磁盘备份文件 [" + backupRecord.getFilePath() + "] 已缺失，请使用【上传离线包恢复】导入本地 .json.gz 备份文件");
            }
            try (InputStream fis = new FileInputStream(diskFile);
                 GZIPInputStream gzis = new GZIPInputStream(fis))
            {
                return objectMapper.readValue(gzis, typeRef);
            }
        }
        if (StringUtils.isEmpty(backupRecord.getBackupContent()))
        {
            throw new ServiceException("备份内容为空且离线压缩包不存在，无法恢复");
        }
        if (backupRecord.getBackupContent().contains("\"gzip-json-v2\""))
        {
            throw new ServiceException("该记录为磁盘压缩包模式但未找到对应文件，请使用【上传离线包恢复】");
        }
        return objectMapper.readValue(backupRecord.getBackupContent(), typeRef);
    }

    private void createPreRestoreSnapshotInNewTransaction(String operatorName, String targetBackupName)
    {
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.executeWithoutResult(status ->
            createBackupWithRemark(BACKUP_TYPE_PRE_RESTORE, operatorName, "执行恢复[" + targetBackupName + "]前系统自动保护快照")
        );
    }

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

    private void insertTableData(String tableName, List<LinkedHashMap<String, Object>> tableDataList)
    {
        // 汇总所有行出现的键集合，并与当前数据库真实表列求交集，避免表结构演进或首行缺列导致恢复失败
        Set<String> existingTableColumns = new LinkedHashSet<>();
        try
        {
            List<String> dbCols = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? ORDER BY ordinal_position ASC",
                String.class,
                tableName
            );
            if (dbCols != null)
            {
                for (String col : dbCols)
                {
                    if (col != null)
                    {
                        existingTableColumns.add(col.toLowerCase(Locale.ROOT));
                    }
                }
            }
        }
        catch (Exception ignored)
        {
            // 若查询元数据失败则回退使用备份键集
        }
        Set<String> allBackupKeys = new LinkedHashSet<>();
        for (LinkedHashMap<String, Object> row : tableDataList)
        {
            if (row != null)
            {
                allBackupKeys.addAll(row.keySet());
            }
        }
        List<String> columnNameList = new ArrayList<>();
        for (String key : allBackupKeys)
        {
            if (existingTableColumns.isEmpty() || existingTableColumns.contains(key.toLowerCase(Locale.ROOT)))
            {
                columnNameList.add(key);
            }
        }
        if (columnNameList.isEmpty())
        {
            return;
        }
        StringBuilder columnNameSqlBuilder = new StringBuilder();
        StringBuilder placeHolderSqlBuilder = new StringBuilder();
        for (int columnIndex = 0; columnIndex < columnNameList.size(); columnIndex++)
        {
            if (columnIndex > 0)
            {
                columnNameSqlBuilder.append(",");
                placeHolderSqlBuilder.append(",");
            }
            columnNameSqlBuilder.append("`").append(columnNameList.get(columnIndex)).append("`");
            placeHolderSqlBuilder.append("?");
        }
        String insertSql = "INSERT INTO `" + tableName + "` (" + columnNameSqlBuilder + ") VALUES (" + placeHolderSqlBuilder + ")";
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
        // 修复 MySQL 8 information_schema 二进制排序规则下 TABLE_TYPE 为大写 'BASE TABLE' 导致返回空列表的问题
        String listTableSql =
            "SELECT table_name AS tableName FROM information_schema.tables " +
            "WHERE table_schema = DATABASE() AND UPPER(table_type) = 'BASE TABLE' ORDER BY table_name";
        List<String> allTableNameList = jdbcTemplate.queryForList(listTableSql, String.class);
        List<String> backupTableNameList = new ArrayList<String>();
        for (String rawName : allTableNameList)
        {
            if (rawName == null)
            {
                continue;
            }
            String normalized = rawName.trim().toLowerCase(Locale.ROOT);
            if (isSupportedBackupTable(normalized))
            {
                backupTableNameList.add(normalized);
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
        String lower = tableName.trim().toLowerCase(Locale.ROOT);
        if ("biz_data_backup".equals(lower))
        {
            return false;
        }
        return lower.startsWith("md_")
            || lower.startsWith("crm_")
            || lower.startsWith("wms_")
            || lower.startsWith("fin_")
            || lower.startsWith("biz_");
    }

    private File getBackupBaseDir()
    {
        String profile = RuoYiConfig.getProfile();
        if (StringUtils.isEmpty(profile))
        {
            profile = System.getProperty("java.io.tmpdir") + File.separator + "saleerp";
        }
        return new File(profile, "backup");
    }

    private String generateBackupName(String backupType)
    {
        SimpleDateFormat datetimeFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
        return backupType + "_" + datetimeFormatter.format(new Date());
    }
}
