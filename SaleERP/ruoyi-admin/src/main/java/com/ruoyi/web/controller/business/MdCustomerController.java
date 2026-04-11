package com.ruoyi.web.controller.business;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.domain.MdCustomer;
import com.ruoyi.business.service.IMdCustomerService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/business/customer")
public class MdCustomerController extends BaseController
{
    @Autowired
    private IMdCustomerService mdCustomerService;

    @PreAuthorize("@ss.hasPermi('business:customer:list')")
    @GetMapping("/list")
    public TableDataInfo list(MdCustomer mdCustomer)
    {
        startPage();
        List<MdCustomer> mdCustomerList = mdCustomerService.selectMdCustomerList(mdCustomer);
        return getDataTable(mdCustomerList);
    }

    @PreAuthorize("@ss.hasPermi('business:customer:export')")
    @Log(title = "客户资料", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(javax.servlet.http.HttpServletResponse response, MdCustomer mdCustomer)
    {
        List<MdCustomer> mdCustomerList = mdCustomerService.selectMdCustomerList(mdCustomer);
        ExcelUtil<MdCustomer> util = new ExcelUtil<MdCustomer>(MdCustomer.class);
        util.exportExcel(response, mdCustomerList, "客户资料数据");
    }

    @PreAuthorize("@ss.hasPermi('business:customer:import')")
    @Log(title = "客户资料", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<MdCustomer> util = new ExcelUtil<MdCustomer>(MdCustomer.class);
        List<MdCustomer> customerList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = mdCustomerService.importMdCustomer(customerList, updateSupport, operName);
        return success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(javax.servlet.http.HttpServletResponse response)
    {
        ExcelUtil<MdCustomer> util = new ExcelUtil<MdCustomer>(MdCustomer.class);
        util.importTemplateExcel(response, "客户资料数据");
    }

    @PreAuthorize("@ss.hasPermi('business:customer:query')")
    @GetMapping("/{customerId}")
    public AjaxResult getInfo(@PathVariable Long customerId)
    {
        return success(mdCustomerService.selectMdCustomerById(customerId));
    }

    @PreAuthorize("@ss.hasPermi('business:customer:add')")
    @Log(title = "客户资料", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public AjaxResult add(@RequestBody MdCustomer mdCustomer)
    {
        mdCustomer.setCreateBy(getUsername());
        return toAjax(mdCustomerService.insertMdCustomer(mdCustomer));
    }

    @PreAuthorize("@ss.hasPermi('business:customer:edit')")
    @Log(title = "客户资料", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public AjaxResult edit(@RequestBody MdCustomer mdCustomer)
    {
        mdCustomer.setUpdateBy(getUsername());
        return toAjax(mdCustomerService.updateMdCustomer(mdCustomer));
    }

    @PreAuthorize("@ss.hasPermi('business:customer:edit')")
    @Log(title = "客户资料", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody MdCustomer mdCustomer)
    {
        mdCustomer.setUpdateBy(getUsername());
        return toAjax(mdCustomerService.updateMdCustomer(mdCustomer));
    }

    @PreAuthorize("@ss.hasPermi('business:customer:remove')")
    @Log(title = "客户资料", businessType = BusinessType.DELETE)
    @DeleteMapping("/{customerIds}")
    public AjaxResult remove(@PathVariable Long[] customerIds)
    {
        return toAjax(mdCustomerService.deleteMdCustomerByIds(customerIds));
    }
}
