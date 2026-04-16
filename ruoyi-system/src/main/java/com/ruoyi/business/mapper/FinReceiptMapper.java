package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.FinReceipt;

public interface FinReceiptMapper
{
    /**
     * 查询回款登记
     * @param receiptId 回款编号
     * @return 回款登记
     */
    public FinReceipt selectFinReceiptById(Long receiptId);

    /**
     * 查询回款登记列表
     * @param finReceipt 回款登记
     * @return 回款登记集合
     */
    public List<FinReceipt> selectFinReceiptList(FinReceipt finReceipt);

    /**
     * 新增回款登记
     * @param finReceipt 回款登记
     * @return 结果
     */
    public int insertFinReceipt(FinReceipt finReceipt);

    /**
     * 修改回款登记
     * @param finReceipt 回款登记
     * @return 结果
     */
    public int updateFinReceipt(FinReceipt finReceipt);

    /**
     * 删除回款登记
     * @param receiptId 回款编号
     * @return 结果
     */
    public int deleteFinReceiptById(Long receiptId);

    /**
     * 批量删除回款登记
     * @param receiptIds 回款编号数组
     * @return 结果
     */
    public int deleteFinReceiptByIds(Long[] receiptIds);

    /**
     * 查询旧版回款登记
     * @param receiptId 回款编号
     * @return 回款登记
     */
    public FinReceipt selectFinReceiptByIdLegacy(Long receiptId);

    /**
     * 查询缺少回款方式字段的回款登记
     * @param receiptId 回款编号
     * @return 回款登记
     */
    public FinReceipt selectFinReceiptByIdWithoutMethod(Long receiptId);

    /**
     * 查询旧版回款登记列表
     * @param finReceipt 回款登记
     * @return 回款登记集合
     */
    public List<FinReceipt> selectFinReceiptListLegacy(FinReceipt finReceipt);

    /**
     * 查询缺少回款方式字段的回款登记列表
     * @param finReceipt 回款登记
     * @return 回款登记集合
     */
    public List<FinReceipt> selectFinReceiptListWithoutMethod(FinReceipt finReceipt);

    /**
     * 新增旧版回款登记
     * @param finReceipt 回款登记
     * @return 结果
     */
    public int insertFinReceiptLegacy(FinReceipt finReceipt);

    /**
     * 新增缺少回款方式字段的回款登记
     * @param finReceipt 回款登记
     * @return 结果
     */
    public int insertFinReceiptWithoutMethod(FinReceipt finReceipt);
}
