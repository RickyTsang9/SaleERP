package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.WmsTransfer;
import com.ruoyi.business.domain.WmsTransferItem;

public interface WmsTransferMapper 
{
    public WmsTransfer selectWmsTransferByTransferId(Long transferId);

    public List<WmsTransfer> selectWmsTransferList(WmsTransfer wmsTransfer);

    public int insertWmsTransfer(WmsTransfer wmsTransfer);

    public int updateWmsTransfer(WmsTransfer wmsTransfer);

    public int deleteWmsTransferByTransferId(Long transferId);

    public int deleteWmsTransferByTransferIds(Long[] transferIds);

    public int deleteWmsTransferItemByTransferIds(Long[] transferIds);

    public int batchWmsTransferItem(List<WmsTransferItem> wmsTransferItemList);

    public int deleteWmsTransferItemByTransferId(Long transferId);

    public int updateWmsTransferStatus(@Param("transferId") Long transferId,
                                       @Param("oldStatus") String oldStatus,
                                       @Param("newStatus") String newStatus,
                                       @Param("updateBy") String updateBy);

    public int confirmReceiveWmsTransferStatus(@Param("transferId") Long transferId,
                                               @Param("receiveBy") String receiveBy);

    public int rejectWmsTransferStatus(@Param("transferId") Long transferId,
                                       @Param("rejectReason") String rejectReason,
                                       @Param("updateBy") String updateBy);

    public int recallInTransitWmsTransferStatus(@Param("transferId") Long transferId,
                                                @Param("rejectReason") String rejectReason,
                                                @Param("updateBy") String updateBy);
}
